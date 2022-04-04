package de.jmonitoring.base;

/**
 * Interface combining methods used to handle the frames in the desktop panel
 *
 * @author togro
 */
public interface DesktopManager {

    /**
     * Iconify all JInternalFrames
     *
     * @param iconify
     */
    void iconifyWindows(boolean iconify);

        /**
     * Cascade all JInternalFrames
     *
     * @param iconify
     */
    void cascadeWindows();

    /**
     * Close all windows
     */
    void closeAllWindows();

    /**
     * Try to place all windwos next to each other
     */
    void tileAllWindows();

    /**
     * Show window contents while dragged or not
     * @param dragMode 
     */
    void setDragMode(int dragMode);
}
