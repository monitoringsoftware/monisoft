/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Consistency;

import java.util.ResourceBundle;

import javax.swing.JDesktopPane;

import de.jmonitoring.base.MainApplication;
import de.jmonitoring.base.charts.QualityOverviewFrame;
import de.jmonitoring.utils.StoppableThread;

/**
 *
 * @author togro
 */
public class QualityPlotThread extends StoppableThread {

    private JDesktopPane deskTopPane;
    private int x,y;
	private final MainApplication gui;

    public QualityPlotThread(JDesktopPane pane, int x, int y, MainApplication gui) {
        deskTopPane = pane;
		this.gui = gui;
    }

    public JDesktopPane getDeskTopPane() {
        return deskTopPane;
    }

    @Override
    public void run() {
        running = true;
        QualityOverviewFrame f = new QualityOverviewFrame(ResourceBundle.getBundle("de/jmonitoring/Consistency/Bundle").getString("QualitiyOverviewFrame.QUALITY_OVERVIEW"),
        		true, true, true, true, this.gui);
        f.pack();
        getDeskTopPane().add(f);
        f.setLocation(x, y);
        f.moveToFront();
    }
}
