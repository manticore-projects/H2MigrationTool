/*
 * To change this license header, choose License Headers in Project Properties. To change this
 * template file, choose Tools | Templates and open the template in the editor.
 */

package com.manticore;

import com.manticore.h2.DriverRecord;
import com.manticore.h2.H2MigrationTool;
import com.manticore.h2.H2MigrationUI;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;

import static com.manticore.h2.H2MigrationTool.LOGGER;
import static com.manticore.h2.H2MigrationTool.getAbsoluteFileName;

/**
 * @author are
 */
public class Recovery {
    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("l", "lib-dir", true, "(Relative) Folder containing the H2 jar files.");
        options.addRequiredOption("f", "version-from", true,
                "H2 version of the existing database.");
        options.addRequiredOption("d", "db-file", true,
                "The (relative) existing H2 database file.");
        options.addOption("h", "help", false, "Show the help message.");

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.getOptions().length==0 && !GraphicsEnvironment.isHeadless()) {
                System.setProperty("awt.useSystemAAFontSettings", "lcd");
                System.setProperty("swing.aatext", "true");
                System.setProperty("prism.lcdtext", "true");

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
                        } catch (ClassNotFoundException | InstantiationException
                                 | IllegalAccessException
                                 | UnsupportedLookAndFeelException ex) {
                            LOGGER.log(Level.SEVERE, "Error when setting the NIMBUS L&F", ex);
                        }

                        try {
                            H2MigrationTool.readDriverRecords();

                            H2MigrationUI frame = new H2MigrationUI();
                            frame.buildUI(true);
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE, "Error when reading the H2 Database drivers",
                                    ex);
                        }
                    }
                });
                return;
            }

            if (line.hasOption("help") || line.getOptions().length==0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setOptionComparator(null);
                formatter.printHelp("java -cp H2MigrationTool.jar com.manticore.Recovery", options,
                        true);
                return;
            } else if (!line.hasOption("db-file")) {
                throw new Exception(
                        "Nothing to recover. Please define the Database to recover,\neither by providing the DB Name or the DB Folder.");
            }

            try {
                String resourceName =
                        line.hasOption("lib-dir")
                        ? getAbsoluteFileName(line.getOptionValue("lib-dir"))
                        :null;

                String versionFrom =
                        line.hasOption("version-from") ? line.getOptionValue("version-from"):null;

                String databaseFileName = line.getOptionValue("db-file");
                databaseFileName = getAbsoluteFileName(databaseFileName);

                File databaseFile = new File(databaseFileName);


                H2MigrationTool app = new H2MigrationTool();
                H2MigrationTool.readDriverRecords(resourceName);

                if (versionFrom!=null && versionFrom.length() > 1) {
                    DriverRecord driverRecordFrom = H2MigrationTool
                            .getDriverRecord(H2MigrationTool.DRIVER_RECORDS, versionFrom);
                    app.writeRecoveryScript(driverRecordFrom, databaseFile.getParent(),
                            databaseFile.getName());
                }

            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Failed to recover the database. Reason: {0}",
                        ex.getMessage());
                throw new Exception("Failed to recover the database.", ex);
            }

        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Parsing failed.  Reason: {0}", ex.getMessage());

            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(null);
            formatter.printHelp("java -cp H2MigrationTool.jar com.manticore.Recovery", options,
                    true);
        }
    }
}
