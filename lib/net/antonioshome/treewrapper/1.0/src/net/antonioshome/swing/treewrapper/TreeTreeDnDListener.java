/*
 * $Id: TreeTreeDnDListener.java,v 1.2 2011/09/26 15:46:42 togro Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper;

import java.util.EventListener;

/**
 * TreeTreeDnDListener represents a listener that receives notifications
 *   when some node from a tree is moved or copied into itself
 *   (or into another tree).
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: togro $
 * @version $Revision: 1.2 $
 */
public interface TreeTreeDnDListener
  extends EventListener
{
  /**
   * Invoked to verify that a node may be dropped into another node.
   * @param anEvent a TreeTreeDnDEvent the event containing information about
   *  the Drag and Drop operation.
   * @throws DnDVetoException if the drag and drop operation is not valid.
   */
  public void mayDrop( TreeTreeDnDEvent anEvent )
    throws DnDVetoException;
  
  /**
   * Invoked when the drop operation happens.
   * @param anEvent a TreeTreeDnDEvent the event containing information about
   *  the Drag and Drop operation.
   * @throws DnDVetoException if the drag and drop operation is not valid.
   */
  public void drop( TreeTreeDnDEvent anEvent )
    throws DnDVetoException;
}
