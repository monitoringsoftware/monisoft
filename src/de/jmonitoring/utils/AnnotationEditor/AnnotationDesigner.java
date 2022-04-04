package de.jmonitoring.utils.AnnotationEditor;

import de.jmonitoring.help.ManualBookmarks;
import de.jmonitoring.help.ManualViewer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPolygonAnnotation;

/**
 * The AnnotationDesigner displays a chart for creating new or editing existing
 * annotations.<p>
 *
 * The anntations consist of one ore more {@link AnnotationElement}s and are
 * stored in an {@link AnnotationContainer}.<br> A {@link AnnotationElement} can
 * be a filled area defined by a closed polygon or a line with multiple
 * straights defined by a open polygon.<br> For storage the
 * {@link AnnotationContainer} is named and serialized by
 * {@link AnnotationHandler} to XML in the database.
 *
 * @author togro
 */
public class AnnotationDesigner extends javax.swing.JInternalFrame implements ListSelectionListener {

    private final AnnotationDesignerChart annotationChart;
    private final DecimalFormat df = new DecimalFormat("0.0", DecimalFormatSymbols.getInstance(Locale.US));
    private final AnnotationTabManager tabManager = new AnnotationTabManager();
    private boolean active = false;
    private boolean isPreview = false;
    private int tabCount = 0;
    private final String NO_NAME = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("UNNAMEND");

    /**
     * Creates new form AnnotationDesigner
     */
    public AnnotationDesigner() {
        initComponents();

        fillAnnotationChooser();

        DefaultListModel model = new DefaultListModel();
        pointList.setModel(model);
        pointList.addListSelectionListener(this);
        pointList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        annotationChart = new AnnotationDesignerChart(this);
        getContentPane().add(annotationChart, BorderLayout.CENTER);
        active = true;

        addNewElement();
        setAnnotationNameLabel(NO_NAME);
    }

    /**
     * Adds and displays a new point (x,y) int the coordinate list
     *
     * @param x
     * @param y
     */
    public void addToList(Double x, Double y) {
        DefaultListModel model = (DefaultListModel) pointList.getModel();
        model.add(model.getSize(), model.getSize() + 1 + ": (" + df.format(x) + ";  " + df.format(y) + ")");
    }

    /**
     * Updates the point with the given index in the coordinate list with the
     * given (x,y)-values
     *
     * @param index the index of the point to be updated
     * @param x
     * @param y
     */
    public void updateList(int index, Double x, Double y) {
        DefaultListModel model = (DefaultListModel) pointList.getModel();
        String label = index + ": (" + df.format(x) + ";  " + df.format(y) + ")";
        model.setElementAt(label, index);
    }

    /**
     * Removes the last point from the coordinate list
     */
    public void removeLastFromList() {
        DefaultListModel model = (DefaultListModel) pointList.getModel();
        if (model.getSize() == 0) {
            return;
        }
        model.remove(model.getSize() - 1);
    }

    /**
     * Clears the the coordinate list completely
     */
    public void clearPointList() {
        DefaultListModel model = (DefaultListModel) pointList.getModel();
        model.removeAllElements();
    }

    /**
     * Selects the point with the given index in the coordinate list
     *
     * @param i
     */
    public void setListSelection(int i) {
        pointList.setSelectedIndex(i);
    }

    /**
     * Sets the two coordinate fields to the respective values of the point
     * which is selected in the coordinate list. Also updates the live
     * coordinates and marks the point in the chart.
     */
    private void setPointFields() {
        String item = (String) pointList.getSelectedValue();
        if (item == null || item.isEmpty()) {
            return;
        }
        item = item.split(":")[1];
        item = item.replace("(", "");
        item = item.replace(")", "");
        String[] koords = item.split(";");
        xField.setText(koords[0]);
        yField.setText(koords[1]);

        markPoint(koords[0], koords[1]);
        setLiveX(Double.valueOf(koords[0]));
        setLiveY(Double.valueOf(koords[1]));
    }

    /**
     * Tell the displyed chart to mark (crosshairs) the given point
     *
     * @param xString
     * @param yString
     */
    private void markPoint(String xString, String yString) {
        Double x = Double.valueOf(xString);
        Double y = Double.valueOf(yString);
        annotationChart.markPoint(x, y);
    }

