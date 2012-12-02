/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego.core;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ankur
 */
public class FourBitSteganographyTest {
    
    public FourBitSteganographyTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of hide method, of class FourBitSteganography.
     */
    @Test
    public void testHide() throws Exception {
        System.out.println("hide");
        String fileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/t4.txt";
        String imageFileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/14.png";
        String outputFileName = "/home/ankur/NetBeansProjects/ImageSteganography/output/4bit.png";
        boolean expResult = true;
        boolean result = FourBitSteganography.hide(fileName, imageFileName, outputFileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of reveal method, of class FourBitSteganography.
     */
    @Test
    public void testReveal() {
        System.out.println("reveal");
        String fileName = "/home/ankur/NetBeansProjects/ImageSteganography/output/4bit.png";
        String outputFilePath = "/home/ankur/NetBeansProjects/ImageSteganography/output/4bit.txt";
        boolean expResult = true;
        boolean result = FourBitSteganography.reveal(fileName, outputFilePath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }
}
