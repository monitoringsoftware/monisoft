package de.jmonitoring.utils.AnnotationEditor;

import com.thoughtworks.xstream.XStream;
import de.jmonitoring.DBOperations.DBConnector;
import de.jmonitoring.base.Messages;
import java.awt.BasicStroke;
import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYPolygonAnnotation;

/**
 * A class used forwriting and reading annotations to the database.<p>
 *
 * Table structure:<p>
 * <code>
 * CREATE TABLE T_Annotations`( <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;name VARCHAR(1000)  NOT NULL, <br>
 *  &nbsp;&nbsp;&nbsp;&nbsp;object MEDIUMTEXT  NOT NULL, <br>
 * &nbsp;&nbsp;&nbsp;&nbsp;PRIMARY KEY (`name`) <br>
 *) <br>
 *ENGINE = MyISAM; <br>
 * </code>
 *
 * @author togro
 */
public class AnnotationHandler {

    private static final String WRITE_OBJECT = "INSERT INTO T_Annotations (name, object) VALUES (?, ?) ON DUPLICATE KEY UPDATE object = ?"; //NOI18N
    private static final String READ_OBJECT = "SELECT object FROM T_Annotations WHERE name = ?"; //NOI18N
    private static final String GET_NAMES = "SELECT name FROM T_Annotations order by name"; //NOI18N
    private static final String REMOVE = "DELETE FROM T_Annotations WHERE NAME = ?"; //NOI18N

    /**
     * Writes the given {@link AnnotationContainer} to the database
     *
     * @param name
     * @param annotation
     * @return <code>true</code> if something was written, *      * otherwise <code>false</code>
     */
    public static boolean writeAnnotation(String name, AnnotationContainer annotation) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String xml = annotationToXML(annotation);
        if (xml == null || xml.isEmpty()) {
            return false;
        }

        int numWritten = 0;
        try {
            conn = DBConnector.openConnection();
            pstmt = conn.prepareStatement(WRITE_OBJECT);
            pstmt.setString(1, name);
            pstmt.setString(2, xml);
            pstmt.setString(3, xml);
            numWritten = pstmt.executeUpdate();
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(conn, pstmt, null);
        }

        if (numWritten > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reads the {@link AnnotationContainer} with the given name from the
     * database
     *
     * @param name
     * @return the {@link AnnotationContainer} or <code>null</code> if no
     * annotation with that name was found.
     */
    public static AnnotationContainer readAnnotation(String name) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String containerXML = null;
        try {
            conn = DBConnector.openConnection();
            pstmt = conn.prepareStatement(READ_OBJECT);
            pstmt.setString(1, name);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                containerXML = rs.getString(1);
            }
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(conn, pstmt, rs);
        }

        if (containerXML == null || containerXML.isEmpty()) {
            return null;
        } else {
            return annotationFromXML(containerXML);
        }
    }

    /**
     * Returns a list with the names of all annotations stored in the database
     *
     * @return
     */
    public static List<String> getAnnotationAsList() {
        ArrayList<String> list = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String name = null;
        try {
            conn = DBConnector.openConnection();
            pstmt = conn.prepareStatement(GET_NAMES);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                name = rs.getString(1);
                list.add(name);
            }
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(conn, pstmt, rs);
        }

        return list;
    }

    /**
     * Checks if an annotation of that name exists
     *
     * @param name
     * @return <code>true</code> if the annotation exists, *      * otherwise <code>false</code>
     */
    public static boolean annotationExists(String name) {
        if (readAnnotation(name) != null) {
            return true;
        }
        return false;
    }

    /**
     * Deletes the annotation with the given name from the database
     *
     * @param name
     * @return the number of deleted rows (should only be 0 or 1)
     */
    public static int removeAnnotation(String name) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        int numDeleted = 0;
        try {
            conn = DBConnector.openConnection();
            pstmt = conn.prepareStatement(REMOVE);
            pstmt.setString(1, name);
            numDeleted = pstmt.executeUpdate();
        } catch (SQLException e) {
            Messages.showException(e);
            Messages.showException(e);
        } finally {
            DBConnector.closeConnection(conn, pstmt, null);
        }
        return numDeleted;
    }

    /**
     * Deletes all annotations from the database.
     *
     * @return the number of deleted annotations
     */
    public static int removeAllAnnotations() {
        int numDeleted = 0;
        for (String name : getAnnotationAsList()) {
            numDeleted += removeAnnotation(name);
        }
        return numDeleted;
    }

    /**
     * Builds a {@link XYAnnotation} from the given {@link AnnotationElement}
     * and returns it.<p>If the {@link AnnotationElement} is closed is will be
     * diplayed as {@link XYPolygonAnnotation} otherwise as
     * {@link MoniSoftLineAnnotation}.<br> In the latter case the coordinates
     * have to be reorganized because the line annotations with multiple lines
     * must be build up of single lines.<br>Therefore the last point of the
     * previous line must be used as the first point of the adjacent line.
     *
     * @param element
     * @return
     */
    public static List<XYAnnotation> getAnnotationForElement(AnnotationElement element) {
        Double[][] points = element.getPoints();
        XYAnnotation annotation;
        List<XYAnnotation> list = new ArrayList<XYAnnotation>();
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
            annotation = new XYPolygonAnnotation(annotationPoints, element.getStroke(), lineColor, fillColor);
            list.add(annotation);
        } else { // line annotation
            if (element.getStroke() == null) {
                element.setStroke(new BasicStroke(1f));
            }
            double[] linePoints = new double[4];
            for (int i = 0; i < points.length - 1; i++) {
                linePoints[0] = points[i][0];
                linePoints[1] = points[i][1];
                linePoints[2] = points[i + 1][0];
                linePoints[3] = points[i + 1][1];

                annotation = new MoniSoftLineAnnotation(linePoints[0], linePoints[1], linePoints[2], linePoints[3], element.getStroke(), lineColor);
                list.add(annotation);
            }
        }

        return list;
    }

    /**
     * Serialize this {@link AnnotationContainer} to XML
     *
     * @param container
     * @return
     */
    private static String annotationToXML(AnnotationContainer container) {
        XStream xstream = new XStream();
        xstream.alias("AnnotationContainer", AnnotationContainer.class); //NOI18N
        xstream.alias("AnnotationElement", AnnotationElement.class); //NOI18N
        return xstream.toXML(container);
    }

    /**
     * Deserialize this XML to an {@link AnnotationContainer}
     *
     * @param xml
     * @return the {@link AnnotationContainer} or <code>null</code> if it cannot
     * be built.
     */
    private static AnnotationContainer annotationFromXML(String xml) {
        XStream xstream = new XStream();
        xstream.alias("AnnotationContainer", AnnotationContainer.class); //NOI18N
        xstream.alias("AnnotationElement", AnnotationElement.class); //NOI18N
        return (AnnotationContainer) xstream.fromXML(xml);
    }
}
