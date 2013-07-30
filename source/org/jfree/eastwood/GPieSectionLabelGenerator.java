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
 * ------------------------------
 * GPieSectionLabelGenerator.java
 * ------------------------------
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

import java.text.AttributedString;
import java.util.List;

import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.data.general.PieDataset;

/**
 * A pie section label generator used by the {@link ChartServlet} servlet that
 * emulates the Google Chart API.
 */
class GPieSectionLabelGenerator implements PieSectionLabelGenerator {

    /** Storage for the section labels. */
    private List labels;

    /**
     * Creates a new generator.
     *
     * @param labels  the labels (<code>null</code> not permitted).
     */
    public GPieSectionLabelGenerator(List labels) {
        if (labels == null) {
            throw new IllegalArgumentException("Null 'labels' argument.");
        }
        this.labels = labels;
    }

    /**
     * Returns the section label as supplied in the constructor.
     *
     * @param dataset  the dataset.
     * @param key  the section key.
     *
     * @return The section label.
     */
    public String generateSectionLabel(PieDataset dataset, Comparable key) {
        int index = dataset.getIndex(key);
        if (index < this.labels.size()) {
            return this.labels.get(index).toString();
        }
        else {
            return null;
        }
    }

    /**
     * This method doesn't get used.
     *
     * @param dataset  the dataset.
     * @param key  the section key.
     *
     * @return The section label.
     */
    public AttributedString generateAttributedSectionLabel(PieDataset dataset,
            Comparable key) {
        return new AttributedString(generateSectionLabel(dataset, key));
    }
}
