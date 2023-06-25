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

import org.h2.tools.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class LinkedTableTest {

    public static final Logger LOGGER = Logger.getLogger(LinkedTableTest.class.getName());
    public static final String H2_VERSION = "2.0.201";
    public static final Properties PROPERTIES = new Properties();
    public static String dbFileUriStr;
    private static Server server = null;
    private static String connectionStr;

    @BeforeEach
    public void setUp() throws Exception {
        PROPERTIES.setProperty("user", "sa");
        PROPERTIES.setProperty("password", "");

        File file = File.createTempFile("h2_" + H2_VERSION + "_", ".lck");
        file.deleteOnExit();

        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        dbFileUriStr = file.getParentFile().toURI().toASCIIString() + fileName;
        connectionStr =
                "jdbc:h2:tcp://localhost/"
                        + dbFileUriStr
                        + ";IFEXISTS=FALSE;COMPRESS=TRUE;PAGE_SIZE=128;DB_CLOSE_DELAY=0;AUTO_RECONNECT=FALSE;CACHE_SIZE=8192;MODE=Oracle;LOCK_TIMEOUT=10";

        try {
            (new Socket("localhost", 9092)).close();
            LOGGER.info("Found open port 9092, assume running H2 Database instance.");
        } catch (IOException ex) {
            LOGGER.info("Could not open port 9092. Will try to start H2 Database instance.");
            server = Server.createTcpServer("-ifNotExists");
            server.start();
        }

        try (Connection con = DriverManager.getConnection(connectionStr, PROPERTIES);
                Statement st = con.createStatement()) {
            st.executeUpdate("CREATE SCHEMA common;");
            st.executeUpdate(
                    "CREATE TABLE common.test (id VARCHAR(40) NOT NULL PRIMARY KEY, field1 VARCHAR(40))");
            for (int i = 0; i < 10000; i++) {
                st.executeUpdate("INSERT INTO common.test VALUES ('" + i + "', 'test')");
            }

            st.executeUpdate("SHUTDOWN");
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.shutdown();
        }

        File temp = new File(H2MigrationTool.getTempFolderName());
        for (String s : Collections.singletonList(dbFileUriStr)) {
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
        }
    }

    @Test
    public void createTableTest() throws Exception {
        String linkedConnectionStr =
                "jdbc:h2:tcp://localhost/"
                        + dbFileUriStr
                        + "_1;IFEXISTS=FALSE;COMPRESS=TRUE;PAGE_SIZE=128;DB_CLOSE_DELAY=0;AUTO_RECONNECT=FALSE;CACHE_SIZE=8192;MODE=Oracle;LOCK_TIMEOUT=10";

        try (Connection con = DriverManager.getConnection(linkedConnectionStr, PROPERTIES);
                Statement st = con.createStatement()) {
            st.executeUpdate("CREATE SCHEMA common;");
            st.executeUpdate(
                    "CREATE TABLE common.test (id VARCHAR(40) NOT NULL PRIMARY KEY, field1 VARCHAR(40))");

            st.executeUpdate(
                    "CREATE LINKED TABLE \n"
                            + "IF NOT EXISTS \n"
                            + "common.test_linked(\n"
                            + "    'org.h2.Driver'\n"
                            + "    , '"
                            + connectionStr
                            + "'\n"
                            + "    , 'SA'\n"
                            + "    , null\n"
                            + "    , 'COMMON'\n"
                            + "    , 'TEST') \n"
                            + "READONLY;");

            int r = st.executeUpdate("INSERT INTO common.test SELECT * FROM common.test_linked ");

            LOGGER.info(r + " records inserted.");
            st.executeUpdate("SHUTDOWN");
        }
        Thread.sleep(5000);

        try (Connection con = DriverManager.getConnection(linkedConnectionStr, PROPERTIES);
                Statement st = con.createStatement()) {
            st.executeUpdate(
                    "CREATE LINKED TABLE \n"
                            + "IF NOT EXISTS \n"
                            + "common.test_linked(\n"
                            + "    'org.h2.Driver'\n"
                            + "    , '"
                            + connectionStr
                            + "'\n"
                            + "    , 'SA'\n"
                            + "    , null\n"
                            + "    , 'COMMON'\n"
                            + "    , 'TEST') \n"
                            + "READONLY;");

            int r =
                    st.executeUpdate(
                            "MERGE INTO common.test a USING common.test_linked b ON (a.id=b.id) WHEN MATCHED THEN UPDATE SET a.field1=b.field1 WHEN NOT MATCHED THEN INSERT VALUES (b.id, b.field1)");

            LOGGER.info(r + " records merged.");

            st.executeUpdate("SHUTDOWN");
        }
        Thread.sleep(5000);

        try (Connection con = DriverManager.getConnection(linkedConnectionStr, PROPERTIES);
                Statement st = con.createStatement()) {
            st.executeUpdate(
                    "CREATE LINKED TABLE \n"
                            + "IF NOT EXISTS \n"
                            + "common.test_linked(\n"
                            + "    'org.h2.Driver'\n"
                            + "    , '"
                            + connectionStr
                            + "'\n"
                            + "    , 'SA'\n"
                            + "    , null\n"
                            + "    , 'PUPBLIC'\n"
                            + "    , 'COMMON') \n"
                            + "READONLY;");

            int r =
                    st.executeUpdate(
                            "MERGE INTO common.test a USING common.test_linked b ON (a.id=b.id) WHEN MATCHED THEN UPDATE SET a.field1=b.field1 WHEN NOT MATCHED THEN INSERT VALUES (b.id, b.field1)");

            LOGGER.info(r + " records merged.");

            st.executeUpdate("SHUTDOWN");
        }
        Thread.sleep(5000);
    }
}
