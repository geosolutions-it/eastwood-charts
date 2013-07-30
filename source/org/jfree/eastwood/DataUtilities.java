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
 * DataUtilities.java
 * ------------------
 * (C) Copyright 2008, 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     Niklas Therning (patch 2001599);
 *                   Andrea Aime (bug 2780611);
 *
 * Changes
 * -------
 * 14-Jul-2008 : Version 1, methods factored out of ChartEngine.java (DG);
 * 14-Jul-2008 : Added data scaling support - see patch 2001599 by
 *               Niklas Therning (DG);
 * 24-Apr-2009 : Fixed bug 2780611, failing for data in scientific
 *               notation (AA);
 *
 */

package org.jfree.eastwood;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.List;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A utility class for reading data that uses the Google chart encoding.
 */
public class DataUtilities {

    /**
     * Parses a string that uses the "simple" data encoding from the Google
     * Chart API.  The method returns a list of lists, with the sublists
     * each containing the data values for one series, the values being
     * represented by Double objects in the range 0.0 to 1.0.  Missing values
     * (which are encoded as '_') are returned as null entries in the list.
     *
     * @param s  the data string (<code>null</code> not permitted).
     *
     * @return A list of sublists.
     */
    public static List parseSimpleData(String s) {
    	if (s == null) {
    		throw new IllegalArgumentException("Null 's' argument.");
    	}
        List dataset = new java.util.ArrayList();
        StringCharacterIterator iterator = new StringCharacterIterator(s, 2);
        List series = new java.util.ArrayList();
        while (iterator.current() != CharacterIterator.DONE) {
            char c = iterator.current();
            if (c == ',') {
                dataset.add(series);
                series = new java.util.ArrayList();
            }
            else {
                int v = googleValue(c);
                if (v < Integer.MAX_VALUE) {
                    series.add(new Double(v / 61.0));
                }
                else {
                    series.add(null);
                }
            }
            iterator.next();
        }
        dataset.add(series);
        return dataset;
    }

    /**
     * Parses a string that uses the "extended" data encoding from the Google
     * Chart API.  The method returns a list of lists, with the sublists
     * each containing the data values for one series, the values being
     * represented by Double objects in the range 0.0 to 1.0.  Missing values
     * (which are encoded as '__') are returned as null entries in the list.
     *
     * @param s  the data string (<code>null</code> not permitted).
     *
     * @return A list of sublists.
     */
    public static List parseExtendedData(String s) {
    	if (s == null) {
    		throw new IllegalArgumentException("Null 's' argument.");
    	}
        List dataset = new java.util.ArrayList();
        StringCharacterIterator iterator = new StringCharacterIterator(s, 2);
        List series = new java.util.ArrayList();
        while (iterator.current() != CharacterIterator.DONE) {
            char c1 = iterator.current();
            if (c1 == ',') {
                dataset.add(series);
                series = new java.util.ArrayList();
            }
            else {
                char c2 = iterator.next();
                int c1v = googleValueExt(c1);
                int c2v = googleValueExt(c2);
                if (c1v < Integer.MAX_VALUE && c2v < Integer.MAX_VALUE) {
                    series.add(new Double((c1v * 64.0 + c2v) / 4095.0));
                }
                else {
                    series.add(null);
                }
            }
            iterator.next();
        }
        dataset.add(series);
        return dataset;
    }

    /**
     * Parses a string with text data encoding.  The method returns a list of
     * lists, with the sublists each containing the data values for one series,
     * the values being represented by Float objects in the range 0.0 to 1.0.
     * Missing values (which are encoded as any values outside the range 0.0 to
     * 100.0) are returned as null entries in the list.
     *
     * @param dataStr  the data string.
     * @param scaling  the scaling parameters.
     *
     * @return A list containing a sublist of Float objects for each series.
     */
    public static List parseTextData(String dataStr, float[] scaling) {
        List dataset = new java.util.ArrayList();
        StringCharacterIterator iterator = new StringCharacterIterator(dataStr,
                2);
        List series = new java.util.ArrayList();
        int baseIndex = 2;
        float min = getScalingMin(scaling, 0);
        float max = getScalingMax(scaling, 0);
        while (iterator.current() != CharacterIterator.DONE) {
            char c = iterator.current();
            if (c >= '0' && c <= '9' || c == '-' || c == '.' || c == 'E') {
                iterator.next();
            }
            else if (c == ',') {
                series.add(parseSingleNumber(dataStr.substring(baseIndex, iterator.getIndex()), min, max));
                baseIndex = iterator.getIndex() + 1;
                iterator.next();
            }
            else if (c == '|') {
                series.add(parseSingleNumber(dataStr.substring(baseIndex, iterator.getIndex()), min, max));
                baseIndex = iterator.getIndex() + 1;
                dataset.add(series);
                series = new java.util.ArrayList();
                iterator.next();
            	min = getScalingMin(scaling, dataset.size());
            	max = getScalingMax(scaling, dataset.size());
            }
            else {
                throw new IllegalArgumentException(
                        "'data' contains bad characters.");
            }
        }
        series.add(parseSingleNumber(dataStr.substring(baseIndex, iterator.getIndex()), min, max));
        dataset.add(series);
        return dataset;
    }

