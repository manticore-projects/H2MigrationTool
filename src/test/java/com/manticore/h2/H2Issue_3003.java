/*
 * Copyright (C) 2020 Andreas Reichel<andreas@manticore-projects.com>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package com.manticore.h2;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class H2Issue_3003 {

    public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());
    public static final String H2_VERSION = "2.0.201";
    public static final String DDL_STR = "CREATE SCHEMA common;\n" +
            "CREATE SEQUENCE common.currency_ref_seq START WITH 0;\n" +
            "CREATE TABLE common.currency\n" +
            "  (\n" +
            "     id_currency   VARCHAR(3) PRIMARY KEY NOT NULL\n" +
            "     , ref_currency NUMBER(3) NOT NULL \n" +
            "     , description VARCHAR(255)\n" +
            "  );\n" +
            "ALTER TABLE common.currency ADD UNIQUE (ref_currency);";
    public static Connection con = null;
    public static String dbFileUriStr;

    @BeforeAll
    public static void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");

        File file = File.createTempFile("h2_" + H2_VERSION + "_", ".lck");
        file.deleteOnExit();

        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        dbFileUriStr = file.getParentFile().toURI().toASCIIString() + fileName;

        Driver driver = H2MigrationTool.loadDriver("", H2_VERSION);

        con = driver.connect("jdbc:h2:" + dbFileUriStr, properties);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        URI h2FileUri = new URI(dbFileUriStr + ".mv.db");
        File h2File = new File(h2FileUri);
        if (h2File.exists() && h2File.canWrite()) {
            LOGGER.info("Delete H2 database file " + h2File.getCanonicalPath());
            h2File.delete();
        }
    }

    @Test
    public void createTableTest() throws SQLException {
        try (Statement st = con.createStatement()) {
            for (String command : DDL_STR.split(";")) {
                st.executeUpdate(command);
            }
        }
    }

}
