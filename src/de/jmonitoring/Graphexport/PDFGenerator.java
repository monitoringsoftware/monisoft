/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.Graphexport;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import de.jmonitoring.base.Messages;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import org.jfree.chart.JFreeChart;

/**
 * Generator for PDF output of charts
 *
 * @author togro
 */
public class PDFGenerator {

    public PDFGenerator() {
    }

    /**
     * Save the given chart as PDF
     *
     * @param file The file to generate
     * @param chart The chart to use
     * @param width Width of the result
     * @param height Height of the result
     * @param mapper The fontmapper
     * @throws IOException
     */
    public void saveChartAsPDF(File file, JFreeChart chart, int width, int height, FontMapper mapper) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        writeChartAsPDF(out, chart, width, height, mapper);
    }

    /**
     * Write the PDF
     * @param out The output stream
     * @param chart The chart to use
     * @param width Width of the result
     * @param height Height of the result
     * @param mapper The fontmapper
     */
    private static void writeChartAsPDF(OutputStream out, JFreeChart chart, int width, int height, FontMapper mapper) {
        Rectangle pageSize = new Rectangle(width, height);
        Document document = new Document(pageSize, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.addSubject("MoniSoft PDF-Generator");
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, mapper);
            Rectangle2D r2 = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2, r2);
            g2.dispose();
            cb.addTemplate(tp, 0, 0);
        } catch (DocumentException e) {
            Messages.showMessage("Fehler bei PDF-Erstellung: " + e.getMessage() + "\n", true);
            Messages.showException(e);
        }
        document.close();
    }
}
