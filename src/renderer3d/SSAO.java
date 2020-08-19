package renderer3d;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import javax.swing.JPanel;

/**
 * SSAO class.
 * 
 * @author Leonardo Ono (ono.leo@gmail.com);
 */
public class SSAO extends JPanel {
    
    private final DepthBuffer depthBuffer;
    private final BufferedImage ssao;
    private final BufferedImage ssaoBlurred;
    private final Graphics2D og;
    private final int width;
    private final int height;
    
    public SSAO(DepthBuffer depthBuffer) {
        this.depthBuffer = depthBuffer;
        width = depthBuffer.getWidth();
        height = depthBuffer.getHeight();
        ssao = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ssaoBlurred = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        og = (Graphics2D) ssao.getGraphics();
        og.setBackground(new Color(255, 255, 255, 0));
        initBlur();
    }

    public BufferedImage getSSAO() {
        return ssao;
    }

    private final Vec3 cur = new Vec3();
    private final Vec3 dir2 = new Vec3();
    private final Vec3 distVec = new Vec3();
    
    // https://github.com/ssloy/tinyrenderer/blob/d7c806bc3d598fc54dd446b6c81b94f723728205/main.cpp
    private double calculateMaxElevationAngle(DepthBuffer zbuffer, Vec3 p, Vec3 dir) {
        double maxAngle = 0;
        for (double t = 0; t < 64; t += 1) {
            cur.set(p);
            dir2.set(dir);
            
            dir2.scale(t);
            cur.add(dir2);
            if (cur.x >= width || cur.y >= height || cur.x < 0 || cur.y < 0) {
                return maxAngle;
            }
            distVec.set(p);
            distVec.sub(cur);
            double distance = distVec.getLength();
            if (distance < 1) {
                continue;
            }
            double elevation = zbuffer.get((int) cur.x, (int) cur.y) 
                    - zbuffer.get((int) p.x, (int) p.y);
            
            maxAngle = Math.max(maxAngle, Math.atan(elevation / distance));
        }
        return maxAngle;
    }

    public void process() {
        og.clearRect(0, 0, ssao.getWidth(), ssao.getHeight());
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                
                if (depthBuffer.get(x, y) < -1e5) {
                    continue;
                }
                
                double total = 0;
                for (double a = 0; a < Math.PI * 2 - 1e-4; a += Math.PI / 4) {
                    total += Math.PI / 2 - calculateMaxElevationAngle(depthBuffer
                        , new Vec3(x, y, 1), new Vec3(Math.cos(a), Math.sin(a), 1));
                }
                total /= (Math.PI / 2) * 8;
                
                // test 1
                //total = Math.pow(total, 0.75);
                //total = 1.25 - Math.pow(1 / (total + 1), 2);
                //total = total < 0 ? 0 : total > 1 ? 1 : total;
                
                // test 2
                if (total > 0.65) {
                    total = 1;
                }
                else {
                    total = total * 1.3;
                    total = total < 0 ? 0 : total > 1 ? 1 : total;
                }

                int color = new Color(
                    (int) (total * 255), 0, (int) (64 - total * 64)
                        , (int) (255 - total * 255)).getRGB();
                
                ssao.setRGB(x, y, color);
            }
        }
    }

    private ConvolveOp blurOp;
    
    private void initBlur() {
        int radius = 3;
        int size = radius * 2 + 1;
        float weight = 1.0f / (size * size);
        float[] data = new float[size * size];
        for (int i = 0; i < data.length; i++) {
            data[i] = weight;
        }

        Kernel kernel = new Kernel(size, size, data);
        blurOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
    
    public BufferedImage getBlurred() {
        blurOp.filter(ssao, ssaoBlurred);        
        return ssaoBlurred;
    }
    
}
