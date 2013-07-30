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
 * ------------------
 * GCategoryAxis.java
 * ------------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 13-Dec-2007 : Version 1 (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.text.TextBlock;
import org.jfree.ui.RectangleEdge;

/**
 * A category axis with custom labelling.
 */
class GCategoryAxis extends CategoryAxis implements GLabelledAxis {

	/** Category labels. */
    List labels;

    /**
     * Creates a new instance.
     */
    public GCategoryAxis() {
        super();
        this.labels = null;
        setTickLabelPaint(Color.gray);
        setTickLabelFont(new Font("Dialog", Font.PLAIN, 11));
    }

    /**
     * Sets the tick labels for the axis.
     *
     * @param labels  the labels.
     */
    public void setTickLabels(List labels) {
        this.labels = labels;
    }

    /**
     * Creates a temporary list of ticks that can be used when drawing the axis.
     *
     * @param g2  the graphics device (used to get font measurements).
     * @param state  the axis state.
     * @param dataArea  the area inside the axes.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    public List refreshTicks(Graphics2D g2,
                             AxisState state,
                             Rectangle2D dataArea,
                             RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        // sanity check for data area...
        if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0) {
            return ticks;
        }

        CategoryPlot plot = (CategoryPlot) getPlot();
        List categories = null;
        if (this.labels == null) {
            categories = plot.getCategories();
        }
        else {
            categories = new java.util.ArrayList(this.labels);
            // handle a little quirk in the Google Chart API - for a horizontal
            // bar chart, the labels on the axis get applied in reverse order
            // relative to the data values
            if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                Collections.reverse(categories);
            }
        }
        double max = 0.0;

        if (categories != null) {
            CategoryLabelPosition position
                    = getCategoryLabelPositions().getLabelPosition(edge);
            float r = getMaximumCategoryLabelWidthRatio();
            if (r <= 0.0) {
                r = position.getWidthRatio();
            }

            float l = 0.0f;
            if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                l = (float) calculateCategorySize(categories.size(), dataArea,
                        edge);
            }
            else {
                if (RectangleEdge.isLeftOrRight(edge)) {
                    l = (float) dataArea.getWidth();
                }
                else {
                    l = (float) dataArea.getHeight();
                }
            }
            int categoryIndex = 0;
            Iterator iterator = categories.iterator();
            while (iterator.hasNext()) {
                Comparable category = (Comparable) iterator.next();
                TextBlock label = createLabel(category, l * r, edge, g2);
                if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                    max = Math.max(max, calculateTextBlockHeight(label,
                            position, g2));
                }
                else if (edge == RectangleEdge.LEFT
                        || edge == RectangleEdge.RIGHT) {
                    max = Math.max(max, calculateTextBlockWidth(label,
                            position, g2));
                }
                Tick tick = new CategoryTick(category, label,
                        position.getLabelAnchor(),
                        position.getRotationAnchor(), position.getAngle());
                ticks.add(tick);
                categoryIndex = categoryIndex + 1;
            }
        }
        state.setMax(max);
        return ticks;

    }

    /**
     * An empty method.
     *
     * @param values  the list of label positions.
     */
    public void setTickLabelPositions(List values) {
		// TODO Auto-generated method stub
	}

}
