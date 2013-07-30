/* ===============
 * Eastwood Charts
 * ===============
 *
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Project Info:  http://www.jfree.org/eastwood/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------------------
 * GDataUtilitiesTests.java
 * ------------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 14-Jul-2008 : Version 1 (DG);
 *
 */

package org.jfree.eastwood.junit;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.eastwood.DataUtilities;

/**
 * Tests for the {@link DataUtilities} class.
 */
public class DataUtilitiesTests extends TestCase {

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(DataUtilitiesTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public DataUtilitiesTests(String name) {
        super(name);
    }

    /**
     * A test to confirm that the parsing of data in the simple format is
     * working.
     */
    public void testParseSimpleData() {
    	String s = "s:ATb19,Mn5t_z";
    	List data = DataUtilities.parseSimpleData(s);
    	assertEquals(2, data.size());
    	List s1 = (List) data.get(0);
    	assertEquals(5, s1.size());
        assertEquals(0.0, ((Double) s1.get(0)).doubleValue(), EPSILON);
        assertEquals(19.0 / 61.0, ((Double) s1.get(1)).doubleValue(), EPSILON);
        assertEquals(27.0 / 61.0, ((Double) s1.get(2)).doubleValue(), EPSILON);
        assertEquals(53.0 / 61.0, ((Double) s1.get(3)).doubleValue(), EPSILON);
        assertEquals(1.0, ((Double) s1.get(4)).doubleValue(), EPSILON);
    	List s2 = (List) data.get(1);
    	assertEquals(6, s2.size());
        assertEquals(12.0 / 61.0, ((Double) s2.get(0)).doubleValue(), EPSILON);
        assertEquals(39.0 / 61.0, ((Double) s2.get(1)).doubleValue(), EPSILON);
        assertEquals(57.0 / 61.0, ((Double) s2.get(2)).doubleValue(), EPSILON);
        assertEquals(45.0 / 61.0, ((Double) s2.get(3)).doubleValue(), EPSILON);
        assertNull(s2.get(4));
        assertEquals(51.0 / 61.0, ((Double) s2.get(5)).doubleValue(), EPSILON);
    }

    /**
     * A test to confirm that the parsing of data in the extended format is
     * working.
     */
    public void testParseExtendedData() {
    	String s = "e:.Z__Aa";
    	List data = DataUtilities.parseExtendedData(s);
    	assertEquals(1, data.size());
    	List s1 = (List) data.get(0);
    	assertEquals(3, s1.size());
        assertEquals(4057.0 / 4095.0, ((Double) s1.get(0)).doubleValue(),
        		EPSILON);
        assertNull(s1.get(1));
        assertEquals(26.0 / 4095.0, ((Double) s1.get(2)).doubleValue(),
        		EPSILON);
    }

    /**
     * Some checks for the parseTextData() method.
     */
    public void testParseTextData() {
    	String s = "t:10.0,58.0,95.0|30.0,8.0,63.0";
    	List data = DataUtilities.parseTextData(s, new float[] {0.0f, 100.0f});
    	assertEquals(2, data.size());
    	List s1 = (List) data.get(0);
    	assertEquals(3, s1.size());
    	assertEquals(0.10, ((Float) s1.get(0)).doubleValue(), EPSILON);
    	assertEquals(0.58, ((Float) s1.get(1)).doubleValue(), EPSILON);
    	assertEquals(0.95, ((Float) s1.get(2)).doubleValue(), EPSILON);
    	List s2 = (List) data.get(1);
    	assertEquals(3, s2.size());
    	assertEquals(0.30, ((Float) s2.get(0)).doubleValue(), EPSILON);
    	assertEquals(0.08, ((Float) s2.get(1)).doubleValue(), EPSILON);
    	assertEquals(0.63, ((Float) s2.get(2)).doubleValue(), EPSILON);
    }

    /**
     * The Google data spec says to specify a missing value as '-1', but
     * trial and error suggests that any negative value is treated as a
     * missing value...and any number greater than 100.0 is capped at
     * 100.
     */
    public void testParseTextData2() {
    	String s = "t:-1.0,50.0,100.0,1000.0,-1000.0|-1.0,50.0,-10";
    	List data = DataUtilities.parseTextData(s, new float[] {0.0f, 100.0f});
    	assertEquals(2, data.size());
    	
    	List s1 = (List) data.get(0);
    	assertEquals(5, s1.size());
    	assertNull(s1.get(0));
    	assertEquals(0.50, ((Float) s1.get(1)).doubleValue(), EPSILON);
    	assertEquals(1.00, ((Float) s1.get(2)).doubleValue(), EPSILON);
    	assertEquals(1.00, ((Float) s1.get(3)).doubleValue(), EPSILON);
    	assertNull(s1.get(4));
    	
    	List s2 = (List) data.get(1);
        assertEquals(3, s2.size());
        assertNull(s2.get(0));
        assertEquals(0.50, ((Float) s2.get(1)).doubleValue(), EPSILON);
        assertNull(s2.get(2));
    }
    
    /**
     * Google charts can also parse numbers in scientific notation
     */
    public void testParseTextDataScientific() {
        String s = "t:1.5E7,20000000.0,3.0E-7,-4.0E7";
        List data = DataUtilities.parseTextData(s, new float[] {0.0f, 1E8f});
        assertEquals(1, data.size());
        List s1 = (List) data.get(0);
        assertEquals(4, s1.size());
        assertEquals(0.15, ((Float) s1.get(0)).doubleValue(), EPSILON);
        assertEquals(0.20, ((Float) s1.get(1)).doubleValue(), EPSILON);
        assertEquals(0.00, ((Float) s1.get(2)).doubleValue(), EPSILON);
        assertNull(s1.get(3));
    }

    /**
     * Some checks for the parsePieDataset() method.
     */
    public void testParsePieDataset() {
    	PieDataset d = DataUtilities.parsePieDataset("s:Hello_World", "");
    	assertEquals(11, d.getItemCount());
    	assertEquals(7.0 / 61.0, d.getValue(0).doubleValue(), EPSILON);
    	assertEquals(30.0 / 61.0, d.getValue(1).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(2).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(3).doubleValue(), EPSILON);
    	assertEquals(40.0 / 61.0, d.getValue(4).doubleValue(), EPSILON);
    	assertNull(d.getValue(5));
    	assertEquals(22.0 / 61.0, d.getValue(6).doubleValue(), EPSILON);
    	assertEquals(40.0 / 61.0, d.getValue(7).doubleValue(), EPSILON);
    	assertEquals(43.0 / 61.0, d.getValue(8).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(9).doubleValue(), EPSILON);
    	assertEquals(29.0 / 61.0, d.getValue(10).doubleValue(), EPSILON);
    }

    /**
     * Some basic checks for the parseCategoryDataset() method.
     */
    public void testParseCategoryDataset() {
    	CategoryDataset d = DataUtilities.parseCategoryDataset("s:Hello_World",
    			"");
    	assertEquals(1, d.getRowCount());
    	assertEquals(11, d.getColumnCount());
    	assertEquals(7.0 / 61.0, d.getValue(0, 0).doubleValue(), EPSILON);
    	assertEquals(30.0 / 61.0, d.getValue(0, 1).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(0, 2).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(0, 3).doubleValue(), EPSILON);
    	assertEquals(40.0 / 61.0, d.getValue(0, 4).doubleValue(), EPSILON);
    	assertNull(d.getValue(0, 5));
    	assertEquals(22.0 / 61.0, d.getValue(0, 6).doubleValue(), EPSILON);
    	assertEquals(40.0 / 61.0, d.getValue(0, 7).doubleValue(), EPSILON);
    	assertEquals(43.0 / 61.0, d.getValue(0, 8).doubleValue(), EPSILON);
    	assertEquals(37.0 / 61.0, d.getValue(0, 9).doubleValue(), EPSILON);
    	assertEquals(29.0 / 61.0, d.getValue(0, 10).doubleValue(), EPSILON);
    }

    /**
     * Some basic checks for the parseXYDataset() method.
     */
    public void testParseXYDataset() {
    	XYDataset d = DataUtilities.parseXYDataset("s:Hello_World", "");
    	assertEquals(1, d.getSeriesCount());
    	assertEquals(11, d.getItemCount(0));
    	assertEquals(0.0, d.getXValue(0, 0), EPSILON);
    	assertEquals(0.1, d.getXValue(0, 1), EPSILON);
    	assertEquals(0.2, d.getXValue(0, 2), EPSILON);
    	assertEquals(0.3, d.getXValue(0, 3), EPSILON);
    	assertEquals(0.4, d.getXValue(0, 4), EPSILON);
    	assertEquals(0.5, d.getXValue(0, 5), EPSILON);
    	assertEquals(0.6, d.getXValue(0, 6), EPSILON);
    	assertEquals(0.7, d.getXValue(0, 7), EPSILON);
    	assertEquals(0.8, d.getXValue(0, 8), EPSILON);
    	assertEquals(0.9, d.getXValue(0, 9), EPSILON);
    	assertEquals(1.0, d.getXValue(0, 10), EPSILON);

    	assertEquals(7.0 / 61.0, d.getYValue(0, 0), EPSILON);
    	assertEquals(30.0 / 61.0, d.getYValue(0, 1), EPSILON);
    	assertEquals(37.0 / 61.0, d.getYValue(0, 2), EPSILON);
    	assertEquals(37.0 / 61.0, d.getYValue(0, 3), EPSILON);
    	assertEquals(40.0 / 61.0, d.getYValue(0, 4), EPSILON);
    	assertNull(d.getY(0, 5));
    	assertEquals(22.0 / 61.0, d.getYValue(0, 6), EPSILON);
    	assertEquals(40.0 / 61.0, d.getYValue(0, 7), EPSILON);
    	assertEquals(43.0 / 61.0, d.getYValue(0, 8), EPSILON);
    	assertEquals(37.0 / 61.0, d.getYValue(0, 9), EPSILON);
    	assertEquals(29.0 / 61.0, d.getYValue(0, 10), EPSILON);
    }

    /**
     * Some basic checks for the parseXYDataset2() method.
     */
    public void testParseXYDataset2() {
    	XYDataset d = DataUtilities.parseXYDataset2("s:Hello,Wor_d", "");
    	assertEquals(1, d.getSeriesCount());
    	assertEquals(5, d.getItemCount(0));

    	assertEquals(7.0 / 61.0, d.getXValue(0, 0), EPSILON);
    	assertEquals(30.0 / 61.0, d.getXValue(0, 1), EPSILON);
    	assertEquals(37.0 / 61.0, d.getXValue(0, 2), EPSILON);
    	assertEquals(37.0 / 61.0, d.getXValue(0, 3), EPSILON);
    	assertEquals(40.0 / 61.0, d.getXValue(0, 4), EPSILON);
    	assertEquals(22.0 / 61.0, d.getYValue(0, 0), EPSILON);
    	assertEquals(40.0 / 61.0, d.getYValue(0, 1), EPSILON);
    	assertEquals(43.0 / 61.0, d.getYValue(0, 2), EPSILON);
    	assertNull(d.getY(0, 3));
    	assertEquals(29.0 / 61.0, d.getYValue(0, 4), EPSILON);
    }

    private static final double EPSILON = 0.0000001;

}