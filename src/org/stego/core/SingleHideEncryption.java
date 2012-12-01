/*/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jasypt.util.binary.BasicBinaryEncryptor;

/**
 *
 * @author Dipak
 */
public class SingleHideEncryption {

    // globals
    private static final int PASSWORD_LEN = 10;
    private static Random rand = new Random();
    private static final int DATA_SIZE = 8;
    private static final int MAX_INT_LEN = 4;

    /**
     * @param args the command line arguments
     */
    public static boolean hide(String textFnm, String imFnm, String outputFileName) throws LargeMessageException{
        try {

            byte[] msgBytes = readTextBytes(textFnm);
            if (msgBytes == null) {
                return false;
            }

            String password = genPassword();
            byte[] passBytes = password.getBytes();

            byte[] encryptedMsgBytes = encryptMsgBytes(msgBytes, password);
            if (encryptedMsgBytes == null) {
                return false;
            }
            byte[] stego = buildStegoEncy(passBytes, encryptedMsgBytes);

            BufferedImage im = ImageIO.read(new File(imFnm));
            if (im == null) {
                return false;
            }
            byte imBytes[] = accessBytes(im);
            if (!Commons.singleHide(imBytes, stego)) // im is modified with the stego
            {
                return false;
            }


            return Commons.writeImageToFile(outputFileName, im);
        } catch (Exception e) {
            return false;
        }
    }

    public static String genPassword() {
        String availChars =
                "abcdefghjklmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuffer sb = new StringBuffer(PASSWORD_LEN);
        for (int i = 0; i < PASSWORD_LEN; i++) {
            int pos = rand.nextInt(availChars.length());
            sb.append(availChars.charAt(pos));
        }
        return sb.toString();
    }

    public static byte[] encryptMsgBytes(byte[] msgBytes,
            String password) // encrypt a message (a byte array) using the password
    {
        BasicBinaryEncryptor bbe = new BasicBinaryEncryptor();
        bbe.setPassword(password);
        return bbe.encrypt(msgBytes);
    }

    private static byte[] buildStegoEncy(byte[] passBytes,
            byte[] encryptedMsgBytes) {
        byte[] lenBs = intToBytes(encryptedMsgBytes.length);
        int totalLen = passBytes.length + lenBs.length
                + encryptedMsgBytes.length;
        byte[] stego = new byte[totalLen]; // for holding the stego
// combine the 3 fields into one byte array
        int destPos = 0;
        System.arraycopy(passBytes, 0, stego, destPos, passBytes.length);
        destPos += passBytes.length; // add the password
        System.arraycopy(lenBs, 0, stego, destPos, lenBs.length);
        destPos += lenBs.length; //add length of encrypted binary message
        System.arraycopy(encryptedMsgBytes, 0, stego, destPos,
                encryptedMsgBytes.length);
// encrypted binary message
        return stego;
    } //

    public static boolean reveal(String imFnm, String outputFileFile) {
        
        boolean retval=false;
        try {
            BufferedImage im = ImageIO.read(new File(imFnm));
            if (im == null) {
                return retval;
            }
            byte[] imBytes = accessBytes(im);
            int imLen = imBytes.length;
            System.out.println("Byte Length of image: " + imLen);
            String msg = extractMsg(imBytes, 0);
            if (msg != null) { // save message in a text file

                retval=Commons.writeStringToFile(outputFileFile, msg);
            } else {
                System.out.println("No message found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retval;
    } //

    public static String extractMsg(byte[] imBytes, int offset) {
        String password = getPassword(imBytes, offset);
        if (password == null) {
            return null;
        }
        offset += PASSWORD_LEN * DATA_SIZE; // move past password
        int msgLen = getMsgLength(imBytes, offset);
        if (msgLen == -1) {
            return null;
        }
        offset += MAX_INT_LEN * DATA_SIZE; // move past message length
        return getMessage(imBytes, msgLen, password, offset);
    }

    private static int getMsgLength(byte[] imageBytes, int offset) {

        byte[] lenBytes = extractHiddenBytes(imageBytes, MAX_INT_LEN, offset);

        if (lenBytes == null) {
            return -1;
        }

        int msgLength = ((lenBytes[0] & 0xff) << 24)
                | ((lenBytes[1] & 0xff) << 16)
                | ((lenBytes[2] & 0xff) << 8)
                | (lenBytes[3] & 0xff);

        if (msgLength <= 0 || msgLength > imageBytes.length) {
            System.out.println("incorrect message length");
            return -1;
        }
        return msgLength;
    }

    private static byte[] extractHiddenBytes(byte[] imageBytes, int size, int offset) {

        int finalPosition = offset + (size * DATA_SIZE);

        if (finalPosition > imageBytes.length) {
            System.out.println("image end reached");
            return null;
        }

        byte[] hiddenBytes = new byte[size];


        for (int j = 0; j < size; j++) {
            for (int i = 0; i < DATA_SIZE; i++) {
                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 1) | (imageBytes[offset] & 1));

                offset++;
            }
        }

        return hiddenBytes;
    }

    private static String getPassword(byte[] imBytes, int offset) {
        byte[] passBytes =
                extractHiddenBytes(imBytes, PASSWORD_LEN, offset);
        if (passBytes == null) {
            return null;
        }
        String password = new String(passBytes);


        return password;

    }

    private static String getMessage(byte[] imBytes,
            int msgLen, String password, int offset) {
        byte[] enMsgBytes = extractHiddenBytes(imBytes, msgLen, offset);

        if (enMsgBytes == null) {
            return null;
        }

        BasicBinaryEncryptor bbe = new BasicBinaryEncryptor();
        bbe.setPassword(password);
        byte[] msgBytes = null;
        try {
            msgBytes = bbe.decrypt(enMsgBytes);
        } catch (Exception e) {
            System.out.println("Problem decrypting message");
            return null;
        }
        String msg = new String(msgBytes);

        return msg;

    }

    private static byte[] readTextBytes(String filename) throws FileNotFoundException {
        String read = Commons.readTextFile(filename);
        byte[] b = read.getBytes();


        return b;

    }

    private static byte[] accessBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

        return buffer.getData();
    }

    private static void hideStego(byte[] imBytes, byte[] stego, int offset) {

        for (int i = 0; i < stego.length; i++) {

            int byteVal = stego[i];

            for (int j = 7; j >= 0; j--) {
                int bitVal = (byteVal >>> j) & 1;

                imBytes[offset] = (byte) ((imBytes[offset] & 0xFE) | bitVal);

                offset++;

            }
        }
    }

    private static byte[] intToBytes(int i) {
        byte[] byteArr = new byte[MAX_INT_LEN];

        byteArr[0] = (byte) ((i >>> 24) & 0xFF);
        byteArr[1] = (byte) ((i >>> 16) & 0xFF);
        byteArr[2] = (byte) ((i >>> 8) & 0xFF);
        byteArr[3] = (byte) (i & 0xFF);
        return byteArr;
    }

}