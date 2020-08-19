package renderer3d;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MeshLoader class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class MeshLoader {
    
    private final List<Vec3> points = new ArrayList<>();
    private final List<Vec3> vts = new ArrayList<>();
    private final List<Vec3> vns = new ArrayList<>();
    
    private final List<Face> faces = new ArrayList<>();
    
    MaterialLoader mtlLib = new MaterialLoader();
    private Material currentMaterial;

    private double scaleFactor;
    private double translateX;
    private double translateY;
    private double translateZ;

    public MeshLoader() {
    }

    public List<Vec3> getVertices() {
        return points;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    public double getTranslateZ() {
        return translateZ;
    }
    
    public void load(String meshRes, double scaleFactor, double translateX
            , double translateY, double translateZ) throws Exception {
        
        points.clear();
        faces.clear();
        
        this.scaleFactor = scaleFactor;
        this.translateX = translateX;
        this.translateY = translateY;
        this.translateZ = translateZ;
        
        InputStream is = MeshLoader.class.getResourceAsStream(meshRes);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            
            if (line.startsWith("v ")) {
                parseVertex(line);
            }
            else if (line.startsWith("vt ")) {
                parseVTs(line);
            }
            else if (line.startsWith("vn ")) {
                parseVNs(line);
            }
            else if (line.startsWith("f ")) {
                parseFace(line);
            }
            else if (line.startsWith("mtllib ")) {
                parseMtlLib(line);
            }
            else if (line.startsWith("usemtl ")) {
                parseUseMtl(line);
            }
        }
        br.close();
    }

    private void parseVertex(String line) {
        int beginIndex = "v ".length();
        line = line.substring(beginIndex);
        line = line.trim();
        String[] data = line.split(" ");
        double x = Double.parseDouble(data[0]);
        double y = Double.parseDouble(data[1]);
        double z = Double.parseDouble(data[2]);
        double stx = x * scaleFactor + translateX;
        double sty = y * scaleFactor + translateY;
        double stz = z * scaleFactor + translateZ;
        Vec3 v = new Vec3(stx, sty, stz);
        points.add(v);
    }
    
    private void parseVTs(String line) {
        String[] data = line.split(" ");
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        Vec3 v = new Vec3(x, y, 1);
        vts.add(v);
    }

    private void parseVNs(String line) {
        String[] data = line.split(" ");
        double x = Double.parseDouble(data[1]);
        double y = Double.parseDouble(data[2]);
        double z = Double.parseDouble(data[3]);
        Vec3 v = new Vec3(x, y, z);
        vns.add(v);
    }
    
    private void parseFace(String line) {
        String[] data = line.split(" ");
        if (currentMaterial == null) {
            throw new NullPointerException();
        }
        Face face = new Face(currentMaterial);
        for (int i = 1; i < data.length; i++) {
            String[] data2 = data[i].split("/");
            int pointIndex = Integer.parseInt(data2[0]);
            int vtIndex = Integer.parseInt(data2[1]);
            int vnIndex = Integer.parseInt(data2[2]);
            Vec3 point = points.get(pointIndex - 1);
            Vec3 vt = vts.get(vtIndex - 1);
            Vec3 vn = vns.get(vnIndex - 1);
            Vertex vertex = new Vertex(point, vt, vn);
            face.addPoint(vertex);
        }
        faces.add(face);        
    }
    
    private void parseMtlLib(String line) {
        int beginIndex = "mtllib ".length();
        line = line.substring(beginIndex);
        String mtlLibFile = "/res/" + line;
        try {
            mtlLib.load(mtlLibFile);
        } catch (Exception ex) {
            Logger.getLogger(MeshLoader.class.getName())
                    .log(Level.SEVERE, null, ex);
            
            System.exit(1);
        }
    }    

    private void parseUseMtl(String line) {
        int beginIndex = "usemtl ".length();
        line = line.substring(beginIndex);
        String mtlName = line;
        currentMaterial = mtlLib.getMaterials().get(mtlName);
        if (currentMaterial == null) {
            throw new RuntimeException(
                    "Material '" + mtlName + "' not found !");
        }
    }

    @Override
    public String toString() {
        return "MeshLoader{" + "vertices=" + points + ", faces=" + faces 
            + ", scaleFactor=" + scaleFactor + ", translateX=" + translateX 
            + ", translateY=" + translateY + '}';
    }
    
}
