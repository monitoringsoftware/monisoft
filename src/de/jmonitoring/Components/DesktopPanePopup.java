/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Components;

import de.jmonitoring.base.DesktopManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author togro
 */
public class DesktopPanePopup extends JPopupMenu {

    private final DesktopManager dm;

    public DesktopPanePopup(DesktopManager dm) {
        super();
        this.dm = dm;
        init();
    }

    private void init() {
        JMenuItem closeItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("DesktopPanePopup.ALLE FENSTER SCHLIESSEN"));
        closeItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dm.closeAllWindows();
            }
        });
        JMenuItem cascadeItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("DesktopPanePopup.ALLE FENSTER KASKADIEREN"));
        cascadeItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dm.cascadeWindows();
            }
        });
        JMenuItem iconifyItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("DesktopPanePopupALLE FENSTER MINIMIEREN"));
        iconifyItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dm.iconifyWindows(true);
            }
        });
        JMenuItem restoreItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("UNICONIFY"));
        restoreItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dm.iconifyWindows(false);
            }
        });
        JMenuItem tileItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("DesktopPanePopup.NEBENEINANDER"));
        tileItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                dm.tileAllWindows();
            }
        });


        this.add(closeItem);
        this.add(cascadeItem);
        this.add(tileItem);
        this.add(iconifyItem);
        this.add(restoreItem);
    }
}
