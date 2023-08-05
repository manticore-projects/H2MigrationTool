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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class H2MigrationTool {

    public static final Logger LOGGER = Logger.getLogger(H2MigrationTool.class.getName());
    public static final Pattern VERSION_PATTERN = Pattern
            .compile("([0-9]+)\\.([0-9]+)\\.([0-9]+)(-([a-z0-9]{9}))?", Pattern.CASE_INSENSITIVE);

    private static final TreeSet<DriverRecord> DRIVER_RECORDS = new TreeSet<>();

    public static Set<DriverRecord> getDriverRecords() {
        return Collections.unmodifiableSet(DRIVER_RECORDS);
    }

    public final static javax.swing.filechooser.FileFilter H2_DATABASE_FILE_FILTER =
            new javax.swing.filechooser.FileFilter() {

                @Override
                public boolean accept(File file) {
                    String fileName = file.getName().toLowerCase();
                    return file.isDirectory() || fileName.endsWith(".mv.db")
                            || fileName.endsWith(".h2.db");
                }

                @Override
                public String getDescription() {
                    return "H2 Database Files";
                }

            };

    public final static javax.swing.filechooser.FileFilter SQL_SCRIPT_FILE_FILTER =
            new javax.swing.filechooser.FileFilter() {

                @Override
                public boolean accept(File file) {
                    String fileName = file.getName().toLowerCase();
                    return file.isDirectory() || fileName.endsWith(".sql")
                            || fileName.endsWith(".sql.gz")
                            || fileName.endsWith(".sql.zip");
                }

                @Override
                public String getDescription() {
                    return "SQL Script Files";
                }

            };
    private final TreeSet<Hook> hooks = new TreeSet<>();

    public static String getTempFolderName() {
        String tempPath = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();

        return tempPath;
    }

    public static File getAbsoluteFile(String filename) {
        String homePath = new File(System.getProperty("user.home")).toURI().getPath();

        String modifiedFilename = filename.replaceFirst("~", Matcher.quoteReplacement(homePath));
        modifiedFilename = modifiedFilename.replaceFirst("\\$\\{user.home}",
                Matcher.quoteReplacement(homePath));

        File f = new File(modifiedFilename);

        if (!f.isAbsolute()) {
            Path basePath = Paths.get("").toAbsolutePath();

            Path resolvedPath = basePath.resolve(modifiedFilename);
            Path absolutePath = resolvedPath.normalize();
            f = absolutePath.toFile();
        }
        return f;
    }

    public static String getAbsoluteFileName(String filename) {
        return getAbsoluteFile(filename).getAbsolutePath();
    }

    public static Collection<Path> findFilesInPathRecursively(Path parentPath, int depth,
            String prefix, String suffix) throws IOException {
        ArrayList<Path> fileNames = new ArrayList<>();
        try (Stream<Path> paths = Files.find(parentPath, depth, (path, attr) -> {
            if (attr.isRegularFile()) {
                String pathName = path.getFileName().toString().toLowerCase();
                return pathName.startsWith(prefix.toLowerCase())
                        && pathName.endsWith(suffix.toLowerCase());
            }
            return false;
        })) {
            paths.sorted().collect(Collectors.toCollection(() -> fileNames));
        }
        return fileNames;
    }

    public static Collection<Path> findFilesInPathRecursively(Path parentPath, int depth,
            FileFilter... fileFilters) throws IOException {
        ArrayList<Path> fileNames = new ArrayList<>();
        try (Stream<Path> paths = Files.find(parentPath, depth, (path, attr) -> {
            if (attr.isRegularFile()) {
                for (FileFilter fileFilter : fileFilters) {
                    if (fileFilter.accept(path.toFile())) {
                        return true;
                    }
                }
            }
            return false;
        })) {
            paths.sorted().collect(Collectors.toCollection(() -> fileNames));
        }
        return fileNames;
    }

    public static Collection<Path> findH2Drivers(String pathName) throws IOException {
        ArrayList<Path> fileNames = new ArrayList<>();

        File folder = new File(pathName);
        if (folder.exists() && folder.canRead() && folder.isDirectory()) {
            fileNames.addAll(
                    findFilesInPathRecursively(Path.of(folder.toURI()), Integer.MAX_VALUE, "h2",
                            ".jar"));
        }
        return fileNames;
    }

    public static Collection<Path> findH2Databases(String pathName, FileFilter... fileFilters)
            throws IOException {
        ArrayList<Path> fileNames = new ArrayList<>();

        File folder = new File(pathName);
        if (folder.exists() && folder.canRead() && folder.isDirectory()) {
            fileNames.addAll(
                    findFilesInPathRecursively(Path.of(folder.toURI()), Integer.MAX_VALUE,
                            fileFilters));
        }
        return fileNames;
    }

    public static TreeSet<DriverRecord> readDriverRecords() throws Exception {
        return readDriverRecords("");
    }

    public static TreeSet<DriverRecord> readDriverRecords(String resourceName) throws Exception {

        Path myPath;
        FileSystem fileSystem = null;

        if (resourceName != null && resourceName.length() > 0) {
            myPath = new File(resourceName).toPath();
        } else {
            URL resourceUrl = H2MigrationTool.class.getResource("/drivers");
            assert resourceUrl != null;
            URI resourceUri = resourceUrl.toURI();
            if (resourceUri.getScheme().equals("jar")) {
                fileSystem = FileSystems.newFileSystem(resourceUri,
                        Collections.emptyMap());
                myPath = fileSystem.getPath("/drivers");
            } else {
                myPath = Paths.get(resourceUri);
            }
        }

        for (Path path : findFilesInPathRecursively(myPath, 1, "h2", ".bin")) {
            LOGGER.info("Found H2 library " + path);
            try {
                URI resourceUri = path.toUri();
                URL url = path.toUri().toURL();

                // @todo: For any reason we can't load a Jar from inside a Jar
                // so we have to extract a local copy first
                // investigate, if the is a better solution, e. g. a special ClassLoader
                if (resourceUri.getScheme().equals("jar")) {
                    String fileName = FilenameUtils.getName(url.getPath());
                    File tmpFile = new File(System.getProperty("java.io.tmpdir"), fileName);
                    tmpFile.deleteOnExit();

                    Files.copy(url.openStream(), tmpFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING);

                    url = tmpFile.toURI().toURL();
                }
                readDriverRecord(url);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE,
                        "Failed to load the driver " + path, ex);
            }
        }

        LOGGER.fine("Driver Records loaded: " + DRIVER_RECORDS.size());

        if (fileSystem != null) {
            fileSystem.close();
        }

        return new TreeSet<>(DRIVER_RECORDS);
    }

    public static void readDriverRecord(Path path) {
        if (path != null) {
            try {
                URL url = path.toUri().toURL();
                readDriverRecord(url);
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE,
                        "Failed to load the driver " + path, ex);
            }
        }
    }

    public static void readDriverRecord(final URL url) {
        try {
            LOGGER.fine("Load Driver from: " + url.toExternalForm());
            Properties properties = new Properties();
            properties.setProperty("user", "sa");
            properties.setProperty("password", "");
            Connection connection =
                    AccessController.doPrivileged((PrivilegedExceptionAction<Connection>) () -> {
                        try (URLClassLoader loader = new URLClassLoader(new URL[] {url},
                                H2MigrationTool.class.getClassLoader())) {
                            Class<?> classToLoad = Class.forName("org.h2.Driver", true, loader);
                            Method method = classToLoad.getDeclaredMethod("load");
                            Object instance = classToLoad.getDeclaredConstructor().newInstance();
                            method.invoke(instance);

                            Driver driver = getDriverFromInstance(loader, instance);
                            return driver.connect("jdbc:h2:mem:test", properties);
                        }
                    });

            Matcher matcher = VERSION_PATTERN.matcher(url.getFile());
            if (matcher.find()) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                int patchId = Integer.parseInt(matcher.group(3));
                String buildId = matcher.groupCount() == 5 ? matcher.group(5) : "";
                DriverRecord driverRecord =
                        new DriverRecord(majorVersion, minorVersion, patchId, buildId, url);
                DRIVER_RECORDS.add(driverRecord);
                LOGGER.info(driverRecord.toString());
            }
            connection.close();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load the driver " + url.toString(), ex);
        }
    }

    private static Driver getDriverFromInstance(ClassLoader loader, Object instance) {
        Driver driver = (Driver) instance;
        if (driver.getMajorVersion() == 2) {
            // @fixme: for unknown reason, we need to load some classes explicitly with 2.0.201
            // only
            try {
                loader.loadClass("org.h2.table.InformationSchemaTable");
                loader.loadClass("org.h2.mvstore.MVMap$2");
                loader.loadClass("org.h2.mvstore.MVMap$2$1");
                loader.loadClass("org.h2.index.MetaIndex");
                loader.loadClass("org.h2.api.ErrorCode");

            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Failed to load additional classes.", ex);
            }
        }
        return driver;
    }

    public static Driver loadDriver(String version) throws Exception {
        return loadDriver("", version);
    }

    public static Driver loadDriver(String resourceStr, String version) throws Exception {
        TreeSet<DriverRecord> driverRecords = H2MigrationTool.readDriverRecords(resourceStr);

        DriverRecord driverRecord = getDriverRecord(driverRecords, version);
        return loadDriver(driverRecord);
    }

    public static Driver loadDriver(TreeSet<DriverRecord> driverRecords, String version)
            throws Exception {
        DriverRecord driverRecord = getDriverRecord(driverRecords, version);
        return loadDriver(driverRecord);
    }

    public static Driver loadDriver(DriverRecord driverRecord) throws PrivilegedActionException {
        return AccessController.doPrivileged((PrivilegedExceptionAction<Driver>) () -> {
            URL url = driverRecord.url;
            try (URLClassLoader loader =
                    new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader())) {
                Class<?> classToLoad = Class.forName("org.h2.Driver", true, loader);
                Method method = classToLoad.getDeclaredMethod("load");
                Object instance = classToLoad.getDeclaredConstructor().newInstance();
                method.invoke(instance);
                return getDriverFromInstance(loader, instance);
            }
        });
    }

    public static DriverRecord getDriverRecord(Set<DriverRecord> driverRecords,
            int majorVersion,
            int minorVersion, int patchId, String buildID) {

        for (DriverRecord r : new TreeSet<>(driverRecords).descendingSet()) {
            if (buildID == null || buildID.isEmpty()) {
                if (r.majorVersion == majorVersion && r.minorVersion == minorVersion
                        && r.patchId == patchId
                        && (r.buildId == null || r.buildId.isEmpty())) {
                    return r;
                }
            } else if (r.majorVersion == majorVersion && r.minorVersion == minorVersion
                    && r.patchId == patchId && r.buildId.equalsIgnoreCase(buildID)) {
                return r;
            }
        }
        return null;
    }

    public static DriverRecord getDriverRecord(Set<DriverRecord> driverRecords,
            int majorVersion,
            int minorVersion) {
        for (DriverRecord r : new TreeSet<>(driverRecords).descendingSet()) {
            if (r.majorVersion == majorVersion && r.minorVersion == minorVersion) {
                return r;
            }
        }
        return null;
    }

    public static DriverRecord getDriverRecord(Set<DriverRecord> driverRecords, String version)
            throws Exception {
        DriverRecord driverRecord;

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (matcher.find()) {
            if (matcher.groupCount() == 2) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));

                driverRecord = getDriverRecord(driverRecords, majorVersion, minorVersion);
            } else if (matcher.groupCount() == 3) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                int patchId = Integer.parseInt(matcher.group(3));

                driverRecord =
                        getDriverRecord(driverRecords, majorVersion, minorVersion, patchId, "");
            } else if (matcher.groupCount() == 5) {
                int majorVersion = Integer.parseInt(matcher.group(1));
                int minorVersion = Integer.parseInt(matcher.group(2));
                int patchId = Integer.parseInt(matcher.group(3));
                String buildId = matcher.group(5);

                driverRecord = getDriverRecord(driverRecords, majorVersion, minorVersion, patchId,
                        buildId);
            } else {
                throw new Exception(
                        "The provided version " + version
                                + " does not match the required format ###.###.###");
            }
        } else {
            throw new Exception(
                    "The provided version " + version
                            + " does not match the required format ###.###.###");
        }

        if (driverRecord == null) {
            throw new Exception("No H2 driver found for requestion version " + version);
        }

        return driverRecord;
    }

    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("l", "lib-dir", true, "(Relative) Folder containing the H2 jar files.");
        options.addOption("f", "version-from", true, "Old H2 version of the existing database.");
        options.addOption("t", "version-to", true, "New H2 version to upgrade to.");
        options.addOption("d", "db-file", true,
                "The (relative) existing H2 database file (in the old format).");
        options.addOption("u", "user", true, "The database username.");
        options.addOption("p", "password", true, "The database password.");
        options.addOption("s", "script-file", true, "The export script file.");
        options.addOption("c", "compression", true, "The compression method [ZIP, GZIP]");
        options.addOption(Option.builder("o").longOpt("options").hasArgs().valueSeparator(' ')
                .desc("The upgrade options [QUIRKS_MODE VARIABLE_BINARY]").build());
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

            if (line.hasOption("help") || line.getOptions().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.setOptionComparator(null);
                formatter.printHelp("java -jar H2MigrationTool.jar", options, true);
                return;
            } else if (!line.hasOption("db-file")) {
                throw new Exception(
                        "Nothing to convert. Please define the Database to convert,\neither by providing the DB Name or the DB Folder.");
            }

            try {
                String ressourceName =
                        line.hasOption("lib-dir")
                                ? getAbsoluteFileName(line.getOptionValue("lib-dir"))
                                : null;

                String versionFrom =
                        line.hasOption("version-from") ? line.getOptionValue("version-from") : null;
                String versionTo =
                        line.hasOption("version-to") ? line.getOptionValue("version-to") : null;

                String databaseFileName = line.getOptionValue("db-file");
                databaseFileName = getAbsoluteFileName(databaseFileName);

                String user = line.hasOption("user") ? line.getOptionValue("user") : "sa";
                String password = line.hasOption("password") ? line.getOptionValue("password") : "";

                String scriptFileName =
                        line.hasOption("script-file") ? line.getOptionValue("script-file") : "";
                if (scriptFileName != null && scriptFileName.length() > 1) {
                    scriptFileName = getAbsoluteFileName(scriptFileName);
                }

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
                        if (i > 0) {
                            stringBuffer.append(" ");
                        }
                        stringBuffer.append(s);
                        i++;
                    }
                    upgradeOptions = stringBuffer.toString();
                }

                boolean overwrite = line.hasOption("force");

                boolean force = line.hasOption("force");

                H2MigrationTool app = new H2MigrationTool();
                H2MigrationTool.readDriverRecords(ressourceName);

                if (versionFrom != null && versionFrom.length() > 1) {
                    app.migrate(versionFrom, versionTo, databaseFileName, user, password,
                            scriptFileName,
                            compression, upgradeOptions, overwrite, force, null);
                } else {
                    app.migrateAuto(versionTo, databaseFileName, user, password, scriptFileName,
                            compression,
                            upgradeOptions, overwrite, force);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.FINE, "Failed to migrate the database.", ex);
                throw new Exception("Failed to migrate the database.", ex);
            }

        } catch (ParseException ex) {
            LOGGER.log(Level.FINE, "Parsing failed.  Reason: " + ex.getMessage(), ex);

            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(null);
            formatter.printHelp("java -jar H2MigrationTool.jar", options, true);

            throw new Exception("Could not parse the Command Line Arguments.", ex);
        }
    }

    private void readHooks(String versionFrom) {
        hooks.clear();
    }

    private void executeCommands(Connection connection, List<String> commands) throws Exception {
        Statement st = null;
        try {
            st = connection.createStatement();
            for (String s : commands) {
                st.executeUpdate(s);
            }
            st.close();
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to close statement.", ex);
                }
            }
        }
    }

    private List<String> executeHooks(Connection connection, HookStage stage) {
        ArrayList<String> commands = new ArrayList<>();

        for (Hook hook : hooks) {
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

                        if (hook.stage.equals(HookStage.EXPORT)) {
                            try {
                                executeCommands(connection, cmds);
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, "Hook " + hook.id + " failed.", ex);
                            }
                        } else {
                            commands.addAll(cmds);
                        }
                    }
                    st.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                } finally {
                    if (st != null) {
                        try {
                            st.close();
                        } catch (SQLException ex) {
                            LOGGER.log(Level.SEVERE, "Failed to close statement.", ex);
                        }
                    }
                }
            }
        }
        return commands;
    }

    private DriverRecord getDriverRecord(String version) throws Exception {
        return getDriverRecord(H2MigrationTool.DRIVER_RECORDS, version);
    }

    private ScriptResult writeScript(DriverRecord driverRecord, String databaseFileName,
            String user,
            String password, String scriptFileName, String options, String connectionParameters)
            throws SQLException, ClassNotFoundException, NoSuchMethodException,
            InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        URL url = driverRecord.url;
        URLClassLoader loader = new URLClassLoader(new URL[] {url});

        Class<?> classToLoad = Class.forName("org.h2.Driver", true, loader);

        Method method = classToLoad.getDeclaredMethod("load");
        Object instance = classToLoad.getDeclaredConstructor().newInstance();
        Driver driver = (java.sql.Driver) method.invoke(instance);
        getDriverFromInstance(loader, instance);

        try (Connection connection = driver.connect(
                "jdbc:h2:" + databaseFileName + ";ACCESS_MODE_DATA=r" + connectionParameters,
                properties)) {
            List<String> commands = executeHooks(connection, HookStage.IMPORT);
            executeHooks(connection, HookStage.EXPORT);
            classToLoad = Class.forName("org.h2.tools.Script", true, loader);

            // processScript(String url, String user, String password, String fileName, String
            // options1,
            // String options2) throws SQLException
            if (driver.getMajorVersion() == 1 && driver.getMinorVersion() <= 3) {
                String sql = String.format("SCRIPT TO '%s' %s", scriptFileName, options);
                try (PreparedStatement st = connection.prepareStatement(sql)) {
                    st.execute(sql);
                    return new ScriptResult(scriptFileName, commands);
                }
            } else {
                Class<?>[] argClasses =
                        new Class<?>[] {Connection.class, String.class, String.class, String.class};
                method = classToLoad.getDeclaredMethod("process", argClasses);
                instance = classToLoad.getDeclaredConstructor().newInstance();

                // Connection conn, String fileName, String options1, String options2
                method.invoke(instance, connection, scriptFileName, "", options);
                return new ScriptResult(scriptFileName, commands);
            }

        }
    }

    public ScriptResult writeRecoveryScript(DriverRecord driverRecord, String folderName,
            String databaseFileName)
            throws Exception {

        String databaseName = "";
        String scriptFileName;

        if (databaseFileName.toLowerCase().endsWith(".mv.db")
                || databaseFileName.toLowerCase().endsWith(".h2.db")) {

            databaseName =
                    databaseFileName.substring(0, databaseFileName.length() - ".mv.db".length());
            LOGGER.info("Found H2 DB " + databaseName + " which will be recovered to SQL Script");

            scriptFileName = new File(folderName, databaseName + ".sql").getCanonicalPath();
        } else {
            throw new Exception("The file " + databaseName
                    + " does not seem to be a H2 database. Only *.h2.db and *.mv.db files are supported.");
        }

        final URL url = driverRecord.url;
        final String finalDatabaseName = databaseName;
        AccessController.doPrivileged((PrivilegedExceptionAction<Driver>) () -> {
            try (URLClassLoader loader =
                    new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader())) {
                Class<?> classToLoad = Class.forName("org.h2.Driver", true, loader);
                Method method = classToLoad.getDeclaredMethod("load");
                Object instance = classToLoad.getDeclaredConstructor().newInstance();
                method.invoke(instance);

                return getDriverFromInstance(loader, instance);
            }
        });

        return AccessController.doPrivileged((PrivilegedExceptionAction<ScriptResult>) () -> {
            try (URLClassLoader loader =
                    new URLClassLoader(new URL[] {url}, H2MigrationTool.class.getClassLoader())) {
                Class<?> classToLoad = Class.forName("org.h2.tools.Recover", true, loader);
                Class<?>[] argClasses = new Class<?>[] {String.class, String.class};
                Method method = classToLoad.getDeclaredMethod("execute", argClasses);
                Object instance = classToLoad.getDeclaredConstructor().newInstance();

                // public static void execute(String dir, String db) throws SQLException
                method.invoke(instance, folderName, finalDatabaseName);
                return new ScriptResult(scriptFileName, new ArrayList<>());
            }
        });
    }

    private ScriptResult createFromScript(DriverRecord driverRecord, String databaseFileName,
            String user, String password, String scriptFileName, String options,
            List<String> commands,
            boolean overwrite, String connectionParameters) throws Exception {

        String modifiedDatabaseFileName = databaseFileName + "." + driverRecord.patchId
                + (!driverRecord.buildId.isEmpty() ? ("-" + driverRecord.buildId) : "");

        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        URL url = driverRecord.url;
        URLClassLoader loader = new URLClassLoader(new URL[] {url});

        Class<?> classToLoad = Class.forName("org.h2.Driver", true, loader);

        Method method = classToLoad.getDeclaredMethod("load");
        Object instance = classToLoad.getDeclaredConstructor().newInstance();
        Driver driver = (java.sql.Driver) method.invoke(instance);
        getDriverFromInstance(loader, instance);

        File dbFile = new File(modifiedDatabaseFileName + ".mv.db");
        if (dbFile.exists()) {
            if (dbFile.isFile() && dbFile.canWrite() && overwrite) {
                boolean delete = dbFile.delete();
            } else if (dbFile.isFile() && !(dbFile.canWrite() && overwrite)) {
                throw new Exception("The Database File " + dbFile
                        + " exists already and should not be overwritten automatically.");
            } else {
                throw new Exception(
                        "The Database File " + dbFile
                                + " points to an existing Folder or irregular .");
            }
        }


        try (Connection connection =
                driver.connect("jdbc:h2:" + modifiedDatabaseFileName + connectionParameters,
                        properties);
                Statement stat = connection.createStatement()) {

            final String sqlStr = String.format("RUNSCRIPT FROM '%s' %s", scriptFileName, options);
            stat.executeUpdate(sqlStr);

            executeCommands(connection, commands);

            List<String> commands1 = executeHooks(connection, HookStage.INIT);
            executeCommands(connection, commands1);

            commands.addAll(commands1);

            stat.executeUpdate("ANALYZE SAMPLE_SIZE 0");
            stat.executeUpdate("SHUTDOWN COMPACT");
        }
        return new ScriptResult(scriptFileName, commands);
    }

    public ScriptResult migrate(String versionFrom, String versionTo, String databaseFileName,
            String user, String password, String scriptFileName, String compression,
            String upgradeOptions, boolean overwrite, boolean force, String connectionParameters)
            throws Exception {

        String modifiedDatabaseFileName = databaseFileName;
        String modifiedScriptFileName = scriptFileName;
        String modifiedCompression = compression;
        ArrayList<String> commands = new ArrayList<>();
        DriverRecord driverRecordFrom = getDriverRecord(versionFrom);
        DriverRecord driverRecordTo = getDriverRecord(versionTo);

        ScriptResult scriptResult = null;
        boolean success = false;

        if (modifiedDatabaseFileName.toLowerCase().endsWith(".mv.db")
                || modifiedDatabaseFileName.toLowerCase().endsWith(".h2.db")) {
            modifiedDatabaseFileName =
                    modifiedDatabaseFileName.substring(0,
                            modifiedDatabaseFileName.length() - ".mv.db".length());
            LOGGER.info(
                    "Found H2 DB " + modifiedDatabaseFileName
                            + " which will be exported to SQL Script");

            if (modifiedScriptFileName == null || modifiedScriptFileName.isEmpty()) {
                modifiedScriptFileName = modifiedDatabaseFileName + ".sql";
            }

            if (modifiedCompression != null && modifiedCompression.endsWith("GZIP")
                    && !modifiedScriptFileName.toLowerCase().endsWith(".gz")) {
                modifiedScriptFileName = modifiedScriptFileName + ".gz";
            } else if (modifiedCompression != null && modifiedCompression.endsWith("ZIP")
                    && !modifiedScriptFileName.toLowerCase().endsWith(".zip")) {
                modifiedScriptFileName = modifiedScriptFileName + ".zip";
            }

            readHooks(versionFrom);
            try {

                scriptResult =
                        writeScript(driverRecordFrom, modifiedDatabaseFileName, user, password,
                                modifiedScriptFileName, modifiedCompression, connectionParameters);

                modifiedScriptFileName = scriptResult.scriptFileName;
                commands.addAll(scriptResult.commands);

                success = true;
                LOGGER.info(
                        "Wrote " + driverRecordFrom + " database to script: "
                                + modifiedScriptFileName);
            } catch (Exception ex) {
                throw new Exception(
                        "Failed to write " + driverRecordFrom + " database to script",
                        ex);
            }
        } else if (modifiedDatabaseFileName.toLowerCase().endsWith(".sql")) {
            LOGGER.info(
                    "Found SQL Script " + modifiedDatabaseFileName
                            + " which will be imported directly.");

            modifiedCompression = "";

            modifiedScriptFileName = modifiedDatabaseFileName;
            modifiedDatabaseFileName =
                    modifiedDatabaseFileName.substring(0,
                            modifiedDatabaseFileName.length() - ".sql".length());
            success = true;

        } else if (modifiedDatabaseFileName.toLowerCase().endsWith(".sql.gz")) {
            LOGGER.info(
                    "Found Compressed SQL Script " + modifiedDatabaseFileName
                            + " which will be imported directly.");

            modifiedCompression = "COMPRESSION GZIP";

            modifiedScriptFileName = modifiedDatabaseFileName;
            modifiedDatabaseFileName =
                    modifiedDatabaseFileName.substring(0,
                            modifiedDatabaseFileName.length() - ".sql.gz".length());
            success = true;

        } else if (modifiedDatabaseFileName.toLowerCase().endsWith(".sql.zip")) {
            LOGGER.info(
                    "Found Compressed SQL Script " + modifiedDatabaseFileName
                            + " which will be imported directly.");

            modifiedCompression = "COMPRESSION ZIP";

            modifiedScriptFileName = modifiedDatabaseFileName;
            modifiedDatabaseFileName =
                    databaseFileName.substring(0,
                            modifiedDatabaseFileName.length() - ".sql.zip".length());
            success = true;

        } else {
            LOGGER.warning("Can't process the file " + modifiedDatabaseFileName
                    + ".\nOnly *.mv.db, *.sql, *.sql.gz or *.sql.zip files are supported.");
        }

        String options =
                modifiedCompression != null && modifiedCompression.length() > 0
                        ? modifiedCompression + " " + upgradeOptions
                        : upgradeOptions;
        if (success) {
            try {
                scriptResult = createFromScript(driverRecordTo, databaseFileName, user, password,
                        modifiedScriptFileName, options, commands, force, connectionParameters);
                LOGGER.info("Created new " + driverRecordTo + " database: "
                        + modifiedDatabaseFileName);

                modifiedDatabaseFileName = scriptResult.scriptFileName;
                commands.addAll(scriptResult.commands);

            } catch (Exception ex) {
                throw new Exception(
                        "Failed to created new " + driverRecordTo + " database: "
                                + modifiedDatabaseFileName,
                        ex);
            }
        }
        return scriptResult;
    }

    public void migrateAuto(String databaseFileName) throws Exception {
        migrateAuto(null, databaseFileName, "SA", "", null, "COMPRESSION ZIP", "VARIABLE_BINARY",
                true,
                true);
    }

    public void migrateAuto(String versionTo, String databaseFileName, String user, String password,
            String scriptFileName, String compression, String upgradeOptions, boolean overwrite,
            boolean force) throws Exception {

        ArrayList<String> databaseNames = new ArrayList<>();
        String modifiedDatabaseFileName = databaseFileName;
        String modifiedScriptFileName = scriptFileName;

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                String filename = name.toLowerCase();
                return filename.endsWith(".mv.db");
            }
        };

        File folder = new File(modifiedDatabaseFileName);
        if (folder.isDirectory()) {
            LOGGER.info("Will convert all H2 databases in folder " + folder.getAbsolutePath());
            File[] files = folder.listFiles(filenameFilter);
            if (files != null) {
                for (File f : files) {
                    String fileName = f.getCanonicalPath();
                    fileName = fileName.substring(0, fileName.length() - ".mv.db".length());
                    databaseNames.add(fileName);

                    LOGGER.info("added DB: " + fileName);
                }
            }
        } else {
            if (modifiedDatabaseFileName.toLowerCase().endsWith(".mv.db")) {
                modifiedDatabaseFileName =
                        modifiedDatabaseFileName.substring(0,
                                modifiedDatabaseFileName.length() - ".mv.db".length());
                LOGGER.info("trimmed DB name to: " + modifiedDatabaseFileName);
            } else if (modifiedDatabaseFileName.toLowerCase().endsWith(".h2.db")) {
                modifiedDatabaseFileName =
                        modifiedDatabaseFileName.substring(0,
                                modifiedDatabaseFileName.length() - ".h2.db".length());
                LOGGER.info("trimmed DB name to: " + modifiedDatabaseFileName);
            }

            databaseNames.add(modifiedDatabaseFileName);
        }

        if (DRIVER_RECORDS.isEmpty()) {
            throw new Exception(
                    "No H2 libraries found and loaded yet. Please define, where to load the H2 libraries from.");
        }

        ArrayList<String> commands = new ArrayList<>();

        DriverRecord firstDriverRecordFrom = DRIVER_RECORDS.last(); // getDriverRecord(1, 4);
        DriverRecord driverRecordTo =
                versionTo != null && versionTo.length() > 1 ? getDriverRecord(versionTo)
                        : DRIVER_RECORDS.last();

        for (String databaseName : databaseNames) {
            if (modifiedScriptFileName == null || modifiedScriptFileName.isEmpty()) {
                modifiedScriptFileName = databaseName + ".sql";
            }

            if (compression != null && compression.endsWith("GZIP")
                    && !modifiedScriptFileName.toLowerCase().endsWith(".gz")) {
                modifiedScriptFileName = modifiedScriptFileName + ".gz";
            } else if (compression != null && compression.endsWith("ZIP")
                    && !modifiedScriptFileName.toLowerCase().endsWith(".zip")) {
                modifiedScriptFileName = modifiedScriptFileName + ".zip";
            }

            boolean success = false;
            NavigableSet<DriverRecord> headSet =
                    DRIVER_RECORDS.headSet(firstDriverRecordFrom, true);
            for (DriverRecord driverRecordFrom : headSet.descendingSet()) {
                readHooks(driverRecordFrom.getVersion());

                try {
                    ScriptResult scriptResult =
                            writeScript(driverRecordFrom, databaseName, user, password,
                                    modifiedScriptFileName, compression, null);

                    modifiedScriptFileName = scriptResult.scriptFileName;

                    success = true;
                    LOGGER.info(
                            "Wrote " + driverRecordFrom + " database to script: "
                                    + modifiedScriptFileName);
                    break;
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                            "Failed to write " + driverRecordFrom
                                    + " database to script",
                            ex);
                }
            }

            String options =
                    compression != null && compression.length() > 0
                            ? compression + " " + upgradeOptions
                            : upgradeOptions;
            if (success) {
                try {
                    ScriptResult scriptResult =
                            createFromScript(driverRecordTo, databaseName, user, password,
                                    modifiedScriptFileName, options, commands, force, null);

                    databaseName = scriptResult.scriptFileName;
                    LOGGER.info("Created new " + driverRecordTo + " database: "
                            + databaseName);
                } catch (Exception ex) {
                    throw new Exception(
                            "Failed to created new " + driverRecordTo.toString() + " database: "
                                    + databaseName,
                            ex);
                }
            } else {
                throw new Exception(
                        " Failed to migrate H2 DB " + databaseName + " to version  " + versionTo
                                + " when exporting failed with all known H2 drivers.");
            }
        }
    }

    private enum HookStage {
        EXPORT, IMPORT, INIT
    }

    private static class Hook implements Comparable<Hook> {

        String id;
        HookStage stage;

        String text;

        public Hook(String name, HookStage stage, String text) {
            String lowerCaseName = name.toLowerCase();

            if (lowerCaseName.endsWith(".sql")) {
                this.id = lowerCaseName.substring(0, name.length() - 4);
            } else if (lowerCaseName.endsWith(".groovy")) {
                this.id = lowerCaseName.substring(0, lowerCaseName.length() - 7);
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Hook other = (Hook) obj;
            return Objects.equals(this.id, other.id);
        }
    }

    public static class ScriptResult {
        final String scriptFileName;
        final List<String> commands;

        public ScriptResult(String scriptFileName, List<String> commands) {
            this.scriptFileName = scriptFileName;
            this.commands = new ArrayList<>(commands);
        }
    }
}
