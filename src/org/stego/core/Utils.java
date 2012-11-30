/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego.core;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author ankur
 */
public class Utils {

    /*
     * Get the extension of a file.
     */
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        System.out.println("ext" + ext);
        return ext;
    }

    
    /*
     * Filter class for showing files which can be steganographed
     */
    public static class TxtFilter extends FileFilter {

        @Override
        public boolean accept(File f) {

            if (f.isDirectory()) {
                return false;
            }

            String extension = Utils.getExtension(f);

            if (extension.equals("txt")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "select only txt files";
        }
    }
    
    
     /*
     * Filter class for showing files which can be steganographed
     */
    public static class PngFilter extends FileFilter {

        @Override
        public boolean accept(File f) {

            if (f.isDirectory()) {
                return false;
            }

            String extension = Utils.getExtension(f);

            if (extension.equals("png")) {
                return true;
            }
            return false;
        }

        @Override
        public String getDescription() {
            return "select only png files";
        }
    }
}