    /**
     * A point in the coordinate list was selected. Update the coordinate
     * fields.
     *
     * @param e
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        setPointFields();
    }

    /**
     * Creates new {@link AnnotationElement} and links it to a new
     * {@link AnnotationSettingPanel} which is then put in the
     * {@link JTabbedPane}<br> The chart is cleared and the
     * {@link AnnotationTabManager} is set to this new element-tab.
     */
    private void addNewElement() {
        tabCount++;
        AnnotationSettingPanel panel = new AnnotationSettingPanel();
        AnnotationElement element = new AnnotationElement(tabCount + ".", new Color(200, 0, 0), new Color(200, 0, 0), null);
        element.setPoints(new Double[][]{});
        element.setFillAlpha(102);
        element.setLineAlpha(255);
        panel.setElement(element);
        if (tabCount == 1) {
            tabManager.setCurrentTab(panel);
        }
        elementTabbedPanel.add(tabCount + ".", panel);
        elementTabbedPanel.setSelectedIndex(elementTabbedPanel.getTabCount() - 1);
        annotationChart.clearData();

        if (tabCount == 1) {
            annotationChart.initAxis(); // it is the first and only tab in the panel: set axis default
        } else {
            annotationChart.zoomAll();
        }
    }

    /**
     * Removes all {@link AnnotationSettingPanel}s form the {@link JTabbedPane}
     */
    private void removeAllElementTabs() {
        tabCount = 0;
        elementTabbedPanel.removeAll();
    }

    /**
     * Returns the currently selected {@link AnnotationSettingPanel}
     *
     * @return
     */
    private AnnotationSettingPanel getSelectedSettingsPanel() {
        AnnotationSettingPanel settingsPanel = (AnnotationSettingPanel) elementTabbedPanel.getSelectedComponent();
        return settingsPanel;
    }

    /**
     * Set the live x coordinate label
     *
     * @param d
     */
    public void setLiveX(Double d) {
        liveX.setText(df.format(d));
    }

    /**
     * Set the live y coordinate label
     *
     * @param d
     */
    public void setLiveY(Double d) {
        liveY.setText(df.format(d));
    }

    /**
     * Gets the points for the currently selected {@link AnnotationSettingPanel}
     * and shows it as a draft
     */
    private void showAnnotationDraft() {
        Double[][] points = getSelectedSettingsPanel().getAnnotationElement().getPoints();
        clearPointList();

        for (int i = 0; i < points.length; i++) {
            Double x = points[i][0];
            Double y = points[i][1];
            setAnnotationClosed(annotationChart.addDataPoint(x, y));
        }
        annotationChart.zoomAll();
    }

    /**
     * Saves the points of the currently drawn draft to the
     * {@link AnnotationElement} of the given {@link AnnotationSettingPanel}
     *
     * @param panel the {@link AnnotationSettingPanel} to be updated
     */
    private void savePointsToElementTab(AnnotationSettingPanel panel) {
        if (panel == null) {
            return;
        }
        panel.getAnnotationElement().setPoints(annotationChart.getDataPoints());
    }

    /**
     * Draw all {@link AnnotationElement} except the given one as real
     * {@link XYAnnotation}s ("preview")
     *
     * @param skipElement the {@link AnnotationElement} that should be omitted
     */
    private void drawElementsExcept(AnnotationElement skipElement) {
        annotationChart.clearData();
        annotationChart.clearAnnotations();
        for (int i = 0; i < elementTabbedPanel.getTabCount(); i++) {
            AnnotationElement element = ((AnnotationSettingPanel) elementTabbedPanel.getComponentAt(i)).getAnnotationElement();
            if (!element.equals(skipElement)) {
                showElementsAsAnnotation(element);
            }
        }
        annotationChart.zoomAll();
    }

