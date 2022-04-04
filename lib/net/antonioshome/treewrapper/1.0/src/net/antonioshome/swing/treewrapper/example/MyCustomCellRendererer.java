/*
 * $Id: MyCustomCellRendererer.java,v 1.2 2011/09/26 15:51:11 togro Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper.example;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * MyCustomCellRendererer is a cell renderer that shows some (ugly, sorry) icons
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: togro $
 * @version $Revision: 1.2 $
 */
class MyCustomCellRendererer
  extends DefaultTreeCellRenderer
{
  ImageIcon orangeIcon;
  ImageIcon appleIcon;
  ImageIcon folderIcon;
  
  public MyCustomCellRendererer()
  {
    orangeIcon = new ImageIcon( getClass().getResource("orange.png") );
    appleIcon = new ImageIcon( getClass().getResource("apple.png") );
    folderIcon = new ImageIcon(getClass().getResource("Folder.png"));
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
  {
    super.getTreeCellRendererComponent( tree, value, sel,
      expanded, leaf, row, hasFocus );
    
    if ( value.toString().startsWith( "orange") )
      setIcon( orangeIcon );
    else if ( value.toString().startsWith("apple") )
      setIcon( appleIcon );
    else
      setIcon( folderIcon );
    
    return this;
  }
  
  
}
