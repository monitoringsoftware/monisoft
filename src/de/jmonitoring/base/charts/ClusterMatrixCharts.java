/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/*
 * ClusterChart.java
 *
 * Created on 30.09.2010, 11:51:14
 */
package de.jmonitoring.base.charts;

import de.jmonitoring.Components.MoniSoftProgressBar;
import de.jmonitoring.SensorCollectionHandling.SensorCollectionHandler;
import de.jmonitoring.DataHandling.*;
import de.jmonitoring.base.buildings.BuildingInformation;
import de.jmonitoring.base.buildings.BuildingProperties;
import de.jmonitoring.base.CtrlChartPanel;
import de.jmonitoring.base.MainApplication;
import de.jmonitoring.References.ReferenceInformation;
import de.jmonitoring.utils.UnitCalulation.UnitInformation;
import de.jmonitoring.Cluster.ClusterMatrixToolTipGenerator;
import de.jmonitoring.References.ReferenceValue;
import de.jmonitoring.utils.StoppableThread;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author togro
 */
public class ClusterMatrixCharts extends javax.swing.JPanel {

    private ArrayList<XYDataset> datasets = new ArrayList<XYDataset>();
    private ArrayList<HashMap<JFreeChart, HashMap<String, String>>> chartList = new ArrayList<HashMap<JFreeChart, HashMap<String, String>>>();
    private ArrayList<CtrlChartPanel> chartPanels = new ArrayList<CtrlChartPanel>();
    private ArrayList<String> selectedFeatures = new ArrayList<String>();
    private ArrayList<String> selectedReferences = new ArrayList<String>();
    private String consumptionCategory;
    private int CHART_COUNT;
    private int year;
    private boolean climateCorrection;
    private final StoppableThread thisThread = (StoppableThread) Thread.currentThread();
    private MoniSoftProgressBar progressBar;
    private final MainApplication gui;
    private CtrlChartPanel cp;

    /**
     * Creates new form ClusterChart
     */
    public ClusterMatrixCharts(ArrayList<String> r, ArrayList<String> f, String cs, String year, boolean climateCorrection, MoniSoftProgressBar progressBar, MainApplication gui) {
        super();
        this.gui = gui;
        if (r.isEmpty() || f.isEmpty()) {
            return;
        }
        initComponents();
        this.consumptionCategory = cs;
        this.year = Integer.parseInt(year);
        this.climateCorrection = climateCorrection;
        this.progressBar = progressBar;
        selectedReferences = r; // == Anzahl Spalten
        selectedFeatures = f;   // == Anzahl Zeilen
        CHART_COUNT = r.size() * f.size();
        createDatasets();
        if (!thisThread.running) {
            return;
        }
        createCharts();
        if (!thisThread.running) {
            return;
        }
        createChartPanels();
        if (!thisThread.running) {
            return;
        }
        placeCharts();
    }

    public CtrlChartPanel getChartPanel() {
        return cp;
    }