    /**
     * Parses the number provided into a float, normalizes it as a percentage 
     * between min and max, and return null if the numer is negative  
     * @param number The number to be parsed
     * @param min the scaling minimum
     * @param max the scaling maximum
     * 
     * @return the parsed and scaled number, or null if the number is negative
     */
    private static Float parseSingleNumber(String number, float min, float max) {
        float val = (Float.parseFloat(number) - min) / (max - min);
        if (val >= 0.0f) {
            return new Float(Math.min(val, 1.0f));
        } else {
        	return null;
        }
    }

    /**
     * Returns the value of the specified character when using the "simple"
     * encoding from the Google Chart API.
     *
     * @param c  the character.
     *
     * @return The value.
     */
    private static int googleValue(char c) {
        if (c == '_') {
            return Integer.MAX_VALUE;
        }
        else if (c >= 'a' && c <= 'z') {
            return c - 97 + 26;
        }
        else if (c >= 'A' && c <= 'Z') {
            return c - 65;
        }
        else if (c >= '0' && c <= '9') {
            return c - 48 + 52;
        }
        else {
            throw new IllegalArgumentException("Invalid character '"
                    + c + "'.");
        }
    }

    /**
     * Returns the value of the specified character when using the "extended"
     * encoding from the Google Chart API.
     *
     * @param c  the character.
     *
     * @return The value.
     */
    private static int googleValueExt(char c) {
        if (c == '_') {
            return Integer.MAX_VALUE;
        }
        else if (c >= 'a' && c <= 'z') {
            return c - 97 + 26;
        }
        else if (c >= 'A' && c <= 'Z') {
            return c - 65;
        }
        else if (c >= '0' && c <= '9') {
            return c - 48 + 52;
        }
        else if (c == '-') {
            return 62;
        }
        else if (c == '.') {
            return 63;
        }
        else {
            throw new IllegalArgumentException("Invalid character '"
                    + c + "'.");
        }
    }

    /**
     * Parses a data scaling string. The returned array will always have a size
     * multiple of 2. If the passed in string is <code>null</code> or empty this
     * method returns {0, 100}. The values at odd positions are minimum values
     * while the values at even positions are the max values.
     *
     * @param scalingStr the scaling string.
     * @return the array of scaling values.
     */
    public static float[] parseDataScaling(String scalingStr) {
    	if (scalingStr == null) {
    		return new float[] {0f, 100.0f};
    	}
    	String[] parts = scalingStr.split(",");
    	if (parts.length == 0) {
    		return new float[] {0f, 100.0f};
    	}
    	// Make result array length at least as long as parts and multiple of 2
    	float[] result = new float[((parts.length + 1) >> 1) << 1];
    	for (int i = 0; i < parts.length; i++) {
    		if ("".equals(parts[i])) {
    			result[i] = 0f;
    		}
    		else {
    			result[i] = Float.parseFloat(parts[i]);
    		}
    	}
    	if (result.length > parts.length) {
    		result[result.length - 1] = 100.0f;
    	}
		return result;
	}

    private static float getScaling(float[] scaling, int datasetIndex,
    		int offset) {
    	int i = datasetIndex * 2;
    	if (i < scaling.length) {
    		return scaling[i + offset];
    	}
    	return scaling[scaling.length - 2 + offset];
    }

    private static float getScalingMin(float[] scaling, int datasetIndex) {
    	return getScaling(scaling, datasetIndex, 0);
    }

    private static float getScalingMax(float[] scaling, int datasetIndex) {
    	return getScaling(scaling, datasetIndex, 1);
    }

