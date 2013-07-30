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
 * ----------------
 * ChartApplet.java
 * ----------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     -;
 *
 * Changes
 * -------
 * 09-Jul-2008 : Version 1 (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.Font;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.swing.JApplet;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * An applet that displays charts that can be configured using the
 * Google Chart API.
 */
public class ChartApplet extends JApplet {

	/** The chart panel in which the chart is displayed. */
	private ChartPanel chartPanel;

	/**
	 * Creates a new applet instance.
	 */
	public ChartApplet() {
		this.chartPanel = new ChartPanel(null);
		this.chartPanel.setPopupMenu(null);
		getContentPane().add(this.chartPanel);
	}

	/**
	 * Starts the applet.
	 */
	public void start() {
	    String chartSpec = getParameter("chart");
	    try {
    	    Map params = Parameters.parseQueryString(chartSpec);
    	    JFreeChart chart = ChartEngine.buildChart(params,
    	    		new Font("Dialog", Font.PLAIN, 12));
    	    this.chartPanel.setChart(chart);
	    }
	    catch (UnsupportedEncodingException e) {
	        e.printStackTrace();
	    }
	}

}
