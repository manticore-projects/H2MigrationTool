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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class SimpleParallelDDL {

    public static final Logger LOGGER = Logger.getLogger(SimpleParallelDDL.class.getName());
    public static final String H2_VERSION = "2.0.201";
    public final static Properties PROPERTIES = new Properties();
    public static String dbFileUriStr;
    private static Server server = null;
    private static String connectionStr;

    @BeforeAll
    public static void setUp() throws Exception {
        PROPERTIES.setProperty("user", "sa");
        PROPERTIES.setProperty("password", "");

        File file = File.createTempFile("h2_" + H2_VERSION + "_", ".lck");
        file.deleteOnExit();

        String fileName = file.getName();
        fileName = fileName.substring(0, fileName.length() - 4);

        dbFileUriStr = file.getParentFile().toURI().toASCIIString() + fileName;
        connectionStr =
                "jdbc:h2:tcp://localhost/" + dbFileUriStr
                        + ";IFEXISTS=FALSE;COMPRESS=TRUE;PAGE_SIZE=128;DB_CLOSE_DELAY=30;AUTO_RECONNECT=TRUE;CACHE_SIZE=8192;MODE=Oracle;LOCK_TIMEOUT=10";

        try {
            (new Socket("localhost", 9092)).close();
            LOGGER.info("Found open port 9092, assume running H2 Database instance.");
        } catch (IOException ex) {
            LOGGER.info("Could not open port 9092. Will try to start H2 Database instance.");
            server = Server.createTcpServer("-ifNotExists");
            server.start();
        }

        try (Connection con = DriverManager.getConnection(
                connectionStr,
                PROPERTIES);
                Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE test (id VARCHAR(40) NOT NULL PRIMARY KEY)");
            for (int i = 0; i < 10000; i++) {
                st.executeUpdate("INSERT INTO test VALUES ('" + i + "')");
            }
        }
    }

    @AfterAll
    public static void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }

        URI h2FileUri = new URI(dbFileUriStr + ".mv.db");
        File h2File = new File(h2FileUri);
        if (h2File.exists() && h2File.canWrite()) {
            LOGGER.info("Delete H2 database file " + h2File.getCanonicalPath());
            h2File.delete();
        }
    }

    @Test
    public void createTableTest() throws Exception {
        int threads = 10;

        ExecutorService exec = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            final String tableName = "test_" + i;
            exec.submit(new Runnable() {
                @Override
                public void run() {
                    try (Connection con = DriverManager.getConnection(connectionStr, PROPERTIES);
                            Statement st = con.createStatement()) {
                        st.executeUpdate(
                                "CREATE /*LOCAL TEMPORARY*/ TABLE " + tableName
                                        + " AS  SELECT a.id FROM test a INNER JOIN test b ON a.id=b.id");
                    } catch (SQLException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            });

        }
        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.DAYS);
    }
}
