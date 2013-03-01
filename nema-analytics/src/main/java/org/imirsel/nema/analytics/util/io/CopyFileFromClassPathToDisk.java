/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imirsel.nema.analytics.util.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kris.west@gmail.com
 */
public class CopyFileFromClassPathToDisk {
    
    public static void copy(String classPath, File fileLocation){
        OutputStream out = null;
        try {
//            ClassLoader loader = ClassLoader.getSystemClassLoader();
//            URL loc = loader.getResource(classPath);
//            InputStream iStream = new FileInputStream(loc);
//            //InputStream iStream = loader.getResourceAsStream(classPath);
            InputStream iStream = CopyFileFromClassPathToDisk.class.getResourceAsStream(classPath);
            if (iStream == null){
                 Logger.getLogger(CopyFileFromClassPathToDisk.class.getName()).log(Level.SEVERE, "Resource not found! Classpath: " + classPath);
            }

            out = new FileOutputStream(fileLocation);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = iStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(CopyFileFromClassPathToDisk.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (out != null){
                    out.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(CopyFileFromClassPathToDisk.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
