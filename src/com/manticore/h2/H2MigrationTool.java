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

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class H2MigrationTool {

  public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());

  protected static final TreeSet<DriverRecord> driverRecords = new TreeSet<>();

  private enum HookType {
    SQL,
    GROOVY
  };

  private enum HookStage {
    EXPORT,
    IMPORT,
    INIT
  };

  private class Hook implements Comparable<Hook> {

    String id;
    HookType type;
    HookStage stage;

    String text;

    public Hook(String name, HookStage stage, String text) {
      name = name.toLowerCase();

      if (name.endsWith(".sql")) {
        this.id = name.substring(0, name.length() - 4);
        this.type = HookType.SQL;
      } else if (name.endsWith(".groovy")) {
        this.id = name.substring(0, name.length() - 7);
        this.type = HookType.GROOVY;
      }
      this.stage = stage;
      this.text = text;
    }

    @Override
    public int compareTo(Hook t) {
      return id.compareToIgnoreCase(t.id);
    }

    @Override
    public int hashCode() {
      int hash = 3;
      hash = 29 * hash + Objects.hashCode(this.id);
      return hash;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final Hook other = (Hook) obj;
      if (!Objects.equals(this.id, other.id))
        return false;
      return true;
    }
  }

  private final TreeSet<Hook> hooks = new TreeSet<>();

  private void readHooks(String versionFrom) {
    hooks.clear();

    FilenameFilter filenameFilter =
                   new FilenameFilter() {
             @Override
             public boolean accept(File file, String string) {
               String s = string.toLowerCase();
               return s.endsWith(".sql") || s.endsWith(".groovy");
             }
           };

//    URL url = H2MigrationTool.class.getResource("com/manticore/hooks/" + versionFrom + "/export");
//
//    if (url != null)
//      try {
//        File d = new File(url.toURI());
//        for (File f : d.listFiles(filenameFilter)) {
//          FileInputStream inputStream;
//          try {
//            inputStream = new FileInputStream(f);
//            String text = IOUtils.toString(inputStream, (String) null);
//            inputStream.close();
//
//            String name = f.getName();
//
//            hooks.add(new Hook(name, HookStage.EXPORT, text));
//
//          } catch (FileNotFoundException ex) {
//            Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//          } catch (IOException ex) {
//            Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//          }
//        }
//      } catch (URISyntaxException ex) {
//        Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//      }
//
//    url =
//        H2MigrationTool.class
//            .getClassLoader()
//            .getResource("com/manticore/hooks/" + versionFrom + "/import");
//
//    if (url != null)
//      try {
//        File d = new File(url.toURI());
//        for (File f : d.listFiles(filenameFilter)) {
//          FileInputStream inputStream;
//          try {
//            inputStream = new FileInputStream(f);
//            String text = IOUtils.toString(inputStream, (String) null);
//            inputStream.close();
//
//            String name = f.getName();
//
//            hooks.add(new Hook(name, HookStage.IMPORT, text));
//
//          } catch (FileNotFoundException ex) {
//            Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//          } catch (IOException ex) {
//            Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//          }
//        }
//      } catch (URISyntaxException ex) {
//        Logger.getLogger(H2MigrationTool.class.getName()).log(Level.SEVERE, null, ex);
//      }
  }

  private void executeCommands(Connection connection, List<String> commands) throws Exception {
    Statement st = null;
    try {
      st = connection.createStatement();
      for (String s : commands)
        st.executeUpdate(s);
      st.close();
    } finally {
      if (st != null)
        try {
        st.close();
      } catch (SQLException ex) {
        LOGGER.log(Level.SEVERE, "Failed to close statement.", ex);
      }
    }
  }

  private List<String> executeHooks(Connection connection, HookStage stage) {
    ArrayList<String> commands = new ArrayList<>();

    for (Hook hook : hooks)
      if (hook.stage.equals(stage)) {
        LOGGER.info("Execute hook for stage " + stage.name());

        Statement st = null;
        try {
          st = connection.createStatement();
          boolean isResultSet = st.execute(hook.text);

          if (isResultSet) {
            ArrayList<String> cmds = new ArrayList<>();

            ResultSet rs = st.getResultSet();
            while (rs.next()) {
              cmds.add(rs.getString(1));
            }
            rs.close();

            if (hook.stage.equals(HookStage.EXPORT))
              try {
              executeCommands(connection, cmds);
            } catch (Exception ex) {
              LOGGER.log(Level.SEVERE, "Hook " + hook.id + " failed.", ex);
            } else
              commands.addAll(cmds);
          }
          st.close();
        } catch (SQLException ex) {
          LOGGER.log(Level.SEVERE, null, ex);
        } finally {
          if (st != null)
            try {
            st.close();
          } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to close statement.", ex);
          }
        }
      }
    return commands;
  }

  public static String getTempFolderName() {
    String tempPath = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();

    return tempPath;
  }
  
  public static File getAbsoluteFile(String filename) {
    String homePath = new File(System.getProperty("user.home")).toURI().getPath();

    filename = filename.replaceFirst("~", Matcher.quoteReplacement(homePath));
    filename = filename.replaceFirst("\\$\\{user.home\\}", Matcher.quoteReplacement(homePath));

    File f = new File(filename);

    if (!f.isAbsolute()) {
      Path basePath = Paths.get("").toAbsolutePath();

      Path resolvedPath = basePath.resolve(filename);
      Path absolutePath = resolvedPath.normalize();
      f = absolutePath.toFile();
    }
    return f;
  }

  public static String getAbsoluteFileName(String filename) {
    return getAbsoluteFile(filename).getAbsolutePath();
  }

  public static Collection<Path> findFilesinPathRecursively(
          Path parentPath, int depth, String prefix, String suffix) throws IOException {
    ArrayList<Path> fileNames = new ArrayList<>();
    try (Stream<Path> paths =
                      Files.find(
                              parentPath,
                              depth,
                              (path, attr) -> {
                                if (attr.isRegularFile()) {
                                  String pathName = path.getFileName().toString().toLowerCase();
                                  return (pathName.startsWith(prefix.toLowerCase()) &&
                                          pathName.endsWith(suffix.toLowerCase()));
                                }
                                return false;
                              })) {
      paths.sorted().collect(Collectors.toCollection(() -> fileNames));
    }
    return fileNames;
  }

  public static Collection<Path> findFilesinPathRecursively(
          Path parentPath, int depth, String... extensions) throws IOException {
    ArrayList<Path> fileNames = new ArrayList<>();
    try (Stream<Path> paths =
                      Files.find(
                              parentPath,
                              depth,
                              (path, attr) -> {
                                if (attr.isRegularFile()) {
                                  String pathName = path.toString().toLowerCase();

                                  for (String s : extensions)
                                    if (pathName.endsWith(s))
                                      return true;
                                }
                                return false;
                              })) {
      paths.sorted().collect(Collectors.toCollection(() -> fileNames));
    }
    return fileNames;
  }

  public static Collection<Path> findH2Databases(String pathName) throws IOException {
    ArrayList<Path> fileNames = new ArrayList<>();

    File folder = new File(pathName);
    if (folder.exists() && folder.canRead() && folder.isDirectory())
      fileNames.addAll(
              findFilesinPathRecursively(Path.of(folder.toURI()), Integer.MAX_VALUE, ".mv.db"));
    return fileNames;
  }

  public static TreeSet<DriverRecord> readDriverRecords() throws Exception {
    return readDriverRecords("");
  }

  public static TreeSet<DriverRecord> readDriverRecords(String resourceName) throws Exception {

    Path myPath;
    FileSystem fileSystem = null;

    if (resourceName != null && resourceName.length() > 0)
      myPath = new File(resourceName).toPath();
    else {
      URL resourceUrl = H2MigrationTool.class.getResource("/drivers");
      URI resourceUri = resourceUrl.toURI();
      if (resourceUri.getScheme().equals("jar")) {
        fileSystem = FileSystems.newFileSystem(resourceUri, Collections.<String, Object>emptyMap());
        myPath = fileSystem.getPath("/drivers");
      } else
        myPath = Paths.get(resourceUri);
    }

    LOGGER.info(myPath.toString());
    for (Path path : findFilesinPathRecursively(myPath, 1, "h2", ".jar")) {
      LOGGER.info("Found H2 library " + path.getFileName().toString());
      try {
        URI resourceUri = path.toUri();
        URL url = path.toUri().toURL();

        // @todo: For any reason we can't load a Jar from inside a Jar
        // so we have to extract a local copy first
        // investigate, if the is a better solution, e. g. a special ClassLoader
        if (resourceUri.getScheme().equals("jar")) {
          String fileName = path.getFileName().toString();

          File tmpFile = new File(System.getProperty("java.io.tmpdir"), fileName);
          tmpFile.deleteOnExit();

          Files.copy(url.openStream(), tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

          url = tmpFile.toURI().toURL();
        }

        LOGGER.fine("Load JAR file from: " + url.toExternalForm());
        URLClassLoader loader =
                       new URLClassLoader(new URL[]{url}, H2MigrationTool.class.getClassLoader());

        Class classToLoad = Class.forName("org.h2.Driver", true, loader);

        Method method = classToLoad.getDeclaredMethod("load");
        Object instance = classToLoad.newInstance();
        Object result = method.invoke(instance);

        Driver driver = getDriverFromInstance(loader, instance);

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
        LOGGER.log(Level.SEVERE, "Failed to load the driver " + path.getFileName().toString(), ex);
      }
    }

    LOGGER.fine("Driver Records loaded: " + driverRecords.size());

    if (fileSystem != null)
      fileSystem.close();

    return driverRecords;
  }

  private static Driver getDriverFromInstance(ClassLoader loader, Object instance) {
    Driver driver = (Driver) instance;

    if (driver.getMajorVersion() == 2)
      // @fixme: for unknown reason, we need to load some classes explicitely with 2.0.201 only
      try {
      loader.loadClass("org.h2.table.InformationSchemaTable");
      loader.loadClass("org.h2.mvstore.MVMap$2");
      loader.loadClass("org.h2.mvstore.MVMap$2$1");
      loader.loadClass("org.h2.index.MetaIndex");
      loader.loadClass("org.h2.api.ErrorCode");
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "Failed to load additional classes.", ex);
    }
    return driver;
  }

  public static Driver loadDriver(String version) throws Exception {
    return loadDriver("", version);
  }

  public static Driver loadDriver(String resourceStr, String version) throws Exception {
    TreeSet<DriverRecord> driverRecords = H2MigrationTool.readDriverRecords(resourceStr);

    DriverRecord driverRecord = getDriverRecord(driverRecords, version);
    if (driverRecord != null)
      return loadDriver(driverRecord);
    else
      throw new Exception("No Driver found for requested version " + version);
  }

  public static Driver loadDriver(TreeSet<DriverRecord> driverRecords, String version)
          throws Exception {
    DriverRecord driverRecord = getDriverRecord(driverRecords, version);
    if (driverRecord != null)
      return loadDriver(driverRecord);
    else
      throw new Exception("No Driver found for requested version " + version);
  }

  public static Driver loadDriver(DriverRecord driverRecord) throws Exception {
    Driver driver;

    URL url = driverRecord.url;
    URLClassLoader loader =
                   new URLClassLoader(new URL[]{url}, H2MigrationTool.class.getClassLoader());

    try {
      Class classToLoad = Class.forName("org.h2.Driver", true, loader);

      Method method = classToLoad.getDeclaredMethod("load");
      Object instance = classToLoad.newInstance();
      Object result = method.invoke(instance);

      driver = getDriverFromInstance(loader, instance);

      return driver;
    } finally {
      //      try {
      //        loader.close();
      //      } catch (IOException ex) {
      //        LOGGER.log(Level.SEVERE, "Close the Classloader", ex);
      //      }
    }
  }

  public static DriverRecord getDriverRecord(
          TreeSet<DriverRecord> driverRecords, int majorVersion, int minorVersion, int buildId) {
    for (DriverRecord r : driverRecords.descendingSet())
      if (r.majorVersion == majorVersion && r.minorVersion == minorVersion && r.buildId == buildId)
        return r;
    return null;
  }

  private DriverRecord getDriverRecord(int majorVersion, int minorVersion, int buildId) {
    for (DriverRecord r : driverRecords.descendingSet())
      if (r.majorVersion == majorVersion && r.minorVersion == minorVersion && r.buildId == buildId)
        return r;
    return null;
  }

  public static DriverRecord getDriverRecord(
          TreeSet<DriverRecord> driverRecords, int majorVersion, int minorVersion) {
    for (DriverRecord r : driverRecords.descendingSet())
      if (r.majorVersion == majorVersion && r.minorVersion == minorVersion)
        return r;
    return null;
  }

  private DriverRecord getDriverRecord(int majorVersion, int minorVersion) {
    for (DriverRecord r : driverRecords.descendingSet())
      if (r.majorVersion == majorVersion && r.minorVersion == minorVersion)
        return r;
    return null;
  }

  public static DriverRecord getDriverRecord(TreeSet<DriverRecord> driverRecords, String version)
          throws Exception {
    DriverRecord driverRecord = null;

    Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(version);
    if (matcher.find())
      if (matcher.groupCount() == 2) {
        int majorVersion = Integer.valueOf(matcher.group(1));
        int minorVersion = Integer.valueOf(matcher.group(2));

        driverRecord = getDriverRecord(driverRecords, majorVersion, minorVersion);
      } else if (matcher.groupCount() == 3) {
        int majorVersion = Integer.valueOf(matcher.group(1));
        int minorVersion = Integer.valueOf(matcher.group(2));
        int buildId = Integer.valueOf(matcher.group(3));

        driverRecord = getDriverRecord(driverRecords, majorVersion, minorVersion, buildId);
      } else
        throw new Exception(
                "The provided version " + version + " does not match the required format ###.###.###");
    else
      throw new Exception(
              "The provided version " + version + " does not match the required format ###.###.###");

    if (driverRecord == null)
      throw new Exception("No H2 driver found for requestion version " + version);

    return driverRecord;
  }

  private DriverRecord getDriverRecord(String version) throws Exception {
    DriverRecord driverRecord = null;

    Pattern pattern = Pattern.compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(version);
    if (matcher.find())
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
    else
      throw new Exception(
              "The provided version " + version + " does not match the required format ###.###.###");

    if (driverRecord == null)
      throw new Exception("No H2 driver found for requestion version " + version);

    return driverRecord;
  }

  public class ScriptResult {

    String scriptFileName;
    List<String> commands;

    public ScriptResult(String scriptFileName, List<String> commands) {
      this.scriptFileName = scriptFileName;
      this.commands = commands;
    }
  }

  private ScriptResult writeScript(
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
    URLClassLoader loader = new URLClassLoader(new URL[]{url});

    Class classToLoad = Class.forName("org.h2.Driver", true, loader);

    Method method = classToLoad.getDeclaredMethod("load");
    Object instance = classToLoad.newInstance();
    Object result = method.invoke(instance);

    Driver driver = getDriverFromInstance(loader, instance);

    Connection connection = null;

    try {
      //      connection =
      //          driver.connect("jdbc:h2://" + databaseFileName + ";ACCESS_MODE_DATA=r",
      // properties);

      connection = driver.connect("jdbc:h2://" + databaseFileName + ";ACCESS_MODE_DATA=r", properties);

      List<String> commands = executeHooks(connection, HookStage.IMPORT);

      executeHooks(connection, HookStage.EXPORT);

      classToLoad = Class.forName("org.h2.tools.Script", true, loader);

      // Connection conn, String fileName, String options1, String options2
      Class<?>[] argClasses =
                 new Class<?>[]{Connection.class, String.class, String.class, String.class};
      method = classToLoad.getDeclaredMethod("process", argClasses);
      instance = classToLoad.newInstance();

      // Connection conn, String fileName, String options1, String options2
      result = method.invoke(instance, connection, scriptFileName, "", options);
      return new ScriptResult(scriptFileName, commands);

    } finally {

      if (connection != null)
        connection.close();

      try {
        loader.close();
      } catch (Exception ex) {
        LOGGER.log(Level.FINEST, null, ex);
      }
    }
  }

  private ScriptResult createFromScript(
          DriverRecord driverRecord,
          String databaseFileName,
          String user,
          String password,
          String scriptFileName,
          String options,
          List<String> commands,
          boolean overwrite)
          throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
          IllegalAccessException, IllegalArgumentException, InvocationTargetException, SQLException,
          Exception {

    databaseFileName = databaseFileName + "." + driverRecord.buildId;

    Properties properties = new Properties();
    properties.setProperty("user", user);
    properties.setProperty("password", password);

    URL url = driverRecord.url;
    URLClassLoader loader = new URLClassLoader(new URL[]{url});

    Class classToLoad = Class.forName("org.h2.Driver", true, loader);

    Method method = classToLoad.getDeclaredMethod("load");
    Object instance = classToLoad.newInstance();
    Object result = method.invoke(instance);

    Driver driver = getDriverFromInstance(loader, instance);

    File dbFile = new File(databaseFileName + ".mv.db");
    if (dbFile.exists())
      if (dbFile.isFile() && dbFile.canWrite() && overwrite)
        dbFile.delete();
      else if (dbFile.isFile() && !(dbFile.canWrite() && overwrite))
        throw new Exception(
                "The Database File " +
                dbFile +
                " exists already and should not be overwritten automatically.");
      else
        throw new Exception(
                "The Database File " + dbFile + " points to an existing Folder or irregular .");

    Connection connection = null;
    Statement stat = null;
    try {
      connection = driver.connect("jdbc:h2://" + databaseFileName, properties);
      stat = connection.createStatement();

      stat.execute("RUNSCRIPT FROM '" + scriptFileName + "' " + options);

      executeCommands(connection, commands);

      List<String> commands1 = executeHooks(connection, HookStage.INIT);
      executeCommands(connection, commands1);

      commands.addAll(commands1);

      stat.execute("ANALYZE SAMPLE_SIZE 0");
      stat.execute("SHUTDOWN COMPACT");
    } finally {
      if (stat != null)
        stat.close();
      if (connection != null)
        connection.close();

      try {
        loader.close();
      } catch (Exception ex) {
        LOGGER.log(Level.FINEST, null, ex);
      }
    }
    return new ScriptResult(scriptFileName, commands);
  }

  public ScriptResult migrate(
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
		
	ScriptResult scriptResult =null;	

    if (databaseFileName.toLowerCase().endsWith(".mv.db")) {
      databaseFileName =
      databaseFileName.substring(0, databaseFileName.length() - ".mv.db".length());
      LOGGER.info("trimmed DB name to: " + databaseFileName);
    }

    ArrayList<String> commands = new ArrayList<>();

    DriverRecord driverRecordFrom = getDriverRecord(versionFrom);
    DriverRecord driverRecordTo = getDriverRecord(versionTo);

    if (scriptFileName == null || scriptFileName.isEmpty())
      scriptFileName = databaseFileName + ".sql";

    if (compression != null &&
        compression.endsWith("GZIP") &&
        !scriptFileName.toLowerCase().endsWith(".gz"))
      scriptFileName = scriptFileName + ".gz";
    else if (compression != null &&
             compression.endsWith("ZIP") &&
             !scriptFileName.toLowerCase().endsWith(".zip"))
      scriptFileName = scriptFileName + ".zip";

    readHooks(versionFrom);

    boolean success = false;
    try {

      scriptResult =
                   writeScript(
                           driverRecordFrom, databaseFileName, user, password, scriptFileName, compression);

      scriptFileName = scriptResult.scriptFileName;
      commands.addAll(scriptResult.commands);

      success = true;
      LOGGER.info(
              "Wrote " + driverRecordFrom.toString() + " database to script: " + scriptFileName);
    } catch (Exception ex) {
       throw new Exception(
              "Failed to write " + driverRecordFrom.toString() + " database to script",
              ex);
    }

    String options =
           compression != null && compression.length() > 0
           ? compression + " " + upgradeOptions
           : upgradeOptions;
    if (success)
      try {
         scriptResult =
                   createFromScript(
                           driverRecordTo,
                           databaseFileName,
                           user,
                           password,
                           scriptFileName,
                           options,
                           commands,
                           force);
      LOGGER.info("Created new " + driverRecordTo.toString() + " database: " + databaseFileName);

      databaseFileName = scriptResult.scriptFileName;
      commands.addAll(scriptResult.commands);

    } catch (Exception ex) {
      throw new Exception(
              "Failed to created new " + driverRecordTo.toString() + " database: " + databaseFileName,
              ex);
    }
	return scriptResult;	
  }

  public void migrateAuto(String databaseFileName) throws Exception {
    migrateAuto(
            null, databaseFileName, "SA", "", null, "COMPRESSION ZIP", "VARIABLE_BINARY", true, true);
  }

  public void migrateAuto(
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

    ArrayList<String> databaseNames = new ArrayList<>();

    FilenameFilter filenameFilter =
                   new FilenameFilter() {
             @Override
             public boolean accept(File dir, String name) {
               return name.toLowerCase().endsWith(".mv.db");
             }
           };

    File folder = new File(databaseFileName);
    if (folder.isDirectory()) {
      LOGGER.info("Will convert all H2 databases in folder " + folder.getAbsolutePath());
      for (File f : folder.listFiles(filenameFilter)) {
        String fileName = f.getCanonicalPath();
        fileName = fileName.substring(0, fileName.length() - ".mv.db".length());
        databaseNames.add(fileName);

        LOGGER.info("added DB: " + fileName);
      }
    } else {
      if (databaseFileName.toLowerCase().endsWith(".mv.db")) {
        databaseFileName =
        databaseFileName.substring(0, databaseFileName.length() - ".mv.db".length());
        LOGGER.info("trimmed DB name to: " + databaseFileName);
      }

      databaseNames.add(databaseFileName);
    }

    if (driverRecords.isEmpty())
      throw new Exception(
              "No H2 libraries found and loaded yet. Please define, where to load the H2 libraries from.");

    ArrayList<String> commands = new ArrayList<>();

    DriverRecord firstDriverRecordFrom = driverRecords.last(); // getDriverRecord(1, 4);
    DriverRecord driverRecordTo =
                 versionTo != null && versionTo.length() > 1
                 ? getDriverRecord(versionTo)
                 : driverRecords.last();

    for (String databaseName : databaseNames) {
      if (scriptFileName == null || scriptFileName.isEmpty())
        scriptFileName = databaseName + ".sql";

      if (compression != null &&
          compression.endsWith("GZIP") &&
          !scriptFileName.toLowerCase().endsWith(".gz"))
        scriptFileName = scriptFileName + ".gz";
      else if (compression != null &&
               compression.endsWith("ZIP") &&
               !scriptFileName.toLowerCase().endsWith(".zip"))
        scriptFileName = scriptFileName + ".zip";

      boolean success = false;
      NavigableSet<DriverRecord> headSet = driverRecords.headSet(firstDriverRecordFrom, true);
      for (DriverRecord driverRecordFrom : headSet.descendingSet()) {
        readHooks(driverRecordFrom.getVersion());

        try {
          ScriptResult scriptResult =
                       writeScript(
                               driverRecordFrom, databaseName, user, password, scriptFileName, compression);

          scriptFileName = scriptResult.scriptFileName;

          success = true;
          LOGGER.info(
                  "Wrote " + driverRecordFrom.toString() + " database to script: " + scriptFileName);
          break;
        } catch (Exception ex) {
          LOGGER.log(
                  Level.WARNING,
                  "Failed to write " + driverRecordFrom.toString() + " database to script",
                  ex);
        }
      }

      String options =
             compression != null && compression.length() > 0
             ? compression + " " + upgradeOptions
             : upgradeOptions;
      if (success)
        try {
        ScriptResult scriptResult =
                     createFromScript(
                             driverRecordTo,
                             databaseName,
                             user,
                             password,
                             scriptFileName,
                             options,
                             commands,
                             force);

        databaseName = scriptResult.scriptFileName;
        LOGGER.info("Created new " + driverRecordTo.toString() + " database: " + databaseName);
      } catch (Exception ex) {
        throw new Exception(
                "Failed to created new " + driverRecordTo.toString() + " database: " + databaseName,
                ex);
      } else
        throw new Exception(
                " Failed to migrate H2 DB " +
                databaseName +
                " to version  " +
                versionTo +
                " when exporting failed with all known H2 drivers.");
    }
  }

  public static void main(String[] args) throws Exception {

    Options options = new Options();

    options.addOption("l", "lib-dir", true, "(Relative) Folder containing the H2 jar files.");
    options.addOption("f", "version-from", true, "Old H2 version of the existing database.");
    options.addOption("t", "version-to", true, "New H2 version to upgrade to.");
    options.addOption(
            "d", "db-file", true, "The (relative) existing H2 database file (in the old format).");
    options.addOption("u", "user", true, "The database username.");
    options.addOption("p", "password", true, "The database password.");
    options.addOption("s", "script-file", true, "The export script file.");
    options.addOption("c", "compression", true, "The compression method [ZIP, GZIP]");
    options.addOption(
            Option.builder("o")
                    .longOpt("options")
                    .hasArgs()
                    .valueSeparator(' ')
                    .desc("The upgrade options [QUIRKS_MODE VARIABLE_BINARY]")
                    .build());
    options.addOption(null, "force", false, "Overwrite files and continue on failure.");
    options.addOption("h", "help", false, "Show the help message.");

    // create the parser
    CommandLineParser parser = new DefaultParser();
    try {
      // parse the command line arguments
      CommandLine line = parser.parse(options, args);

      if (line.getOptions().length == 0 && !GraphicsEnvironment.isHeadless()) {
        System.setProperty("awt.useSystemAAFontSettings", "lcd");
        System.setProperty("swing.aatext", "true");
        System.setProperty("prism.lcdtext", "true");

        SwingUtilities.invokeLater(
                new Runnable() {
          @Override
          public void run() {
            try {
              UIManager.setLookAndFeel(NimbusLookAndFeel.class.getName());
            } catch (ClassNotFoundException
                    | InstantiationException
                    | IllegalAccessException
                    | UnsupportedLookAndFeelException ex) {
              LOGGER.log(Level.SEVERE, "Error when setting the NIMBUS L&F", ex);
            }

            try {
              H2MigrationTool.readDriverRecords();

              H2MigrationUI frame = new H2MigrationUI();
              frame.buildUI(true);
            } catch (Exception ex) {
              LOGGER.log(Level.SEVERE, "Error when reading the H2 Database drivers", ex);
            }
          }
        });
        return;
      }

      if (line.hasOption("help") || line.getOptions().length == 0) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator((Comparator<Option>) null);
        formatter.printHelp("java -jar H2MigrationTool.jar", options, true);
        return;
      } else if (!line.hasOption("db-file"))
        throw new Exception(
                "Nothing to concert. Please define the Database to convert,\neither by providing the DB Name or the DB Folder.");

      try {
        String ressourceName =
               line.hasOption("lib-dir")
               ? getAbsoluteFileName(line.getOptionValue("lib-dir"))
               : null;

        String versionFrom =
               line.hasOption("version-from")
               ? line.getOptionValue("version-from")
               : null;
        String versionTo = line.hasOption("version-to")
               ? line.getOptionValue("version-to")
               : null;

        String databaseFileName = line.getOptionValue("db-file");
        databaseFileName = getAbsoluteFileName(databaseFileName);

        String user = line.hasOption("user")
               ? line.getOptionValue("user")
               : "sa";
        String password = line.hasOption("password")
               ? line.getOptionValue("password")
               : "";

        String scriptFileName =
               line.hasOption("script-file")
               ? line.getOptionValue("script-file")
               : "";
        if (scriptFileName != null && scriptFileName.length() > 1)
          scriptFileName = getAbsoluteFileName(scriptFileName);

        // "COMPRESSION ZIP";
        String compression =
               line.hasOption("compression")
               ? "COMPRESSION " + line.getOptionValue("compression")
               : "";

        // "VARIABLE_BINARY"
        String upgradeOptions = "";
        if (line.hasOption("options")) {
          StringBuilder stringBuffer = new StringBuilder();
          int i = 0;
          for (String s : line.getOptionValues("options")) {
            if (i > 0)
              stringBuffer.append(" ");
            stringBuffer.append(s);
            i++;
          }
          upgradeOptions = stringBuffer.toString();
        }

        boolean overwrite = line.hasOption("force");

        boolean force = line.hasOption("force");

        H2MigrationTool app = new H2MigrationTool();
        H2MigrationTool.readDriverRecords(ressourceName);

        if (versionFrom != null && versionFrom.length() > 1)
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
        else
          app.migrateAuto(
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
        LOGGER.log(Level.FINE, "Failed to migrate the database.", ex);
        throw new Exception("Failed to migrate the database.", ex);
      }

    } catch (ParseException ex) {
      LOGGER.log(Level.FINE, "Parsing failed.  Reason: " + ex.getMessage(), ex);

      HelpFormatter formatter = new HelpFormatter();
      formatter.setOptionComparator((Comparator<Option>) null);
      formatter.printHelp("java -jar H2MigrationTool.jar", options, true);

      throw new Exception("Could not parse the Command Line Arguments.", ex);
    }
  }
}
