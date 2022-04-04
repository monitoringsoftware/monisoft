package de.jmonitoring.help;

import de.jmonitoring.utils.swing.EDT;
import java.awt.HeadlessException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.util.PropertiesManager;

/**
 * A viewer for the pdf-manual.<p> Creates a {@link JFrame} and fills it with a
 * {@link JPanel} containing the document<br> The static field
 * <code>isShown</code> is used to determine if the Frame is already shown, so
 * clients can decide if they can just jump to the desired page or if they have
 * to create a new instance.
 *
 * @author togro
 */
public class ManualViewer extends JFrame {

    private static final SwingController c = new SwingController();
    public static boolean isShown = false;
    private static JFrame frame;

    /**
     * Creates a new instance and sets the frame-title.
     *
     * @throws HeadlessException
     */
    public ManualViewer() throws HeadlessException {
        super("MoniSoft Manual");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isShown = false;
                setVisible(false);
                dispose();
            }
        });
        frame = this;
    }

    /**
     * Creates the viewer with all settings and builds the viewer panel.<p> The
     * frame is then displayed.
     */
    public void showManual() {
        setIsShown(true);
        PropertiesManager properties = new PropertiesManager(System.getProperties(), ResourceBundle.getBundle(PropertiesManager.DEFAULT_MESSAGE_BUNDLE));
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ROTATE, false);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_ANNOTATION, false);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_TOOL, false);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_TOOLBAR_UTILITY, true);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_BOOKMARKS, true);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_ANNOTATION, false);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_THUMBNAILS, false);
        properties.setBoolean(PropertiesManager.PROPERTY_SHOW_UTILITYPANE_SEARCH, true);

        SwingViewBuilder b = new SwingViewBuilder(c, properties);
        JPanel panel = b.buildViewerPanel();
        URL url = getClass().getResource("/de/jmonitoring/help/manual.pdf");
        c.openDocument(url);
        c.toggleUtilityPaneVisibility();

        getContentPane().add(panel);
        pack();
        setVisible(true);
        toFront();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    /**
     * Jumpto given page
     *
     * @param page
     */
    public static void goToPage(final int page) {
        EDT.perform(new Runnable() {
            @Override
            public void run() {
                c.showPage(page);
                frame.toFront();
//                frame.revalidate();
            }
        });
    }

    public static boolean isIsShown() {
        return isShown;
    }

    public static void setIsShown(boolean isShown) {
        ManualViewer.isShown = isShown;
    }
}
