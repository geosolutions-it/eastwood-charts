*******************************************
  EASTWOOD CHART
*******************************************


(C)opyright 2007-2009, by Object Refinery Limited and Contributors.


-----------------
1.  INTRODUCTION
-----------------
Eastwood is a free Java servlet for displaying charts.  Eastwood emulates most 
of the features in the Google Chart API, and also provides some custom 
extensions.  It runs on the Java 2 Platform (JDK 1.4 or later), 
and uses JFreeChart (http://www.jfree.org/jfreechart/) to render the charts.

Eastwood is free software, licensed under the terms of the GNU Lesser General
Public Licence version 2.1 or later (the same licence as JFreeChart).  A copy 
of the licence is included in the distribution.

Please note that Eastwood Chart Servlet is distributed WITHOUT ANY WARRANTY;
without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.  Please refer to the licence for details.



-----------------
2.  DOCUMENTATION
-----------------
See the file manual.odt in the distribution.


-----------------
3.  DEPENDENCIES
-----------------
Eastwood has the following dependencies:

(a)  JDK 1.4.2 or higher.

(b)  JFreeChart - version 1.0.8 or later (the 1.0.13 jar file is included in the
distribution).  You can obtain the complete source code for JFreeChart from:

    http://www.jfree.org/jfreechart/
    
JFreeChart is licensed under the terms of the GNU Lesser General Public Licence.

(c)  JCommon - version 1.0.0 or later (the 1.0.16 jar file is included in the
distribution).  You can obtain the complete source code for JCommon from:

    http://www.jfree.org/jcommon/

JCommon is licensed under the terms of the GNU Lesser General Public Licence.

(d)  servlet.jar - for compilation only.  The Eastwood distribution includes 
the servlet.jar file distributed with Tomcat 4.1.31.  Applicable license 
terms are published at:  

    http://java.sun.com/products/servlet/LICENSE   


---------------
4.  MAVEN BUILD
---------------
A Maven project descriptor (pom.xml) is included in the distribution.
Quick cheatsheet:
    - build and test
      mvn clean install

----------------
5.  CONTRIBUTING
----------------
We welcome external contributions to the project.  You can retain the 
copyright for contributions, or assign copyright to Object Refinery Limited, 
at your option.

Thanks to the following contributors:  Andrea Aime, Bill Bejeck, Ulf Dittmer,
Ingo Kegel and Niklas Therning.

---------------
6.  LIMITATIONS
---------------
Eastwood has the following limitations:

    - Maps are not implemented;
    - radar charts are not implemented;
    - Venn diagrams are not implemented;
    - the Google-o-meter chart is not implemented;
    - QR codes are not implemented;
    - the 'chbh' attribute is ignored, and bar widths are automatically
      calculated to fit the available space (this is actually easier than
      trying to figure out the right values for the 'chbh' attribute, which
      looks like a bit of a kludge to me);
    - stripe fills are not yet supported;
    - area fills are not yet supported;
    - chart markers are not fully supported yet.
      
If there are other items that you think should be listed here,
please post a bug report.

Current Maintainers:
Andrea Aime (andrea.aime@geo-solutions.it)
Simone Giannecchini (simone.giannecchini@geo-solutions.it)

Original Authour:
Dave Gilbert (david.gilbert@object-refinery.com)
Eastwood Project Leader
