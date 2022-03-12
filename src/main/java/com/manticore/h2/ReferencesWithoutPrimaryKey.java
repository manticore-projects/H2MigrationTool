/*
 * Copyright (C) 2020 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.manticore.h2;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class ReferencesWithoutPrimaryKey {

  public static final Logger LOGGER = Logger.getLogger(ReferencesWithoutPrimaryKey.class.getName());

  public static String dbFileUriStr;

  public static void main(String[] args) {
    try {
      Driver driver = H2MigrationTool.loadDriver("/home/are/Downloads", "1.4.199");

      prepare(driver, "SA", null);

      getDDLStatements(dbFileUriStr, "SA", null);

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    }
  }

  public static void prepare(Driver driver, String user, String password) throws IOException, SQLException {
    Properties properties = new Properties();
    properties.setProperty("user", "sa");
    properties.setProperty("password", "");

    File file = File.createTempFile(ReferencesWithoutPrimaryKey.class.getSimpleName(), ".lck");
    file.deleteOnExit();

    String fileName = file.getName();
    fileName = fileName.substring(0, fileName.length() - 4);

    dbFileUriStr = file.getParentFile().toURI().toASCIIString() + fileName;

    Connection con = null;

    try {
      con = driver.connect("jdbc:h2:" + dbFileUriStr, properties);

      String scriptContent = IOUtils.toString(ClassLoader.getSystemResource("com/manticore/h2/sql/test1.sql"), Charset.
             defaultCharset());

      Statement st = null;
      try {
        st = con.createStatement();
        for (String s : scriptContent.split(";"))
          st.execute(s);
      } catch (Exception ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      } finally {
        if (st != null & !st.isClosed())
          try {
          st.close();
        } catch (Exception ex1) {

        }
      }

    } finally {
      if (con != null && !con.isClosed()) 
        try {
        con.close();
      } catch (Exception ex1) {

      }
    }
  }

  public static List<String> getDDLStatements(String dbFileUriStr, String user, String password) throws Exception {
    ArrayList<String> ddlStatements = new ArrayList<>();

    Properties properties = new Properties();
    properties.setProperty("user", "sa");
    properties.setProperty("password", "");

    Driver driver = H2MigrationTool.loadDriver("/home/are/Downloads", "1.4.199");
    Connection con = null;

    try {
      con = driver.connect("jdbc:h2:" + dbFileUriStr, properties);

      MetaData metaData = new MetaData(con);
      metaData.build();

      Statement st = null;
      try {
        st = con.createStatement();
      } catch (Exception ex) {
        LOGGER.log(Level.SEVERE, null, ex);
      } finally {
        if (st != null & !st.isClosed())
          try {
          st.close();
        } catch (Exception ex1) {

        }
      }

    } finally {
      if (con != null && !con.isClosed()) 
        try {
        con.close();
      } catch (Exception ex1) {

      }
    }

    return ddlStatements;
  }

  public static List<String> getDropStatements() {
    ArrayList<String> dropStatements = new ArrayList<>();

    return dropStatements;
  }
}
