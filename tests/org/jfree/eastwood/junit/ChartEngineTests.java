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
 * ---------------------
 * ChartEngineTests.java
 * ---------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 27-Jun-2008 : Version 1 (DG);
 *
 */

package org.jfree.eastwood.junit;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.eastwood.ChartEngine;
import org.jfree.eastwood.Parameters;

/**
 * Tests for the {@link ChartEngine} class.
 */
public class ChartEngineTests extends TestCase {
	
	private static final double EPSILON = 0.0000001;

    /**
     * Returns the tests as a test suite.
     *
     * @return The test suite.
     */
    public static Test suite() {
        return new TestSuite(ChartEngineTests.class);
    }

    /**
     * Constructs a new set of tests.
     *
     * @param name  the name of the tests.
     */
    public ChartEngineTests(String name) {
        super(name);
    }

    /**
     * A test for bug report 2001830.
     *
     * @throws UnsupportedEncodingException
     */
    public void test2001830() throws UnsupportedEncodingException {
    	String paramStr = "cht=bhs&chco=ff0000|00ff00|0000ff,&chs=200x125" +
    	        "&chd=s:elo&chxt=x,y&chxl=1:|Dec|Nov|Oct|0:||20K||60K||100K|";
    	Map params = Parameters.parseQueryString(paramStr);
    	/* JFreeChart chart = */ ChartEngine.buildChart(params);
    }
    
    /**
     * Test background color can be expressed as RRGGBBAA 
     */
    public void testAlphaColor() throws Exception {
        // see report 2780330
        Map params = Parameters.parseQueryString("cht=p3&chd=t:60,40&chs=250x100&&chf=bg,s,FFFFFF00");
        JFreeChart chart = ChartEngine.buildChart(params);
        assertEquals(new Color(255, 255, 255, 0), chart.getBackgroundPaint());
    }
    
    /**
     * Make sure the pie background is transparent by default
     * @throws Exception
     */
    public void testPieBackground() throws Exception {
        Map params = Parameters.parseQueryString("cht=p&chd=t:60,40&chs=250x100");
        JFreeChart chart = ChartEngine.buildChart(params);
        assertNull(chart.getPlot().getBackgroundPaint());
    }
 
    /**
     * Make sure the pie 3d background is transparent by default
     * @throws Exception
     */
    public void testPie3DBackground() throws Exception {
        Map params = Parameters.parseQueryString("cht=p3&chd=t:60,40&chs=250x100");
        JFreeChart chart = ChartEngine.buildChart(params);
        assertNull(chart.getPlot().getBackgroundPaint());
    }
    
    /**
     * Check the chp parameter is properly parsed
     * @throws Exception
     */
    public void testPieRotation() throws Exception {
    	double rotation = Math.PI / 4;
    	Map params = Parameters.parseQueryString("cht=p&chd=t:60,40&chs=250x100&chp=" + rotation);
    	JFreeChart chart = ChartEngine.buildChart(params);
    	PiePlot plot = (PiePlot) chart.getPlot();
    	assertEquals(-45.0, plot.getStartAngle(), EPSILON);
    }

    /**
     * Checks the minimal chxs string is properly parsed and
     * assigned to the respective axis
     */
    public void testAxisStyleBasic() throws Exception {
    	String base = "cht=lc&chs=200x125&chd=t:40,60,70&";
    	Map params = Parameters.parseQueryString(base + "chxt=x,y&chxs=0,00FF00|1,FF0000");
    	JFreeChart chart = ChartEngine.buildChart(params);
    	
    	XYPlot plot = (XYPlot) chart.getPlot();
    	assertEquals(Color.GREEN, plot.getDomainAxis().getTickLabelPaint());
    	assertEquals(Color.RED, plot.getRangeAxis().getTickLabelPaint());
    }
    
    /**
     * Checks the full chxs string is properly parsed
     */
    public void testAxisStyleFull() throws Exception {
    	String base = "cht=lc&chs=200x125&chd=t:40,60,70&";
    	Map params = Parameters.parseQueryString(base + "chxt=x&chxs=0,00FF00,15,0,lt,00FF00");
    	JFreeChart chart = ChartEngine.buildChart(params);
    	
    	XYPlot plot = (XYPlot) chart.getPlot();
    	Axis axis = plot.getDomainAxis();
    	assertEquals(Color.GREEN, axis.getTickLabelPaint());
    	assertEquals(15, axis.getTickLabelFont().getSize());
    	assertTrue(axis.isVisible());
    	assertTrue(axis.isTickMarksVisible());
    	assertTrue(axis.isTickLabelsVisible());
    	assertTrue(axis.isAxisLineVisible());
    }
    
    /**
     * Checks the undocumented transparent axis feature of Google Charts is supported
     * (drawing control = '_')
     */
    public void testTransparentAxis() throws Exception {
    	String base = "cht=lc&chs=200x125&chd=t:40,60,70&";
    	Map params = Parameters.parseQueryString(base + "chxt=x&chxs=0,000000,0,0,_");
    	JFreeChart chart = ChartEngine.buildChart(params);
    	
    	XYPlot plot = (XYPlot) chart.getPlot();
    	Axis axis = plot.getDomainAxis();
    	assertFalse(axis.isVisible());
    }

}