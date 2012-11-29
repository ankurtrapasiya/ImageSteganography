/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego;

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
public class ThreeBit {

    private static final int DATA_SIZE = 8;
    private static final int MAX_INT_LEN = 4;

    public static byte[] buildStego(String inputText) {
        byte[] stego = null;


        byte[] msgBytes = inputText.getBytes();
        byte[] dataLength = intToBytes(msgBytes.length);

        int totalLen = dataLength.length + msgBytes.length;

        stego = new byte[totalLen];

        System.arraycopy(dataLength, 0, stego, 0, dataLength.length);

        System.arraycopy(msgBytes, 0, stego, dataLength.length, msgBytes.length);

        return stego;
    }

    private static byte[] intToBytes(int i) {
        byte[] byteArr = new byte[MAX_INT_LEN];

        byteArr[0] = (byte) ((i >>> 24) & 0xFF);
        byteArr[1] = (byte) ((i >>> 16) & 0xFF);
        byteArr[2] = (byte) ((i >>> 8) & 0xFF);
        byteArr[3] = (byte) (i & 0xFF);

        return byteArr;
    }

    private static byte[] accessBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

        return buffer.getData();
    }

    private static boolean doubleHide(byte[] imBytes, byte[] stego) {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        System.out.println("total lenght" + totalLength);
        
        if ((totalLength * ((int)((double) DATA_SIZE / 3.0))) > imageLength) {
            System.out.println("too big message for image...oops");
            return false;
        }

        hideStego(imBytes, stego, 0);
        return true;
    }

    private static void hideStego(byte[] imBytes, byte[] stego, int offset) {

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

    public static boolean hide(String fileName, String imageFileName) {

        String inputText = readTextFile(fileName);

        if (inputText.length() == 0) {
            return false;
        }

        byte[] stego = buildStego(inputText);
        try {

            BufferedImage image = ImageIO.read(new File(imageFileName));

            if (image == null) {
                return false;
            }

            byte[] imageBytes = accessBytes(image);


            if (!doubleHide(imageBytes, stego)) {
                return false;
            }

            String outputFileName = "abc.png";

            return writeImageToFile(outputFileName, image);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    private static boolean writeImageToFile(String fileName, BufferedImage image) {

        File file = new File(fileName);
        try {
            ImageIO.write(image, "png", file);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static String readTextFile(String fileName) {

        StringBuilder text = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
            String str = null;
            while ((str = br.readLine()) != null) {

                text.append(str);
                text.append("\n");

            }

        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return text.toString();
    }

    private static boolean writeStringToFile(String fileName, String message) {
        File file = new File(fileName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(message);
            bw.flush();
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean reveal(String fileName) {
        try {

            BufferedImage bi = ImageIO.read(new File(fileName));

            if (bi == null) {
                return false;
            }

            byte[] imageBytes = accessBytes(bi);

            int msgLength = getMsgLength(imageBytes, 0);

            if (msgLength == -1) {
                return false;
            }

            String msg = getMessage(imageBytes, msgLength, Double.valueOf(Math.ceil(((double) MAX_INT_LEN) * ((double) DATA_SIZE / 3.0))).intValue());

            if (msg != null) {

                writeStringToFile("msg.txt", msg);

            } else {
                System.out.println("No message found");
                return false;
            }


        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return true;

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

        double data = Double.valueOf(Math.ceil(((double) size) * ((double) DATA_SIZE / 3.0)));

        int finalPosition = offset + (int) data;

        System.out.println("data" + data);

        if (finalPosition > imageBytes.length) {
            System.out.println("image end reached");
            return null;
        }

        byte[] hiddenBytes = new byte[size];


        for (int j = -1; j < size;) {

            int k = (8 - (((offset) * 3) % 8)) % 8;

            k = (k >= 3) ? 3 : k;

            if (k == 0) {
                j++;
            }

            int byteVal = imageBytes[offset];

            hiddenBytes[j] = (byte) (hiddenBytes[j] << k);

            int val = byteVal & 7;

            if ((k == 1)) {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | (val >> 2));

                val = val & 3;

                if (j + 1 < size) {

                    hiddenBytes[j + 1] = (byte) (hiddenBytes[j + 1] | val);

                    j++;
                } else {
                    j++;
                }


            } else if (k == 2) {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | (val >> 1));

                val = val & 1;

                if (j + 1 < size) {

                    hiddenBytes[j + 1] = (byte) (hiddenBytes[j + 1] | val);

                    j++;

                } else {
                    j++;
                }


            } else {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | val);

            }

            offset++;

        }
        return hiddenBytes;
    }

    private static String getMessage(byte[] imageBytes, int msgLength, int offset) {


        byte[] msgBytes = getActualData(imageBytes, msgLength, offset);

        if (msgBytes == null) {
            return null;
        }

        String msg = new String(msgBytes);

        System.out.println("message" + msg);

        return msg;
    }

    private static byte[] getActualData(byte[] imageBytes, int size, int offset) {
        double data = Double.valueOf(Math.ceil(((double) size) * ((double) DATA_SIZE / 3.0)));

        int finalPosition = offset + (int) data;

        System.out.println("data" + data);

        if (finalPosition > imageBytes.length) {
            System.out.println("image end reached");
            return null;
        }

        byte[] hiddenBytes = new byte[size];


        for (int j = 0; j < size;) {

            int k = (8 - (((offset) * 3) % 8)) % 8;

            k = (k >= 3) ? 3 : k;

            if (k == 0) {
                j++;
            }

            int byteVal = imageBytes[offset];

            hiddenBytes[j] = (byte) (hiddenBytes[j] << k);

            int val = byteVal & 7;

            if ((k == 1)) {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | (val >> 2));

                val = val & 3;

                if (j + 1 < size) {

                    hiddenBytes[j + 1] = (byte) (hiddenBytes[j + 1] | val);

                    j++;
                } else {
                    j++;
                }


            } else if (k == 2) {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | (val >> 1));

                val = val & 1;

                if (j + 1 < size) {

                    hiddenBytes[j + 1] = (byte) (hiddenBytes[j + 1] | val);

                    j++;

                } else {
                    j++;
                }


            } else {

                hiddenBytes[j] = (byte) (hiddenBytes[j] | val);

            }

            offset++;

        }
               
        
        return hiddenBytes;
    }
}
