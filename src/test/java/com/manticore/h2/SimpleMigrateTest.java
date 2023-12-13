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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class SimpleMigrateTest {

    public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());

    public static final String[] H2_VERSIONS =
            new String[] {
                    "1.3.176", "1.4.199", "1.4.200", "2.0.201"
            };

    /*
     * @todo: move DDLs and Test SQLs into a text file to read from
     */
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

        for (String versionStr : H2_VERSIONS) {
            File file = File.createTempFile("h2_" + versionStr + "_", ".lck");
            file.deleteOnExit();

            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.length() - 4);

            dbFileUriStr.add(file.getParentFile().toURI().toASCIIString() + fileName);

            Driver driver = H2MigrationTool.loadDriver(versionStr);

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
    public void autoConvertTo201LatestTest() throws Exception {
        String version = "2.0.201";
        H2MigrationTool tool = new H2MigrationTool();
        H2MigrationTool.readDriverRecords();

        for (String s : dbFileUriStr) {
            URI h2FileUri = new URI(s + ".mv.db");
            File h2File = new File(h2FileUri);

            if (h2File.exists() && h2File.canWrite()) {
                LOGGER.info(
                        "Will Auto-Convert H2 database file " +
                                h2File.getCanonicalPath() +
                                " to Version " + version);

                tool.migrateAuto(version, h2File.getCanonicalPath(), "SA", "", "", "", "", true,
                        true);
            }
        }
    }

    @Test
    public void autoConvertToLatestTest() throws Exception {

        H2MigrationTool tool = new H2MigrationTool();
        H2MigrationTool.readDriverRecords("");

        for (String s : dbFileUriStr) {
            URI h2FileUri = new URI(s + ".mv.db");
            File h2File = new File(h2FileUri);

            if (h2File.exists() && h2File.canWrite()) {
                LOGGER.info(
                        "Will Auto-Convert H2 database file " +
                                h2File.getCanonicalPath() +
                                " to Latest Available H2 version");

                tool.migrateAuto(h2File.getCanonicalPath());
            }
        }
    }

    @Test
    public void databaseScriptResultTest() throws Exception {
        H2MigrationTool tool = new H2MigrationTool();
        H2MigrationTool.readDriverRecords("");
        String version = "2.0.201";
        String s = dbFileUriStr.get(dbFileUriStr.size()-1);
        URI h2FileUri = new URI(s);
        File h2File = new File(h2FileUri);

        DriverRecord driverRecord = tool.getDriverRecord(version);
        final H2MigrationTool.ScriptResult scripResult = tool.writeScript(
                driverRecord,
                h2File.getCanonicalPath(),
                "SA",
                "",
                null,
                "",
                ""
        );

        Assertions.assertEquals("", scripResult.scriptFileName);


//        H2MigrationTool.ScriptResult scriptResult =
//                createFromScript(driverRecordTo, databaseName, user, password,
//                                 modifiedScriptFileName, options, commands, force, null);
    }
}
