package de.jmonitoring.DataHandling.CounterChange;

import de.jmonitoring.Components.BuildingEditorDialog;
import java.awt.Component;
import java.util.Date;
import java.util.TreeMap;

/**
 * This panel holds multiple {@link CounterChangeItem}s
 *
 * @author togro
 */
public class CounterChangeItemHolder extends javax.swing.JPanel {

    private CounterChangeDialog caller;

    /**
     * Creates new form CounterChangeItemHolder with ge given caller for
     * reference
     */
    public CounterChangeItemHolder(CounterChangeDialog caller) {
        this.caller = caller;
        initComponents();
    }

    /**
     * Add a counter change to the holder
     *
     * @param change The {@link CounterChange}
     */
    public void addCounterChange(CounterChange change) {
        add(new CounterChangeItem(change, caller)); // Bezugsgröße hinzufügen
        sortCounterChanges();
    }

    /**
     * Delete the given {@link CounterChangeItem} from this holder
     *
     * @param item The {@link CounterChangeItem} to be removed
     */
    public void removeCounterChange(CounterChangeItem item) {
        remove(item);
        this.caller.updateScrollPane(BuildingEditorDialog.LEAVE);
    }

    /**
     * Sort the {@link CounterChangeItem}s in this hold be their date
     */
    private void sortCounterChanges() {
        TreeMap<Date, CounterChangeItem> sortedList = new TreeMap<Date, CounterChangeItem>();
        CounterChangeItem item;
        for (Component c : getComponents()) {
            if (c instanceof CounterChangeItem) {
                item = (CounterChangeItem) c;
                sortedList.put(item.getChange().getTime(), item);
            }
        }

        removeAll();

        for (Date d : sortedList.keySet()) {
            add(sortedList.get(d));
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 204));
        setMinimumSize(new java.awt.Dimension(400, 150));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
