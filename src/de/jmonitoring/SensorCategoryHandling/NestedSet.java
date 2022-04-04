package de.jmonitoring.SensorCategoryHandling;

import de.jmonitoring.base.sensors.SensorInformation;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author togro
 */
public class NestedSet {

    public static final String POSSIBLY_DELETED = " (possibly deleted sensor?)";

    /**
     * Erzeugt aus einer übergebenen {@link TreeMap} die ein Nested Set enthält
     * ein {@link TreeModel}. Der Schlüssel ist der linke Wert des nested set
     * und der Wert ein {@link NestedSetElement}
     *
     * @param map
     * @return
     */
    public TreeModel getTreeModelFromNestedSet(TreeMap<Integer, NestedSetElement> map) {
        TreeModel newModel = null;
        Iterator<Integer> it = map.keySet().iterator();
        DefaultMutableTreeNode currentNodeToAppend = null;
        DefaultMutableTreeNode newNode;
        Stack<Integer> rightStack = new Stack<Integer>();
        NestedSetElement element;
        String sensorName;
        while (it.hasNext()) {
            element = map.get(it.next());
            if (newModel == null) { // beim ersten Durchlauf den Wurzelknoten anlegen)
                currentNodeToAppend = new CategoryTreeNode(element.getName());
                newModel = new DefaultTreeModel(currentNodeToAppend);
                rightStack.push(0);
            } else {
                // unterscheiden, ob es ein Messpunkt oder eine Kategorie ist
                sensorName = element.getName().split("\u2015")[0].split("@")[0];
                if (SensorInformation.getSensorIDFromNameORKey(sensorName) > 0 || sensorName.endsWith(POSSIBLY_DELETED)) { // es ist ein Messpunkt
                    newNode = new SensorTreeNode(element.getName());
                } else {
                    newNode = new CategoryTreeNode(element.getName()); // es ist eie Kategorie
                }
//                newNode = new DefaultMutableTreeNode(element.getName());
                while (element.getLeft() > rightStack.peek() && rightStack.peek() > 0) { // wenn der links-Wert größer ist als der momentane rechtswert (des einfügepunkts)
                    currentNodeToAppend = (DefaultMutableTreeNode) currentNodeToAppend.getParent();
                    rightStack.pop();
                }
                currentNodeToAppend.add(newNode);
                if ((element.getRight() - element.getLeft()) > 1) { // wenn es ein Ordner ist diesen als neuen Einfügepunkt setzen und dessen rechten Wert merken
                    currentNodeToAppend = newNode;
                    rightStack.push(element.getRight());
                }
            }
//            System.out.println(element.getName() + " " + element.getLeft() + " " + element.getRight());
        }
        return newModel;
    }

    /**
     * Erzeugt aus einem übergebenen {@link TreeModel} eine {@link TreeMap} mit
     * den Linkswerten als Schlüssel und {@link NestedSetElement} als Wert
     *
     * @param model
     * @return
     */
    public TreeMap<Integer, NestedSetElement> getNestedSetFromTreeModel(TreeModel model) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) model.getRoot();
        Vector<NestedSetElement> vector = new Vector<NestedSetElement>(256);
        runThroughTree(node, 1, vector);
        NestedSetElement element;
        TreeMap<Integer, NestedSetElement> map = new TreeMap<Integer, NestedSetElement>();
        for (Enumeration<NestedSetElement> e = vector.elements(); e.hasMoreElements();) {
            element = e.nextElement();
            map.put(element.getLeft(), element);
//            System.out.println(element.getName() + " " + element.getLeft() + " " + element.getRight());
        }
        return map;
    }

    /**
     * Durchlauft ab dem übergebenen {@link DefaultMutableTreeNode} ein
     * TreeModel und speichert die Werte in eine {@link Vector} von
     * {@link NestedSetElement}
     *
     * @param node
     * @param left
     * @param vector
     * @return
     */
    private int runThroughTree(DefaultMutableTreeNode node, int left, Vector<NestedSetElement> vector) {
        int right = left + 1;
        for (int i = 0; i < node.getChildCount(); i++) {
            right = runThroughTree((DefaultMutableTreeNode) node.getChildAt(i), right, vector);
        }
        vector.add(new NestedSetElement(node.getUserObject().toString(), left, right));
//        System.out.println(node.getUserObject().toString() + " " + left + " " + right);
        return right + 1;
    }
}
