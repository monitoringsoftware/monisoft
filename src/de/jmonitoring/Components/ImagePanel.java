/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Components;

/**
 *
 * @author togro
 */
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImagePanel extends Panel {

    private BufferedImage image;

    public ImagePanel() {
        image = null;
    }

    public ImagePanel(File file) {
        try {
            image = ImageIO.read(file);
        } catch (IOException ie) {
            System.out.println("Error:" + ie.getMessage());
        }
    }

    public void setImage(File file) {
        try {
            image = ImageIO.read(file);
        } catch (IOException ie) {
            System.out.println("Error:" + ie.getMessage());
        }
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }

//    static public void main(String args[]) throws
//            Exception {
//        JFrame frame = new JFrame("Display image");
//        Panel panel = new ImagePanel();
//        frame.getContentPane().add(panel);
//        frame.setSize(500, 500);
//        frame.setVisible(true);
//    }
}
