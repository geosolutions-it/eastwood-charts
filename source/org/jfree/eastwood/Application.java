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
 * Application.java
 * ----------------
 * (C) Copyright 2008, 2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 *
 * Changes
 * -------
 * 10-Jun-2008 : Version 1 (DG);
 * 26-Jun-2008 : Updated for changes to Parameters class (DG);
 * 02-Jul-2008 : Changed call to ChartEngine.buildChart() in renderChart() to
 *               supply the font to be used (NT);
 * 15-Jul-2008 : For convenience, check for and strip out the Google URL in a
 *               chart spec (DG);
 *
 */

package org.jfree.eastwood;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

/**
 * A client application that allows chart expressions to be submitted and
 * drawn directly.  THIS IS A PROOF OF CONCEPT ONLY.  IT NEEDS MORE WORK.
 */
public class Application extends JFrame implements ActionListener {

	/** The entry field for the chart definition. */
	private JComboBox entryField;

	/** The combo box model which stores a history of entries. */
	private DefaultComboBoxModel entryModel;

	/** The page that the chart panel is centred within. */
	private JPanel paper;

	/** The panel used to display the chart. */
	private ChartPanel chartPanel;

	/**
	 * Creates a new application instance.
	 *
	 * @param title  the frame title.
	 */
	public Application(String title) {
		super(title);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.entryModel = new DefaultComboBoxModel();
		setJMenuBar(createMenuBar());
		getContentPane().add(createContent());
	}

	/**
	 * Handles action events from the entry field.
	 *
	 * @param e  the event.
	 */
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if ("EXIT".equals(cmd)) {
			System.exit(0);
		}
		else if ("ABOUT".equals(cmd)) {
		    JOptionPane.showMessageDialog(this, "Eastwood v1.2.0\n"
		    		+ "(C)opyright 2008, 2009, by Object Refinery Limited");
		}
		else {
    		if (e.getSource() == this.entryField) {
			    String text = (String) this.entryField.getSelectedItem();
			    int index = this.entryModel.getIndexOf(text);
			    if (index == -1) {
			    	this.entryModel.addElement(text);
			    }
			    try {
    			    renderChart(text);
			    }
			    catch (Exception ex) {
			    	ex.printStackTrace();
			    }
		    }
		}
	}
	/**
	 * Creates and returns the menu bar for the application.
	 *
	 * @return The menu bar.
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem exit = new JMenuItem("Exit");
		exit.setMnemonic(KeyEvent.VK_X);
		exit.setActionCommand("EXIT");
		exit.addActionListener(this);

		fileMenu.add(exit);
		menuBar.add(fileMenu);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem about = new JMenuItem("About");
		about.setActionCommand("ABOUT");
		about.addActionListener(this);
		helpMenu.add(about);
		menuBar.add(helpMenu);
		return menuBar;
	}

	/**
	 * Creates the content for the main frame.
	 *
	 * @return The content.
	 */
	private JPanel createContent() {
		JPanel content = new JPanel(new BorderLayout());
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		this.entryField = new JComboBox(this.entryModel);
		this.entryField.setEditable(true);
		this.entryField.addActionListener(this);

		northPanel.add(this.entryField);
		content.add(northPanel, BorderLayout.NORTH);
		JPanel centrePanel = new JPanel(new BorderLayout());
		centrePanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
		this.chartPanel = new ChartPanel(null);
		this.chartPanel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createEmptyBorder(4, 4, 4, 4),
				BorderFactory.createLineBorder(Color.black)));
		this.chartPanel.setPreferredSize(new Dimension(500, 300));
		this.chartPanel.setMinimumDrawHeight(0);
		this.chartPanel.setMinimumDrawWidth(0);
		this.chartPanel.setMaximumDrawHeight(Integer.MAX_VALUE);
		this.chartPanel.setMaximumDrawWidth(Integer.MAX_VALUE);
		this.chartPanel.setPopupMenu(null);

		this.paper = new JPanel(new CentreLayout());
		this.paper.add(this.chartPanel);
		JScrollPane scroller = new JScrollPane(this.paper);
		//scroller.setBorder(null);
		centrePanel.add(scroller);
		content.add(centrePanel);
		return content;
	}

	/**
	 * Renders the chart.
	 *
	 * @param paramStr  the parameter string.
	 *
	 * @throws UnsupportedEncodingException
	 */
	protected void renderChart(String paramStr)
	        throws UnsupportedEncodingException {
		if (paramStr.startsWith("http://chart.apis.google.com/chart?")) {
			paramStr = paramStr.substring(35);
		}
		Map params = Parameters.parseQueryString(paramStr);
		JFreeChart chart = ChartEngine.buildChart(params,
		        new Font("Dialog", Font.PLAIN, 12));

		String[] size = (String[]) params.get("chs");
        Dimension d;
        if (size != null) {
            d = ChartEngine.parseDimensions(size[0]);
        }
        else {
            d = new Dimension(200, 125);
        }
        // account for panel insets
        Insets insets = this.chartPanel.getInsets();
        if (insets != null) {
            d.height += insets.top + insets.bottom;
            d.width += insets.left + insets.right;
        }
        this.chartPanel.setPreferredSize(d);
        this.chartPanel.setSize(d);
        this.paper.validate();
		this.chartPanel.setChart(chart);
	}

	/**
	 * Starting point for the application.
	 *
	 * @param args  ignored.
	 */
	public static void main(String[] args) {
		try {
    		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Application app = new Application("Eastwood Charts 1.2.0");
		app.pack();
		app.setVisible(true);
	}
}
