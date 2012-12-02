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
 * @author WecKen
 */
public class MultiStegCrypt {

    private static final String STEGO_HEADER = "Header1";
    private static int DATA_SIZE = 8;

    public static boolean hide(String fileName, String imageFileName, String outputFileName) throws LargeMessageException {

        String msg = Commons.readTextFile(fileName);
        if (msg.length() == 0) {
            return false;
        }

        String password = SingleHideEncryption.genPassword();
        byte[] passBytes = password.getBytes();

        byte[] msgBytes = msg.getBytes();
        byte[] encryptedMsgBytes = SingleHideEncryption.encryptMsgBytes(msgBytes, password);

        if (encryptedMsgBytes == null) {
            return false;
        }


        byte[] stego = buildStego(passBytes, encryptedMsgBytes);

        try {

            BufferedImage image = ImageIO.read(new File(imageFileName));

            if (image == null) {
                return false;
            }

            byte[] imageBytes = Commons.accessBytes(image);

            if (!multiHide(imageBytes, stego)) {
                return false;
            }


            return Commons.writeImageToFile(outputFileName, image);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }

        return true;
    }

    private static byte[] buildStego(byte[] passBytes, byte[] encryptedMsgBytes) {

        byte[] stego = null;

        byte headerBytes[] = STEGO_HEADER.getBytes();

        byte[] lenBs = Commons.intToBytes(encryptedMsgBytes.length);

        int totalLen = STEGO_HEADER.length() + passBytes.length + lenBs.length + encryptedMsgBytes.length;

        stego = new byte[totalLen];

        int destPos = 0;
        System.arraycopy(headerBytes, 0, stego, destPos, STEGO_HEADER.length()); // header
        destPos += STEGO_HEADER.length();
        System.arraycopy(passBytes, 0, stego, destPos, passBytes.length); // password
        destPos += passBytes.length;
        System.arraycopy(lenBs, 0, stego, destPos, lenBs.length); // length of message
        destPos += lenBs.length;
        System.arraycopy(encryptedMsgBytes, 0, stego, destPos, encryptedMsgBytes.length); //message
        destPos += encryptedMsgBytes.length;
        return stego;
    }

    private static boolean multiHide(byte[] imageBytes, byte[] stego) throws LargeMessageException {

        int imLen = imageBytes.length;

        int totalLen = stego.length;

        if ((totalLen * DATA_SIZE) > imLen) {
            throw new LargeMessageException("Message is too big to be stored in the image");
        }

        int numHides = imLen / (totalLen * DATA_SIZE); 

        for (int i = 0; i < numHides; i++) 
        {
            Commons.singleHideStego(imageBytes, stego, (i * totalLen * DATA_SIZE));
        }

        return true;
    }

    private static byte[] extractHiddenBytes(byte[] imageBytes, int size, int offset) {

        int finalPosition = offset + (size * Commons.DATA_SIZE);

        if (finalPosition > imageBytes.length) {
            JOptionPane.showMessageDialog(null, "Reached end of the image while reading data");
            return null;
        }

        byte[] hiddenBytes = new byte[size];


        for (int j = 0; j < size; j++) {
            for (int i = 0; i < Commons.DATA_SIZE; i++) {
                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 1) | (imageBytes[offset] & 1));
                offset++;
            }
        }
        return hiddenBytes;
    }

    private static boolean findHeader(byte[] imBytes, int offset) {
        byte[] headerBytes =
                extractHiddenBytes(imBytes, STEGO_HEADER.length(), offset);
        if (headerBytes == null) {
            return false;
        }
        String header = new String(headerBytes);
        if (!header.equals(STEGO_HEADER)) {
            return false;
        }
        return true;
    }

    public static boolean reveal(String fileName, String outputFileName) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(fileName));
        } catch (IOException ex) {
            Logger.getLogger(MultiStegCrypt.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (bi == null) {
            return false;
        }
        byte[] imBytes = Commons.accessBytes(bi);
        int imLen = imBytes.length;
        int headOffset = STEGO_HEADER.length() * DATA_SIZE;
        String msg = null;
        boolean foundMsg = false;
        int i = 0;
        while ((i < imLen) && !foundMsg) {
            if (!findHeader(imBytes, i)) 
            {
                i++; 
            } else { 
                i += headOffset; 
                msg = SingleHideEncryption.extractMsg(imBytes, i);
                if (msg != null) {
                    foundMsg = true;
                }
            }
        }

        if (foundMsg) {

            return Commons.writeStringToFile(outputFileName, msg);
        } else {
            JOptionPane.showMessageDialog(null, "error reading the message");
            return false;
        }
    }
}
