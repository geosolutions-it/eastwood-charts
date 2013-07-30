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
 * -----------------
 * GValueAxis3D.java
 * -----------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 16-Jul-2008 : Version 1, based on GValueAxis.java (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * An axis implementation designed exclusively for use with Eastwood.
 */
class GValueAxis3D extends NumberAxis3D implements GLabelledAxis {

	/**
	 * A hidden axis that is used for generating axis labels if no labels
	 * are explicitly set.
	 */
	private NumberAxis3D axisForAutoLabels;

	/** The start value for the range for the labelling axis. */
	private double labelAxisStart = 0.0;

	/** The end value for the range for the labelling axis. */
	private double labelAxisEnd = 100.0;

	/** The tick labels (this list may be empty). */
    private List tickLabels;

    /** The tick label positions (as Number objects). */
    private List tickLabelPositions;

    /**
     * Creates a new axis.
     */
    public GValueAxis3D() {
        super();
        this.axisForAutoLabels = new NumberAxis3D(null);
        this.axisForAutoLabels.setRange(0.0, 100.0);
        this.axisForAutoLabels.setStandardTickUnits(
        		NumberAxis.createIntegerTickUnits());
        this.labelAxisStart = 0.0;
        this.labelAxisEnd = 100.0;
        this.tickLabels = new java.util.ArrayList();
        this.tickLabelPositions = new java.util.ArrayList();
        setLowerMargin(0.0);
        setUpperMargin(0.0);
        // the data is normalised into the range 0.0 to 1.0, so the real axis
        // has the same range...
        setRange(0.0, 1.0);
        setTickLabelPaint(Color.gray);
        setTickLabelFont(new Font("Dialog", Font.PLAIN, 11));
    }

    /**
     * Returns the start value for the label axis.  This value corresponds to
     * 0.0 on the real axis (or 1.0 if the labels are inverted).
     *
     * @return The start value.
     */
    public double getLabelAxisStart() {
    	return this.labelAxisStart;
    }

    /**
     * Sets the start value for the label axis.
     *
     * @param start  the start value.
     */
    public void setLabelAxisStart(double start) {
    	this.labelAxisStart = start;
    }

    /**
     * Returns the end value for the label axis.  This value corresponds to
     * 1.0 on the real axis (or 0.0 if the labels are inverted).
     *
     * @return The end value.
     */
    public double getLabelAxisEnd() {
    	return this.labelAxisEnd;
    }

    /**
     * Sets the end value for the label axis.
     *
     * @param end  the end value.
     */
    public void setLabelAxisEnd(double end) {
    	this.labelAxisEnd = end;
    }

    /**
     * Sets the labels to be displayed along the axis.
     *
     * @param labels  the labels (<code>null</code> not permitted).
     */
    public void setTickLabels(List labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Null 'labels' argument.");
        }
        this.tickLabels = labels;
        setAutoTickUnitSelection(false);
    }

    /**
     * Sets the tick values.
     *
     * @param values  the tick values (<code>null</code> not permitted.
     */
    public void setTickLabelPositions(List values) {
        if (values == null) {
            throw new IllegalArgumentException("Null 'values' argument.");
        }
        this.tickLabelPositions = values;
        setAutoTickUnitSelection(false);
    }

    /**
     * Creates and returns a list of ticks to display for the
     * axis.
     *
     * @param g2  the graphics target.
     * @param state
     * @param dataArea
     * @param edge
     *
     * @return The list.
     */
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {

    	if (this.tickLabels.isEmpty()) {

    		// here we auto-generate the axis labels using a non-visible axis
    		// first we must align the axis range
    		boolean inverted = (this.labelAxisStart > this.labelAxisEnd);
    		double range = this.labelAxisEnd - this.labelAxisStart;
    		double v0 = this.labelAxisStart + getLowerBound() * range;
    		double v1 = this.labelAxisStart + getUpperBound() * range;
    		this.axisForAutoLabels.setRange(Math.min(v0, v1), Math.max(v0, v1));
    		this.axisForAutoLabels.setInverted(inverted);

    		List ticks = this.axisForAutoLabels.refreshTicks(g2, state,
    				dataArea, edge);
    		// now we must 'normalise' the tick positions into the range
    		// 0.0 to 1.0
    		List normalisedTicks = new java.util.ArrayList();
    		double min = Math.min(this.labelAxisStart, this.labelAxisEnd);
    		double max = Math.max(this.labelAxisStart, this.labelAxisEnd);
    		Iterator iterator = ticks.iterator();
    		while (iterator.hasNext()) {
    			NumberTick tick = (NumberTick) iterator.next();
    			double v = tick.getValue();
    			double vv = (v - min) / (max - min);
    			if (this.axisForAutoLabels.isInverted()) {
    				vv = 1.0 - vv;
    			}
     			normalisedTicks.add(new NumberTick(new Double(vv),
       					tick.getText(), tick.getTextAnchor(),
   	    				tick.getRotationAnchor(), tick.getAngle()));
    		}
    		return normalisedTicks;
    	}

    	List result = new java.util.ArrayList();
        int labelCount = this.tickLabels.size();
        int positionCount = this.tickLabelPositions.size();
        int tickCount = Math.max(labelCount, positionCount);

        // work out the label anchor points according to which side of the
        // chart the axis lies on...
        TextAnchor anchor = null;
        TextAnchor rotationAnchor = TextAnchor.CENTER;
        if (edge == RectangleEdge.LEFT) {
            anchor = TextAnchor.CENTER_RIGHT;
        }
        else if (edge == RectangleEdge.BOTTOM) {
            anchor = TextAnchor.TOP_CENTER;
        }
        else if (edge == RectangleEdge.TOP) {
            anchor = TextAnchor.BOTTOM_CENTER;
        }
        else if (edge == RectangleEdge.RIGHT) {
            anchor = TextAnchor.CENTER_LEFT;
        }

        for (int i = 0; i < tickCount; i++) {
            String tickLabel = null;
            if (i < labelCount) {
                tickLabel = this.tickLabels.get(i).toString();
            }
            else {
                tickLabel = String.valueOf(this.tickLabelPositions.get(i));
            }

            // is there a value specified
            double tickValue = Double.NaN;
            if (i < positionCount) {
                Number tv = (Number) this.tickLabelPositions.get(i);
                if (tv != null) {
                    tickValue = (tv.doubleValue() - this.labelAxisStart)
                            / (this.labelAxisEnd - this.labelAxisStart);
                }
            }
            if (Double.isNaN(tickValue)) {
                tickValue = (i * (1.0 / (Math.max(labelCount - 1, 1))));
            }
            Tick tick = new NumberTick(new Double(tickValue), tickLabel,
                    anchor, rotationAnchor, 0.0);

            result.add(tick);
        }
        return result;
    }

}
