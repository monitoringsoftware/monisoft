package de.jmonitoring.base;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

/**
 * This initializes the real spalsh screen used when starting the GUI
 *
 * @author togro
 */
public class SwingBasedMonisoftSplash implements MonisoftSplash {

    private final SplashScreen splash;
    private final Graphics2D g;
    private final Dimension size;

    /**
     * Create new Object with given splash
     *
     * @param splash
     */
    public SwingBasedMonisoftSplash(SplashScreen splash) {
        this.splash = splash;
        g = (Graphics2D) splash.createGraphics();
        this.size = splash.getSize();
    }

    /**
     * Prepares the splash screen
     */
    @Override
    public void initialize() {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, size.width, size.height);
        g.setColor(Color.WHITE);
        g.setPaintMode();
        g.setFont(new Font("Dialog", Font.BOLD, 13));
        g.drawString("Version " + MoniSoftConstants.getVersion(), size.width - 150, size.height - 60);
        g.setFont(new Font("Dialog", Font.PLAIN, 13));
        g.drawString("Preparing environment ...", 10, 20);
        g.setColor(Color.RED);
        g.fillRect(0, size.height - 2, size.width / 5, size.height);
        splash.update();
    }

    /**
     * Shows a message on the splash screen and moves the progress bar forward
     *
     * @param text The message
     * @param step The step by which the progress is moved
     */
    @Override
    public void showMessage(String text, int step) {
        g.setBackground(new Color(0f, 0f, 0f, 0f));
        g.clearRect(0, 0, size.width, 30);
        g.setColor(Color.WHITE);
        g.drawString(text, 10, 20);
        g.setColor(Color.RED);
        g.fillRect(0, size.height - 2, step * size.width / 5, size.height);
        splash.update();
    }
}
