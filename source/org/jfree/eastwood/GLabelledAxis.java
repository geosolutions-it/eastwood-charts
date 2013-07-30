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
 * GLabelledAxis.java
 * ------------------
 * (C) Copyright 2007, 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 13-Dec-2007 : Version 1 (DG);
 * 15-Jul-2008 : Added setTickLabelPositions() method (DG);
 *
 */

package org.jfree.eastwood;

import java.util.List;

/**
 * An interface for axes that accept custom labels.
 */
interface GLabelledAxis {

    /**
     * Sets the tick labels for the axis.
     *
     * @param labels  the labels.
     */
    public void setTickLabels(List labels);

    /**
     * Sets the tick label positions for the axis.
     *
     * @param values  the position values.
     */
    public void setTickLabelPositions(List values);

}
