/*
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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author ankur
 */
public class Commons {

    public static final int DATA_SIZE = 8;
    public static final int MAX_INT_LEN = 4;

    /**
     *
     * returns the byte[] which is about to be written in the image
     *
     */
    public static byte[] buildStego(String inputText) {
        byte[] stego = null;

        byte[] msgBytes = inputText.getBytes();

        //gives size of the data 
        byte[] dataLength = intToBytes(msgBytes.length);

        int totalLen = dataLength.length + msgBytes.length;

        stego = new byte[totalLen];

        System.arraycopy(dataLength, 0, stego, 0, dataLength.length);

        System.arraycopy(msgBytes, 0, stego, dataLength.length, msgBytes.length);

        return stego;
    }

    /**
     * *
     *
     * converts an integer to byte array
     *
     */
    public static byte[] intToBytes(int i) {
        byte[] byteArr = new byte[MAX_INT_LEN];

        byteArr[0] = (byte) ((i >>> 24) & 0xFF);
        byteArr[1] = (byte) ((i >>> 16) & 0xFF);
        byteArr[2] = (byte) ((i >>> 8) & 0xFF);
        byteArr[3] = (byte) (i & 0xFF);

        return byteArr;
    }

    /**
     * *
     *
     * Gives byte[] of the image
     */
    public static byte[] accessBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

        return buffer.getData();
    }

    /**
     *
     *
     * write image to a file
     *
     */
    public static boolean writeImageToFile(String fileName, BufferedImage image) {

        File file = new File(fileName);
        try {
            ImageIO.write(image, "png", file);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /**
     *
     * read text data from the file
     *
     */
    public static String readTextFile(String fileName) {

        StringBuilder text = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            String str = null;
            while ((str = br.readLine()) != null) {

                text.append(str);
                text.append("\n");

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return text.toString();
    }

    /**
     *
     * write string data to file
     *
     */
    public static boolean writeStringToFile(String fileName, String message) {
        File file = new File(fileName);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(message);
            bw.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Actual hiding of single bit data into image
     *
     */
    public static void singleHideStego(byte[] imBytes, byte[] stego, int offset) {

        for (int i = 0; i < stego.length; i++) {

            int byteVal = stego[i];

            for (int j = 7; j >= 0; j--) {
                int bitVal = (byteVal >>> j) & 1;

                imBytes[offset] = (byte) ((imBytes[offset] & 0xFE) | bitVal);

                offset++;

            }
        }
    }

    /**
     * Hide single bit data into image
     *
     */
    public static boolean singleHide(byte[] imBytes, byte[] stego) throws LargeMessageException {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        if ((totalLength * Commons.DATA_SIZE) > imageLength) {
            throw new LargeMessageException("Message is too big to be stored in the image");
        }

        Commons.singleHideStego(imBytes, stego, 0);
        return true;
    }

    /**
     * Actual hiding of double bit data into image
     *
     */
    public static void doubleHideStego(byte[] imBytes, byte[] stego, int offset) {

        for (int i = 0; i < stego.length; i++) {


            int byteVal = stego[i];

            for (int j = 6; j >= 0; j -= 2) {


                int bitVal = (byteVal >>> j) & 3;

                imBytes[offset] = (byte) ((imBytes[offset] & 0xFC) | bitVal);

                offset++;

            }
        }
    }

    /**
     * Hide double bit data into image
     *
     */
    public static boolean doubleHide(byte[] imBytes, byte[] stego) throws LargeMessageException {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        if ((totalLength * (DATA_SIZE / 2)) > imageLength) {
            throw new LargeMessageException("Message is too big to be stored in the image");
        }

        Commons.doubleHideStego(imBytes, stego, 0);
        return true;
    }

    /**
     * Actual hiding of three bit data into image
     *
     */
    public static boolean tripleHide(byte[] imBytes, byte[] stego) throws LargeMessageException {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        System.out.println("total lenght" + totalLength);

        if ((totalLength * ((int) ((double) DATA_SIZE / 3.0))) > imageLength) {
            throw new LargeMessageException("Message is too big to be stored in the image");
        }

        tripleHideStego(imBytes, stego, 0);
        return true;
    }

    /**
     * Hide three bit data into image
     *
     */
    public static void tripleHideStego(byte[] imBytes, byte[] stego, int offset) {

        /*for (int i = 0; i < stego.length; i++) {


         int byteVal = stego[i];

         int bitVal;
         int shift;

         //1

         shift = 5;

         bitVal = (byteVal >>> shift) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;

         //2

         shift = 2;

         bitVal = (byteVal >>> shift) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;


         //3

         bitVal = byteVal & 3;

         bitVal = bitVal << 1;


         if (++i > stego.length) {

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         break;
         }

         int temp = stego[i];

         shift = 7;

         int bitTemp = (temp >>> shift) & 1;

         bitVal = bitVal | bitTemp;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;


         //4

         shift = 4;

         bitVal = (temp >>> shift) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;

         //5

         shift = 1;

         bitVal = (temp >>> shift) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;

         //6


         bitTemp = temp & 1;

         if (++i > stego.length) {

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         break;
         }

         temp = stego[i];

         shift = 6;

         bitVal = (temp >>> shift) & 3;

         bitVal = bitVal << 1;

         bitVal = bitVal | bitTemp;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         //7

         shift = 3;

         bitVal = (temp >>> shift) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;

         //8

         bitVal = (temp) & 7;

         imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);

         offset++;


         }*/

        for (int i = 0; i < stego.length;) {
            int k = (8 - (((offset + 1) * 3) % 8)) % 8;
            int byteVal = stego[i];
            int bitVal;
            if ((k != 6) && (k != 7)) {
                bitVal = (byteVal >>> k) & 7;
            } else {
                int l = 3 - (Math.abs(5 - k));
                bitVal = (byteVal) & 7;
                bitVal = (bitVal << l) & 7;
                if (i + 1 < stego.length) {
                    byteVal = stego[i + 1];
                    int bitVal2 = (byteVal >>> k) & (k == 7 ? 1 : 3);
                    bitVal = bitVal | bitVal2;
                }
            }
            if (k == 7 || k == 6 || k == 0) {
                i++;
            }
            imBytes[offset] = (byte) ((imBytes[offset] & 0xF8) | bitVal);
            offset++;
        }

    }

    /**
     * Actual hiding of double bit data into image
     *
     */
    public static void quadrupleHideStego(byte[] imBytes, byte[] stego, int offset) {

        for (int i = 0; i < stego.length; i++) {

            int byteVal = stego[i];

            for (int j = 4; j >= 0; j -= 4) {

                int bitVal = (byteVal >>> j) & 0xF;

                imBytes[offset] = (byte) ((imBytes[offset] & 0xF0) | bitVal);

                offset++;

            }
        }
    }

    /**
     * Hide double bit data into image
     *
     */
    public static boolean quadrupleHide(byte[] imBytes, byte[] stego) throws LargeMessageException {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        if ((totalLength * (DATA_SIZE / 4)) > imageLength) {
            throw new LargeMessageException("Message is too big to be stored in the image");
        }

        Commons.quadrupleHideStego(imBytes, stego, 0);
        return true;
    }
}