    /**
     * Draw the given {@link AnnotationElement} as real {@link XYAnnotation}
     * ("preview")<p> If the {@link AnnotationElement} is closed is will be
     * diplayed as {@link XYPolygonAnnotation} otherwise as
     * {@link MoniSoftLineAnnotation}.<br> In the latter case the coordinates
     * have to be reorganized because the line annotations with multiple lines
     * must be build up of single lines.<br>Therefore the last point of the
     * previous line must be used as the first point of the adjacent line.
     *
     * @param element the {@link AnnotationElement} to de drawn
     */
    private void showElementsAsAnnotation(AnnotationElement element) {
        Double[][] points = element.getPoints();
        double[] annotationPoints = new double[points.length * 2];
        for (int i = 0; i < points.length; i++) {
            annotationPoints[i * 2] = points[i][0];
            annotationPoints[i * 2 + 1] = points[i][1];
        }

        Color fillColor = null;
        if (element.getFillColor() != null) {
            fillColor = new Color(element.getFillColor().getRed(), element.getFillColor().getGreen(), element.getFillColor().getBlue(), element.getFillAlpha());
        }
        Color lineColor = null;
        if (element.getLineColor() != null) {
            lineColor = new Color(element.getLineColor().getRed(), element.getLineColor().getGreen(), element.getLineColor().getBlue(), element.getLineAlpha());
        }

        if (element.isClosed()) {
            XYPolygonAnnotation polyyAnno = new XYPolygonAnnotation(annotationPoints, element.getStroke(), lineColor, fillColor);
            annotationChart.plotAnnotation(polyyAnno);
        } else { // line annotation
            if (element.getStroke() == null) {
                return;
            }

            double[] linePoints = new double[4];
            for (int i = 0; i < points.length - 1; i++) {
                linePoints[0] = points[i][0];
                linePoints[1] = points[i][1];
                linePoints[2] = points[i + 1][0];
                linePoints[3] = points[i + 1][1];
                MoniSoftLineAnnotation lineAnno = new MoniSoftLineAnnotation(linePoints[0], linePoints[1], linePoints[2], linePoints[3], element.getStroke(), lineColor);
                annotationChart.plotAnnotation(lineAnno);
            }
        }
    }

    /**
     * If the preview togglebutton is selected show a preview of all
     * {@link AnnotationElement}s.<br> Otherwise show all except the selected
     * {@link AnnotationElement} as {@link XYAnnotation} which will be shown as
     * draft.<p> During preview all controlls are locked.
     */
    private void showPreview() {
        tabManager.getCurrentTab().updateElement();
        if (previewButton.isSelected()) {
            isPreview = true;
            savePointsToElementTab(tabManager.getCurrentTab());
            drawElementsExcept(null);
        } else {
            isPreview = false;
            drawElementsExcept(tabManager.getCurrentTab().getAnnotationElement());
            showAnnotationDraft();
        }
        annotationChart.showCrosshairs(!isPreview);
        lockControls();
    }

    /**
     * Sets the text for the annotation name label
     *
     * @param text
     */
    private void setAnnotationNameLabel(String text) {
        currentAnnotationName.setText(text);
    }

    /**
     * Disable all controlls of the gui is we are in preview mode
     */
    private void lockControls() {
        elementTabbedPanel.setEnabled(!isPreview);
        for (int i = 0; i < jPanel2.getComponents().length; i++) {
            jPanel2.getComponent(i).setEnabled(!isPreview);
        }
        newAnnotation.setEnabled(!isPreview);
        loadButton.setEnabled(!isPreview);
        annotationChooser.setEnabled(!isPreview);
        tabManager.getCurrentTab().setElementsEnabled(!isPreview);
    }

    /**
     * Load the annotation in the {@link AnnotationContainer} with the given
     * name from the {@link AnnotationHandler} and display it.
     *
     * @param name
     */
    private void loadAnnotation(String name) {
        active = false;
        annotationChart.clearData();
        annotationChart.clearAnnotations();
        removeAllElementTabs();
        clearPointList();

        AnnotationContainer container = AnnotationHandler.readAnnotation(name);
        tabCount = 0;
        if (container != null) {
            for (AnnotationElement element : container.getAnnotationElements()) {
                tabCount++;
                AnnotationSettingPanel settingsPanel = new AnnotationSettingPanel();
                settingsPanel.setElement(element);
                elementTabbedPanel.add(element.getName(), settingsPanel);

                if (tabCount == 1) {
                    tabManager.setCurrentTab(settingsPanel);
                }
            }
        }
        drawElementsExcept(tabManager.getCurrentTab().getAnnotationElement());
        showAnnotationDraft();
        annotationChart.zoomAll();
        active = true;
    }

