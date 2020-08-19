package renderer3d;

import java.awt.image.BufferedImage;

/**
 * Material class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Material {
    
    private final String name;
    private BufferedImage texture;

    public Material(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public BufferedImage getTexture() {
        return texture;
    }

    public void setTexture(BufferedImage texture) {
        this.texture = texture;
    }
    
}