    private void placeCharts() {
        progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("PLACING_PANELS"));
        removeAll();
        setLayout(new GridLayout(selectedReferences.size() > 1 ? selectedReferences.size() : 1, selectedFeatures.size() > 1 ? selectedFeatures.size() : 1));
        for (int i = 0; i < CHART_COUNT; i++) {
            cp = chartPanels.get(i);
            cp.setMinimumDrawHeight(0); // dafür sorgen, dass Fonts bei kleinen Grafiken nicht verzerrt werden
            cp.setMinimumDrawWidth(0);
            cp.setPreferredSize(new Dimension(100, 100));
            cp.setMouseWheelEnabled(true);
            add(cp);
        }
        doLayout();
        revalidate();
    }

    private void createChartPanels() {
        progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("GENERATE_PANLES"));
        for (int i = 0; i < CHART_COUNT; i++) {
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
            HashMap<JFreeChart, HashMap<String, String>> currentEntry = chartList.get(i);
            JFreeChart chart = currentEntry.keySet().iterator().next();

            HashMap<String, String> labelMap = currentEntry.get(chart);
            String rangeLabel = labelMap.keySet().iterator().next();
            String domainLabel = labelMap.values().iterator().next();

            XYPlot plot = chart.getXYPlot();
            progressBar.setMinMax(0, plot.getDataset().getSeriesCount());
            for (int s = 0; s < plot.getDataset().getSeriesCount(); s++) {
                progressBar.setValue(s);
                if (!thisThread.running) {
                    return;
                }
                renderer.setSeriesShape(s, getShape(0, 2));
                renderer.setBaseToolTipGenerator(new ClusterMatrixToolTipGenerator(rangeLabel, domainLabel));
                plot.setRenderer(0, renderer);
            }

            plot.setDomainPannable(true);
            plot.setRangePannable(true);
            plot.getRangeAxis().setLabelFont(new Font("Dialog", Font.PLAIN, 9));
            plot.getDomainAxis().setLabelFont(new Font("Dialog", Font.BOLD, 9));
            plot.getDomainAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 8));
            plot.getRangeAxis().setTickLabelFont(new Font("Dialog", Font.PLAIN, 8));
            plot.setBackgroundPaint(new Color(244, 244, 244));
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            chartPanels.add(new CtrlChartPanel(chart, this.gui));
        }
    }

    private void createCharts() {
        int row = 0;
        int col = 0;
        String rangeLabel;
        String domainLabel;

        progressBar.setMinMax(0, CHART_COUNT);
        progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("GENERATE_CHARTS"));
        for (int i = 0; i < CHART_COUNT; i++) {
            progressBar.setValue(i);
            String rowLabel, colLabel;
            HashMap<String, String> labelMap = new HashMap<String, String>();
            HashMap<JFreeChart, HashMap<String, String>> chartMap = new HashMap<JFreeChart, HashMap<String, String>>();
//            String refUnit = "m²";

//            ReferenceInformation.getUnitIDForReference(selectedReferences.get(row));
            String refUnit = UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(selectedReferences.get(row)));
            String featureUnit = UnitInformation.getUnitNameFromID(ReferenceInformation.getUnitIDForReference(selectedFeatures.get(col)));

            rowLabel = " kWh/" + refUnit + "<sub>" + selectedReferences.get(row) + "</sub> a";
            colLabel = featureUnit + " " + selectedFeatures.get(col);

            labelMap.put(rowLabel, colLabel);

            if (col == 0) {
                rangeLabel = "kWh/" + refUnit + " " + selectedReferences.get(row) + " a";
            } else {
                rangeLabel = "";
            }

            if (row == selectedReferences.size() - 1) {
                domainLabel = selectedFeatures.get(col);
            } else {
                domainLabel = "";
            }

            JFreeChart chart = ChartFactory.createXYLineChart(
                    null, // chart title
                    domainLabel, // x axis label
                    rangeLabel, // y axis label
                    datasets.get(i), // data
                    PlotOrientation.VERTICAL,
                    false, // include legend
                    true, // tooltips
                    false // urls
                    );

            chartMap.put(chart, labelMap);
            chartList.add(chartMap);
            col++;
            if (col > selectedFeatures.size() - 1) {
                col = 0;
                row++;
            }
        }
    }

    private void createDatasets() {
        // Alle Gebäude holen
        ArrayList<BuildingProperties> buildings = BuildingInformation.getBuildingList();
        DatabaseQuery dq;
        String buildingName;
        Integer sensorID;
        ArrayList<XYSeriesCollection> seriesCollections = new ArrayList<XYSeriesCollection>(); // TODO anpassen Anzahl der Grafken
        for (int i = 0; i < CHART_COUNT; i++) {
            seriesCollections.add(new XYSeriesCollection());
        }

        ArrayList<ReferenceValue> refList;
        int chartcount;
        int buildingID;
        int count = 0;

        progressBar.setMinMax(0, buildings.size());
        progressBar.setValue(0);

        for (BuildingProperties props : buildings) {
            count++;
            if (!thisThread.running) {
                return;
            }
            progressBar.setValue(count);
            chartcount = 0;
            buildingName = props.getBuildingName();
            buildingID = props.getBuildingID();

            refList = BuildingInformation.getBuildingReferences(buildingID); // Ermitteln aller zu diesem Gebäude gehörenden Bezugsgrößen
            sensorID = SensorCollectionHandler.getCategorySensorForBuilding(buildingID, consumptionCategory); // Sensor der für das Gebäude in der angegebenen Kategorie "zuständig"ist

            if (sensorID == null) {
                seriesCollections.get(chartcount).addSeries(new XYSeries(buildingName)); // eine Serie hinzufeügen auch wenn das Gebäude nicht vorkommt, damit die Reihenfolge (Farben) bleibt)
                continue; // das Gebäude hat keinen Messpunkt für diese Kategorie, mit dem nächsten Gebäude weiter machen
            }

            dq = new DatabaseQuery(sensorID);

            for (String ref : selectedReferences) { // "Zeilen" (Bezugsgrößen) durchlaufen
                progressBar.setText(java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("Processing building") + " " + count + " " + java.util.ResourceBundle.getBundle("de/jmonitoring/base/charts/Bundle").getString("FROM") + "  " + buildings.size() + " (" + ref + ")");
                if (!thisThread.running) {
                    return;
                }
                boolean hasRef = false;
                for (ReferenceValue r : refList) {
                    if (r.getName().equals(ref)) {
                        hasRef = true;
                        break;
//                        chartcount += selectedFeatures.size();
//                        continue; // wenn es diese Bezugsgröße für das Gebäude nicht gibt weiterlaufen
                    }
                }

//                System.out.println("Ref " + ref + " vorhanden :" + hasRef);
                if (!hasRef) {
                    chartcount += selectedFeatures.size();
                    continue;
                }

                for (String feature : selectedFeatures) {   // "Spalten" (Clustermerkmale) durchlaufen
                    boolean hasFeature = false;
                    for (ReferenceValue r : refList) {
                        if (r.getName().equals(feature)) {
//                                System.out.println(">>" + r.getName() + " " + feature);
                            hasFeature = true;
                            break;
                        }
                    }

                    if (!hasFeature) {
                        chartcount++;
                        continue;
                    }
//                    System.out.println("Feature " + feature + " vorhanden :" + hasFeature);

                    XYSeries series = new XYSeries(buildingName);
//                    System.out.println("Füge hinzu " + buildingName + " Grafik " + chartcount + " Bezug " + ref + " Cluster " + feature);
                    Double climateFactor = 1d;
                    if (climateCorrection) {
                        climateFactor = new ClimateFactorHandler(this.gui).getClimateFactor(props.getPlz(), year, 0);
                    }

                    series.add(BuildingInformation.getBuildingreference(buildingID, feature).getValue(), dq.getValueForYear(year, ref, UnitInformation.getUnitFromName("kWh"), climateFactor, true));
                    seriesCollections.get(chartcount).addSeries(series); // füge EINEN Punkt für das Gebäude hinzu
                    chartcount++;
                }
            }
        }

        Iterator<XYSeriesCollection> itt = seriesCollections.iterator();
        while (itt.hasNext()) {
            XYSeriesCollection s = itt.next();
            datasets.add(s);
        }
    }

    private Shape getShape(int type, int index) {
        int size = index + 1; //
        float s;
        switch (type) {
            case 0:
                s = (size + 1f) / 2;
                return new Ellipse2D.Float(-s, -s, 2 * s, 2 * s);
            case 1:
                s = (size + 1f) / 2;
                return new Rectangle2D.Float(-s, -s, 2 * s, 2 * s);
            case 2:
                s = (size + 1f);
                double element = s * Math.sin(Math.PI / 4.);
                GeneralPath p = new GeneralPath();
                p.moveTo(0, -s);
                p.lineTo(element, element);
                p.lineTo(-element, element);
                p.lineTo(0, -s);
                return p;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
