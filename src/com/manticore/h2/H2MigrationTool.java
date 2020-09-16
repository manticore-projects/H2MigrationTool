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
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.util.Comparator;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.cli.*;

/** @author Andreas Reichel <andreas@manticore-projects.com> */
public class H2MigrationTool {
  public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());

  private class DriverRecord implements Comparable<DriverRecord> {
    int majorVersion;
    int minorVersion;
    int buildId;
    URL url;

    public DriverRecord(int majorVersion, int minorVersion, int buildId, URL url) {
      this.majorVersion = majorVersion;
      this.minorVersion = minorVersion;
      this.buildId = buildId;
      this.url = url;
    }

    @Override
    public int compareTo(DriverRecord t) {
      int compareTo = Integer.compare(majorVersion, t.majorVersion);

      if (compareTo == 0) compareTo = Integer.compare(minorVersion, t.minorVersion);

      if (compareTo == 0) compareTo = Integer.compare(buildId, t.buildId);

      return compareTo;
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 29 * hash + this.majorVersion;
      hash = 29 * hash + this.minorVersion;
      hash = 29 * hash + this.buildId;
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final DriverRecord other = (DriverRecord) obj;
      if (this.majorVersion != other.majorVersion) return false;
      if (this.minorVersion != other.minorVersion) return false;
      if (this.buildId != other.buildId) return false;
      return true;
    }

    @Override
    public String toString() {
      return "H2-" + majorVersion + "." + minorVersion + "." + buildId;
    }
  }

  private final TreeSet<DriverRecord> driverRecords = new TreeSet<>();

  private void readDriverRecords(String ressourceName) {
    File driverFolder = new File(ressourceName);

    FilenameFilter filenameFilter =
        new FilenameFilter() {
          @Override
          public boolean accept(File file, String string) {
            String s = string.toLowerCase();
            return s.startsWith("h2") && s.endsWith(".jar");
          }
        };
    for (File file : driverFolder.listFiles(filenameFilter)) {
      LOGGER.info("Found H2 library " + file.getAbsolutePath());
      try {
        URL url = file.toURI().toURL();
        URLClassLoader loader =
            new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader());

        Class classToLoad = Class.forName("org.h2.Driver", true, loader);

        Method method = classToLoad.getDeclaredMethod("load");
        Object instance = classToLoad.newInstance();
        Object result = method.invoke(instance);

        Driver driver = (Driver) instance;

        // LOGGER.info("major: " + driver.getMajorVersion());
        // LOGGER.info("minor: " + driver.getMinorVersion());
        if (driver.getMajorVersion() == 2) {
          // @fixme: for unknown reason, we need to load some classes explicitely with 2.0.201 only
          try {
            loader.loadClass("org.h2.table.InformationSchemaTable");
            loader.loadClass("org.h2.mvstore.MVMap$2");
            loader.loadClass("org.h2.mvstore.MVMap$2$1");
            loader.loadClass("org.h2.index.MetaIndex");
          } catch (Exception ex1) {
            LOGGER.log(Level.SEVERE, "Failed to load additional classes.", ex1);
          }
        }

        Properties properties = new Properties();
        properties.setProperty("user", "sa");
        properties.setProperty("password", "");

        Connection connection = driver.connect("jdbc:h2:mem:test", properties);
        DatabaseMetaData metaData = connection.getMetaData();

        String driverVersion = metaData.getDriverVersion();

        Pattern pattern =
            Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(driverVersion);
        if (matcher.find()) {
          int majorVersion = Integer.valueOf(matcher.group(1));
          int minorVersion = Integer.valueOf(matcher.group(2));
          int buildId = Integer.valueOf(matcher.group(3));

          DriverRecord driverRecord = new DriverRecord(majorVersion, minorVersion, buildId, url);
          driverRecords.add(driverRecord);

          LOGGER.info(driverRecord.toString());
        }

        connection.close();
        loader.close();
      } catch (Exception ex) {

      }
    }
  }

  private DriverRecord getDriverRecord(int majorVersion, int minorVersion, int buildId) {
    for (DriverRecord r : driverRecords.descendingSet()) {
      if (r.majorVersion == majorVersion
          && r.minorVersion == minorVersion
          && r.buildId == buildId) {
        return r;
      }
    }
    return null;
  }

  private DriverRecord getDriverRecord(int majorVersion, int minorVersion) {
    for (DriverRecord r : driverRecords.descendingSet()) {
      if (r.majorVersion == majorVersion && r.minorVersion == minorVersion) {
        return r;
      }
    }
    return null;
  }

  private DriverRecord getDriverRecord(String version) throws Exception {
    DriverRecord driverRecord = null;

    Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(version);
    if (matcher.find()) {
      if (matcher.groupCount() == 2) {
        int majorVersion = Integer.valueOf(matcher.group(1));
        int minorVersion = Integer.valueOf(matcher.group(2));

        driverRecord = getDriverRecord(majorVersion, minorVersion);
      } else if (matcher.groupCount() == 3) {
        int majorVersion = Integer.valueOf(matcher.group(1));
        int minorVersion = Integer.valueOf(matcher.group(2));
        int buildId = Integer.valueOf(matcher.group(3));

        driverRecord = getDriverRecord(majorVersion, minorVersion, buildId);
      } else
        throw new Exception(
            "The provided version " + version + " does not match the required format ###.###.###");
    } else
      throw new Exception(
          "The provided version " + version + " does not match the required format ###.###.###");

    if (driverRecord == null)
      throw new Exception("No H2 driver found for requestion version " + version);

    return driverRecord;
  }

  private String writeScript(
      DriverRecord driverRecord,
      String databaseFileName,
      String user,
      String password,
      String scriptFileName,
      String options)
      throws SQLException, ClassNotFoundException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException {

    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);

    URL url = driverRecord.url;
    URLClassLoader loader =
        new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader());

    Class classToLoad = Class.forName("org.h2.Driver", true, loader);

    Method method = classToLoad.getDeclaredMethod("load");
    Object instance = classToLoad.newInstance();
    Object result = method.invoke(instance);

    Driver driver = (Driver) instance;

    if (driver.getMajorVersion() == 2) {
      // @fixme: for unknown reason, we need to load some classes explicitely with 2.0.201 only
      try {
        loader.loadClass("org.h2.table.InformationSchemaTable");
        loader.loadClass("org.h2.mvstore.MVMap$2");
        loader.loadClass("org.h2.mvstore.MVMap$2$1");
        loader.loadClass("org.h2.index.MetaIndex");
      } catch (Exception ex1) {
        LOGGER.log(Level.SEVERE, "Failed to load additional classes.", ex1);
      }
    }

    Connection connection = null;

    try {
      connection = driver.connect("jdbc:h2://" + databaseFileName, properties);

      classToLoad = Class.forName("org.h2.tools.Script", true, loader);

      // Connection conn, String fileName, String options1, String options2
      Class<?>[] argClasses =
          new Class<?>[] {Connection.class, String.class, String.class, String.class};
      method = classToLoad.getDeclaredMethod("process", argClasses);
      instance = classToLoad.newInstance();

      // Connection conn, String fileName, String options1, String options2
      result = method.invoke(instance, connection, scriptFileName, "", options);
      return scriptFileName;

    } finally {

      if (connection != null) connection.close();

      try {
        loader.close();
      } catch (IOException ex) {
        LOGGER.log(Level.FINEST, null, ex);
      }
    }
  }

  private String createFromScript(
      DriverRecord driverRecord,
      String databaseFileName,
      String user,
      String password,
      String scriptFileName,
      String options)
      throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException,
          SQLException {

    databaseFileName = databaseFileName + "." + driverRecord.buildId;

    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);

    URL url = driverRecord.url;
    URLClassLoader loader =
        new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader());

    Class classToLoad = Class.forName("org.h2.Driver", true, loader);

    Method method = classToLoad.getDeclaredMethod("load");
    Object instance = classToLoad.newInstance();
    Object result = method.invoke(instance);

    Driver driver = (Driver) instance;

    if (driver.getMajorVersion() == 2) {
      // @fixme: for unknown reason, we need to load some classes explicitely with 2.0.201 only
      try {
        loader.loadClass("org.h2.table.InformationSchemaTable");
        loader.loadClass("org.h2.mvstore.MVMap$2");
        loader.loadClass("org.h2.mvstore.MVMap$2$1");
        loader.loadClass("org.h2.index.MetaIndex");
      } catch (Exception ex1) {
        LOGGER.log(Level.SEVERE, "Failed to load additional classes.", ex1);
      }
    }

    Connection connection = null;
    Statement stat = null;
    try {
      connection = driver.connect("jdbc:h2://" + databaseFileName, properties);
      stat = connection.createStatement();

      stat.execute("RUNSCRIPT FROM '" + scriptFileName + "' " + options);
      stat.execute("ANALYZE SAMPLE_SIZE 0");
      stat.execute("SHUTDOWN COMPACT");

      return databaseFileName;

    } finally {
      if (stat != null) stat.close();
      if (connection != null) connection.close();

      try {
        loader.close();
      } catch (IOException ex) {
        LOGGER.log(Level.FINEST, null, ex);
      }
    }
  }

  public void migrate(
      String versionFrom,
      String versionTo,
      String databaseFileName,
      String user,
      String password,
      String scriptFileName,
      String compression,
      String upgradeOptions,
      boolean overwrite,
      boolean force)
      throws Exception {

    DriverRecord driverRecordFrom = getDriverRecord(versionFrom);
    DriverRecord driverRecordTo = getDriverRecord(versionTo);

    if (scriptFileName == null || scriptFileName.isEmpty())
      scriptFileName = databaseFileName + ".sql";

    if (compression != null
        && compression.endsWith("ZIP")
        && !scriptFileName.toLowerCase().endsWith(".zip")) scriptFileName = scriptFileName + ".zip";

    if (compression != null
        && compression.endsWith("GZIP")
        && !scriptFileName.toLowerCase().endsWith(".gz")) scriptFileName = scriptFileName + ".gz";

    boolean success = false;
    try {
      scriptFileName =
          writeScript(
              driverRecordFrom, databaseFileName, user, password, scriptFileName, compression);
      success = true;
      LOGGER.info(
          "Wrote " + driverRecordFrom.toString() + " database to script: " + scriptFileName);
    } catch (Exception ex) {
      LOGGER.log(
          Level.SEVERE,
          "Failed to write " + driverRecordFrom.toString() + " database to script",
          ex);
    }

    String options =
        compression != null && compression.length() > 0
            ? compression + " " + upgradeOptions
            : upgradeOptions;
    if (success)
      try {
        databaseFileName =
            createFromScript(
                driverRecordTo, databaseFileName, user, password, scriptFileName, options);
        LOGGER.info("Created new " + driverRecordTo.toString() + " database: " + databaseFileName);
      } catch (Exception ex) {
        LOGGER.log(
            Level.SEVERE,
            "Failed to created new " + driverRecordTo.toString() + " database: " + databaseFileName,
            ex);
      }
  }

  public static void main(String[] args) {

    Options options = new Options();

    options.addRequiredOption("l", "lib-dir", true, "Folder containing the H2 jar files.");
    options.addRequiredOption(
        "f", "version-from", true, "Old H2 version of the existing database.");
    options.addRequiredOption("t", "version-to", true, "New H2 version to upgrade to.");
    options.addRequiredOption("d", "db-file", true, "The existing H2 database (old format).");
    options.addOption("u", "user", true, "The database username.");
    options.addOption("p", "password", true, "The database password.");
    options.addOption("s", "script-file", true, "The export script file.");
    options.addOption("c", "compression", true, "The Compression Method [ZIP, GZIP]");
    options.addOption(
        Option.builder("o")
            .longOpt("options")
            .required()
            .hasArgs()
            .valueSeparator(' ')
            .desc("The upgrade options [TRUNCATE_LARGE_LENGTH VARIABLE_BINARY]")
            .build());
    options.addOption(null, "force", false, "Overwrite files and continue on failure.");
    options.addOption("h", "help", false, "Show the help mesage.");

    // create the parser
    CommandLineParser parser = new DefaultParser();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.hasOption("help")) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator((Comparator<Option>) null);
        formatter.printHelp("java -jar H2MigrationTool.jar", options, true);
        return;
      }
      try {
        String ressourceName = line.getOptionValue("lib-dir");

        String versionFrom = line.getOptionValue("version-from");
        String versionTo = line.getOptionValue("version-to");
        String databaseFileName = line.getOptionValue("db-file");
        String user = line.hasOption("user") ? line.getOptionValue("user") : "sa";
        String password = line.hasOption("password") ? line.getOptionValue("password") : "";
        String scriptFileName =
            line.hasOption("script-file") ? line.getOptionValue("script-file") : "";

        // "COMPRESSION ZIP";
        String compression =
            line.hasOption("compression")
                ? "COMPRESSION " + line.getOptionValue("compression")
                : "";

        // "TRUNCATE_LARGE_LENGTH VARIABLE_BINARY"
        String upgradeOptions = "";
        if (line.hasOption("options")) {
          StringBuffer stringBuffer = new StringBuffer();
          int i = 0;
          for (String s : line.getOptionValues("options")) {
            if (i > 0) stringBuffer.append(" ");
            stringBuffer.append(s);
            i++;
          }
          upgradeOptions = stringBuffer.toString();
        }

        boolean overwrite = line.hasOption("force") ? true : false;

        boolean force = line.hasOption("force") ? true : false;

        H2MigrationTool app = new H2MigrationTool();
        app.readDriverRecords(ressourceName);

        app.migrate(
            versionFrom,
            versionTo,
            databaseFileName,
            user,
            password,
            scriptFileName,
            compression,
            upgradeOptions,
            overwrite,
            force);
      } catch (Exception ex) {
        Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
      }

    } catch (ParseException ex) {
      // LOGGER.log(Level.SEVERE, "Parsing failed.  Reason: " + ex.getMessage(), ex);
      HelpFormatter formatter = new HelpFormatter();
      formatter.setOptionComparator((Comparator<Option>) null);
      formatter.printHelp("java -jar H2MigrationTool.jar", options, true);
    }
  }
}
