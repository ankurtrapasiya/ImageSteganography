/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego.core;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jasypt.util.binary.BasicBinaryEncryptor;

/**
 *
 * @author Time
 */
public class FragMultiStegCrypt {
//Global

    private static final int NUM_FRAGS = 100;
    private static final int PASSWORD_LEN = 3;
    private static final int NUM_FRAG_LEN = 4;
    private static final String STEGO_HEADER = "XXX";
    private static Random rand = new Random();
    private static int currMsgFragLen;
    private static final int DATA_SIZE = 8;
    private static final int MAX_INT_LEN = 4;

    public static boolean hide(String textFrm, String imFrm,String outputFileName) {
        try {
            
            byte[][] msgFrags = readByteFrags(textFrm);
            if (msgFrags == null) {
                return false;
            }
            byte[][] stegoFrags = new byte[NUM_FRAGS][]; 
            for (int i = 0; i < NUM_FRAGS; i++) {
                String password = getPassword();
                byte[] passBytes = password.getBytes();
                
                byte[] encryptedFrag = encryptMsgBytes(msgFrags[i], password);
                if (encryptedFrag == null) {
                    return false;
                }
               
                stegoFrags[i] = buildStegoFrag(i, passBytes, encryptedFrag);
            }
            BufferedImage im = ImageIO.read(new File(imFrm));
            if (im == null) {
                return false;
            }
            byte imBytes[] = commons.accessBytes(im);
            
            if (!multipleHide(imBytes, stegoFrags)) {
                return false;
            }
           
            return commons.writeImageToFile(outputFileName, im);
        } catch (IOException ex) {
            Logger.getLogger(FragMultiStegCrypt.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

    }

    public static byte[][] readByteFrags(String fnm) {
        String inputText = commons.readTextFile(fnm);
        
        if ((inputText == null) || (inputText.length() == 0)) {
            return null;
        }
        
        int fragLen = inputText.length() / NUM_FRAGS; 
        System.out.print("Input Text Size :- " + inputText.length() + " fragment length :- " + fragLen);
        if (fragLen == 0) 
        {
            inputText = String.format("%1$-" + NUM_FRAGS + "s", inputText);
            
            fragLen = 1;
        }
       
        int startPosn = 0;
        int endPosn = fragLen;
        String textFrag;
        
        byte[][] msgFrags = new byte[NUM_FRAGS][];
       
        for (int i = 0; i < NUM_FRAGS - 1; i++) {
            textFrag = inputText.substring(startPosn, endPosn);
            System.out.print(textFrag + "\n");
            msgFrags[i] = textFrag.getBytes();
//            byte[] b=new  byte[msgFrags[i].length];
//            for(int j=0;j<msgFrags[i].length;j++)
//                     b[j]=msgFrags[i][j];
//            String s =new String(b);
//            System.out.print(s);
            startPosn = endPosn;
            endPosn += fragLen;
        }
        textFrag = inputText.substring(startPosn);
        System.out.print("End :-" + textFrag + "\n");
        msgFrags[NUM_FRAGS - 1] = textFrag.getBytes();
        return msgFrags;
       

    }

    public static String getPassword() {
        String availChars = "abcdefghjklmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        // chars available for password
        StringBuffer sb = new StringBuffer(PASSWORD_LEN);
        for (int i = 0; i < PASSWORD_LEN; i++) {
            int pos = rand.nextInt(availChars.length());
            sb.append(availChars.charAt(pos));
        }
        return sb.toString();
    } 

    private static boolean multipleHide(byte[] imBytes,
            byte[][] stegoFrags) // store the stego fragments multiple times in the image
    {
        int imLen = imBytes.length;
        System.out.println("Byte length of image: " + imLen);

        int totalLen = 0;
        for (int i = 0; i < NUM_FRAGS; i++) {
            totalLen += stegoFrags[i].length;
        }
        System.out.println("Total byte length of info: " + totalLen);

        if ((totalLen * DATA_SIZE) > imLen) {
            System.out.println("Image not big enough for message");
            return false;
        }

        int numHides = imLen / (totalLen * DATA_SIZE); // integer div
        System.out.println("No. of message duplications: " + numHides);
        int offset = 0;
        for (int h = 0; h < numHides; h++) //hide all frags, numHides times
        {
            for (int i = 0; i < NUM_FRAGS; i++) {
                hideStego(imBytes, stegoFrags[i], offset);
//Utils.hideStegoFrags(imBytes, stegoFrags[i], offset);
                offset += stegoFrags[i].length * DATA_SIZE;

            }
        }
        return true;
    }

    public static byte[] buildStegoFrag(int i, byte[] passBytes, byte[] encryptedFrag) {
        { 
            byte headerBytes[] = STEGO_HEADER.getBytes(); 

            byte[] fragNumBs = commons.intToBytes(i);
            byte[] lenBs = commons.intToBytes(encryptedFrag.length);
            int totalLen = STEGO_HEADER.length() + fragNumBs.length
                    + passBytes.length + lenBs.length
                    + encryptedFrag.length;
            byte[] sFrag = new byte[totalLen];
            int destPos = 0;
            System.arraycopy(headerBytes, 0, sFrag, destPos,
                    STEGO_HEADER.length()); 
            destPos += STEGO_HEADER.length(); 
            System.arraycopy(fragNumBs, 0, sFrag, destPos, fragNumBs.length);
            destPos += fragNumBs.length; 
            System.arraycopy(passBytes, 0, sFrag, destPos, passBytes.length);
            destPos += passBytes.length;
            System.arraycopy(lenBs, 0, sFrag, destPos, lenBs.length);

            destPos += lenBs.length;
            System.arraycopy(encryptedFrag, 0, sFrag, destPos,
                    encryptedFrag.length); 

            return sFrag;
        } 
    }

    private static int findFragNumber(byte[] imBytes, int offset) // extract a fragment number in the range 0-(NUM_FRAGS-1)
    {

        int fNum = -1;
        try {
            fNum = getFragNo(imBytes, offset);
        } catch (NumberFormatException e) {
            System.out.println("Could not parse frag no: " + fNum);
        }
        if ((fNum < 0) || (fNum > NUM_FRAGS)) {
            System.out.println("Incorrect fragment number");
            return -1;
        }
        return fNum;
    } 

    public static int getFragNo(byte[] imageBytes, int offset) {

        byte[] lenBytes = extractHiddenBytes(imageBytes, MAX_INT_LEN, offset);

        if (lenBytes == null) {
            return -1;
        }

        int fragNo = ((lenBytes[0] & 0xff) << 24)
                | ((lenBytes[1] & 0xff) << 16)
                | ((lenBytes[2] & 0xff) << 8)
                | (lenBytes[3] & 0xff);

        if (fragNo < 0 || fragNo > imageBytes.length) {
            System.out.println("incorrect message length");
            return -1;
        }
        return fragNo;
    }

    private static String extractMsgFrag(byte[] imBytes, int offset) {
        String password = getPassword(imBytes, offset);
        System.out.print(password);

        if (password == null) {
            return null;
        }
        offset += PASSWORD_LEN * DATA_SIZE; 
        int msgFragLen = getMsgLength(imBytes, offset);
        if (msgFragLen == -1) {
            return null;
        }
        currMsgFragLen = msgFragLen; 
        offset += MAX_INT_LEN * DATA_SIZE; 
        return getMessage(imBytes, msgFragLen, password, offset);
    } 

    public static int getMsgLength(byte[] imageBytes, int offset) {

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

    private static String combineFragments(String[] msgFrags,
            int numFragsFound) 
    {
        if (numFragsFound == NUM_FRAGS) {
            System.out.println("ALL message fragments extracted");
        } else {
            System.out.println("Only found " + numFragsFound + "/"
                    + NUM_FRAGS + " message fragments");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < NUM_FRAGS; i++) {
            if (msgFrags[i] != null) {
                sb.append(msgFrags[i]);
            } else {
                sb.append("\n????? missing fragment " + i + " ?????");
            }

        }
        return sb.toString();
    } 

    private static int findFragments(byte[] imBytes, String[] msgFrags) {
        for (int i = 0; i < NUM_FRAGS; i++) {
            msgFrags[i] = null;
        }
        int numFragsFound = 0;
        int imLen = imBytes.length;
        System.out.println("Byte Length of image: " + imLen);
        int headOffset = STEGO_HEADER.length() * DATA_SIZE;
 
        int fragNumOffset = NUM_FRAG_LEN * DATA_SIZE;
        
        int i = 0;
        while ((i < imLen) && (numFragsFound < NUM_FRAGS)) {
            if (!findHeader(imBytes, i)) 
            {
                i++; 
            } else { 
                i += headOffset;
                int fNum = findFragNumber(imBytes, i);
                System.out.print("\t \t \t \tF No :- " + fNum + "\n");

                if (fNum != -1) {
                    i += fragNumOffset;
                    if (msgFrags[fNum] != null) 
                    {
                        System.out.println("Fragment " + fNum + " already extracted");
                    } else { 
                        String msgFrag = extractMsgFrag(imBytes, i);
                        if (msgFrag == null) {
                            System.out.println("Failed to extract fragment " + fNum);
                        } else { 
                            System.out.print("hello:- " + i + "  Length:- " + imLen + " mumFrag:- " + numFragsFound + " munFrag:- " + NUM_FRAGS + " \n");
                            System.out.println("Storing fragment " + fNum);
                            msgFrags[fNum] = msgFrag;
                            numFragsFound++;
                            i += (PASSWORD_LEN + commons.MAX_INT_LEN
                                    + currMsgFragLen) * DATA_SIZE;
                            
                        }
                    }
                }
            }
        }
        return numFragsFound;
    } 

    public static String getMessage(byte[] imageBytes, int msgLength, String password, int offset) {
        byte[] msgBytes = extractHiddenBytes(imageBytes, msgLength, offset);

        if (msgBytes == null) {
            return null;
        }
        BasicBinaryEncryptor bbe = new BasicBinaryEncryptor();
        bbe.setPassword(password);
        byte[] msgBytes1 = null;
        try {
            msgBytes1 = bbe.decrypt(msgBytes);
        } catch (Exception e) 
        {
            System.out.println("Problem decrypting message");
            return null;
        }

        String msg = new String(msgBytes1);

        return msg;
    }

    private static boolean findHeader(byte[] imBytes, int offset) // does a stego header start at the offset position in the image?
    {
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
    private static byte[] extractHiddenBytes(byte[] imBytes,
            int size, int offset) {
        int finalPosn = offset + (size * DATA_SIZE);
        if (finalPosn > imBytes.length) {
            System.out.println("End of image reached");
            return null;
        }
        byte[] hiddenBytes = new byte[size];
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < DATA_SIZE; i++) {

                hiddenBytes[j] = (byte) ((hiddenBytes[j] << 1)
                        | (imBytes[offset] & 1));
                offset++;
            }
        }
        return hiddenBytes;
    } 

    public static boolean reveal(String imFnm,String outputFileName) {
        try {
           
            BufferedImage im = ImageIO.read(new File(imFnm));
            if (im == null) {
                return false;
            }
            byte[] imBytes = commons.accessBytes(im);
//            int len=Utils.getMsgLength(imBytes, 0);
//            System.out.print("Size ;- "+len+"\n");
            //System.out.print(Utils.getMessage(imBytes,len,0));
            String[] msgFrags = new String[NUM_FRAGS]; // holds message frags
            int numFragsFound = findFragments(imBytes, msgFrags);
            if (numFragsFound == 0) {
                System.out.println("No message found");
                return false;
            }
            String msg = combineFragments(msgFrags, numFragsFound);
            
            return commons.writeStringToFile(outputFileName, msg); // save message
        } 
        catch (IOException ex) {
            Logger.getLogger(FragMultiStegCrypt.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    } 

    public static String getPassword(byte[] imBytes, int offset) {
        byte[] passBytes =
                extractHiddenBytes(imBytes, PASSWORD_LEN, offset);
        if (passBytes == null) {
            return null;
        }
        String password = new String(passBytes);
        return password;
    }

    public static void main(String[] args) {
        String fileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/t1.txt";
        String imageFileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/14.png";
        String outputFileName="/home/ankur/NetBeansProjects/ImageSteganography/image/14.png0";
        hide(fileName, imageFileName,outputFileName);
        fileName = "/home/ankur/NetBeansProjects/ImageSteganography/abc.png";
        reveal(fileName,outputFileName);
        System.out.print("Ok");
    }

    public static String toBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++) {
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        }
        return sb.toString();
    }

    private static byte[] encryptMsgBytes(byte[] msgBytes,
            String password)
    {
        BasicBinaryEncryptor bbe = new BasicBinaryEncryptor();
        bbe.setPassword(password);
        return bbe.encrypt(msgBytes);
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
}