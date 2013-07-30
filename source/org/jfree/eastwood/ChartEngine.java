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
 * ----------------
 * ChartEngine.java
 * ----------------
 * (C) Copyright 2008, 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     Niklas Therning;
 *                   Andrea Aime;
 *                   Ulf Dittmer (fix for bug 2513305);
 *                   Ingo Kegel (patch 2124467);
 *                   Bill Bejeck (patch 2604383).
 *
 * Changes
 * -------
 * 09-Jun-2008 : Version 1, code factored out of ChartServlet.java (DG);
 * 10-Jun-2008 : Added support for sparklines (cht='ls'), and values in
 *               parameter maps are String[] not String (DG);
 * 12-Jun-2008 : Set label background to null, fixing bug 1872190 (DG);
 * 27-Jun-2008 : Added support for 'chdl' in bar charts, and also 'chdlp' -
 *               see patch 2002341 by Niklas Therning (DG);
 * 27-Jun-2008 : Disable plot outlines - see patch 2003913 by Niklas
 *               Therning (DG);
 * 30-Jun-2008 : Added almost full support for 'chg' in xy and bar charts (NT);
 * 01-Jul-2008 : Added new Eastwood tags 'ewd2' and 'ewtr' (DG);
 * 02-Jul-2008 : Removed unnecessary code in createBarChart() (NT);
 * 02-Jul-2008 : Added font parameter to buildChart() and made sure the
 *               configured font is used for all titles and labels (NT);
 * 14-Jul-2008 : Added buildChart(Map) method for convenience (DG);
 * 14-Jul-2008 : Moved data parsing methods to DataUtilities.java (DG);
 * 14-Jul-2008 : Added support for 'chds' data scaling - see patch 2001599
 *               by Niklas Therning (DG);
 * 15-Jul-2008 : Added check for XYPlot in 'chls' to prevent
 *               ClassCastException (DG);
 * 16-Jul-2008 : Added new bar charts with 3D effect (DG);
 * 16-Jul-2008 : Fixed gridlines (DG);
 * 18-Jul-2008 : Applied workaround in bug report 2001830
 *               by Niklas Therning (DG);
 * 11-Aug-2008 : Handle 'chxr' for 3D axis (DG);
 * 03-May-2009 : Added support for axis styles ('chxs') (AA);
 * 05-May-2009 : Added support for pie chart rotation ('chp') (AA);
 * 13-May-2009 : Fix for bug 2513305 (ClassCastException in scatter plots) by
 *               Ulf Dittmer (DG);
 * 13-May-2009 : Support background paint in bar charts (DG);
 * 13-May-2009 : Applied patch 2124467 by Ingo Kegel for axis label
 *               orientation (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Point;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.DialBackground;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.GradientPaintTransformType;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.util.SortOrder;

/**
 * The chart engine contains the code that reads the chart parameters and
 * returns a corresponding <code>JFreeChart</code> instance.
 */
public class ChartEngine {

    /**
     * Creates and returns a new <code>JFreeChart</code> instance that
     * reflects the specified parameters (which should be equivalent to
     * a parameter map returned by HttpServletRequest.getParameterMap() for
     * a valid URI for the Google Chart API.
     *
     * @param params  the parameters (<code>null</code> not permitted).
     *
     * @return A chart corresponding to the specification in the supplied
     *         parameters.
     */
    public static JFreeChart buildChart(Map params) {
        return buildChart(params, new Font("Dialog", Font.PLAIN, 12));
    }

    /**
     * Creates and returns a new <code>JFreeChart</code> instance that
     * reflects the specified parameters (which should be equivalent to
     * a parameter map returned by HttpServletRequest.getParameterMap() for
     * a valid URI for the Google Chart API.
     *
     * @param params  the parameters (<code>null</code> not permitted).
     * @param font    the font to use to draw titles, labels and legends.
     *
     * @return A chart corresponding to the specification in the supplied
     *         parameters.
     */
    public static JFreeChart buildChart(Map params, Font font) {
        if (params == null) {
            throw new IllegalArgumentException("Null 'params' argument.");
        }

        JFreeChart chart = null;

        // *** CHART TYPE **
        String[] chartType = (String[]) params.get("cht");
        int dataType = -1;  // 0 = PieDataset; 1 = XYDataset; 2 = special

        // pie charts: 'p' and 'p3'
        if (chartType[0].equals("p")) {
            chart = createPieChart();
            dataType = 0;
        }
        else if (chartType[0].equals("p3")) {
            chart = createPieChart3D();
            dataType = 0;
        }
        // line chart: 'lc'
        else if (chartType[0].equals("lc")) {
            chart = createLineChart();
            dataType = 1;
        }
        // sparkline: 'ls'
        else if (chartType[0].equals("ls")) {
            chart = createSparklineChart();
            dataType = 1;
        }
        // xy chart: 'lxy'
        else if (chartType[0].equals("lxy")) {
            chart = createLineChart();
            dataType = 3;
        }
        // bar charts: 'bhs', 'bhg', 'bhs' and 'bhg'
        else if (chartType[0].equals("bhs")) {
            chart = createStackedBarChart(PlotOrientation.HORIZONTAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bhg")) {
            chart = createBarChart(PlotOrientation.HORIZONTAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bvs")) {
            chart = createStackedBarChart(PlotOrientation.VERTICAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bvg")) {
            chart = createBarChart(PlotOrientation.VERTICAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bhs3")) {
            chart = createStackedBarChart3D(PlotOrientation.HORIZONTAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bhg3")) {
            chart = createBarChart3D(PlotOrientation.HORIZONTAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bvs3")) {
            chart = createStackedBarChart3D(PlotOrientation.VERTICAL);
            dataType = 2;
        }
        else if (chartType[0].equals("bvg3")) {
            chart = createBarChart3D(PlotOrientation.VERTICAL);
            dataType = 2;
        }
        // scatter chart: 's'
        else if (chartType[0].equals("s")) {
            chart = createScatterChart();
            dataType = 4;
        }
        else if (chartType[0].equals("dial")) {
            chart = createDialChart();
            dataType = 5;
        }
        else if (chartType[0].equals("v")) {
            throw new RuntimeException("Venn diagrams not implemented.");
            // TODO: fix this.
        }
        else {
            throw new RuntimeException("Unknown chart type: " + chartType[0]);
        }

        chart.getPlot().setOutlineVisible(false);

        
        // *** FORCE DEFAULT FONT ***
        if (chart.getPlot() instanceof XYPlot) {
            XYPlot plot = (XYPlot) chart.getPlot();
            plot.getDomainAxis().setLabelFont(font);
            plot.getDomainAxis().setTickLabelFont(font);
            plot.getRangeAxis().setLabelFont(font);
            plot.getRangeAxis().setTickLabelFont(font);
        }
        else if (chart.getPlot() instanceof CategoryPlot) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.getDomainAxis().setLabelFont(font);
            plot.getDomainAxis().setTickLabelFont(font);
            plot.getRangeAxis().setLabelFont(font);
            plot.getRangeAxis().setTickLabelFont(font);
        }

        
        // *** CHART AXES ***
        List axes = new java.util.ArrayList();
        String[] axisStr = (String[]) params.get("chxt");
        if (axisStr != null) {
            if (chart.getPlot() instanceof XYPlot) {
                XYPlot plot = (XYPlot) chart.getPlot();
                processAxisStr(plot, axisStr[0], axes);
            }
            else if (chart.getPlot() instanceof CategoryPlot) {
                CategoryPlot plot = (CategoryPlot) chart.getPlot();
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    processAxisStrV(plot, axisStr[0], axes);
                }
                else {
                    processAxisStrH(plot, axisStr[0], axes);
                }
            }
        }

        // *** AXIS LABEL ORIENTATIONS ***
        // eastwood extension, not part of Google chart API

        String[] axisLabelOrientationStr = (String[]) params.get("ewlo");
        if (axisLabelOrientationStr != null) {
            String[] orientations = breakString(axisLabelOrientationStr[0],
                    '|');
            for (int i = 0; i < orientations.length; i++) {
                int comma = orientations[i].indexOf(',');
                int axisIndex = Integer.parseInt(orientations[i].substring(0,
                        comma));
                String orientation = orientations[i].substring(comma + 1);
                Axis axis = (Axis) axes.get(axisIndex);
                if (axis instanceof GCategoryAxis) {
                    GCategoryAxis gaxis = (GCategoryAxis) axis;
                    gaxis.setCategoryLabelPositions(getCategoryLabelPositions(
                            orientation));
                }
            }
        }

        // *** AXIS RANGES ***
        String[] axisRangeStr = (String[]) params.get("chxr");
        if (axisRangeStr != null) {
            String[] ranges = breakString(axisRangeStr[0], '|');
            for (int i = 0; i < ranges.length; i++) {
                int comma1 = ranges[i].indexOf(',');
                int comma2 = ranges[i].indexOf(',', comma1 + 1);
                int axisIndex = Integer.parseInt(ranges[i].substring(0,
                        comma1));
                float lowerBound = Float.parseFloat(ranges[i].substring(
                        comma1 + 1, comma2));
                float upperBound = Float.parseFloat(ranges[i].substring(
                        comma2 + 1));
                Axis axis = (Axis) axes.get(axisIndex);
                if (axis instanceof GValueAxis) {
                    GValueAxis gaxis = (GValueAxis) axis;
                    gaxis.setLabelAxisStart(lowerBound);
                    gaxis.setLabelAxisEnd(upperBound);
                }
                else if (axis instanceof GValueAxis3D) {
                    GValueAxis3D gaxis3 = (GValueAxis3D) axis;
                    gaxis3.setLabelAxisStart(lowerBound);
                    gaxis3.setLabelAxisEnd(upperBound);
                }
            }
        }


        // *** AXIS LABELS ***
        String[] axisLabelStr = (String[]) params.get("chxl");
        if (axisLabelStr != null) {
            Pattern p = Pattern.compile("\\d+:\\|");
            Matcher m = p.matcher(axisLabelStr[0]);
            if (m.find()) {
                int keyStart = m.start();
                int labelStart = m.end();
                while (m.find(labelStart)) {
                    String keyStr = axisLabelStr[0].substring(keyStart,
                            labelStart - 2);
                    int axisIndex = Integer.parseInt(keyStr);
                    keyStart = m.start();
                    String labelStr = axisLabelStr[0].substring(labelStart,
                            keyStart - 1);
                    String[] labels = breakString(labelStr, '|');
                    GLabelledAxis axis = (GLabelledAxis) axes.get(axisIndex);
                    axis.setTickLabels(Arrays.asList(labels));
                    labelStart = m.end();
                }
                // process the final item
                String keyStr = axisLabelStr[0].substring(keyStart,
                        labelStart - 2);
                String labelStr = axisLabelStr[0].substring(labelStart);
                int axisIndex = Integer.parseInt(keyStr);
                if (labelStr.endsWith("|")) {
                    labelStr = labelStr.substring(0, labelStr.length() - 1);
                }
                String[] labels = breakString(labelStr, '|');
                GLabelledAxis axis = (GLabelledAxis) axes.get(axisIndex);
                axis.setTickLabels(Arrays.asList(labels));

            }
            else {
                throw new RuntimeException("No matching pattern!");
            }

        }


        // ** EXPLICIT AXIS LABEL POSITIONS
        String[] axisPositionStr = (String[]) params.get("chxp");
        if (axisPositionStr != null) {
            String[] positions = breakString(axisPositionStr[0], '|');
            for (int i = 0; i < positions.length; i++) {
                int c1 = positions[i].indexOf(',');
                int axisIndex = Integer.parseInt(positions[i].substring(0, c1));
                String remainingStr = positions[i].substring(c1 + 1);
                String[] valueStr = breakString(remainingStr, ',');
                List tickValues = new java.util.ArrayList(valueStr.length);
                Axis axis = (Axis) axes.get(axisIndex);
                if (axis instanceof GValueAxis) {
                    GValueAxis gaxis = (GValueAxis) axes.get(axisIndex);
                    for (int j = 0; j < valueStr.length; j++) {
                        float pos = Float.parseFloat(valueStr[j]);
                        tickValues.add(new Float(pos));
                    }
                    gaxis.setTickLabelPositions(tickValues);
                }
                // FIXME: what about a CategoryAxis?
            }
        }
        
        
        // ** AXIS STYLE 
        String[] axisStyleStr = (String[]) params.get("chxs");
        if (axisStyleStr != null) {
            String[] styles = breakString(axisStyleStr[0], '|');
            for (int i = 0; i < styles.length; i++) {
                // mandatory params, axis number and color
                String[] styleSpec = breakString(styles[i], ',');
                int axisIndex = Integer.parseInt(styleSpec[0]);
                Axis axis = (Axis) axes.get(axisIndex);
                Color axisColor = parseColor(styleSpec[1]);
                axis.setTickLabelPaint(axisColor);
                // parse optional params
                if (styleSpec.length > 2) {
                    // axis font size
                    int fontSize = Integer.parseInt(styleSpec[2]);
                    if (fontSize > 0) {
                        // TODO: this is not accurate, the Java font
                        // size is points, the GC font size is pixels
                        Font oldFont = axis.getTickLabelFont();
                        Font newFont = oldFont.deriveFont((float) fontSize);
                        axis.setTickLabelFont(newFont);
                    }
                    else {
                        axis.setTickLabelsVisible(false);
                    }
                }
                if (styleSpec.length > 3) {
                    // this would be alignement, we don't handle it
                }
                if (styleSpec.length > 4) {
                    // drawing control
                    String drawingControl = styleSpec[4];
                    if ("l".equals(drawingControl)) {
                        axis.setTickMarksVisible(false);
                    }
                    else if ("t".equals(drawingControl)) {
                        axis.setAxisLineVisible(false);
                    }
                    else if ("_".equals(drawingControl)) {
                        axis.setVisible(false);
                    }
                    else if (!"lt".equals(drawingControl)) {
                        throw new RuntimeException("Unknown drawing control "
                                + drawingControl);
                    }
                }
                if (styleSpec.length > 5) {
                    Color tickColor = parseColor(styleSpec[5]);
                    axis.setTickMarkPaint(tickColor);
                }
            }
        }


        // *** CHART TITLE ***
        String[] titleStr = (String[]) params.get("chtt");
        if (titleStr != null) {
            // process the title
            String[] s = breakString(titleStr[0], '|');
            for (int i = 0; i < s.length; i++) {
                TextTitle t = new TextTitle(s[i].replace('+', ' '));
                t.setPaint(Color.gray);
                // Google seems to use 14pt fonts for titles and 12pt fonts for
                // all other text. Make sure this relationship remains.
                t.setFont(font.deriveFont(font.getSize2D() * 14f / 12f));
                chart.addSubtitle(t);
            }
            // and the font and colour
            String[] fontStr = (String[]) params.get("chts");
            if (fontStr != null) {
                int c1 = fontStr[0].indexOf(',');
                String colorStr = null;
                String fontSizeStr = null;
                if (c1 != -1) {
                    colorStr = fontStr[0].substring(0, c1);
                    fontSizeStr = fontStr[0].substring(c1 + 1);
                }
                else {
                    colorStr = fontStr[0];
                }
                Color color = parseColor(colorStr);
                int size = 12;
                if (fontSizeStr != null) {
                    size = Integer.parseInt(fontSizeStr);
                }
                for (int i = 0; i < chart.getSubtitleCount(); i++) {
                    Title t = chart.getSubtitle(i);
                    if (t instanceof TextTitle) {
                        TextTitle tt = (TextTitle) t;
                        tt.setPaint(color);
                        tt.setFont(font.deriveFont((float) size));
                    }
                }
            }
        }

        // *** CHART DATA ***
        String[] dataStr = (String[]) params.get("chd");
        String scalingStr = null;
        if (dataStr.length > 0 && dataStr[0].startsWith("t:")) {
            // Only look at chds when text encoding is used
            String[] chds = (String[]) params.get("chds");
            if (chds != null && chds.length > 0) {
                scalingStr = chds[0];
            }
        }

        // we'll also process an optional second dataset that is provided as
        // an Eastwood extension...this isn't part of the Google Chart API
        String[] d2Str = (String[]) params.get("ewd2");

        // 'p' and 'p3' - create PieDataset
        if (dataType == 0) {
            PieDataset dataset = DataUtilities.parsePieDataset(dataStr[0],
                    scalingStr);
            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setDataset(dataset);

            // ignore d2Str as there is currently no need for a second pie
            // dataset.
        }

        // 'lc' - create XYDataset
        else if (dataType == 1) {
            XYPlot plot = (XYPlot) chart.getPlot();
            XYDataset dataset = DataUtilities.parseXYDataset(dataStr[0],
                    scalingStr);
            plot.setDataset(dataset);

            if (d2Str != null) {  // an Eastwood extension
                XYDataset d2 = DataUtilities.parseXYDataset(d2Str[0],
                        scalingStr);
                plot.setDataset(1, d2);
            }
        }

        // 'bhs', 'bhg', 'bvs', 'bvg'
        else if (dataType == 2) {
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            CategoryDataset dataset = DataUtilities.parseCategoryDataset(
                    dataStr[0], scalingStr);
            plot.setDataset(dataset);

            if (d2Str != null) {  // an Eastwood extension
                CategoryDataset d2 = DataUtilities.parseCategoryDataset(
                        d2Str[0], scalingStr);
                plot.setDataset(1, d2);
            }
        }

        // 'lxy'
        else if (dataType == 3) {
            XYPlot plot = (XYPlot) chart.getPlot();
            XYDataset dataset = DataUtilities.parseXYDataset2(dataStr[0],
                    scalingStr);
            plot.setDataset(dataset);

            if (d2Str != null) {  // an Eastwood extension
                XYDataset d2 = DataUtilities.parseXYDataset2(d2Str[0],
                        scalingStr);
                plot.setDataset(1, d2);
            }
        }
        else if (dataType == 4) {
            XYPlot plot = (XYPlot) chart.getPlot();
            XYSeriesCollection dataset = DataUtilities.parseScatterDataset(
                    dataStr[0], scalingStr);
            if (dataset.getSeriesCount() > 1) {
                dataset.removeSeries(1);
            }
            plot.setDataset(dataset);
            if (d2Str != null) {  // an Eastwood extension
                XYDataset d2 = DataUtilities.parseXYDataset2(d2Str[0],
                        scalingStr);
                plot.setDataset(1, d2);
            }
        }
        else if (dataType == 5) { // Dial chart
            DialPlot  plot = (DialPlot) chart.getPlot();
            String[] data = dataStr[0].substring(dataStr[0].indexOf(":")
                    + 1).split(",");

            if (data.length == 0) {
                throw new RuntimeException("Dial chart needs data points");
            }

            float value =  Float.parseFloat(data[0]);

            plot.setDataset(new DefaultValueDataset(value));
            double lowerBound = 0;
            double upperBound = 100;
            double majorTickIncrement = 10.0;
            int  minorTickIncrement = 4;

            if (data.length > 1) {
                for (int i = 1; i < data.length; i++) {
                    if (data[i].indexOf("dr=") == 0) {
                        String[] rd = data[i].substring(3).split(":");
                        if (rd.length == 3) {
                             StandardDialRange range = new StandardDialRange(
                                     Float.parseFloat(rd[0]),
                                     Float.parseFloat(rd[1]),
                                     new Color(Integer.parseInt(rd[2],16)));
                             range.setInnerRadius(0.52);
                             range.setOuterRadius(0.55);
                             plot.addLayer(range);
                        }
                    }
                    else if (data[i].indexOf("lb=") == 0) {
                        lowerBound = Double.parseDouble(data[i].substring(3));
                    }
                    else if (data[i].indexOf("ub=") == 0) {
                        upperBound = Double.parseDouble(data[i].substring(3));
                    }
                    else if (data[i].indexOf("mt=") == 0) {
                        minorTickIncrement = Integer.parseInt(
                                data[i].substring(3));
                    }
                    else if (data[i].indexOf("mjt=") == 0) {
                        majorTickIncrement = Double.parseDouble(
                                data[i].substring(4));
                    }
                }
            }

            StandardDialScale scale = new StandardDialScale(lowerBound,
                    upperBound, -120, -300, majorTickIncrement,
                    minorTickIncrement);
            scale.setTickRadius(0.90);
            scale.setTickLabelOffset(0.17);
            scale.setTickLabelFont(font);
            plot.addScale(0, scale);

            plot.removePointer(0);
            DialPointer.Pointer p = new DialPointer.Pointer();
            p.setFillPaint(Color.gray);
            plot.addPointer(p);
        }

        // *** CHART COLOURS ***
        String[] colorStr = (String[]) params.get("chco");
        if (colorStr != null) {
            Color[] colors = parseColors(colorStr[0]);
            if (dataType == 0) {
                PiePlot plot = (PiePlot) chart.getPlot();
                applyColorsToPiePlot(plot, colors);
            }
            else {
                AbstractRenderer renderer = null;
                if (chart.getPlot() instanceof CategoryPlot) {
                    CategoryPlot plot = (CategoryPlot) chart.getPlot();
                    renderer = (AbstractRenderer) plot.getRenderer();
                    renderer.setBasePaint(colors[0]);
                }
                else if (chart.getPlot() instanceof XYPlot) {
                    XYPlot plot = (XYPlot) chart.getPlot();
                    renderer = (AbstractRenderer) plot.getRenderer();
                    renderer.setBasePaint(colors[colors.length - 1]);
                }
                for (int i = 0; i < colors.length; i++) {
                    renderer.setSeriesPaint(i, colors[i]);
                }
            }
        }
        else {
            Plot plot = chart.getPlot();
            if (plot instanceof PiePlot) {
                applyColorsToPiePlot((PiePlot) chart.getPlot(),
                        new Color[] { new Color(255, 153, 0) });
            }
        }


        // *** CHART LINE STYLES ***
        String[] lineStr = (String[]) params.get("chls");
        if (lineStr != null && chart.getPlot() instanceof XYPlot) {
            Stroke[] strokes = parseLineStyles(lineStr[0]);
            XYPlot plot = (XYPlot) chart.getPlot();
            XYItemRenderer renderer = plot.getRenderer();
            for (int i = 0; i < strokes.length; i++) {
                renderer.setSeriesStroke(i, strokes[i]);
            }
            renderer.setBaseStroke(strokes[strokes.length - 1]);
        }


        // *** CHART GRID LINES
        if (dataType != 0) {
            String[] gridStr = (String[]) params.get("chg");
            if (gridStr != null) {
                processGridLinesSpec(gridStr[0], chart);
            }
        }

        // *** CHART LEGEND LABELS
        if (dataType == 0) {  // pie chart
            String[] labelStr = (String[]) params.get("chl");
            if (labelStr != null) {
                String[] s = breakString(labelStr[0], '|');
                List labels = Arrays.asList(s);
                PiePlot plot = (PiePlot) chart.getPlot();
                if (labels.size() > 0) {
                    plot.setLabelGenerator(
                            new GPieSectionLabelGenerator(labels));
                    plot.setLabelFont(font);
                    plot.setLabelPaint(Color.gray);
                }
            }
        }
        else if (dataType == 5) {
            DialPlot plot = (DialPlot) chart.getPlot();
            String[] labelStr = (String[]) params.get("chl");
            if (labelStr != null) {
                DialTextAnnotation annotation1 = new DialTextAnnotation(
                        labelStr[0]);
                annotation1.setFont(font);
                annotation1.setRadius(0.7);
                plot.addLayer(annotation1);
            }
        }
        else {
            String[] legendStr = (String[]) params.get("chdl");
            if (legendStr != null) {
                // process the title
                String[] s = breakString(legendStr[0], '|');
                List labels = Arrays.asList(s);
                if (labels.size() > 0) {
                    Plot p = chart.getPlot();
                    if (p instanceof CategoryPlot) {
                        CategoryPlot plot = (CategoryPlot) chart.getPlot();
                        BarRenderer renderer = (BarRenderer) plot.getRenderer();
                        renderer.setLegendItemLabelGenerator(
                                new GSeriesLabelGenerator(labels));
                        renderer.setBaseSeriesVisibleInLegend(false);
                        for (int i = 0; i < labels.size(); i++) {
                            renderer.setSeriesVisibleInLegend(i, Boolean.TRUE);
                        }
                    }
                    else if (p instanceof XYPlot) {
                        XYPlot plot = (XYPlot) chart.getPlot();
                        XYItemRenderer renderer = plot.getRenderer();
                        renderer.setLegendItemLabelGenerator(
                                new GSeriesLabelGenerator(labels));
                        renderer.setBaseSeriesVisibleInLegend(false);
                        for (int i = 0; i < labels.size(); i++) {
                            renderer.setSeriesVisibleInLegend(i, Boolean.TRUE);
                        }
                    }
                    LegendTitle legend = new LegendTitle(chart.getPlot());
                    RectangleEdge pos = RectangleEdge.RIGHT;
                    String[] chdlp = (String[]) params.get("chdlp");
                    if (chdlp != null) {
                        if ("b".equals(chdlp[0])) {
                            pos = RectangleEdge.BOTTOM;
                        }
                        else if ("t".equals(chdlp[0])) {
                            pos = RectangleEdge.TOP;
                        }
                        else if ("l".equals(chdlp[0])) {
                            pos = RectangleEdge.LEFT;
                        }
                    }
                    legend.setPosition(pos);
                    legend.setItemFont(font);
                    legend.setItemPaint(Color.gray);
                    chart.addSubtitle(legend);
                }
            }
        }


        // *** CHART MARKERS ***
        String[] markerStr = (String[]) params.get("chm");
        if (markerStr != null) {
            String[] markers = breakString(markerStr[0], '|');
            for (int i = 0; i < markers.length; i++) {
                addMarker(markers[i], chart);
            }
        }


        // *** CHART FILL ***/
        String[] fillStr = (String[]) params.get("chf");
        if (fillStr != null) {
            // process the 1 or 2 fill specs
            int i = fillStr[0].indexOf('|');
            if (i == -1) {
                processFillSpec(fillStr[0], chart);
            }
            else {
                String fs1 = fillStr[0].substring(0, i);
                String fs2 = fillStr[0].substring(i + 1);
                processFillSpec(fs1, chart);
                processFillSpec(fs2, chart);
            }
        }
        
        // ** PIE CHART ROTATION **/
        String[] pieRotation = (String[]) params.get("chp");
        if (pieRotation != null) {
            // rotation is in radians, and it's supposed to be clockwise
            double rotation = -Math.toDegrees(Double.parseDouble(
                    pieRotation[0]));
            //double startAngle =
            Plot plot = chart.getPlot();
            if (plot instanceof PiePlot) {
                ((PiePlot) plot).setStartAngle(rotation);
            }
            else if (plot instanceof PiePlot3D) {
                ((PiePlot3D) plot).setStartAngle(rotation);
            }
        }

        // process the 'ewtr' tag, if present
        processEWTR(params, chart);

        return chart;

    }

    /**
     * The 'ewtr' tag is an Eastwood extension that draws a trend line over a
     * chart, using data that has been added to a secondary dataset using the
     * 'ewd2' tag.
     *
     * @param params  the chart parameters;
     * @param chart  the chart under construction (will be updated by this
     *         method if necessary).
     */
    public static void processEWTR(Map params, JFreeChart chart) {
        // the 'ewtr' arguments are:
        // - <seriesIndex> : the index of the series in the secondary dataset;
        // - <colour> : the colour;
        // - <lineThickness> : the line thickness;
        String[] ewtrStr = (String[]) params.get("ewtr");
        if (ewtrStr != null) {
            String[] atts = ewtrStr[0].split(",");
            int series = Integer.parseInt(atts[0]);
            Color color = parseColor(atts[1]);
            float lineWidth = Float.parseFloat(atts[2]);
            Plot plot = chart.getPlot();
            if (plot instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) plot;
                if (cp.getDataset(1) != null) {
                    LineAndShapeRenderer r = new LineAndShapeRenderer(true,
                            false);
                    r.setBaseSeriesVisible(false);
                    r.setSeriesVisible(series, Boolean.TRUE);
                    r.setSeriesPaint(series, color);
                    r.setSeriesStroke(series, new BasicStroke(lineWidth));
                    cp.setRenderer(1, r);

                    cp.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                }
            }
            else if (plot instanceof XYPlot) {
                XYPlot xp = (XYPlot) plot;
                if (xp.getDataset(1) != null) {
                    XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true,
                            false);
                    r.setBaseSeriesVisible(false);
                    r.setSeriesVisible(series, Boolean.TRUE);
                    r.setSeriesPaint(series, color);
                    r.setSeriesStroke(series, new BasicStroke(lineWidth));
                    xp.setRenderer(1, r);
                    xp.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
                }
            }
        }

    }

    /**
     * Creates a dial chart.
     *
     * @return A dial chart.
     */
    private static JFreeChart createDialChart(){
        DialPlot plot = new DialPlot();
        plot.setDialFrame(new StandardDialFrame());
        GradientPaint gp = new GradientPaint(new Point(),
                new Color(255, 255, 255), new Point(),
                new Color(170, 170, 220));
        DialBackground db = new DialBackground(gp);
        db.setGradientPaintTransformer(new StandardGradientPaintTransformer(
                GradientPaintTransformType.VERTICAL));
        plot.setBackground(db);
        plot.addPointer(new DialPointer.Pin());
        DialCap cap = new DialCap();
        plot.setCap(cap);

        return new JFreeChart(plot);
    }

    /**
     * Creates a pie chart.
     *
     * @return A pie chart.
     */
    private static JFreeChart createPieChart() {
        JFreeChart chart = ChartFactory.createPieChart(null, null,
                false, true, false);
        chart.setBackgroundPaint(Color.white);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setInteriorGap(0.06);
        plot.setStartAngle(0.0);
        plot.setLabelGenerator(null);
        plot.setBaseSectionOutlinePaint(Color.white);
        plot.setBaseSectionOutlineStroke(new BasicStroke(1.2f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPadding(RectangleInsets.ZERO_INSETS);
        plot.setLabelFont(new Font("Dialog", Font.PLAIN, 12));
        plot.setLabelPaint(Color.gray);
        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{2}"));
        return chart;
    }

    /**
     * Creates a pie chart with 3D effect.
     *
     * @return A pie chart.
     */
    private static JFreeChart createPieChart3D() {
        JFreeChart chart = ChartFactory.createPieChart3D(null, null,
                false, true, false);
        chart.setBackgroundPaint(Color.white);
        chart.setBorderPaint(Color.white);
        PiePlot3D plot = (PiePlot3D) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setDarkerSides(true);
        plot.setBaseSectionOutlinePaint(new Color(0,0,0,0));
        plot.setStartAngle(0.0);
        plot.setInteriorGap(0.10);
        plot.setLabelGenerator(null);
        plot.setOutlineVisible(false);
        plot.setLabelBackgroundPaint(Color.white);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPadding(RectangleInsets.ZERO_INSETS);
        plot.setLabelFont(new Font("Dialog", Font.PLAIN, 12));
        plot.setLabelPaint(Color.gray);
        plot.setToolTipGenerator(new StandardPieToolTipGenerator("{2}"));
        return chart;
    }

    /**
     * Creates a line chart.
     *
     * @return A line chart.
     */
    private static JFreeChart createLineChart() {
        GXYPlot plot = new GXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        plot.setRenderer(renderer);
        JFreeChart chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        renderer.setBasePaint(new Color(0xFF9900));
        renderer.setBaseStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        renderer.setAutoPopulateSeriesPaint(false);

        GValueAxis xAxis = new GValueAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        plot.setDomainAxis(xAxis);
        GValueAxis yAxis = new GValueAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        plot.setRangeAxis(yAxis);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        return chart;
    }

    /**
     * Creates a sparkline chart.
     *
     * @return A sparkline chart.
     */
    private static JFreeChart createSparklineChart() {
        GXYPlot plot = new GXYPlot();
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        plot.setRenderer(renderer);
        JFreeChart chart = new JFreeChart(plot);
        chart.setPadding(RectangleInsets.ZERO_INSETS);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        renderer.setBasePaint(new Color(0xFF9900));
        renderer.setBaseStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
        renderer.setAutoPopulateSeriesPaint(false);

        GValueAxis xAxis = new GValueAxis();
        xAxis.setVisible(false);
        plot.setDomainAxis(xAxis);
        GValueAxis yAxis = new GValueAxis();
        yAxis.setVisible(false);
        plot.setRangeAxis(yAxis);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        return chart;
    }

    /**
     * Creates a bar chart with the specified orientation and using the
     * specified renderer.
     *
     * @param orientation  the plot orientation.
     * @param renderer     the renderer.
     *
     * @return A bar chart.
     */
    private static JFreeChart createBarChart(PlotOrientation orientation,
            BarRenderer renderer) {
        GCategoryPlot plot = new GCategoryPlot();
        plot.setOrientation(orientation);
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT,
                plot, false);

        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(null);
        plot.setRenderer(renderer);
        renderer.setBasePaint(new Color(0xFFCC33));
        renderer.setAutoPopulateSeriesPaint(false);
        GCategoryAxis xAxis = new GCategoryAxis();
        xAxis.setAxisLineVisible(true);
        xAxis.setTickLabelsVisible(false);
        xAxis.setMaximumCategoryLabelLines(5);
        plot.setDomainAxis(xAxis);
        GValueAxis yAxis = new GValueAxis();
        yAxis.setAxisLineVisible(true);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        plot.setRangeAxis(yAxis);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        return chart;
    }

    /**
     * Creates a bar chart with the specified orientation.
     *
     * @param orientation  the plot orientation.
     *
     * @return A bar chart.
     */
    private static JFreeChart createBarChart(PlotOrientation orientation) {
        return createBarChart(orientation, new BarRenderer());
    }

    /**
     * Creates a stacked bar chart with the specified orientation.
     *
     * @param orientation  the orientation.
     *
     * @return A stacked bar chart.
     */
    private static JFreeChart createStackedBarChart(
            PlotOrientation orientation) {
        return createBarChart(orientation, new StackedBarRenderer());
    }
    /**
     * Creates a bar chart with the specified orientation and using the
     * specified renderer.
     *
     * @param orientation  the plot orientation.
     * @param renderer     the renderer.
     *
     * @return A bar chart.
     */
    private static JFreeChart createBarChart3D(PlotOrientation orientation,
            BarRenderer renderer) {
        GCategoryPlot plot = new GCategoryPlot();
        plot.setOrientation(orientation);
        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            plot.setColumnRenderingOrder(SortOrder.DESCENDING);
        }
        JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT,
                plot, false);

        chart.setBackgroundPaint(Color.white);
        plot.setBackgroundPaint(null);
        plot.setRenderer(renderer);
        renderer.setBasePaint(new Color(0xFFCC33));
        renderer.setAutoPopulateSeriesPaint(false);
        GCategoryAxis3D xAxis = new GCategoryAxis3D();
        xAxis.setAxisLineVisible(true);
        xAxis.setTickLabelsVisible(false);
        xAxis.setMaximumCategoryLabelLines(5);
        plot.setDomainAxis(xAxis);
        GValueAxis3D yAxis = new GValueAxis3D();
        yAxis.setAxisLineVisible(true);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        plot.setRangeAxis(yAxis);
        plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);
        return chart;
    }

    /**
     * Creates a bar chart with the specified orientation.
     *
     * @param orientation  the plot orientation.
     *
     * @return A bar chart.
     */
    private static JFreeChart createBarChart3D(PlotOrientation orientation) {
        return createBarChart3D(orientation, new BarRenderer3D());
    }

    /**
     * Creates a stacked bar chart with the specified orientation.
     *
     * @param orientation  the orientation.
     *
     * @return A stacked bar chart.
     */
    private static JFreeChart createStackedBarChart3D(
            PlotOrientation orientation) {
        return createBarChart3D(orientation, new StackedBarRenderer3D());
    }

    /**
     * Creates a scatter chart.
     *
     * @return A scatter chart.
     */
    private static JFreeChart createScatterChart() {
        GXYPlot plot = new GXYPlot();
        plot.setBackgroundPaint(null);
        plot.setOutlinePaint(null);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false,
                true);
        renderer.setBasePaint(new Color(0x76A4FB));
        renderer.setAutoPopulateSeriesPaint(false);
        plot.setRenderer(renderer);

        GValueAxis xAxis = new GValueAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarksVisible(false);
        plot.setDomainAxis(xAxis);

        GValueAxis yAxis = new GValueAxis();
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarksVisible(false);
        plot.setRangeAxis(yAxis);
        plot.setDomainGridlinesVisible(false);
        plot.setRangeGridlinesVisible(false);

        JFreeChart chart = new JFreeChart(plot);
        chart.setBackgroundPaint(Color.white);
        chart.removeLegend();

        return chart;
    }

    /**
     * Processes a string that indicates the axes that should be visible on
     * the plot.
     *
     * @param plot  the plot.
     * @param axisStr  the axis specification.
     * @param axes  a list that will be populated with any axes added to the
     *              plot.
     */
    private static void processAxisStr(XYPlot plot, String axisStr, List axes) {
        int xAxisCount = 0;
        int yAxisCount = 0;
        for (int i = 0; i < axisStr.length(); i++) {
            char c = axisStr.charAt(i);
            if (c == 'x') {
                if (xAxisCount == 0) {
                    Axis xAxis = plot.getDomainAxis();
                    xAxis.setTickMarksVisible(true);
                    xAxis.setTickLabelsVisible(true);
                    axes.add(xAxis);
                    xAxisCount++;
                }
                else {
                    GValueAxis axis = new GValueAxis();
                    axis.setAxisLineVisible(false);
                    plot.setDomainAxis(xAxisCount, axis);
                    plot.setDomainAxisLocation(xAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    xAxisCount++;
                }
            }
            else if (c == 'y') {
                if (yAxisCount == 0) {
                    Axis yAxis = plot.getRangeAxis();
                    yAxis.setTickMarksVisible(true);
                    yAxis.setTickLabelsVisible(true);
                    axes.add(yAxis);
                    yAxisCount++;
                }
                else {
                    GValueAxis axis = new GValueAxis();
                    axis.setAxisLineVisible(false);
                    plot.setRangeAxis(yAxisCount, axis);
                    plot.setRangeAxisLocation(yAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    yAxisCount++;
                }
            }
            else if (c == 'r') {
                GValueAxis axis = new GValueAxis();
                plot.setRangeAxis(yAxisCount, axis);
                plot.setRangeAxisLocation(yAxisCount,
                        AxisLocation.BOTTOM_OR_RIGHT);
                axes.add(axis);
                yAxisCount++;
            }
            else if (c == 't') {
                GValueAxis axis = new GValueAxis();
                plot.setDomainAxis(xAxisCount, axis);
                plot.setDomainAxisLocation(xAxisCount,
                        AxisLocation.TOP_OR_LEFT);
                axes.add(axis);
                xAxisCount++;
            }
            else if (c == ',') {
                // nothing to do
            }
            else {
                throw new RuntimeException("Bad character " + c);
            }
        }

    }

    /**
     * Processes a string that indicates the axes that should be visible on
     * the plot.
     *
     * @param plot  the plot.
     * @param axisStr  the axis specification.
     * @param axes  a list that will be populated with any axes added to the
     *              plot.
     */
    private static void processAxisStrV(CategoryPlot plot, String axisStr,
            List axes) {
        int xAxisCount = 0;
        int yAxisCount = 0;
        for (int i = 0; i < axisStr.length(); i++) {
            char c = axisStr.charAt(i);
            if (c == 'x') {
                if (xAxisCount == 0) {
                    CategoryAxis xAxis = plot.getDomainAxis();
                    xAxis.setTickLabelsVisible(true);
                    axes.add(xAxis);
                    xAxisCount++;
                }
                else {
                    GCategoryAxis axis = new GCategoryAxis();
                    axis.setAxisLineVisible(false);
                    plot.setDomainAxis(xAxisCount, axis);
                    plot.setDomainAxisLocation(xAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    xAxisCount++;
                }
            }
            else if (c == 'y') {
                if (yAxisCount == 0) {
                    Axis yAxis = plot.getRangeAxis();
                    yAxis.setTickLabelsVisible(true);
                    yAxis.setTickMarksVisible(true);
                    axes.add(yAxis);
                    yAxisCount++;
                }
                else {
                    GValueAxis axis = new GValueAxis();
                    axis.setAxisLineVisible(false);
                    plot.setRangeAxis(yAxisCount, axis);
                    plot.setRangeAxisLocation(yAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    yAxisCount++;
                }
            }
            else if (c == 'r') {
                GValueAxis axis = new GValueAxis();
                plot.setRangeAxis(yAxisCount, axis);
                plot.setRangeAxisLocation(yAxisCount,
                        AxisLocation.BOTTOM_OR_RIGHT);
                axes.add(axis);
                yAxisCount++;
            }
            else if (c == 't') {
                GCategoryAxis axis = new GCategoryAxis();
                axis.setAxisLineVisible(false);
                plot.setDomainAxis(xAxisCount, axis);
                plot.setDomainAxisLocation(xAxisCount,
                        AxisLocation.TOP_OR_LEFT);
                axes.add(axis);
                xAxisCount++;
            }
            else if (c == ',') {
                // nothing to do
            }
            else {
                throw new RuntimeException("Bad character " + c);
            }
        }

    }

    /**
     * Processes a string that indicates the axes that should be visible on
     * the plot.
     *
     * @param plot  the plot.
     * @param axisStr  the axis specification.
     * @param axes  a list that will be populated with any axes added to the
     *              plot.
     */
    private static void processAxisStrH(CategoryPlot plot, String axisStr,
            List axes) {
        int xAxisCount = 0;
        int yAxisCount = 0;
        for (int i = 0; i < axisStr.length(); i++) {
            char c = axisStr.charAt(i);
            if (c == 'y') {
                if (yAxisCount == 0) {
                    CategoryAxis axis = plot.getDomainAxis();
                    axis.setTickLabelsVisible(true);
                    axes.add(axis);
                    yAxisCount++;
                }
                else {
                    GCategoryAxis axis = new GCategoryAxis();
                    axis.setAxisLineVisible(false);
                    plot.setDomainAxis(yAxisCount, axis);
                    plot.setDomainAxisLocation(xAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    yAxisCount++;
                }
            }
            else if (c == 'x') {
                if (xAxisCount == 0) {
                    Axis axis = plot.getRangeAxis();
                    axis.setTickLabelsVisible(true);
                    axis.setTickMarksVisible(true);
                    axes.add(axis);
                    xAxisCount++;
                }
                else {
                    GValueAxis axis = new GValueAxis();
                    axis.setAxisLineVisible(false);
                    plot.setRangeAxis(xAxisCount, axis);
                    plot.setRangeAxisLocation(xAxisCount,
                            AxisLocation.BOTTOM_OR_LEFT);
                    axes.add(axis);
                    xAxisCount++;
                }
            }
            else if (c == 't') {
                GValueAxis axis = new GValueAxis();
                plot.setRangeAxis(xAxisCount, axis);
                plot.setRangeAxisLocation(yAxisCount,
                        AxisLocation.TOP_OR_LEFT);
                axes.add(axis);
                xAxisCount++;
            }
            else if (c == 'r') {
                GCategoryAxis axis = new GCategoryAxis();
                plot.setDomainAxis(yAxisCount, axis);
                plot.setDomainAxisLocation(xAxisCount,
                        AxisLocation.BOTTOM_OR_RIGHT);
                axes.add(axis);
                yAxisCount++;
            }
            else if (c == ',') {
                // nothing to do
            }
            else {
                throw new RuntimeException("Bad character " + c);
            }
        }

    }

    /**
     * A utility method that splits the supplied string at every occurrence of
     * the specified separator.
     *
     * @param text  the text (<code>null</code> not permitted).
     * @param separator  the separator character.
     *
     * @return An array of strings.
     */
    private static String[] breakString(String text, char separator) {
        if (text == null) {
            throw new IllegalArgumentException("Null 'text' argument.");
        }
        List temp = new java.util.ArrayList();
        String current = text;
        int i = current.indexOf(separator);
        while (i != -1) {
            String line = current.substring(0, i);
            temp.add(line);
            current = current.substring(i + 1);
            i = current.indexOf(separator);
        }
        temp.add(current);
        String[] result = new String[temp.size()];
        for (int j = 0; j < temp.size(); j++) {
            result[j] = (String) temp.get(j);
        }
        return result;
    }

    /**
     * Creates a color from the supplied string, which should be in RRGGBB
     * format, or RRGGBBAA.
     *
     * @param text  the text encoding (<code>null</code> not permitted).
     *
     * @return A color.
     */
    private static Color parseColor(String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Null 'text' argument (in parseColor(String)).");
        } else if(text.length() != 6 && text.length() != 8) {
            throw new IllegalArgumentException(
            "Invalid color representation " + text + ", it should be RRGGBB or RRGGBBAA");
        }
        // parse RRGGBB format
        if(text.length() == 6)
            return Color.decode("0x" + text);
        
        // parse RRGGBBAA format
        int r = Integer.parseInt(text.substring(0, 2), 16);
        int g = Integer.parseInt(text.substring(2, 4), 16);
        int b = Integer.parseInt(text.substring(4, 6), 16);
        int a = Integer.parseInt(text.substring(6, 8), 16);
        return new Color(r,g,b,a);
    }

    /**
     * Parses a string containing a comma-separated list of color values (in
     * the format RRGGBB or RRGGBBAA).
     *
     * @param text  the text (<code>null</code> not permitted).
     *
     * @return The colors.
     */
    private static Color[] parseColors(String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Null 'text' argument (in parseColors(String)).");
        }
        String[] codes = breakString(text, ',');
        Color[] result = new Color[codes.length];
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].length() > 0) {
                String code = breakString(codes[i], '|')[0];
                result[i] = Color.decode("0x" + code);
            }
            else {
                result[i] = Color.black;
            }
        }
        return result;
    }

    private static void applyColorsToPiePlot(PiePlot plot, Color[] colors) {
        if (colors.length == 1) {
            Color c = colors[0];
            colors = new Color[2];
            colors[0] = c;
            colors[1] = new Color(255 - ((255 - c.getRed()) / 5),
                    255 - ((255 - c.getGreen()) / 5),
                    255 - ((255 - c.getBlue()) / 5));
        }
        PieDataset dataset = plot.getDataset();
        int sectionCount = dataset.getItemCount();
        if (colors.length < sectionCount) {  // we need to interpolate some
                                             // colors
            for (int i = 0; i < colors.length - 1; i++) {
                Color c1 = colors[i];
                Color c2 = colors[i + 1];
                int s1 = sectionIndexForColor(i, colors.length, sectionCount);
                int s2 = sectionIndexForColor(i + 1, colors.length,
                        sectionCount);
                for (int s = s1; s <= s2; s++) {
                    Color c = interpolatedColor(c1, c2, s - s1, s2 - s1);
                    plot.setSectionPaint(dataset.getKey(s), c);
                }
            }
        }
        else {
            for (int i = 0; i < sectionCount; i++) {
                plot.setSectionPaint(dataset.getKey(i), colors[i]);
            }
        }
    }


    private static int sectionIndexForColor(int item, int itemCount,
            int sectionCount) {
        return (int) (Math.min(sectionCount - 1, item * sectionCount
                / (itemCount - 1.0)));
    }

    private static Color interpolatedColor(Color c1, Color c2, int index,
            int range) {
        if (index == 0) {
            return c1;
        }
        if (index == range) {
            return c2;
        }
        double fraction = (index + 0.0) / range;
        int r1 = c1.getRed(); int g1 = c1.getGreen(); int b1 = c1.getBlue();
        int r2 = c2.getRed(); int g2 = c2.getGreen(); int b2 = c2.getBlue();
        return new Color((int) (r1 + fraction * (r2 - r1)),
                (int) (g1 + fraction * (g2 - g1)),
                (int) (b1 + fraction * (b2 - b1)));
    }

    /**
     * Parses a string containing a sequence of line style specifications,
     * returning an array of <code>Stroke</code> objects.
     *
     * @param text  the line styles.
     *
     * @return The strokes representing the line styles.
     */
    private static Stroke[] parseLineStyles(String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Null 'text' argument (in parseStrokes(String)).");
        }
        String[] codes = breakString(text, '|');
        Stroke[] result = new BasicStroke[codes.length];
        for (int i = 0; i < codes.length; i++) {
            float width = 1.0f;
            float lineRun = 1.0f;
            float gapRun = 0.0f;
            int pos = codes[i].indexOf(',');
            if (pos == -1) {
                width = Float.parseFloat(codes[i]);
            }
            else {
                String widthStr = codes[i].substring(0, pos);
                width = Float.parseFloat(widthStr);
                String remaining = codes[i].substring(pos + 1);
                pos = remaining.indexOf(',');
                if (pos == -1) {
                    lineRun = Float.parseFloat(remaining);
                    gapRun = lineRun;
                }
                else {
                    String s1 = remaining.substring(0, pos);
                    lineRun = Float.parseFloat(s1);
                    String s2 = remaining.substring(pos + 1);
                    gapRun = Float.parseFloat(s2);
                }
            }
            if (gapRun <= 0.0f) {
                result[i] = new BasicStroke(width, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND);
            }
            else {
                result[i] = new BasicStroke(width, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND, 5.0f,
                        new float[] {lineRun, gapRun}, 0.0f);
            }
        }
        return result;
    }

    /**
     * Adds a marker to the chart.
     *
     * @param markerStr  the string encoding of the marker.
     * @param chart  the chart to apply the marker to.
     */
    private static void addMarker(String markerStr, JFreeChart chart) {
        String[] args = breakString(markerStr, ',');
        Plot p = chart.getPlot();
        Color c = parseColor(args[1]);
        if ("r".equals(args[0])) {
            // add a range marker
            float f0 = Float.parseFloat(args[3]);
            float f1 = Float.parseFloat(args[4]);
            if (p instanceof CategoryPlot) {
                CategoryPlot cp = (CategoryPlot) p;
                Range yRange = cp.getRangeAxis().getRange();
                double r0 = yRange.getLowerBound();
                double rl = yRange.getLength();
                IntervalMarker m = new IntervalMarker(r0 + Math.min(f0, f1)
                        * rl, r0 + Math.max(f0, f1) * rl);
                m.setPaint(c);
                cp.addRangeMarker(m, Layer.BACKGROUND);
            }
            else if (p instanceof XYPlot) {
                XYPlot cp = (XYPlot) p;
                Range yRange = cp.getRangeAxis().getRange();
                double r0 = yRange.getLowerBound();
                double rl = yRange.getLength();
                IntervalMarker m = new IntervalMarker(r0 + Math.min(f0, f1)
                        * rl, r0 + Math.max(f0, f1) * rl);
                m.setPaint(c);
                cp.addRangeMarker(m, Layer.BACKGROUND);
            }

        }
        else if ("R".equals(args[0])) {
            // add a domain marker
            float f0 = Float.parseFloat(args[3]);
            float f1 = Float.parseFloat(args[4]);
            if (p instanceof XYPlot) {
                XYPlot xyp = (XYPlot) p;
                Range yRange = xyp.getRangeAxis().getRange();
                double r0 = yRange.getLowerBound();
                double rl = yRange.getLength();
                IntervalMarker m = new IntervalMarker(r0 + Math.min(f0, f1)
                        * rl, r0 + Math.max(f0, f1) * rl);
                m.setPaint(c);
                xyp.addDomainMarker(m, Layer.BACKGROUND);
            }
        }
    }

    /**
     * Process the string which contains an encoding for the chart background
     * fill, and apply it to the chart.
     *
     * @param spec  the fill specification.
     * @param chart  the chart.
     */
    private static void processFillSpec(String spec, JFreeChart chart) {
        if (spec.startsWith("bg")) {
            // do the chart background
            spec = spec.substring(3);
            if (spec.startsWith("s")) {
                spec = spec.substring(2);
                Color c = parseColor(spec);
                chart.setBackgroundPaint(c);
            }
            else if (spec.startsWith("lg")) {
                spec = spec.substring(3);
                String[] args = breakString(spec, ',');
                int angle = Integer.parseInt(args[0]);
                Color c0 = parseColor(args[1]);
                float f0 = Float.parseFloat(args[2]);
                Color c1 = parseColor(args[3]);
                float f1 = Float.parseFloat(args[4]);
                if (chart.getPlot() instanceof GXYPlot) {
                    GXYPlot gxyplot = (GXYPlot) chart.getPlot();
                    gxyplot.setF0(f0);
                    gxyplot.setF1(f1);
                    gxyplot.setAngle(Math.PI / 180.0 * angle);
                    gxyplot.setBackgroundPaint(new GradientPaint(0.0f, 0.0f,
                            c0, 0.0f, 0.0f, c1));
                }
                else if (chart.getPlot() instanceof GCategoryPlot) {
                    GCategoryPlot gcplot = (GCategoryPlot) chart.getPlot();
                    gcplot.setF0(f0);
                    gcplot.setF1(f1);
                    gcplot.setAngle(Math.PI / 180.0 * angle);
                    gcplot.setBackgroundPaint(new GradientPaint(0.0f, 0.0f,
                            c0, 0.0f, 0.0f, c1));
                }
            }
        }
        else if (spec.startsWith("c")) {
            // do the plot background
            spec = spec.substring(2);
            if (spec.startsWith("s")) {
                spec = spec.substring(2);
                Color c = parseColor(spec);
                chart.getPlot().setBackgroundPaint(c);
            }
            else if (spec.startsWith("lg")) {
                spec = spec.substring(3);
                String[] args = breakString(spec, ',');
                int angle = Integer.parseInt(args[0]);
                Color c0 = parseColor(args[1]);
                float f0 = Float.parseFloat(args[2]);
                Color c1 = parseColor(args[3]);
                float f1 = Float.parseFloat(args[4]);
                if (chart.getPlot() instanceof GXYPlot) {
                    GXYPlot gxyplot= (GXYPlot) chart.getPlot();
                    gxyplot.setF0(f0);
                    gxyplot.setF1(f1);
                    gxyplot.setAngle(Math.PI / 180.0 * angle);
                    gxyplot.setBackgroundPaint(new GradientPaint(0.0f, 0.0f,
                            c0, 0.0f, 0.0f, c1));
                }
                else if (chart.getPlot() instanceof GCategoryPlot) {
                    GCategoryPlot gcplot = (GCategoryPlot) chart.getPlot();
                    gcplot.setF0(f0);
                    gcplot.setF1(f1);
                    gcplot.setAngle(Math.PI / 180.0 * angle);
                    gcplot.setBackgroundPaint(new GradientPaint(0.0f, 0.0f,
                            c0, 0.0f, 0.0f, c1));
                }
            }
            else {
                throw new RuntimeException(
                        "'c' background fill not implemented yet.");
            }
        }
        else {
            throw new RuntimeException("Bad fill specification: " + spec);
        }
    }

    /**
     * Process a string which specifies grid line steps and line segment sizes.
     *
     * @param spec  the grid lines specification.
     * @param chart  the chart.
     */
    private static void processGridLinesSpec(String spec, JFreeChart chart) {
        String[] parts = breakString(spec, ',');

        double xAxisStepSize = 0.0;
        double yAxisStepSize = 0.0;
        float lineSegLength = 3f;
        float blankSegLength = 6f;

        if (parts.length > 0) {
            try {
                xAxisStepSize = Double.parseDouble(parts[0]);
            }
            catch (NumberFormatException e) {

            }
        }
        if (parts.length > 1) {
            try {
                yAxisStepSize = Double.parseDouble(parts[1]);
            }
            catch (NumberFormatException e) {

            }
        }
        if (parts.length > 2) {
            try {
                lineSegLength = Float.parseFloat(parts[2]) * 0.85f;
            }
            catch (NumberFormatException e) {

            }
        }
        if (parts.length > 3) {
            try {
                blankSegLength = Float.parseFloat(parts[3]) * 0.85f;
            }
            catch (NumberFormatException e) {

            }
        }

        if (lineSegLength == 0 && blankSegLength == 0) {
            lineSegLength = 1f;
        }

        if (lineSegLength > 0) {
            Stroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_ROUND, 10f,
                    new float[] {lineSegLength, blankSegLength}, 0);

            Plot p = chart.getPlot();
            if (p instanceof CategoryPlot) {
                GCategoryPlot plot = (GCategoryPlot) p;
                plot.setDomainGridlinesVisible(true);
                plot.setRangeGridlinesVisible(true);
                plot.setDomainGridlineStroke(stroke);
                plot.setRangeGridlineStroke(stroke);
                plot.setXAxisStepSize(xAxisStepSize / 100.0);
                plot.setYAxisStepSize(yAxisStepSize / 100.0);
            }
            else if (p instanceof XYPlot) {
                GXYPlot plot = (GXYPlot) p;
                plot.setDomainGridlinesVisible(true);
                plot.setRangeGridlinesVisible(true);
                plot.setDomainGridlineStroke(stroke);
                plot.setRangeGridlineStroke(stroke);
                plot.setXAxisStepSize(xAxisStepSize / 100.0);
                plot.setYAxisStepSize(yAxisStepSize / 100.0);
            }
        }
    }

    /**
     * Parses a string containing the chart dimensions.
     *
     * @param text  the text (<code>null</code> not permitted).
     *
     * @return The chart dimensions.
     */
    public static Dimension parseDimensions(String text) {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Null 'text' argument (in parseChartDimensions(String)).");
        }
        int splitIndex = text.indexOf('x');
        String xStr = text.substring(0, splitIndex);
        String yStr = text.substring(splitIndex + 1);
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        return new Dimension(x, y);
    }

    /**
     * Parses a string containing a category label orientation
     *
     * @param orientation the text
     *
     * @return the CategoryLabelPositions object
     */
    private static CategoryLabelPositions getCategoryLabelPositions(
            String orientation) {
        if (orientation.equals("s")) {
            return CategoryLabelPositions.STANDARD;
        }
        else if (orientation.equals("u")) {
            return CategoryLabelPositions.UP_90;
        }
        else if (orientation.equals("d")) {
            return CategoryLabelPositions.DOWN_90;
        }
        else {
            throw new IllegalArgumentException("Axis label orientation '"
                    + orientation + "' not supported.");
        }
    }

}