    /**
     * Parses a dataset from the incoming string (which may be encoded using
     * the 'simple', 'extended' or 'text' encodings from the Google Chart API).
     *
     * @param dataStr  the data string (<code>null</code> not permitted).
     * @param scalingStr  the scaling.
     *
     * @return A pie dataset.
     */
    public static PieDataset parsePieDataset(String dataStr,
    		String scalingStr) {
        if (dataStr == null) {
            throw new IllegalArgumentException("Null 'dataStr' argument.");
        }
        DefaultPieDataset result = new DefaultPieDataset();
        List dataList = null;
        if (dataStr.startsWith("s:")) { // simple encoding
            dataList = parseSimpleData(dataStr);
        }
        else if (dataStr.startsWith("t:")) {
        	float[] scaling = parseDataScaling(scalingStr);
            dataList = parseTextData(dataStr, scaling);
        }
        else if (dataStr.startsWith("e:")) {
            dataList = parseExtendedData(dataStr);
        }
        else {
            throw new RuntimeException("Unrecognised data encoding: "
                    + dataStr.substring(0, 2));
        }

        if (dataList.size() > 0) {
            List series = (List) dataList.get(0);
            int s = 1;
            Iterator iterator = series.iterator();
            while (iterator.hasNext()) {
                Number i = (Number) iterator.next();
                result.setValue("S" + s, i);
                s++;
            }
        }
        else {
            throw new RuntimeException("Empty data list.");
        }
        return result;
    }

    /**
     * Parses a category dataset for use in bar charts.
     *
     * @param dataStr  the data string (<code>null</code> not permitted).
     * @param scalingStr  the scaling string.
     *
     * @return A category dataset.
     */
    public static DefaultCategoryDataset parseCategoryDataset(String dataStr,
    		String scalingStr) {
        if (dataStr == null) {
            throw new IllegalArgumentException("Null 'dataStr' argument.");
        }
        List dataList = null;
        if (dataStr.startsWith("s:")) { // simple encoding
            dataList = DataUtilities.parseSimpleData(dataStr);
        }
        else if (dataStr.startsWith("t:")) {
        	float[] scaling = parseDataScaling(scalingStr);
            dataList = DataUtilities.parseTextData(dataStr, scaling);
        }
        else if (dataStr.startsWith("e:")) {
            dataList = DataUtilities.parseExtendedData(dataStr);
        }
        else {
            throw new RuntimeException("Unrecognised data encoding: "
                    + dataStr.substring(0, 2));
        }

        DefaultCategoryDataset result = new DefaultCategoryDataset();
        for (int i = 0; i < dataList.size(); i++) {
            List values = (List) dataList.get(i);
            for (int j = 0; j < values.size(); j++) {
                Number n = (Number) values.get(j);
                if (n != null) {
                    double y = n.doubleValue();
                    result.setValue(y, String.valueOf(i), String.valueOf(j));
                }
                else {
                    result.setValue(n, String.valueOf(i), String.valueOf(j));
                }
            }
        }

        return result;
    }

    /**
     * Parses a dataset from the incoming string.  The data encoding only
     * specifies the y-values.
     *
     * @param dataStr  the data string (<code>null</code> not permitted).
     * @param scalingStr  the scaling string.
     *
     * @return A dataset.
     */
    public static XYDataset parseXYDataset(String dataStr, String scalingStr) {

        if (dataStr == null) {
            throw new IllegalArgumentException("Null 'dataStr' argument.");
        }
        List dataList;
        if (dataStr.startsWith("s:")) { // simple encoding
            dataList = DataUtilities.parseSimpleData(dataStr);
        }
        else if (dataStr.startsWith("t:")) {
        	float[] scaling = parseDataScaling(scalingStr);
            dataList = DataUtilities.parseTextData(dataStr, scaling);
        }
        else if (dataStr.startsWith("e:")) {
            dataList = DataUtilities.parseExtendedData(dataStr);
        }
        else {
            throw new RuntimeException("Unrecognised data encoding: "
                    + dataStr.substring(0, 2));
        }
        XYSeriesCollection result = new XYSeriesCollection();
        for (int s = 0; s < dataList.size(); s++) {
            List series = (List) dataList.get(s);
            XYSeries ss = new XYSeries("Series " + (s + 1));
            int itemCount = series.size();
            double increment = 1.0 / (Math.max(itemCount - 1, 1));
            for (int i = 0; i < itemCount; i++) {
                Number n = (Number) series.get(i);
                if (n != null) {
                    double y = n.doubleValue();
                    ss.add(i * increment, y);
                }
                else {
                    ss.add(i * increment, null);
                }
            }
            result.addSeries(ss);
        }
        return result;
    }

