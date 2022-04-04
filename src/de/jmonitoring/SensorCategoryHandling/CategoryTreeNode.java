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
public class CategoryTreeNode extends DefaultMutableTreeNode {

    public CategoryTreeNode() {
        super();
    }

    public CategoryTreeNode(Object userObject, boolean allowsChildren) {
        super(userObject, false);
    }

    public CategoryTreeNode(Object userObject) {
        super(userObject);
    }

    @Override
    public boolean isLeaf() {
        return (false);
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

}
