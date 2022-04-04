/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Graphexport;

import de.jmonitoring.base.Messages;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 * Generator for SVG output of charts
 *
 * @author togro
 */
public class SVGGenerator {

    public SVGGenerator() {
    }

    /**
     * Save a chart as SVG file
     *
     * @param file The file to generate
     * @param chart The chart to print
     * @param width The width of the result
     * @param height The height of the result
     */
    public void saveChartAsSVG(File file, JFreeChart chart, int width, int height) {
        try {
            DOMImplementation domImp = GenericDOMImplementation.getDOMImplementation();
            Document doc = domImp.createDocument(null, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
            svgGenerator.getGeneratorContext().setPrecision(100);
            chart.draw(svgGenerator, new Rectangle2D.Double(0, 0, width, height), null);
            boolean useCSS = true;
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            svgGenerator.stream(out, useCSS);
        } catch (Exception e) {
            Messages.showMessage("Fehler bei SVG-Erstellung: " + e.getMessage() + "\n", true);
            Messages.showException(e);
        }
    }
}
