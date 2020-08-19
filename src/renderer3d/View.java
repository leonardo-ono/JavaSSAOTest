package renderer3d;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * SSAO (Screen Space Ambient Occlusion) Test #1
 * 
 * References:
 * https://github.com/ssloy/tinyrenderer/wiki/Lesson-8-Ambient-occlusion
 * https://vr.arvilab.com/blog/ambient-occlusion
 * https://learnopengl.com/Advanced-Lighting/SSAO
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class View extends Canvas implements KeyListener {
    
    private BufferStrategy bs;
    
    private BufferedImage image;
    private DepthBuffer depthBuffer;
    
    private final MeshLoader meshLoader = new MeshLoader();
    
    private SSAO ssao;
    
    private boolean running;
    
    private int showType = 2;
    
    public View() {
    }
    
    public void start() {
        addKeyListener(this);
        
        int width = 800;
        int height = 600;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        depthBuffer = new DepthBuffer(width, height);
        ssao = new SSAO(depthBuffer);
        
        try {
            meshLoader.load("/res/mariohead.obj", 70, 0, 30, 0);
            //meshLoader.load("/res/hero.obj", 2, 0, -350, 0);
        } catch (Exception ex) {
            Logger.getLogger(View.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        
        createBufferStrategy(2);
        bs = getBufferStrategy();
        running = true;
        
        new Thread(() -> {
            while (running) {
                update();
                draw((Graphics2D) image.getGraphics());
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                
                Graphics2D ig = (Graphics2D) image.getGraphics();

                // 2D outline post processing
                
                // only flat
                if (showType == 0) {
                    ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                            , RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    ig.drawImage(image, 0, 0, 400, 300, 0, 0, width, height, this);
                    g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, 400, 300, null);
                }
                // only SSAO
                else if (showType == 1) {
                    ssao.process();
                    g.clearRect(0, 0, getWidth(), getHeight());
                    g.drawImage(ssao.getBlurred(), 0, 0, getWidth(), getHeight(), null);
                }
                // flat + SSAO
                else if (showType == 2) {
                    ssao.process();
                    ig.drawImage(ssao.getBlurred(), 0, 0, null);

                    ig.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                            , RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    ig.drawImage(image, 0, 0, 400, 300, 0, 0, width, height, this);

                    //g.clearRect(0, 0, getWidth(), getHeight());

                    g.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, 400, 300, null);
                }

                
                g.dispose();
                bs.show();
                
                try {
                    Thread.sleep(1000 / 60);
                } catch (InterruptedException ex) {
                }
            }
        }).start();
    }

    private void update() {
    }
    
    private void draw(Graphics2D g) {
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, getWidth(), getHeight());

        depthBuffer.clear();

        meshLoader.getFaces().forEach((face) -> {
            face.draw(g, depthBuffer, image.getWidth() / 2, image.getHeight() / 2);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            view.setPreferredSize(new Dimension(800, 600));
            JFrame frame = new JFrame();
            frame.setTitle("SSAO (Screen Space Ambient Occlusion) Test #1");
            frame.getContentPane().add(view);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            view.requestFocus();
            view.start();
        });
    }    

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_0:
                showType = 0;
                break;
            case KeyEvent.VK_1:
                showType = 1;
                break;
            case KeyEvent.VK_2:
                showType = 2;
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
}
