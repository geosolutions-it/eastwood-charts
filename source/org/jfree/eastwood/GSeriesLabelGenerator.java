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
 * --------------------------
 * GSeriesLabelGenerator.java
 * --------------------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     Niklas Therning;
 *
 * Changes
 * -------
 * 13-Dec-2007 : Version 1 (DG);
 * 27-Jun-2008 : Implemented CategorySeriesLabelGenerator and renamed from
 *               GXYSeriesLabelGenerator to GSeriesLabelGenerator - see patch
 *               2002341 by Niklas Therning (DG);
 *
 */

package org.jfree.eastwood;

import java.util.List;

import org.jfree.chart.labels.CategorySeriesLabelGenerator;
import org.jfree.chart.labels.XYSeriesLabelGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A series label generator that ignores the series keys and just returns
 * the strings specified in the constructor.  This is used by the
 * {@link ChartServlet} servlet to emulate the Google Chart API.
 */
public class GSeriesLabelGenerator implements XYSeriesLabelGenerator,
        CategorySeriesLabelGenerator {

    /** Storage for the labels. */
    private List labels;

    /**
     * Creates a new label generator.
     *
     * @param labels  a list of labels (<code>null</code> not permitted).
     */
    public GSeriesLabelGenerator(List labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Null 'labels' argument.");
        }
        this.labels = labels;
    }

    /**
     * Returns a string that will be used as the label for the specified
     * series.
     *
     * @param dataset  the dataset (ignored here).
     * @param series  the series index.
     *
     * @return The label string.
     */
    public String generateLabel(XYDataset dataset, int series) {
        Object item = this.labels.get(series);
        if (item != null) {
            return item.toString();
        }
        else {
            return "";
        }
    }

    /**
     * Returns a string that will be used as the label for the specified
     * series.
     *
     * @param dataset  the dataset (ignored here).
     * @param series  the series index.
     *
     * @return The label string.
     */
	public String generateLabel(CategoryDataset dataset, int series) {
        Object item = this.labels.get(series);
        if (item != null) {
            return item.toString();
        }
        else {
            return "";
        }
	}

}
