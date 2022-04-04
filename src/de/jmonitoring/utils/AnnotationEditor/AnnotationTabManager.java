/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils.AnnotationEditor;

/**
 * A class which stores the {@link AnnotationSettingPanel} currently on top of
 * the tabbedpanel in the {@link AnnotationDesigner}
 *
 * @author togro
 */
public class AnnotationTabManager {

    private AnnotationSettingPanel shownPanel;

    public AnnotationSettingPanel getCurrentTab() {
        return shownPanel;
    }

    public void setCurrentTab(AnnotationSettingPanel panel) {
        shownPanel = panel;
    }
}
