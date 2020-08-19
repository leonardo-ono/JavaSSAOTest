package renderer3d;

import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

/**
 * Face class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Face {
    
    private final Vec3 normal = new Vec3();
    private final List<Vertex> vertices = new ArrayList<>();
    private final Material material;
    private final Raster textureRaster;
    
    private final TriangleRasterizerComposite triangleComposite 
        = new TriangleRasterizerComposite();
    
    public Face(Material material) {
        this.material = material;
        this.textureRaster = material.getTexture().getRaster();
    }

    public Vec3 getNormal() {
        return normal;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }
    
    public void addPoint(Vertex vertex) {
        vertices.add(vertex);
    }
    
    private final Polygon polygonTmp = new Polygon();

    private final Vec3[] screenPoints = { new Vec3(), new Vec3(), new Vec3() };
    private final Vec3 screenPoint = new Vec3();

    private double angle = 0;
    
    Vec3 copyA = new Vec3();
    Vec3 copyB = new Vec3();
    Vec3 copyC = new Vec3();
    Vec3[] copyVs = { copyA, copyB, copyC };
    
    public void draw(Graphics2D g, DepthBuffer depthBuffer
            , int halfWidth, int halfHeight) {
        
        copyA.set(vertices.get(0).getPoint());
        copyB.set(vertices.get(1).getPoint());
        copyC.set(vertices.get(2).getPoint());
        
        copyA.rotateY(angle);
        copyB.rotateY(angle);
        copyC.rotateY(angle);
        
        copyA.rotateX(-0.25);
        copyB.rotateX(-0.25);
        copyC.rotateX(-0.25);
        
        copyA.z -= 600;
        copyB.z -= 600;
        copyC.z -= 600;
        
        copyA.x = 600 * (copyA.x / -copyA.z);
        copyA.y = 600 * (copyA.y / -copyA.z);
        copyB.x = 600 * (copyB.x / -copyB.z);
        copyB.y = 600 * (copyB.y / -copyB.z);
        copyC.x = 600 * (copyC.x / -copyC.z);
        copyC.y = 600 * (copyC.y / -copyC.z);

        angle += 0.025;
        
        polygonTmp.reset();
        for (int index = 0; index < 3; index++) {
            int x = (int) (halfWidth + copyVs[index].x);
            int y = (int) (halfHeight - copyVs[index].y);
            polygonTmp.addPoint(x, y);
            screenPoints[index].set(x, 0, y);
        }

        calculateTotalWeight(screenPoints[0], screenPoints[1], screenPoints[2]);
        
        // back-face culling
        if (wtotal > 0) {
            return;
        }
        
        triangleComposite.set(depthBuffer);
        g.setComposite(triangleComposite);
        g.fill(polygonTmp);
        
    }

    private class TriangleRasterizerComposite implements Composite {
        
        private final TriangleRasterizerCompositeContext context 
            = new TriangleRasterizerCompositeContext();

        public void set(DepthBuffer depthBuffer) {
            context.setDepthBuffer(depthBuffer);
        }
        
        @Override
        public CompositeContext createContext(ColorModel srcColorModel
                , ColorModel dstColorModel, RenderingHints hints) {
            
            return context;
        }
        
    }
    
    private class TriangleRasterizerCompositeContext 
            implements CompositeContext {

        private DepthBuffer depthBuffer;
        private final int[] pxDst = new int[4];

        public void setDepthBuffer(DepthBuffer depthBuffer) {
            this.depthBuffer = depthBuffer;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            for (int mx = 0; mx < dstOut.getWidth(); mx++) {
                for (int my = 0; my < dstOut.getHeight(); my++) {
                    int x = mx - dstOut.getSampleModelTranslateX();
                    int y = my - dstOut.getSampleModelTranslateY();

                    screenPoint.set(x, 0, y);
                    calculateWeights(screenPoint
                        , screenPoints[0], screenPoints[1], screenPoints[2]);

                    double z = w0 * copyA.z + w1 * copyB.z + w2 * copyC.z;
                    if (!depthBuffer.update(x, y, z)) {
                        continue;
                    }

                    Vec3 st0 = vertices.get(0).getSt();
                    Vec3 st1 = vertices.get(1).getSt();
                    Vec3 st2 = vertices.get(2).getSt();
                    double s = w0 * st0.x + w1 * st1.x + w2 * st2.x;
                    double t = w0 * st0.y + w1 * st1.y + w2 * st2.y;

                    s = s % 1;
                    t = t % 1;
                    if (s < 0) {
                        int si = (int) s;
                        s -= si - 1;
                    }
                    if (t < 0) {
                        int ti = (int) t;
                        t -= ti - 1;
                    }

                    int tx = (int) (s * (material.getTexture().getWidth() - 1));
                    int ty = (int) ((1 - t) 
                        * (material.getTexture().getHeight() - 1));

                    textureRaster.getPixel(tx, ty, pxDst);

                    dstOut.setPixel(mx, my, pxDst);
                }
            }
        }
        
    }

    private double wtotal;
    private double w0;
    private double w1;
    private double w2;
    private final Vec3 v0 = new Vec3();
    private final Vec3 v1 = new Vec3();
    private final Vec3 v2 = new Vec3();
    
    public void calculateTotalWeight(Vec3 p0, Vec3 p1, Vec3 p2) {
        v0.set(p1);
        v0.sub(p0);
        v1.set(p2);
        v1.sub(p0);
        wtotal = v0.cross2D(v1);
        wtotal = 1 / wtotal;
    }
    
    public void calculateWeights(Vec3 p, Vec3 p0, Vec3 p1, Vec3 p2) {
        v0.set(p0, p);
        v1.set(p1, p);
        v2.set(p2, p);
        w0 = v1.cross2D(v2) * wtotal;
        w1 = v2.cross2D(v0) * wtotal;
        w2 = v0.cross2D(v1) * wtotal;
    }
    
}
