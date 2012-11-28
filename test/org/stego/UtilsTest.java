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
public class UtilsTest {

    public UtilsTest() {
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
     * Test of buildStego method, of class Utils.
     */
    //@Test
    public void testBuildStego() {
        String inputText = "";
        byte[] expResult = null;
        byte[] result = Utils.buildStego(inputText);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of hide method, of class Utils.
     */
    @Test
    public void testHide() {
        String fileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/t1.txt";
        String imageFileName = "/home/ankur/NetBeansProjects/ImageSteganography/image/14.png";
        boolean expResult = true;
        boolean result = Utils.hide(fileName, imageFileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of reveal method, of class Utils.
     */
    @Test
    public void testReveal() {
        String fileName = "abc.png";
        boolean expResult = true;
        boolean result = Utils.reveal(fileName);
        assertEquals(expResult, result);
    }
}
