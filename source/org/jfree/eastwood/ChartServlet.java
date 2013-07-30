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
 * -----------------
 * ChartServlet.java
 * -----------------
 * (C) Copyright 2007-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     Niklas Therning;
 *                   Ingo Kegel (patch 2124394);
 *
 * Changes
 * -------
 * 13-Dec-2007 : Version 1 (DG);
 * 09-Jun-2008 : Factored out chart construction code into a new ChartEngine
 *               class (DG);
 * 10-Jun-2008 : Values in parameter map are String[] not String (DG);
 * 26-Jun-2008 : Incorporated patch 2001783 by Niklas Therning (DG);
 * 02-Jul-2008 : Added init() method which reads the font configured by the
 *               user through init-params (NT);
 * 18-Jul-2008 : Replaced call to createFont() that requires JRE1.5, so we can
 *               run on JRE1.4 too (DG);
 * 13-May-2009 : Added servlet init param for maximum image size (see patch
 *               2124394 by Ingo Kegel) (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

/**
 * A servlet that returns a chart as a PNG image.  The servlet is designed to
 * accept arguments formatted according to the Google Chart API.
 * <p>
 * This servlet accepts the following <code>init-param</code>s:
 * <table border="1">
 *  <tr>
 *   <td><code>fontSize</code></td>
 *   <td>The font size in points to be used for all texts. Titles will be
 *       rendered with a slightly larger size if the title font size hasn't
 *       been specified explicitly in the URL. The default is 12.</td>
 *  </tr>
 *  <tr>
 *   <td><code>fontResource</code></td>
 *   <td>Servlet context relative path to a TrueType font file containing the
 *       font which should be used to render texts.
 *       E.g.: <code>/WEB-INF/arial.ttf</code></td>
 *  </tr>
 *  <tr>
 *   <td><code>fontFile</code></td>
 *   <td>File system path to a TrueType font file containing the font which
 *       should be used to render texts.</td>
 *  </tr>
 *  <tr>
 *   <td><code>font</code></td>
 *   <td>The name of the font to use, e.g. <code>Arial</code>,
 *       <code>Verdana</code>. This font must be available to the JVM.</td>
 *  </tr>
 *  <tr>
 *   <td><code>maximumImageSize</code></td>
 *   <td>The maximum image size in pixels (width * height).  The default
 *       value is 360,000.</td>
 *  </tr>
 * </table>
 * The default font if none of the font related parameters has been specified
 * is <code>Dialog</code>, 12pt.
 */
public class ChartServlet extends HttpServlet {

    private Font font;

    /**
     * The maximum image size in pixels (to stop someone requesting an
     * insanely large image).  This is configurable via init params.
     */
    private int maximumImageSize = 360000;

    /**
     * Default constructor.
     */
    public ChartServlet() {
        // nothing required
    }

    /**
     * Initialise the servlet.
     *
     * @param config  the config.
     *
     * @throws ServletException
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        String fontSizeParam = config.getInitParameter("fontSize");
        String fontResourceParam = config.getInitParameter("fontResource");
        String fontFileParam = config.getInitParameter("fontFile");
        String fontParam = config.getInitParameter("font");
        String maximumImageSizeParam = config.getInitParameter(
                "maximumImageSize");

        float fontSize = 12f;
        if (fontSizeParam != null) {
            fontSize = Integer.parseInt(fontSizeParam);
        }

        if (fontResourceParam != null) {
            ServletContext ctx = config.getServletContext();
            this.font = readFontResource(ctx, fontResourceParam, fontSize);
        }
        else if (fontFileParam != null) {
            this.font = readFontFile(fontFileParam, fontSize);
        }
        else if (fontParam != null) {
            this.font = new Font(fontParam, Font.PLAIN, (int) fontSize);
        }
        else {
            this.font = new Font("Dialog", Font.PLAIN, (int) fontSize);
        }

        if (maximumImageSizeParam != null) {
            this.maximumImageSize = Integer.parseInt(maximumImageSizeParam);
        }
    }

    private Font readFontResource(ServletContext ctx, String fontResourceParam,
            float fontSize) throws ServletException {

        if (!fontResourceParam.startsWith("/")) {
            fontResourceParam = "/" + fontResourceParam;
        }
        InputStream is = ctx.getResourceAsStream(fontResourceParam);
        if (is == null) {
            throw new ServletException("Font resource '" + fontResourceParam
                    + "' not found");
        }
        try {
            return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(fontSize);
        }
        catch (FontFormatException e) {
            throw new ServletException("Font resource '" + fontResourceParam
                    + "' is not a truetype font", e);
        }
        catch (IOException e) {
            throw new ServletException("I/O error when reading font resource '"
                    + fontResourceParam + "'", e);
        }
        finally {
            try { is.close(); } catch (IOException e) {}
        }
    }

    private Font readFontFile(String fontFileParam, float fontSize)
            throws ServletException {

        File f = new File(fontFileParam);
        if (!f.exists()) {
            throw new ServletException("Font file '" + f + "' doesn't exist");
        }
        try {
        	FileInputStream fis = new FileInputStream(f);
        	Font font = Font.createFont(Font.TRUETYPE_FONT, fis).deriveFont(
        			fontSize);
        	fis.close();
        	return font;
        }
        catch (FontFormatException e) {
            throw new ServletException("Font file '" + f
                    + "' is not a truetype font", e);
        }
        catch (IOException e) {
            throw new ServletException("I/O error when reading font file '"
                    + f + "'", e);
        }
    }

    /**
     * Process a GET request.
     *
     * @param request  the request.
     * @param response  the response.
     *
     * @throws ServletException if there is a servlet related problem.
     * @throws IOException if there is an I/O problem.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

        OutputStream out = response.getOutputStream();

        try {
            Map params = Parameters.parseQueryString(request.getQueryString());
            JFreeChart chart = ChartEngine.buildChart(params, this.font);

            if (chart != null) {
                response.setContentType("image/png");

                // *** CHART SIZE ***
                String[] size = (String[]) params.get("chs");
                int[] dims = new int[2];
                if (size != null) {
                    dims = parseChartDimensions(size[0]);
                }
                else {
                    dims = new int[] {200, 125};
                }

                ChartUtilities.writeChartAsPNG(out, chart, dims[0], dims[1]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            out.close();
        }

    }

    /**
     * Parses a string containing the chart dimensions.
     *
     * @param text  the text (<code>null</code> not permitted).
     *
     * @return The chart dimensions.
     *
     * @throws ServletException
     */
    private int[] parseChartDimensions(String text) throws ServletException {
        if (text == null) {
            throw new IllegalArgumentException(
                    "Null 'text' argument (in parseChartDimensions(String)).");
        }
        int[] result = new int[2];
        int splitIndex = text.indexOf('x');
        String xStr = text.substring(0, splitIndex);
        String yStr = text.substring(splitIndex + 1);
        int x = Integer.parseInt(xStr);
        int y = Integer.parseInt(yStr);
        if (x <= 1000 && y <= 1000 && x * y < this.maximumImageSize) {
            result[0] = x; result[1] = y;
        }
        else {
            throw new ServletException("Invalid chart dimensions: " + xStr
                    + ", " + yStr);
        }
        return result;
    }

}

