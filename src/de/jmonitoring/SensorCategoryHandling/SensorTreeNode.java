/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.SensorCategoryHandling;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author togro
 */
public class SensorTreeNode extends DefaultMutableTreeNode {

    public SensorTreeNode() {
        super();
    }

    public SensorTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, false);
    }

    public SensorTreeNode(Object userObject) {
        super(userObject);
    }

    @Override
    public boolean isLeaf() {
        return (true);
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }
}
