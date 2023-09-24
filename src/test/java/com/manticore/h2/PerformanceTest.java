package com.manticore.h2;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PerformanceTest {
    private final static Logger LOGGER = Logger.getLogger(PerformanceTest.class.getName());
    private final static String TEST_FILES_URL_STR =
            "build/resources/test/com/manticore/h2/performance";

    public static List<String> getFileNameList(URL baseUrl) throws IOException {

        List<String> fileList = new ArrayList<>();
        URLConnection connection = baseUrl.openConnection();
        try (InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("<a href=")) {
                    int startIdx = line.indexOf("\"") + 1;
                    int endIdx = line.indexOf("\"", startIdx + 1);
                    String fileName = line.substring(startIdx, endIdx);
                    fileList.add(fileName);
                }
            }
        }
        return fileList;
    }

    @Test
    void testClasspath() throws Exception {
        LOGGER.info("Reading H2 Drivers");
        final int repetitions = 5;

        // get the classpath which we need to set for calling the various VMs
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("java.lang:type=Runtime");

        String classpath = (String) mbs.getAttribute(name, "ClassPath");
        LOGGER.info("Classpath: " + classpath);

        // java -jar junit-platform-console-standalone.jar --class-path <your-classpath>
        // --scan-classpath --select-class com.example.MyTest --select-method myTestMethod
    }

    @Test
    @Disabled
    void testJDKs() throws Exception {
        final StringBuilder resultBuilder = new StringBuilder();

        Pattern vendorPattern =
                Pattern.compile("Runtime Environment (.*) \\(.*", Pattern.CASE_INSENSITIVE);
        Pattern productPattern = Pattern.compile("^(.*?)\\s+version", Pattern.CASE_INSENSITIVE);
        Pattern versionPattern = Pattern.compile("\"(.*?)\"");


        /*
         * Serial Garbage Collector (SerialGC): -XX:+UseSerialGC - Enables the Serial Garbage
         * Collector.
         * 
         * Parallel Garbage Collector (ParallelGC): -XX:+UseParallelGC - Enables the Parallel
         * Garbage Collector. -XX:ParallelGCThreads=<N> - Sets the number of threads to use for
         * garbage collection.
         * 
         * Parallel Old Garbage Collector: -XX:+UseParallelOldGC - Enables the Parallel Old Garbage
         * Collector.
         * 
         * Concurrent Mark-Sweep (CMS) Garbage Collector: -XX:+UseConcMarkSweepGC - Enables the CMS
         * Garbage Collector. -XX:CMSInitiatingOccupancyFraction=<percentage> - Sets the percentage
         * of the heap occupancy that triggers the CMS collector.
         * 
         * G1 Garbage Collector: -XX:+UseG1GC - Enables the G1 Garbage Collector.
         * -XX:MaxGCPauseMillis=<milliseconds> - Sets the maximum desired pause time goal.
         * 
         * Z Garbage Collector (Introduced in Java 11 as an experimental feature and later made
         * default in Java 11 and 12): -XX:+UseZGC - Enables the Z Garbage Collector.
         * -XX:ConcGCThreads=<N> - Sets the number of threads used for concurrent phases.
         * 
         * Shenandoah Garbage Collector (Introduced in Java 12 as an experimental feature and later
         * enhanced in subsequent versions): -XX:+UseShenandoahGC - Enables the Shenandoah Garbage
         * Collector.
         * 
         * Epsilon Garbage Collector (Introduced in Java 11 as a no-op garbage collector for
         * performance testing): -XX:+UseEpsilonGC - Enables the Epsilon Garbage Collector.
         */
        final String[] JVM_OPTS = {
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseSerialGC",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseParallelGC -XX:ParallelGCThreads=4",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseConcMarkSweepGC",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:MaxGCPauseMillis=1",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -XX:ConcGCThreads=4",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseShenandoahGC",
                "-Xms4G -Xmx4G -XX:+AlwaysPreTouch -server -XX:+AgressiveOpts -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC"
        };

        final TreeMap<String, String[]> JVM_OPTS_FILTER = new TreeMap<>();
        JVM_OPTS_FILTER.put("*, 11", new String[] {"-XX:+AgressiveOpts"});
        JVM_OPTS_FILTER.put("openjdk, *", new String[] {"-XX:+AgressiveOpts"});
        JVM_OPTS_FILTER.put("GraalVM CE 17.0.8+7.1, *", new String[] {"-XX:+AgressiveOpts"});

        final TreeSet<DriverRecord> driverRecords = H2MigrationTool.readDriverRecords();
        LOGGER.info("Will test " + driverRecords.size() + " different H2 Drivers.");
        int columnCount = driverRecords.size();

        // get the classpath which we need to set for calling the various VMs
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = new ObjectName("java.lang:type=Runtime");

        String classpath = (String) mbs.getAttribute(name, "ClassPath");
        LOGGER.info("Classpath: " + classpath);

        // java -jar junit-platform-console-standalone.jar --class-path <your-classpath>
        // --scan-classpath --select-class com.example.MyTest --select-method myTestMethod

        Path parentPath = new File("/usr/lib/jvm").toPath();
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.canExecute() && pathname.getName().toLowerCase().startsWith("java")) {
                    ProcessBuilder builder =
                            new ProcessBuilder(pathname.getAbsolutePath(), "-version");
                    builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    builder.redirectErrorStream(true);
                    try {
                        Process p = builder.start();
                        String outputAsString =
                                IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
                        int exitCode = p.waitFor();
                        return exitCode == 0 && outputAsString.contains("Runtime Environment");
                    } catch (IOException | InterruptedException ignore) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        };

        final Collection<Path> javaExecutables = H2MigrationTool.findFilesInPathRecursively(
                parentPath,
                5,
                fileFilter);

        for (Path pathname : javaExecutables) {
            LOGGER.info("Found: " + pathname.toString());

            String vendor = null;
            String jdkVersion = null;

            ProcessBuilder builder = new ProcessBuilder(pathname.toString(), "-version");
            builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            builder.redirectErrorStream(true);
            try {
                Process p = builder.start();
                String outputAsString =
                        IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
                int exitCode = p.waitFor();

                Matcher vendorMatcher = vendorPattern.matcher(outputAsString);
                Matcher productMatcher = productPattern.matcher(outputAsString);
                Matcher versionMatcher = versionPattern.matcher(outputAsString);

                // Find and print vendor and version information
                if (versionMatcher.find()) {
                    vendor = vendorMatcher.find() ? vendorMatcher.group(1)
                            : (productMatcher.find() ? productMatcher.group(1) : "Unknown");
                    jdkVersion = versionMatcher.group(1);
                    LOGGER.info("Vendor: " + vendor + ", Version: " + jdkVersion);
                } else {
                    LOGGER.warning("Failed to parse JDK information.\n" + outputAsString);
                }
            } catch (IOException | InterruptedException ignore) {
                // yes, really ignore this!
            }

            if (jdkVersion != null) {
                for (String option : JVM_OPTS) {

                    ArrayList<String> args = new ArrayList<>();
                    // Java Executable
                    args.add(pathname.toString());
                    // JVM Options
                    List<String> jvmOptions = new ArrayList<>(List.of(option.split("\\s+")));
                    for (Map.Entry<String, String[]> e : JVM_OPTS_FILTER.entrySet()) {
                        // @todo: rewrite that properly
                        String[] s = e.getKey().split(",");
                        if (s[0].trim().equals("*") || s[0].trim().equalsIgnoreCase(vendor)) {
                            if (s[1].trim().equals("*") || jdkVersion.startsWith(s[1].trim())) {
                                jvmOptions.removeAll(List.of(e.getValue()));
                            }
                        }
                    }
                    args.addAll(jvmOptions);
                    // Classpath
                    args.add("-cp");
                    args.add(classpath);
                    // Class
                    args.add(PerformanceTest.class.getName());


                    builder = new ProcessBuilder(args);
                    builder.redirectOutput(ProcessBuilder.Redirect.PIPE);
                    builder.redirectErrorStream(true);
                    try {
                        Process p = builder.start();
                        String outputAsString =
                                IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
                        int exitCode = p.waitFor();

                        for (String line : outputAsString.split("\n")) {
                            resultBuilder.append(vendor).append(";").append(jdkVersion).append(";")
                                    .append(option).append(";").append(line).append(";\n");
                        }

                    } catch (IOException | InterruptedException ignore) {
                        // ignore
                    }
                }
            }
        }
        System.out.println(resultBuilder);
    }

    public static void main(String[] args) throws Exception {
        LOGGER.fine("Reading H2 Drivers");
        final int repetitions = 5;


        final TreeSet<DriverRecord> driverRecords = H2MigrationTool.readDriverRecords();
        for (Driver driver : Collections.list(DriverManager.getDrivers())) {
            LOGGER.fine("Deregister " + driver.getMajorVersion() + "." + driver.getMinorVersion());
            DriverManager.deregisterDriver(driver);
        }

        Properties properties = new Properties();
        properties.setProperty("user", "SA");
        properties.setProperty("password", "");

        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");
        Assertions.assertNotNull(engine, "Could not innate the Groovy Script Engine.");

        LinkedHashMap<String, long[]> timeInMillis = new LinkedHashMap<>();
        for (DriverRecord driverRecord : driverRecords) {
            LOGGER.fine("Test Driver " + driverRecord.toString());
            Driver driver = H2MigrationTool.loadDriver(driverRecord);
            LOGGER.fine("Loaded " + driver.getMajorVersion() + "." + driver.getMinorVersion());

            Assertions.assertEquals(driverRecord.majorVersion, driver.getMajorVersion(),
                    "Wrong Driver loaded.");
            Assertions.assertEquals(driverRecord.minorVersion, driver.getMinorVersion(),
                    "Wrong Driver loaded.");

            try (Connection conn = driver.connect(
                    "jdbc:h2:mem:performance_test",
                    properties); Statement statement = conn.createStatement()) {
                // engine.getContext().setWriter(new PrintWriter(err, true));
                // engine.getContext().setErrorWriter(new PrintWriter(out, true));
                Bindings bindings = engine.createBindings();

                bindings.put("logger", LOGGER);
                bindings.put("connection", conn);
                bindings.put("statement", statement);
                LOGGER.fine("Groovy script executed successfully.");

                File[] files = new File(TEST_FILES_URL_STR).listFiles();
                if (files != null) {
                    for (File file : files) {
                        LOGGER.fine("start test " + file.getName());
                        String content = FileUtils.readFileToString(file, Charset.defaultCharset());

                        long[] results = new long[repetitions];
                        for (int i = 0; i < repetitions; i++) {
                            long startMillis = System.currentTimeMillis();
                            try {
                                Object returnValue = engine.eval(content, bindings);
                                results[i] = System.currentTimeMillis() - startMillis;
                            } catch (Exception ex) {
                                LOGGER.log(Level.FINE, "Script failed: " + file.getName(), ex);
                            }
                        }
                        timeInMillis.put(driverRecord.getVersion(), results);
                    }
                }
            } catch (SQLException ignore) {
                // ignore
            }
        }

        for (Map.Entry<String, long[]> e : timeInMillis.entrySet()) {
            ArrayStats stats = new ArrayStats(e.getValue());
            System.out.println(e.getKey() + stats.toCSV());
        }
    }

    static class ArrayStats {
        final double average;
        final double median;
        final double variance;

        final double cv;

        public ArrayStats(long[] values) {

            // Calculate the average
            long sum = 0;
            for (long value : values) {
                sum += value;
            }
            average = (double) sum / values.length;

            // Calculate the median
            Arrays.sort(values);
            if (values.length % 2 == 0) {
                median = (double) (values[values.length / 2 - 1] + values[values.length / 2]) / 2;
            } else {
                median = values[values.length / 2];
            }

            // Calculate the variance
            double squaredDifferencesSum = 0;
            for (long value : values) {
                double difference = value - average;
                squaredDifferencesSum += difference * difference;
            }
            variance = squaredDifferencesSum / values.length;

            cv = average != 0 ? variance / average : 0;
        }

        @Override
        public String toString() {
            return "\tavg=" + average + " \tmed=" + median + " \tvar=" + variance;
        }

        public String toCSV() {
            return ";" + average + ";" + median + ";" + variance;
        }
    }
}
