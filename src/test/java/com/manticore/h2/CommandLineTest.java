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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class CommandLineTest {

    public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());

    /*
     * @todo: move DDLs and Test SQLs into a text file to read from
     */
    public static final String DDL_STR =
            "drop table IF EXISTS B cascade;\n" +
                    "drop table IF EXISTS A cascade;\n" +
                    "\n" +
                    "CREATE TABLE a\n" +
                    "  (\n" +
                    "     field1 varchar(1)\n" +
                    "  );\n" +
                    "\n" +
                    "CREATE TABLE b\n" +
                    "  (\n" +
                    "     field2 varchar(1)\n" +
                    "  );\n" +
                    "\n" +
                    "ALTER TABLE b\n" +
                    "  ADD FOREIGN KEY (field2) REFERENCES a(field1);";

    @Test
    public void migrateAutoCommandLine() throws Exception {

        String versionFrom = "1.4.200";
        String versionTo = "2.0.201";
        String databaseName = "~/" + CommandLineTest.class.getSimpleName() + "_" + versionFrom;
        String username = "SA";
        String password = "";
        String compression = "ZIP";
        String options = "VARIABLE_BINARY";

        String dbFileUriStr = H2MigrationTool.getAbsoluteFileName(databaseName);

        File h2File = new File(dbFileUriStr + ".mv.db");
        if (h2File.exists()) {
            Assertions.fail(
                    "The H2 Database file " +
                            h2File.getCanonicalPath() +
                            "  exists already. Please remove it manually.");
        }

        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);

        Driver driver =
                H2MigrationTool.loadDriver(
                        (versionFrom != null && versionFrom.length() > 0)
                                ? versionFrom
                                : "1.4.200");
        try (Connection con = driver.connect("jdbc:h2:" + dbFileUriStr, properties);
                Statement st = con.createStatement()) {

            for (String sqlStr : DDL_STR.split(";")) {
                st.executeUpdate(sqlStr);
            }

        } catch (Exception ex) {
            LOGGER.log(
                    Level.SEVERE,
                    "Error when create the database " + dbFileUriStr + "  version " + versionFrom,
                    ex);
            Assertions.fail();
        }

        ArrayList<String> args = new ArrayList<>();

        if (versionFrom != null && versionFrom.length() > 0) {
            args.add("-f");
            args.add(versionFrom);
        }

        if (versionTo != null && versionTo.length() > 0) {
            args.add("-t");
            args.add(versionTo);
        }

        if (databaseName != null && databaseName.length() > 0) {
            args.add("-d");
            args.add(databaseName);
        }

        if (compression != null && compression.length() > 0) {
            args.add("-c");
            args.add(compression);
        }

        if (options != null && options.length() > 0) {
            args.add("-o");
            args.add(options);
        }

        args.add("--force");

        // Example:
        // -l ~/h2-libs -f 1.4.200 -t 2.0.201 -d ~/ifrsbox -c ZIP -o VARIABLE_BINARY
        try {
            H2MigrationTool.main(args.toArray(new String[args.size()]));
        } catch (Exception ex) {
            Assertions.fail(ex.getMessage());
        } finally {

            h2File = new File(dbFileUriStr + ".mv.db");
            if (h2File.exists() && h2File.canWrite()) {
                LOGGER.fine("Delete H2 database file " + h2File.getCanonicalPath());
                h2File.delete();
            }
        }
    }

    @Test
    public void migrateExistingDB() throws Exception {

        String versionFrom = "";
        String versionTo = "";
        String databaseName = "~/.manticore.oplon/riskbox";
        String compression = "ZIP";
        String options = "VARIABLE_BINARY";

        String dbFileUriStr = H2MigrationTool.getAbsoluteFileName(databaseName);

        ArrayList<String> args = new ArrayList<>();

        if (versionFrom != null && versionFrom.length() > 0) {
            args.add("-f");
            args.add(versionFrom);
        }

        if (versionTo != null && versionTo.length() > 0) {
            args.add("-t");
            args.add(versionTo);
        }

        if (databaseName != null && databaseName.length() > 0) {
            args.add("-d");
            args.add(databaseName);
        }

        if (compression != null && compression.length() > 0) {
            args.add("-c");
            args.add(compression);
        }

        if (options != null && options.length() > 0) {
            args.add("-o");
            args.add(options);
        }

        args.add("--force");

        // Example:
        // -l ~/h2-libs -f 1.4.200 -t 2.0.201 -d ~/ifrsbox -c ZIP -o VARIABLE_BINARY
        try {
            H2MigrationTool.main(args.toArray(new String[args.size()]));
        } catch (Exception ex) {
            Assertions.fail(ex.getMessage());
        }
    }
}
