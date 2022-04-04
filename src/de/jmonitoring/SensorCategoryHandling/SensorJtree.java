/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.SensorCategoryHandling;

import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author togro
 */
public class SensorJtree extends JTree {

    public SensorJtree(TreeModel newModel) {
        super(newModel);
    }

    public SensorJtree(TreeNode root, boolean asksAllowsChildren) {
        super(root, asksAllowsChildren);
    }

    public SensorJtree(TreeNode root) {
        super(root);
    }

    public SensorJtree(Hashtable<?, ?> value) {
        super(value);
    }

    public SensorJtree(Vector<?> value) {
        super(value);
    }

    public SensorJtree(Object[] value) {
        super(value);
    }

    public SensorJtree() {
    }

    @Override
    public boolean isPathEditable(TreePath path) {
        if (isEditable()) {
            return ! getModel().isLeaf(path.getLastPathComponent());
        }
        return false;
    }
}
