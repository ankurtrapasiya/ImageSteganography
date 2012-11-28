/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.stego;

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
public class TwoBitTest {
    
    public TwoBitTest() {
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
     * Test of buildStego method, of class TwoBit.
     */
   // @Test
    public void testBuildStego() {
        System.out.println("buildStego");
        String inputText = "";
        byte[] expResult = null;
        byte[] result = TwoBit.buildStego(inputText);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of hide method, of class TwoBit.
     */
    @Test
    public void testHide() {
        System.out.println("hide");
        String fileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/t1.txt";
        String imageFileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/14.png";
        boolean expResult = true;
        boolean result = TwoBit.hide(fileName, imageFileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of reveal method, of class TwoBit.
     */
    @Test
    public void testReveal() {
        System.out.println("reveal");
        String fileName = "abc.png";
        boolean expResult = true;
        boolean result = TwoBit.reveal(fileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
}
