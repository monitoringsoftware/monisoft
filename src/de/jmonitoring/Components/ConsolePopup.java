/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.jmonitoring.Components;

import de.jmonitoring.base.ConsoleManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author togro
 */
public class ConsolePopup extends JPopupMenu {

    private final ConsoleManager cm;

	public ConsolePopup(ConsoleManager cm) {
		super();
        this.cm = cm;
		init();
    }

    private void init() {
        JMenuItem deleteItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsolePopup.KONSOLE LEEREN"));
        deleteItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cm.clearConsole();
            }
        });

        JMenuItem copyItem = new JMenuItem(java.util.ResourceBundle.getBundle("de/jmonitoring/Components/Bundle").getString("ConsolePopup.AUSWAHL KOPIEREN"));
        deleteItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cm.copyConsoleToClipBoard();
            }
        });
        this.add(deleteItem);
        this.add(copyItem);
    }
}
