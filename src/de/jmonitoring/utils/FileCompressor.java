/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.jmonitoring.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A class used to compress files
 *
 * @author togro
 */
public class FileCompressor {

    /**
     * COmpress the given file
     *
     * @param inFile The file to be compressed
     * @param remove if <code>ture</code> the original will be deleted after
     * compressing
     * @return
     */
    public boolean compressFile(File inFile, boolean remove) {
        boolean success = false;
        File outFile = new File(inFile.getAbsoluteFile() + ".zip");
        try {
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
            byte[] data = new byte[1000];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(inFile));
            int count;
            out.putNextEntry(new ZipEntry(inFile.getName()));
            while ((count = in.read(data, 0, 1000)) != -1) {
                out.write(data, 0, count);
            }
            in.close();
            out.flush();
            out.close();

            if (remove && outFile.exists() && outFile.length() > 0) {
                inFile.delete();
            }
            success = true;
        } catch (SecurityException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return success;
    }
}
