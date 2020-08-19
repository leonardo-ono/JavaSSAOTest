package renderer3d;

/**
 * Vertex class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Vertex {

    private final Vec3 point;
    private final Vec3 st;
    private final Vec3 vn;

    public Vertex(Vec3 point, Vec3 st, Vec3 vn) {
        this.point = point;
        this.st = st;
        this.vn = vn;
    }

    public Vec3 getPoint() {
        return point;
    }

    public Vec3 getSt() {
        return st;
    }

    public Vec3 getVn() {
        return vn;
    }

    @Override
    public String toString() {
        return "Vertex{" + "point=" + point + ", st=" + st + ", vn=" + vn + '}';
    }
    
}
