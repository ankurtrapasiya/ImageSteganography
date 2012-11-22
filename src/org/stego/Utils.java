/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego;

/**
 *
 * @author ankur
 */
public class Utils {

    private static final int DATA_SIZE = 8;
    private static final int MAX_INT_LEN = 4;

    public static byte[] buildStego(String inputText) {
        byte[] retval = null;


        byte[] msgBytes = inputText.getBytes();



        return retval;
    }

    private static byte[] intToBytes(int i) {
        byte[] byteArr = new byte[MAX_INT_LEN];
        
        //byteArr[0]=((i>>)&& 0xFF)
        
        return byteArr;
    }
}
