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

import java.sql.*;
import java.util.Properties;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;

/** @author Andreas Reichel <andreas@manticore-projects.com> */
public class H2Issue_3041 {

    public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());
    public static final String H2_VERSION = "2.0.201-2c7cb8658";

    public static final String CONNECTION_URL = "jdbc:h2:mem:test;LOCK_TIMEOUT=1000";

    @Test
    public void createTableTest() throws SQLException, InterruptedException, Exception {
        Driver driver = H2MigrationTool.loadDriver(H2_VERSION);
        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");

        ExecutorService executor = Executors.newFixedThreadPool(8);
        try (Connection connection = driver.connect(CONNECTION_URL, properties);
                Statement st = connection.createStatement();) {

            try {
                st.executeUpdate(
                        "CREATE TABLE public.EXAMPLE_0"
                                + " (GENERATED_ID IDENTITY PRIMARY KEY, SIMPLE_VALUE INT)");
            } catch (SQLException ex) {
            }

            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 1; i < 10000; i++) {
                                try {
                                    st.executeUpdate(
                                            "CREATE TABLE public.EXAMPLE_"
                                                    + i
                                                    + " (GENERATED_ID IDENTITY PRIMARY KEY, SIMPLE_VALUE INT)");
                                } catch (SQLException ex) {
                                    LOGGER.log(Level.SEVERE, "Statement failed on i=" + i, ex);
                                }
                            }
                        }
                    });

            executor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 10000; i++) {
                                try (ResultSet rs =
                                        st.executeQuery(
                                                "WITH TMP_EXAMPLE_"
                                                        + i
                                                        + "  as (SELECT avg(SIMPLE_VALUE) AVG_SIMPLE_VALUE FROM public.EXAMPLE_0)  SELECT * FROM TMP_EXAMPLE_"
                                                        + i)) {

                                } catch (SQLException ex) {
                                    LOGGER.log(Level.SEVERE, "Statement failed on i=" + i, ex);
                                }
                            }
                        }
                    });

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Statement failed.", ex);
        }
    }
}
