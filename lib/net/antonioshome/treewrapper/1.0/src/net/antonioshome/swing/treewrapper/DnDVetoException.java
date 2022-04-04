/*
 * $Id: DnDVetoException.java,v 1.2 2011/09/26 15:46:42 togro Exp $
 * Read the "license.txt" file for licensing information.
 * (C) Antonio Vieiro. All rights reserved.
 */

package net.antonioshome.swing.treewrapper;

/**
 * DnDVetoException is an exception thrown to signal that a drag and drop
 *   operation is not valid.
 * @author Antonio Vieiro (antonio@antonioshome.net), $Author: togro $
 * @version $Revision: 1.2 $
 */
public class DnDVetoException
  extends Exception
{
  /**
   * Creates a new instance of DnDVetoException
   * @param aMessage the message to show.
   */
  public DnDVetoException( String aMessage )
  {
    super( aMessage );
  }
  
  
}
