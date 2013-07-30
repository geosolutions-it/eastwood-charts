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
 * ---------------
 * Parameters.java
 * ---------------
 * (C) Copyright 2008, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributors:     Niklas Therning;
 *
 * Changes
 * -------
 * 09-Jun-2008 : Version 1 (DG);
 * 26-Jun-2008 : Integrated patch 2001783 by Niklas Therning (DG);
 *
 */

package org.jfree.eastwood;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class that parses a string into a <code>Map</code> containing
 * the parameters for a chart.
 */
public class Parameters {

    /**
     * Parses the query string part of a URL. We need this since we shouldn't
     * try to decode %-characters which aren't proper %xx sequences (this is
     * the way the Google Chart API does it). Servlet containers like Tomcat
     * and Jetty don't work that way. Also, our custom parser always uses
     * UTF-8 to do byte -> char conversion, regardless of what encoding the
     * servlet environment has been configured to use.
     *
     * @param qs  the string to be decoded.
     *
     * @return The decoded string.
     * @throws UnsupportedEncodingException if UTF-8 isn't supported by the
     *         JVM.
     */
    public static Map parseQueryString(String qs)
            throws UnsupportedEncodingException {
    	if (qs == null) {
            return Collections.EMPTY_MAP;
    	}

    	Map params = new HashMap();
    	String[] parts = qs.split("&");
    	for (int i = 0; i < parts.length; i++) {
    		String part = parts[i];
    		int idx = part.indexOf('=');
    		if (idx == -1) {
    			continue;
    		}
    		String name = urlDecode(part.substring(0, idx));
    		String value = urlDecode(part.substring(idx + 1));
    		if (!params.containsKey(name)) {
        		params.put(name, new String[] {value});
    		} else {
    			String[] old = (String[]) params.get(name);
    			String[] values = new String[old.length + 1];
    			System.arraycopy(old, 0, values, 0, old.length);
    			values[values.length - 1] = value;
    			params.put(name, values);
    		}
    	}
    	return params;
    }

    /**
     * Decodes %xx sequences and + characters in the specified string. UTF-8
     * is used for byte .> char conversions.
     *
     * @param s the string to be decoded.
     * @return the decoded string.
     * @throws UnsupportedEncodingException if UTF-8 isn't supported by the
     *         JVM.
     */
    public static String urlDecode(String s)
            throws UnsupportedEncodingException {
    	Pattern pattern = Pattern.compile("(%[a-fA-F0-9]{2})+|\\+");
    	Matcher m = pattern.matcher(s);
    	int start = 0;
    	StringBuffer sb = new StringBuffer();
    	while (m.find(start)) {
    		if (start < m.start()) {
    			sb.append(s.substring(start, m.start()));
    		}
    		if ("+".equals(m.group())) {
    			sb.append(' ');
    		} else {
    			String hex = m.group();
    			byte[] bytes = new byte[hex.length() / 3];
    			for (int i = 0; i < bytes.length; i++) {
    				int b = Integer.parseInt(hex.substring(i * 3 + 1,
    						i * 3 + 3), 16);
    				bytes[i] = (byte) b;
    			}
    			sb.append(new String(bytes, "UTF8"));
    		}
    		start = m.end();
    	}
    	if (start < s.length()) {
    		sb.append(s.substring(start));
    	}
    	return sb.toString();
    }

}

