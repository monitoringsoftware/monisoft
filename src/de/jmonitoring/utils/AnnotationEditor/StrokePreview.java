package de.jmonitoring.utils.AnnotationEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.swing.ImageIcon;

/**
 * A class which generates an {@link ImageIcon} of a stroke which can then be
 * used to fill a combobox.
 *
 * @author togro
 */
public class StrokePreview {

    /**
     * The ImageIcon to use for the stroke in a GUI.
     */
    protected ImageIcon icon;
    /**
     * The BasicStroke that is represented.
     */
    protected BasicStroke stroke;
    protected int DEFAULT_ICON_WIDTH = 200;
    protected int DEFAULT_ICON_HEIGHT = 17;

    /**
     * Create a LineChoice from an resource to use for the ImageIcon, and the
     * BasicStroke to represent.
     *
     * @param imageResourceName the resource of the image to use on the icon.
     * @param stroke BasicStroke.
     * @param toolTip to use for the icon.
     */
    public StrokePreview(String imageResourceName, BasicStroke stroke, String toolTip) {
        URL url = this.getClass().getResource(imageResourceName);
        icon = new ImageIcon(url, toolTip);

        this.stroke = stroke;
    }

    /**
     * Create a LineChoice from the BasicStroke represented. The ImageIcon will
     * be created. The default size of the icon will be used, in horizontal
     * orientation.
     *
     * @param stroke BasicStroke.
     * @param toolTip to use for the icon.
     */
    public StrokePreview(BasicStroke stroke, String toolTip) {
        icon = createIcon(stroke, DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT, true);
        this.stroke = stroke;
    }

    /**
     * Create a LineChoice from the BasicStroke represented. The ImageIcon will
     * be created to the dimensions and orientation given.
     *
     * @param stroke BasicStroke.
     * @param width the width of the icon.
     * @param height the height of the icon.
     */
    public StrokePreview(BasicStroke stroke, int width, int height, boolean horizontalOrientation, String toolTip) {
        icon = createIcon(stroke, width, height, horizontalOrientation);
        this.stroke = stroke;
    }

    /**
     * Get the stroke for this LineChoice.
     */
    public BasicStroke getStroke() {
        return stroke;
    }

    /**
     * Set the stroke for this LineChoice.
     */
    public void setStroke(BasicStroke stroke) {
        this.stroke = stroke;
    }

    /**
     * Get the ImageIcon for this LineChoice.
     */
    public ImageIcon getIcon() {
        return icon;
    }

    /**
     * Set the ImageIcon for this LineChoice.
     */
    public void setIcon(ImageIcon iicon) {
        icon = iicon;
    }

    /**
     * Given a BasicStroke, create an ImageIcon that shows it.
     *
     * @param stroke the BasicStroke to draw on the Icon.
     * @param width the width of the icon.
     * @param height the height of the icon.
     * @param horizontalOrientation if true, draw line on the icon horizontally,
     * else draw it vertically.
     */
    public static ImageIcon createIcon(BasicStroke stroke, int width, int height, boolean horizontalOrientation) {

        BufferedImage bigImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Graphics2D g = ge.createGraphics(bigImage);

        g.setBackground(Color.WHITE);
        g.setPaint(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setStroke(stroke);
        g.setPaint(Color.black);
        if (horizontalOrientation) {
            g.drawLine(0, height / 2, width, height / 2);
        } else {
            g.drawLine(width / 2, 0, width / 2, height);
        }

        return new ImageIcon(bigImage);
    }
}