    /**
     * Sets the annotation combobox with the names of all annotationss stored in
     * the database
     */
    private void fillAnnotationChooser() {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (String name : AnnotationHandler.getAnnotationAsList()) {
            model.addElement(name);
        }
        annotationChooser.setModel(model);
    }

    /**
     * Returns the preview state
     *
     * @return
     */
    public boolean isPreview() {
        return isPreview;
    }

    /**
     * Sets the current {@link AnnotationElement}s
     * <code>isClosed</code> field
     *
     * @param b
     */
    public void setAnnotationClosed(boolean b) {
        tabManager.getCurrentTab().getAnnotationElement().setClosed(b);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        bindingGroup = new org.jdesktop.beansbinding.BindingGroup();

        jPanel1 = new javax.swing.JPanel();
        liveX = new javax.swing.JLabel();
        liveY = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        cancelButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        currentAnnotationName = new javax.swing.JTextField();
        jButton7 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        clearButton = new javax.swing.JButton();
        undoButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        pointList = new javax.swing.JList();
        xField = new javax.swing.JTextField();
        yField = new javax.swing.JTextField();
        updateButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        elementTabbedPanel = new javax.swing.JTabbedPane();
        addElementButton = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        annotationChooser = new javax.swing.JComboBox();
        loadButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        newAnnotation = new javax.swing.JButton();
        deleteAnnotation = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        previewButton = new javax.swing.JToggleButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle"); // NOI18N
        setTitle(bundle.getString("EDITOR_TITLE")); // NOI18N
        setFrameIcon(null);
        setMinimumSize(new java.awt.Dimension(875, 600));
        setPreferredSize(new java.awt.Dimension(875, 600));

        jPanel1.setPreferredSize(new java.awt.Dimension(607, 30));

        liveX.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        liveX.setText("jLabel1");

        liveY.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        liveY.setText("jLabel2");

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel1.setText("X:");

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel2.setText("Y:");

        cancelButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        cancelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/cross-circle.png"))); // NOI18N
        cancelButton.setText(bundle.getString("CLOSE")); // NOI18N
        cancelButton.setToolTipText(bundle.getString("CLOSE_TOOLTIP")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel5.setText(bundle.getString("CURRENT_NAME")); // NOI18N

        currentAnnotationName.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        currentAnnotationName.setText(" ");

        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/question-frame.png"))); // NOI18N
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.setFocusPainted(false);
        jButton7.setIconTextGap(0);
        jButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7help(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(liveX, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(liveY, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(3, 3, 3)
                .addComponent(currentAnnotationName, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 59, Short.MAX_VALUE)
                .addComponent(jButton7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel1)
                    .addComponent(liveX)
                    .addComponent(jLabel2)
                    .addComponent(liveY)
                    .addComponent(jLabel5)
                    .addComponent(currentAnnotationName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton)
                    .addComponent(jButton7))
                .addContainerGap())
        );

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("ELEMENT_POINTS_TITLE"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N
        jPanel2.setPreferredSize(new java.awt.Dimension(150, 379));

        clearButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        clearButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/broom.png"))); // NOI18N
        clearButton.setText(bundle.getString("CLEAR")); // NOI18N
        clearButton.setToolTipText(bundle.getString("CLEAR_TOOLTIP")); // NOI18N
        clearButton.setPreferredSize(new java.awt.Dimension(80, 23));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        undoButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/arrow_undo.png"))); // NOI18N
        undoButton.setText(bundle.getString("UNDO")); // NOI18N
        undoButton.setToolTipText(bundle.getString("UNDO_TOOLTIP")); // NOI18N
        undoButton.setPreferredSize(new java.awt.Dimension(80, 23));
        undoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        jScrollPane1.setViewportView(pointList);

        updateButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        updateButton.setText(bundle.getString("UPDATE_POINT")); // NOI18N
        updateButton.setToolTipText(bundle.getString("UPDATE_POINT_TOOLTIP")); // NOI18N
        updateButton.setActionCommand("update point");
        updateButton.setIconTextGap(2);
        updateButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });

        addButton.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        addButton.setText(bundle.getString("ADD_POINT")); // NOI18N
        addButton.setToolTipText(bundle.getString("ADD_POINT_TOOLTIP")); // NOI18N
        addButton.setIconTextGap(2);
        addButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel3.setText("X:");

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel4.setText("Y:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(xField, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(yField))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(undoButton, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(updateButton, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(undoButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(jLabel3)
                    .addComponent(xField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(yField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel3.setPreferredSize(new java.awt.Dimension(105, 115));

        elementTabbedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("TITLE_BORDER_ELEMENTS"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N
        elementTabbedPanel.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        elementTabbedPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                elementTabbedPanelStateChanged(evt);
            }
        });

        addElementButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addElementButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/paint-brush--plus.png"))); // NOI18N
        addElementButton.setText(bundle.getString("ADD")); // NOI18N
        addElementButton.setToolTipText(bundle.getString("ADD_TOOLTIP")); // NOI18N

        org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, previewButton, org.jdesktop.beansbinding.ELProperty.create("${!selected}"), addElementButton, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
        bindingGroup.addBinding(binding);

        addElementButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addElementButtonActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("TITLE_BORDER_EXISTING"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N

        annotationChooser.setFont(new java.awt.Font("Dialog", 0, 9)); // NOI18N
        annotationChooser.setMinimumSize(new java.awt.Dimension(56, 20));
        annotationChooser.setPreferredSize(new java.awt.Dimension(56, 20));

        loadButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        loadButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/database_edit.png"))); // NOI18N
        loadButton.setText(bundle.getString("LOAD_EXISTING")); // NOI18N
        loadButton.setToolTipText(bundle.getString("LOAD_ANNOTATION_TOOLTIP")); // NOI18N
        loadButton.setMaximumSize(new java.awt.Dimension(58, 20));
        loadButton.setMinimumSize(new java.awt.Dimension(58, 20));
        loadButton.setPreferredSize(new java.awt.Dimension(58, 20));
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(annotationChooser, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(loadButton, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(annotationChooser, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(loadButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(elementTabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addGap(5, 5, 5)
                .addComponent(addElementButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(elementTabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(addElementButton))
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_START);

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, bundle.getString("TITLE_BORDER_ACTIONS"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Andale Mono", 0, 9))); // NOI18N

        newAnnotation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        newAnnotation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/asterisk_yellow.png"))); // NOI18N
        newAnnotation.setText(bundle.getString("NEW")); // NOI18N
        newAnnotation.setToolTipText(bundle.getString("NEW_TOOLTIP")); // NOI18N
        newAnnotation.setMaximumSize(new java.awt.Dimension(65, 20));
        newAnnotation.setMinimumSize(new java.awt.Dimension(65, 20));
        newAnnotation.setPreferredSize(new java.awt.Dimension(65, 20));
        newAnnotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newAnnotationActionPerformed(evt);
            }
        });

        deleteAnnotation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        deleteAnnotation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/delete.png"))); // NOI18N
        deleteAnnotation.setText(bundle.getString("DELETE")); // NOI18N
        deleteAnnotation.setToolTipText(bundle.getString("DELETE_TOOLTIP")); // NOI18N
        deleteAnnotation.setMaximumSize(new java.awt.Dimension(65, 20));
        deleteAnnotation.setMinimumSize(new java.awt.Dimension(65, 20));
        deleteAnnotation.setPreferredSize(new java.awt.Dimension(65, 20));
        deleteAnnotation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteAnnotationActionPerformed(evt);
            }
        });

        saveButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        saveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/disk.png"))); // NOI18N
        saveButton.setText(bundle.getString("SAVE")); // NOI18N
        saveButton.setToolTipText(bundle.getString("SAVE_TOOLTIP")); // NOI18N
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        previewButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        previewButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/jmonitoring/icons/eye.png"))); // NOI18N
        previewButton.setText(bundle.getString("PREVIEW")); // NOI18N
        previewButton.setToolTipText(bundle.getString("PREVIEW_TOOLTIP")); // NOI18N
        previewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(2, 2, 2)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(deleteAnnotation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE)
                                .addComponent(newAnnotation, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addComponent(previewButton, javax.swing.GroupLayout.Alignment.TRAILING))
                    .addComponent(saveButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deleteAnnotation, previewButton, saveButton});

        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newAnnotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteAnnotation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 243, Short.MAX_VALUE)
                .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel5Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {deleteAnnotation, newAnnotation, saveButton});

