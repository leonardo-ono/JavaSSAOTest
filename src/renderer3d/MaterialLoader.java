package renderer3d;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * MaterialLoader class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class MaterialLoader {
    
    private final Map<String, Material> materials = new HashMap<>();
    private Material currentMaterial;
    
    public MaterialLoader() {
    }

    public Map<String, Material> getMaterials() {
        return materials;
    }
    
    public void load(String materialRes) throws Exception {
        materials.clear();
        
        InputStream is = MaterialLoader.class.getResourceAsStream(materialRes);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("newmtl ")) {
                parseNewMtl(line);
            }
            else if (line.startsWith("map_Kd ")) {
                parseTexture(line);
            }
        }
        br.close();
    }

    private void parseNewMtl(String line) {
        int beginIndex = "newmtl ".length();
        line = line.substring(beginIndex);
        String materialName = line;
        Material material = new Material(materialName);
        materials.put(materialName, material);
        currentMaterial = material;
    }
    
    private void parseTexture(String line) {
        int beginIndex = "map_Kd ".length();
        line = line.substring(beginIndex);
        String textureFile = "/res/" + line;
        try {
            BufferedImage texture = ImageIO.read(
                    getClass().getResourceAsStream(textureFile));
            
            currentMaterial.setTexture(texture);
        } catch (IOException ex) {
            Logger.getLogger(
                MaterialLoader.class.getName()).log(Level.SEVERE, null, ex);
            
            System.exit(1);
        }
    }

    @Override
    public String toString() {
        return "MaterialLoader{" + "materials=" + materials 
                + ", currentMaterial=" + currentMaterial + '}';
    }

}
