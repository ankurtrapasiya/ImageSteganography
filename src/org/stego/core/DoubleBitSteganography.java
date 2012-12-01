/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author ankur
 */
public class DoubleBitSteganography {

    public static boolean hide(String fileName, String imageFileName, String outputFileName) throws LargeMessageException{

        String inputText = Commons.readTextFile(fileName);

        if (inputText.length() == 0) {
            return false;
        }

        byte[] stego = Commons.buildStego(inputText);
        try {

            BufferedImage image = ImageIO.read(new File(imageFileName));

            if (image == null) {
                return false;
            }

            byte[] imageBytes = Commons.accessBytes(image);


            if (!Commons.doubleHide(imageBytes, stego)) {
                return false;
            }

            return Commons.writeImageToFile(outputFileName, image);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    public static boolean reveal(String fileName, String outputFilePath) {
        try {

            BufferedImage bi = ImageIO.read(new File(fileName));

            if (bi == null) {
                return false;
            }

            byte[] imageBytes = Commons.accessBytes(bi);

            int msgLength = getMsgLength(imageBytes, 0);

            if (msgLength == -1) {
                return false;
            }

            String msg = getMessage(imageBytes, msgLength, (Commons.MAX_INT_LEN * Commons.DATA_SIZE) / 2);

            if (msg != null) {

                Commons.writeStringToFile(outputFilePath, msg);

            } else {
                JOptionPane.showMessageDialog(null, "error reading the message");
                return false;
            }


        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return true;

    }

    private static int getMsgLength(byte[] imageBytes, int offset) {

        byte[] lenBytes = extractHiddenBytes(imageBytes, Commons.MAX_INT_LEN, offset);

        if (lenBytes == null) {
            return -1;
        }

        int msgLength = ((lenBytes[0] & 0xff) << 24)
                | ((lenBytes[1] & 0xff) << 16)
                | ((lenBytes[2] & 0xff) << 8)
                | (lenBytes[3] & 0xff);

        if (msgLength <= 0 || msgLength > imageBytes.length) {
            JOptionPane.showMessageDialog(null, "Incorrect message length");
            return -1;
        }
        return msgLength;
    }

    private static byte[] extractHiddenBytes(byte[] imageBytes, int size, int offset) {

        int finalPosition = offset + (size * (Commons.DATA_SIZE / 2));

        if (finalPosition > imageBytes.length) {
            JOptionPane.showMessageDialog(null, "Reached end of the image while reading data");
            return null;
        }

        byte[] hiddenBytes = new byte[size];


        for (int j = 0; j < size; j++) {
            for (int i = 0; i < Commons.DATA_SIZE / 2; i++) {
                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 2) | (imageBytes[offset] & 3));

                offset++;
            }
        }

        return hiddenBytes;
    }

    private static String getMessage(byte[] imageBytes, int msgLength, int offset) {


        byte[] msgBytes = extractHiddenBytes(imageBytes, msgLength, offset);

        if (msgBytes == null) {
            return null;
        }

        String msg = new String(msgBytes);

        return msg;
    }
}
