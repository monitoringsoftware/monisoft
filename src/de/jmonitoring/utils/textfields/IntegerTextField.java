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
public class IntegerTextField extends JTextField {

//    final static String badchars = "`~!@#$%^&*()_+=\\|\"':;?/>.<, ";
    final static String badchars = "`~!@#$°[]{}³²%^&*()_+=\\|\"':;?/>.<, ";

    @Override
    public void processKeyEvent(KeyEvent ev) {

        char c = ev.getKeyChar();

        if ((Character.isLetter(c) && !ev.isAltDown()) || badchars.indexOf(c) > -1) {
            ev.consume();
            return;
        }
        
        // this should prevent entering a "-" when the field is not empty, but fails when the text in the field is selected
//        if (c == '-' && getDocument().getLength() > 0) {
//            ev.consume();
//        } else {
        
        super.processKeyEvent(ev);
//        }
    }
}
