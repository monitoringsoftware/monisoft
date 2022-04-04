package de.jmonitoring.TableModels;

import de.jmonitoring.utils.tablecellrenderer.ColoredDoubleCellRenderer;
import de.jmonitoring.utils.tablecellrenderer.DoubleCellRenderer;
import java.util.Vector;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import org.jdesktop.swingx.JXTable;

/**
 * This class defines the table used by the compare table frame
 *
 * @author togro
 */
public class CompareJXTable extends JXTable {

    Integer[] abberationPercentages;

    public CompareJXTable(Object[][] rowData, Object[] columnNames) {
        super(rowData, columnNames);
    }

    public CompareJXTable(Vector rowData, Vector columnNames) {
        super(rowData, columnNames);
    }

    public CompareJXTable(int numRows, int numColumns) {
        super(numRows, numColumns);
    }

    public CompareJXTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
        super(dm, cm, sm);
    }

    public CompareJXTable(TableModel dm, TableColumnModel cm) {
        super(dm, cm);
    }

    public CompareJXTable(TableModel dm) {
        super(dm);
    }

    public CompareJXTable(TableModel dm, Integer[] abberationPercentages) {
        super(dm);
        this.abberationPercentages = abberationPercentages;
    }

    public CompareJXTable() {
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column > 4) {
            return new ColoredDoubleCellRenderer("0.00", abberationPercentages);
        } else if (column > 1) {
            return new DoubleCellRenderer("0.0000");
        }

        // else...
        return super.getCellRenderer(row, column);

    }
}
