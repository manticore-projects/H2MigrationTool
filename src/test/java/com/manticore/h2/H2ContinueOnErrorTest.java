/*
 * H2MigrationTool is a Graphical User Application for Recovering and Migration H2 Database files.
 *
 * Copyright (C) 2020-2025 Andreas Reichel<andreas@manticore-projects.com>
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class H2ContinueOnErrorTest {
    public final Logger LOGGER = Logger.getLogger(H2ContinueOnErrorTest.class.getName());

    public final static String VERSION_STR ="2.4.240";

    public static final String DDL_STR =
            "drop table IF EXISTS B cascade;\n" +
            "drop table IF EXISTS A cascade;\n" +
            "\n" +
            "CREATE TABLE a\n" +
            "  (\n" +
            "     field1 varchar(1) UNIQUE \n" +
            "  );\n" +
            "\n" +
            "CREATE TABLE b\n" +
            "  (\n" +
            "     field2 varchar(1)\n" +
            "  );\n" +
            "\n" +
            "ALTER TABLE b\n" +
            "  ADD FOREIGN KEY (field2) REFERENCES a(field1);";

    public static ArrayList<String> dbFileUriStr = new ArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");

        File file = File.createTempFile("h2_" + VERSION_STR + "_", ".lck");
        file.deleteOnExit();

        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        dbFileUriStr.add(file.getParentFile().toURI().toASCIIString() + fileName);

        Driver driver = H2MigrationTool.loadDriver(VERSION_STR);

        try (Connection con =
                     driver.connect("jdbc:h2:" + dbFileUriStr.get(dbFileUriStr.size() - 1),
                             properties);
             Statement st = con.createStatement()) {

            for (String sqlStr : DDL_STR.split(";")) {
                st.executeUpdate(sqlStr);
            }
        } finally {
            H2MigrationTool.unloadDriver(driver);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        File temp = new File(H2MigrationTool.getTempFolderName());
        for (String s : new ArrayList<>(dbFileUriStr)) {
            Pattern pattern =
                    Pattern.compile(new File(new URI(s)).getName() + ".*\\.(mv.db|sql|zip|gzip)");
            FilenameFilter filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return pattern.matcher(name).matches();
                }
            };
            File[] files = temp.listFiles(filenameFilter);
            if (files != null) {
                for (File f : files) {
                    LOGGER.info("Delete file " + f.getCanonicalPath());
                    boolean delete = f.delete();
                }
            }
            dbFileUriStr.remove(s);
        }
    }

    @Test
    void testContinueOnErrorWithoutCompression() {
        String url = "";

    }
}
