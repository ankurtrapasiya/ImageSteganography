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
public class Utils {

    private static final int DATA_SIZE = 8;
    private static final int MAX_INT_LEN = 4;

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

    public static byte[] intToBytes(int i) {
        byte[] byteArr = new byte[MAX_INT_LEN];

        byteArr[0] = (byte) ((i >>> 24) & 0xFF);
        byteArr[1] = (byte) ((i >>> 16) & 0xFF);
        byteArr[2] = (byte) ((i >>> 8) & 0xFF);
        byteArr[3] = (byte) (i & 0xFF);

        return byteArr;
    }

    public static byte[] accessBytes(BufferedImage image) {
        WritableRaster raster = image.getRaster();
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();

        return buffer.getData();
    }

    public static boolean singleHide(byte[] imBytes, byte[] stego) {
        int imageLength = imBytes.length;

        int totalLength = stego.length;

        if ((totalLength * DATA_SIZE) > imageLength) {
            System.out.println("too big message for image...oops");
            return false;
        }

        hideStego(imBytes, stego, 0);
        return true;
    }

    public static void hideStego(byte[] imBytes, byte[] stego, int offset) {

        for (int i = 0; i < stego.length; i++) {

            int byteVal = stego[i];

            for (int j = 7; j >= 0; j--) {
                int bitVal = (byteVal >>> j) & 1;

                imBytes[offset] = (byte) ((imBytes[offset] & 0xFE) | bitVal);

                offset++;

            }
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


            if (!singleHide(imageBytes, stego)) {
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

    private static boolean writeStringToFile(String fileName, String message) {
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

            String msg = getMessage(imageBytes, msgLength, MAX_INT_LEN * DATA_SIZE);

            if (msg != null) {

                writeStringToFile("abc.txt", msg);

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

    private static String getMessage(byte[] imageBytes, int msgLength, int offset) {
        byte[] msgBytes = extractHiddenBytes(imageBytes, msgLength, offset);

        if (msgBytes == null) {
            return null;
        }

        String msg = new String(msgBytes);

//        if(isPrintable(msg))
//            return msg;
//        else return null;

        return msg;
    }
}