        getContentPane().add(jPanel5, java.awt.BorderLayout.LINE_START);

        bindingGroup.bind();

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        annotationChart.clearData();
        tabManager.getCurrentTab().getAnnotationElement().setClosed(false);
        clearPointList();
    }//GEN-LAST:event_clearButtonActionPerformed
    /**
     * The save button was pressed.<p> Saves the {@link AnnotationContainer} to
     * the database. If is exists asks if it should be overwriten.<br> Asks for
     * the name if it is not already given.
     *
     * @param evt
     */
    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        String name = currentAnnotationName.getText().trim();
        if (name.isEmpty() || name.equals(NO_NAME)) {
            name = JOptionPane.showInputDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ENTER_NAME_MESSAGE"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ENTER_NAME_TITLE"), JOptionPane.OK_CANCEL_OPTION);
        }
        if (name == null || name.isEmpty()) {
            return;
        }

        if (AnnotationHandler.annotationExists(name)) {
            int option = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ALREADY_EXISTS") + "\n\n" + java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("OVERWRITE"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("OVERWRITE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option == JOptionPane.NO_OPTION) {
                return;
            }
        }

        AnnotationContainer container = new AnnotationContainer(name);
        tabManager.getCurrentTab().updateElement();
        savePointsToElementTab(tabManager.getCurrentTab());
        for (int i = 0; i < elementTabbedPanel.getTabCount(); i++) {
            AnnotationElement element = ((AnnotationSettingPanel) elementTabbedPanel.getComponentAt(i)).getAnnotationElement();
            container.addAnnotationElement(element);
        }

        if (AnnotationHandler.writeAnnotation(name, container)) {
            fillAnnotationChooser();
        }
    }//GEN-LAST:event_saveButtonActionPerformed
    /**
     * The undo button was pressed.<p> Tell the chart to remove its last point
     * and also remove it from the coordinate list.<br> The current
     * {@link AnnotationElement} can then no longer be closed.
     *
     * @param evt
     */
    private void undoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoButtonActionPerformed
        annotationChart.undo();
        tabManager.getCurrentTab().getAnnotationElement().setClosed(false);
        removeLastFromList();
    }//GEN-LAST:event_undoButtonActionPerformed
    /**
     * The load stored annotation button was pressed<p> Invoke loading and set
     * name label.
     *
     * @param evt
     */
    private void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadButtonActionPerformed
        loadAnnotation((String) annotationChooser.getSelectedItem());
        setAnnotationNameLabel(annotationChooser.getSelectedItem().toString());
    }//GEN-LAST:event_loadButtonActionPerformed
    /**
     * The update button was pressed<p> Update the selected point in the chart
     * with the new coordinates and also update the values in the coordinate
     * list.
     *
     * @param evt
     */
    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        int index = pointList.getSelectedIndex();
        if (index != -1) {
            annotationChart.update(index, Double.valueOf(xField.getText()), Double.valueOf(yField.getText()));
            updateList(index, Double.valueOf(xField.getText()), Double.valueOf(yField.getText()));
        }
    }//GEN-LAST:event_updateButtonActionPerformed
    /**
     * The add point button was pressed<p> Adds a new point to the chart with
     * the values in the coodinate entry fields.<br> If the returned value is
     * true (the polygion is closed) set the
     * <code>isClosed</code> field of the {@link AnnotationElement}
     *
     * @param evt
     */
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        setAnnotationClosed(annotationChart.addDataPoint(Double.valueOf(xField.getText()), Double.valueOf(yField.getText())));
    }//GEN-LAST:event_addButtonActionPerformed

    private void elementTabbedPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_elementTabbedPanelStateChanged
        if (tabManager.getCurrentTab() == null) {
            return;
        }

        if (active && !isPreview) {
            savePointsToElementTab(tabManager.getCurrentTab());
            tabManager.getCurrentTab().updateElement();
        }

        tabManager.setCurrentTab((AnnotationSettingPanel) elementTabbedPanel.getSelectedComponent());

        if (active && !isPreview) {
            if (tabManager.getCurrentTab() == null) {
                return;
            }
            drawElementsExcept(tabManager.getCurrentTab().getAnnotationElement());
            showAnnotationDraft();
        }
    }//GEN-LAST:event_elementTabbedPanelStateChanged
    /**
     * The add element button was pressed.<p> Invoke addition of a new
     * {@link AnnotationSettingPanel} to the tabbedpanel and clear the
     * coordinate list
     *
     * @param evt
     */
    private void addElementButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addElementButtonActionPerformed
        addNewElement();
        clearPointList();
    }//GEN-LAST:event_addElementButtonActionPerformed
    /**
     * The preview button was pressed<p>
     *
     * @param evt
     */
    private void previewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previewButtonActionPerformed
        showPreview();
    }//GEN-LAST:event_previewButtonActionPerformed
    /**
     * The new annotation button was pressed<p> Ask for a name for the new
     * {@link AnnotationContainer} and clear all data and the
     * {@link AnnotationElement}s
     *
     * @param evt
     */
    private void newAnnotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newAnnotationActionPerformed
        String name = JOptionPane.showInputDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ENTER_NAME_MESSAGE"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ENTER_NAME_TITLE"), JOptionPane.OK_CANCEL_OPTION);
        if (name == null || name.isEmpty()) {
            return;
        }
        annotationChart.clearData();
        annotationChart.clearAnnotations();
        removeAllElementTabs();
        clearPointList();
        addNewElement();
        setAnnotationNameLabel(name);
    }//GEN-LAST:event_newAnnotationActionPerformed
    /**
     * The cancel button was pressed<p> Close frame.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed
    /**
     * The delete annotation button was pressed<p> Ask if the current annotation
     * should really be deleted and if so invole deletion.<br> Clear all data
     * and the {@link AnnotationElement}s, reset name
     *
     * @param evt
     */
    private void deleteAnnotationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteAnnotationActionPerformed
        String name = currentAnnotationName.getText().trim();
        if (name.isEmpty()) {
            return;
        }

        int option = JOptionPane.showConfirmDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("REALLY_DELETE") + "\n\n" + name, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("DELETE"), JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.NO_OPTION) {
            return;
        }

        int num = AnnotationHandler.removeAnnotation(name);
        if (num == 0) {
            JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("ANNOTATION_NOT_EXISTS"), java.util.ResourceBundle.getBundle("de/jmonitoring/utils/AnnotationEditor/Bundle").getString("WARNING"), JOptionPane.WARNING_MESSAGE);
        }

        fillAnnotationChooser();
        annotationChart.clearData();
        annotationChart.clearAnnotations();
        removeAllElementTabs();
        clearPointList();
        setAnnotationNameLabel(NO_NAME);
    }//GEN-LAST:event_deleteAnnotationActionPerformed

    private void jButton7help(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7help
        if (!ManualViewer.isShown) {
            ManualViewer viewer = new ManualViewer();
            viewer.showManual();
        }
        ManualViewer.goToPage(ManualBookmarks.ANNOTATION_DESIGNER.getPage());
    }//GEN-LAST:event_jButton7help

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton addElementButton;
    private javax.swing.JComboBox annotationChooser;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JTextField currentAnnotationName;
    private javax.swing.JButton deleteAnnotation;
    private javax.swing.JTabbedPane elementTabbedPanel;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel liveX;
    private javax.swing.JLabel liveY;
    private javax.swing.JButton loadButton;
    private javax.swing.JButton newAnnotation;
    private javax.swing.JList pointList;
    private javax.swing.JToggleButton previewButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton undoButton;
    private javax.swing.JButton updateButton;
    private javax.swing.JTextField xField;
    private javax.swing.JTextField yField;
    private org.jdesktop.beansbinding.BindingGroup bindingGroup;
    // End of variables declaration//GEN-END:variables
}
