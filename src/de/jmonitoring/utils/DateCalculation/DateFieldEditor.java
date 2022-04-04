/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.DateCalculation;

import com.toedter.calendar.JTextFieldDateEditor;
import de.jmonitoring.Components.ManualEntryPanel;
import java.awt.Color;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.CaretEvent;

/**
 *
 * @author togro
 */
public class DateFieldEditor extends JTextFieldDateEditor {

    /**
     * After any user input, the value of the textfield is proofed. Depending on
     * being a valid date, the value is colored green or red.
     * 
     * @param event
     *            the caret event
     */
    @Override
    public void caretUpdate(CaretEvent event) {
        Border redLine = BorderFactory.createLineBorder(Color.red, 2);
        Border greyLine = BorderFactory.createEtchedBorder();
        Color errorColor = new Color(255, 205, 205);

        String text = getText().trim();
        String emptyMask = maskPattern.replace('#', placeholder);

        if (text.length() == 0 || text.equals(emptyMask)) {
            setBackground(errorColor);
            setBorder(redLine);
            ((ManualEntryPanel) this.getParent().getParent().getParent()).reactToDateChange(false);
            return;
        }

        try {
            Date tryDate = dateFormatter.parse(getText());
            if (dateUtil.checkDate(tryDate)) {
                setBackground(Color.WHITE);
                setBorder(greyLine);
                ((ManualEntryPanel) this.getParent().getParent().getParent()).reactToDateChange(true);
            } else {
                setBackground(errorColor);
                setBorder(redLine);
                ((ManualEntryPanel) this.getParent().getParent().getParent()).reactToDateChange(false);
            }
        } catch (Exception e) {
            setBackground(errorColor);
            setBorder(redLine);
            ((ManualEntryPanel) this.getParent().getParent().getParent()).reactToDateChange(false);
        }
    }
}
