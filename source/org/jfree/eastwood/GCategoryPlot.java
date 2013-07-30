/* ===============
 * Eastwood Charts
 * ===============
 *
 * (C) Copyright 2007-2009, by Object Refinery Limited.
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
 * GCategoryPlot.java
 * ------------------
 * (C) Copyright 2007-2009, by Object Refinery Limited.
 *
 * Original Author:  Niklas Therning;
 * Contributors:     David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 30-Jun-2008 : Version 1 (NT);
 * 13-May-2009 : Added support for background fill (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;

/**
 * A custom plot class (adds support for specifying the step size to use for
 * grid lines). NOTE: X axis step size has not been fully implemented. We will
 * always place the vertical grid lines in the middle of each bar if
 * <code>xAxisStepSize &gt; 0</code>. If <code>xAxisStepSize == 0</code>
 * vertical grid lines will be disabled.
 */
class GCategoryPlot extends CategoryPlot {

    /** The angle in radians for the background fill gradient. */
    private double angle;

    /** The factor for the start of the background gradient. */
    private double f0;

    /** The factor for the end of the background gradient. */
    private double f1;

    /** The step size to use when drawing the grid lines for the x axis. */
    private double xAxisStepSize;

    /** The step size to use when drawing the grid lines for the y axis. */
    private double yAxisStepSize;

    /**
     * Default constructor.
     */
    public GCategoryPlot() {
        this.angle = 0.0;
        this.f0 = 0.0;
        this.f1 = 1.0;
        this.xAxisStepSize = 0.0;
        this.yAxisStepSize = 0.0;
    }

    /**
     * Sets the angle (in radians).
     *
     * @param angle  the angle.
     */
    public void setAngle(double angle) {
        this.angle = angle;
    }

    /**
     * Sets the factor for the beginning of the background gradient.
     *
     * @param f0  the factor.
     */
    public void setF0(double f0) {
        this.f0 = f0;
    }

    /**
     * Sets the factor for the end of the background gradient.
     *
     * @param f1  the factor.
     */
    public void setF1(double f1) {
        this.f1 = f1;
    }

    /**
     * Sets the step size to use when drawing the grid lines for the x axis.
     *
     * @param axisStepSize the step size. A value less than or equal to 0
     *        disables x axis grid lines.
     */
    public void setXAxisStepSize(double axisStepSize) {
        this.xAxisStepSize = axisStepSize;
    }

    /**
     * Sets the step size to use when drawing the grid lines for the y axis.
     *
     * @param axisStepSize the step size. A value less than or equal to 0
     *        disables y axis grid lines.
     */
    public void setYAxisStepSize(double axisStepSize) {
        this.yAxisStepSize = axisStepSize;
    }

    /**
     * Draws the background for the plot.
     *
     * @param g2  the graphics device.
     * @param area  the area.
     */
    public void drawBackground(Graphics2D g2, Rectangle2D area) {
        Paint p = getBackgroundPaint();
        if (p == null) {
            return;
        }
        if (p instanceof GradientPaint) {
            GradientPaint gp = (GradientPaint) p;
            double x0 = area.getMinX();
            double x1 = x0;
            double y0 = area.getMaxY();
            double y1 = y0;
            double w = area.getWidth();
            double h = area.getHeight();
            if (this.angle <= 0.0) {
                // horizontal gradient
                x0 = x0 + this.f0 * w;
                x1 = x1 + this.f1 * w;
            }
            else if (this.angle >= Math.PI / 2.0) {
                // vertical gradient
                y0 = y0 - this.f0 * h;
                y1 = y1 - this.f1 * h;
            }
            else {
                // general case
                double r = Math.sqrt(w * w + h * h);
                x0 = x0 + Math.cos(this.angle) * r * this.f0;
                y0 = y0 - Math.sin(this.angle) * r * this.f0;
                x1 = x1 + Math.cos(this.angle) * r * this.f1;
                y1 = y1 - Math.sin(this.angle) * r * this.f1;
            }

            // create the new gradient paint
            Point2D p0 = new Point2D.Double(x0, y0);
            Point2D p1 = new Point2D.Double(x1, y1);
            p = new GradientPaint(p0, gp.getColor1(), p1,
                    gp.getColor2());
        }
        g2.setPaint(p);
        g2.fill(area);
    }

    /**
     * Draws the gridlines for the plot.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     *
     * @see #drawRangeGridlines(Graphics2D, Rectangle2D, List)
     */
    protected void drawDomainGridlines(Graphics2D g2, Rectangle2D dataArea) {
        // TODO: Implement proper support for x axis step size?
        if (this.xAxisStepSize > 0) {
            super.drawDomainGridlines(g2, dataArea);
        }
    }

    /**
     * Draws the gridlines for the plot.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param ticks  the ticks.
     *
     * @see #drawDomainGridlines(Graphics2D, Rectangle2D)
     */
    protected void drawRangeGridlines(Graphics2D g2, Rectangle2D dataArea,
                                      List ticks) {
        // draw the range grid lines, if any...
        if (isRangeGridlinesVisible()) {
            Stroke gridStroke = getRangeGridlineStroke();
            Paint gridPaint = getRangeGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                ValueAxis axis = getRangeAxis();
                if (axis != null) {
                    double lower = axis.getRange().getLowerBound();
                    double upper = axis.getRange().getUpperBound();
                    double y = lower;
                    while (y <= upper) {
                        Paint paint = gridPaint;
                        if ((y == lower || y == upper)
                        		&& gridPaint instanceof Color) {
                            Color c = (Color) gridPaint;
                            paint = new Color(c.getRed(), c.getGreen(),
                                              c.getBlue(), c.getAlpha() / 3);
                        }
                        try {
                            setRangeGridlinePaint(paint);
                            getRenderer().drawRangeGridline(g2, this,
                            		getRangeAxis(), dataArea, y);
                        }
                        finally {
                            setRangeGridlinePaint(gridPaint);
                        }
                        y += this.yAxisStepSize;
                    }
                }
            }
        }
    }
}
