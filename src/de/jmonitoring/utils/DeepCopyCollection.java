/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import de.jmonitoring.base.Messages;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 *
 * @author togro
 */
public class DeepCopyCollection {

    public Object makeDeepCopy(Object list) {

        // serialize into byte array
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(list);
            byte buf[] = baos.toByteArray();
            oos.close();

            // deserialize byte array into Object
            ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Object o = ois.readObject();
            ois.close();

            return o;
        } catch (Exception e) {
            Messages.showException(e);
        }
        return null;
    }


}
