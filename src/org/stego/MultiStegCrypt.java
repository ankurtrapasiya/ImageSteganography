/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author WecKen
 */
public class MultiStegCrypt {
        
      private static final String STEGO_HEADER = "Header1";
      private static int DATA_SIZE = 8;
     
      public static boolean hide(String fileName, String imageFileName) {
       
        //Read the given text file using Utils.readTextFile(fileName) method  
        String msgBytes = Utils.readTextFile(fileName);
        //Check if it exists or not
         if (msgBytes.length() == 0) {
            return false;
        }
        
        // Generate password for encrypted data 
        String password = null;
        //password = Utils.genPassword();
        byte[] passBytes = null;
        //passBytes = password.getBytes();
        
        // use password to encrypt the message
        byte[] encryptedMsgBytes = null;
        //encryptedMsgBytes = encryptMsgBytes(msgBytes, password);
        //if (encryptedMsgBytes == null)
            //return false;      
        
        
        //Build the stego
        byte[] stego = buildStego(passBytes, encryptedMsgBytes);
        
        try {
            
            //Read the image file
            BufferedImage image = ImageIO.read(new File(imageFileName));

            if (image == null) {
                return false;
            }

            //Convert it into bytes format
            byte[] imageBytes = Utils.accessBytes(image);

            //Hide multiple methods using multiHide method
            if (!multiHide(imageBytes, stego)) {
                return false;
            }

            String outputFileName = "abc.png";
            
            //Store file using Utils.writeImageToFile method
            return Utils.writeImageToFile(outputFileName, image);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }
      
    private static byte[] buildStego(byte[] passBytes, byte[] encryptedMsgBytes) {
       
        byte[] stego = null;
        
        //declaring header
        byte headerBytes[] = STEGO_HEADER.getBytes();
        //length of the encrypted message
        byte[] lenBs = Utils.intToBytes(encryptedMsgBytes.length);

        //int totalLen = dataLength.length + msgBytes.length;
        int totalLen = STEGO_HEADER.length() + passBytes.length + lenBs.length + encryptedMsgBytes.length;

        stego = new byte[totalLen];
        
        //Combine all data in stego
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


    private static boolean multiHide(byte[] imageBytes, byte[] stego) {
        
        int imLen = imageBytes.length;
        //System.out.println("Byte length of image: " + imLen);
        
        int totalLen = stego.length;
        //System.out.println("Total byte length of message: " + totalLen);
        
        // check that the stego will fit into the image
        /* multiply stego length by number of image bytes required to store one stego byte */
        if ((totalLen*DATA_SIZE) > imLen) {
            System.out.println("Image not big enough for message");
            return false;
        }
        
        // calculate the number of times the stego can be hidden
        int numHides = imLen/(totalLen*DATA_SIZE); // integer div
        //System.out.println("No. of message duplications: " + numHides);
        
        for(int i=0; i < numHides; i++) // hide stego numHides times
                Utils.hideStego(imageBytes, stego, (i*totalLen*DATA_SIZE));
      
        return true;
    }
    
    

    
}
