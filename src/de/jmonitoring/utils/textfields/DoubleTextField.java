/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.textfields;

import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/**
 *
 * @author togro
 */
public class DoubleTextField extends JTextField {

    final static String badchars = "`~!@#$°[]{}³²%^&*()_+=\\|\"':;?/>< ";   // Nur Minus, Komma oder Punkt erlaubt!!!

    @Override
    public void processKeyEvent(KeyEvent ev) {

        char c = ev.getKeyChar();

        if ((Character.isLetter(c) && !ev.isAltDown()) || badchars.indexOf(c) > -1) {
            ev.consume();
            return;
        }
//        if (c == '-' && getDocument().getLength() > 0) {
//            ev.consume();
//        } else {
        super.processKeyEvent(ev);
//        }
    }

    @Override
    public String getText() {
        // Komma durch Punkt ersetzen
        return super.getText().replace(",", ".");

    }
}