    /**
     * Parses a dataset from the incoming string.  The data encoding specifies
     * both the x-values and the y-values, in consecutive series.
     *
     * @param dataStr  the data string (<code>null</code> not permitted).
     * @param scalingStr  the scaling string.
     *
     * @return A dataset.
     */
    public static XYDataset parseXYDataset2(String dataStr, String scalingStr) {

        if (dataStr == null) {
            throw new IllegalArgumentException("Null 'dataStr' argument.");
        }
        List dataList;
        if (dataStr.startsWith("s:")) { // simple encoding
            dataList = DataUtilities.parseSimpleData(dataStr);
        }
        else if (dataStr.startsWith("t:")) {
        	float[] scaling = parseDataScaling(scalingStr);
            dataList = DataUtilities.parseTextData(dataStr, scaling);
        }
        else if (dataStr.startsWith("e:")) {
            dataList = DataUtilities.parseExtendedData(dataStr);
        }
        else {
            throw new RuntimeException("Unrecognised data encoding: "
                    + dataStr.substring(0, 2));
        }
        XYSeriesCollection result = new XYSeriesCollection();
        for (int s = 0; s < dataList.size(); s = s + 2) {
            List xSeries = (List) dataList.get(s);
            List ySeries = (List) dataList.get(s + 1);
            XYSeries ss = new XYSeries("Series " + (s / 2));
            int itemCount = xSeries.size();
            // check for special case of x-values = -1
            boolean implicitXValues = false;
            if (itemCount == 1) {
                itemCount = ySeries.size();
                implicitXValues = true;
            }
            for (int i = 0; i < itemCount; i++) {
                double xx;
                if (implicitXValues) {
                    xx = (i + 0.0) / Math.max(itemCount - 1, 1);
                }
                else {
                    Number x = (Number) xSeries.get(i);
                    xx = x.doubleValue();
                }
                Number y = (Number) ySeries.get(i);
                if (y != null) {
                    ss.add(xx, y.doubleValue());
                }
                else {
                    ss.add(xx, null);
                }
            }
            result.addSeries(ss);
        }
        return result;
    }

    /**
     * Parses a dataset from the incoming string.  The data encoding specifies
     * both the x-values and the y-values, in consecutive series, with an
     * optional third series supplying the relative marker sizes.
     *
     * @param dataStr  the data string.
     * @param scalingStr  the scaling string.
     *
     * @return A dataset.
     */
    public static XYSeriesCollection parseScatterDataset(String dataStr,
    		String scalingStr) {

        if (dataStr == null) {
            throw new IllegalArgumentException("Null 'dataStr' argument.");
        }
        List dataList;
        if (dataStr.startsWith("s:")) { // simple encoding
            dataList = DataUtilities.parseSimpleData(dataStr);
        }
        else if (dataStr.startsWith("t:")) {
        	float[] scaling = parseDataScaling(scalingStr);
            dataList = DataUtilities.parseTextData(dataStr, scaling);
        }
        else if (dataStr.startsWith("e:")) {
            dataList = DataUtilities.parseExtendedData(dataStr);
        }
        else {
            throw new RuntimeException("Unrecognised data encoding: "
                    + dataStr.substring(0, 2));
        }
        XYSeriesCollection result = new XYSeriesCollection();
        List xSeries = (List) dataList.get(0);
        List ySeries = (List) dataList.get(1);
        XYSeries ss = new XYSeries("Series");
        int itemCount = xSeries.size();

        for (int i = 0; i < itemCount; i++) {
            Number x = (Number) xSeries.get(i);
            double xx = x.doubleValue();
            Number y = (Number) ySeries.get(i);
            if (y != null) {
                double yy = y.doubleValue();
                ss.add(xx, yy);
            }
            else {
                ss.add(xx, null);
            }
            result.addSeries(ss);
        }

        // is there a third series?
        List zSeries = null;
        if (dataList.size() > 2) {
            zSeries = (List) dataList.get(2);
            XYSeries sz = new XYSeries("Z");
            for (int i = 0; i < zSeries.size(); i++) {
                sz.add(i, (Number) zSeries.get(i));
            }
            // FIXME: we're not doing anything with this
        }

        return result;
    }

}
