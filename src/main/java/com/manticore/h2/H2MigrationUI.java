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

import com.manticore.h2.H2MigrationTool.ScriptResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class H2MigrationUI extends JFrame {

    public static final ImageIcon LIST_ADD_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/list-add.png"),
                    "Add to the list.");
    public static final ImageIcon LIST_REMOVE_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/list-remove.png"),
                    "Remove from the list.");
    public static final ImageIcon EDIT_FIND_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/edit-find.png"),
                    "Find a File.");
    public static final ImageIcon DIALOG_CANCEL_16_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/stop.png"),
                    "Cancel the Dialog.");
    public static final ImageIcon DIALOG_WARNING_16_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/dialog-warning.png"),
                    "Dialog Warning.");
    public static final ImageIcon SEARCH_16_ICON =
            new ImageIcon(ClassLoader.getSystemResource("com/manticore/icons/16/search.png"),
                    "Search.");
    public static final ImageIcon MAIL_NEW_16_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/16/mail-read.png"),
                    "Search.");
    public static final ImageIcon DIALOG_INFORMATION_64_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/64/dialog-information.png"),
                    "Dialog Information.");
    public static final ImageIcon DIALOG_ERROR_64_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/64/dialog-error.png"),
                    "Dialog Error.");
    public static final ImageIcon DIALOG_QUESTION_64_ICON =
            new ImageIcon(
                    ClassLoader.getSystemResource("com/manticore/icons/64/dialog-question.png"),
                    "Dialog Question.");
    private static final Logger LOGGER = Logger.getLogger(H2MigrationUI.class.getName());
    private static final Font MONOSPACED_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 10);
    private final DefaultListModel<File> databaseFileModel = new DefaultListModel<>();
    private final JList<File> databaseFileList = new JList<>(databaseFileModel);
    private final Action recoverAction =
            new AbstractAction("Recover") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedIndices = databaseFileList.getSelectedIndices();
                    int result = JOptionPane.NO_OPTION;

                    if (databaseFileList.getModel().getSize() > 0 && selectedIndices.length == 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Recover ALL listed H2 Database Files?",
                                        "Recovery of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    } else if (selectedIndices.length > 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Recover only the " + selectedIndices.length
                                                + " selected H2 Database Files?",
                                        "Recovery of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    }

                    if (result == JOptionPane.YES_OPTION) {
                        final JTextArea textArea = new JTextArea(24, 72);
                        textArea.setFont(MONOSPACED_FONT);

                        SwingWorker<List<File>, Entry<File, String>> worker =
                                new SwingWorker<List<File>, Entry<File, String>>() {

                                    @Override
                                    protected List<File> doInBackground() throws Exception {
                                        H2MigrationTool tool = new H2MigrationTool();
                                        H2MigrationTool.readDriverRecords();

                                        DriverRecord from = fromVersionList.getSelectedValue();

                                        ArrayList<File> databaseFiles = new ArrayList<>();
                                        ArrayList<File> failedDatabaseFiles = new ArrayList<>();

                                        if (selectedIndices.length > 0) {
                                            for (int i : selectedIndices) {
                                                databaseFiles.add(databaseFileModel.get(i));
                                            }
                                        } else {
                                            databaseFiles.addAll(
                                                    Collections.list(databaseFileModel.elements()));
                                        }

                                        for (final File f : databaseFiles) {
                                            try {
                                                ScriptResult result =
                                                        tool.writeRecoveryScript(from,
                                                                f.getParent(),
                                                                f.getName());

                                                publish(new AbstractMap.SimpleEntry<>(f,
                                                        result.scriptFileName));

                                            } catch (Exception ex) {
                                                LOGGER.log(Level.WARNING,
                                                        "Failed to recover " + f.getAbsolutePath(),
                                                        ex);
                                                failedDatabaseFiles.add(f);
                                            }
                                        }
                                        return failedDatabaseFiles;
                                    }

                                    @Override
                                    protected void process(List<Entry<File, String>> entries) {
                                        for (Entry<File, String> e : entries) {
                                            textArea.append(e.getKey().getAbsolutePath() + " → "
                                                    + e.getValue() + "\n");

                                            if (Desktop.isDesktopSupported()) {
                                                Desktop desktop = Desktop.getDesktop();
                                                if (desktop.isSupported(
                                                        Desktop.Action.BROWSE_FILE_DIR)) {
                                                    desktop.browseFileDirectory(e.getKey());
                                                } else if (desktop
                                                        .isSupported(Desktop.Action.BROWSE)) {
                                                    try {
                                                        desktop.browse(e.getKey().toURI());
                                                    } catch (IOException ex) {
                                                        LOGGER.log(Level.SEVERE, ex.getMessage(),
                                                                ex);
                                                    }
                                                } else if (desktop
                                                        .isSupported(Desktop.Action.OPEN)) {
                                                    try {
                                                        desktop.open(e.getKey());
                                                    } catch (IOException ex) {
                                                        LOGGER.log(Level.SEVERE, ex.getMessage(),
                                                                ex);
                                                    }
                                                }
                                            } else {
                                                LOGGER.warning(
                                                        "Desktop Actions are not supported.");

                                                JFileChooser chooser = new JFileChooser(e.getKey());
                                                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                                                chooser.setSelectedFile(e.getKey());
                                                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                chooser.showSaveDialog(H2MigrationUI.this);
                                            }
                                        }
                                    }

                                    @Override
                                    protected void done() {
                                        try {
                                            List<File> files = get();
                                            int n = files.size();
                                            if (n > 0) {
                                                int[] selectedIndices = new int[n];
                                                textArea.append(
                                                        "\n" + n + " have not been recovered:\n");

                                                int i = 0;
                                                for (File f : files) {
                                                    textArea.append(f.getAbsolutePath() + "\n");
                                                    selectedIndices[i++] =
                                                            databaseFileModel.indexOf(f);
                                                }
                                                databaseFileList
                                                        .setSelectedIndices(selectedIndices);
                                            } else {
                                                textArea.append("\nReady without errors.");
                                            }
                                        } catch (InterruptedException ex) {
                                            Logger.getLogger(H2MigrationUI.class.getName())
                                                    .log(Level.SEVERE, null, ex);
                                        } catch (ExecutionException ex) {
                                            Logger.getLogger(H2MigrationUI.class.getName())
                                                    .log(Level.SEVERE, null, ex);
                                        }
                                    }
                                };
                        executeAndWait(worker, H2MigrationUI.this, textArea);
                    }
                }
            };
    private final JButton recoverButton = new JButton(recoverAction);
    private final Action migrateAction =
            new AbstractAction("Migrate") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedIndices = databaseFileList.getSelectedIndices();
                    int result = JOptionPane.NO_OPTION;

                    if (databaseFileList.getModel().getSize() > 0 && selectedIndices.length == 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Migrate ALL listed H2 Database Files?",
                                        "Migration of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    } else if (selectedIndices.length > 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Migrate only the " + selectedIndices.length
                                                + " selected H2 Database Files?",
                                        "Migration of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    }

                    if (result == JOptionPane.YES_OPTION) {
                        final JTextArea textArea = new JTextArea(24, 72);
                        textArea.setFont(MONOSPACED_FONT);

                        SwingWorker<List<File>, Entry<File, String>> worker =
                                new SwingWorker<List<File>, Entry<File, String>>() {

                                    @Override
                                    protected List<File> doInBackground() throws Exception {
                                        String connectionParameters =
                                                connectionParameterField.getText();

                                        Properties properties = new Properties();
                                        properties.setProperty("user", usernameField.getText());
                                        properties.setProperty("password", passwordField.getText());

                                        H2MigrationTool tool = new H2MigrationTool();
                                        H2MigrationTool.readDriverRecords();

                                        DriverRecord from = fromVersionList.getSelectedValue();
                                        DriverRecord to = toVersionList.getSelectedValue();

                                        ArrayList<File> databaseFiles = new ArrayList<>();
                                        ArrayList<File> failedDatabaseFiles = new ArrayList<>();

                                        if (selectedIndices.length > 0) {
                                            for (int i : selectedIndices) {
                                                databaseFiles.add(databaseFileModel.get(i));
                                            }
                                        } else {
                                            databaseFiles.addAll(
                                                    Collections.list(databaseFileModel.elements()));
                                        }

                                        for (final File f : databaseFiles) {
                                            try {
                                                String versionFrom =
                                                        from != null ? from.getVersion() : "";
                                                String versionTo =
                                                        to != null ? to.getVersion() : "";

                                                String username = usernameField.getText();
                                                String password = passwordField.getText();

                                                String upgradeOptions =
                                                        quirksModeBox.isSelected() ? "QUIRKS_MODE"
                                                                : "";
                                                if (varbinaryBox.isSelected()) {
                                                    upgradeOptions +=
                                                            upgradeOptions.isEmpty()
                                                                    ? "VARIABLE_BINARY"
                                                                    : " VARIABLE_BINARY";
                                                }

                                                String compression =
                                                        (String) compressionBox.getSelectedItem();
                                                if (compression != null
                                                        && compression.length() > 0) {
                                                    compression = "COMPRESSION " + compression;
                                                }

                                                boolean overwrite = overwriteBox.isSelected();

                                                ScriptResult result =
                                                        tool.migrate(versionFrom,
                                                                versionTo,
                                                                f.getAbsolutePath(),
                                                                username,
                                                                password,
                                                                "",
                                                                compression,
                                                                upgradeOptions,
                                                                overwrite,
                                                                overwrite,
                                                                connectionParameters);

                                                publish(new AbstractMap.SimpleEntry<>(f,
                                                        result.scriptFileName));

                                            } catch (Exception ex) {
                                                LOGGER.log(Level.WARNING,
                                                        "Failed to migrate " + f.getAbsolutePath(),
                                                        ex);

                                                failedDatabaseFiles.add(f);
                                            }
                                        }
                                        return failedDatabaseFiles;
                                    }

                                    @Override
                                    protected void process(List<Entry<File, String>> entries) {
                                        for (Entry<File, String> e : entries) {
                                            textArea.append(e.getKey().getAbsolutePath() + "\n \uD83E\uDC32 "
                                                    + e.getValue() + "\n");

                                            if (Desktop.isDesktopSupported()) {
                                                Desktop desktop = Desktop.getDesktop();
                                                 if (desktop.isSupported(
                                                        Desktop.Action.BROWSE_FILE_DIR)) {
                                                    desktop.browseFileDirectory(e.getKey());
                                                } else if (desktop
                                                        .isSupported(Desktop.Action.BROWSE)) {
                                                    try {
                                                        desktop.browse(e.getKey().toURI());
                                                    } catch (IOException ex) {
                                                        LOGGER.log(Level.SEVERE, ex.getMessage(),
                                                                ex);
                                                    }
                                                } else if (desktop
                                                        .isSupported(Desktop.Action.OPEN)) {
                                                    try {
                                                        desktop.open(e.getKey());
                                                    } catch (IOException ex) {
                                                        LOGGER.log(Level.SEVERE, ex.getMessage(),
                                                                ex);
                                                    }
                                                }
                                            } else {
                                                LOGGER.warning(
                                                        "Desktop Actions are not supported.");

                                                JFileChooser chooser = new JFileChooser(e.getKey());
                                                chooser.setDialogType(JFileChooser.SAVE_DIALOG);
                                                chooser.setSelectedFile(e.getKey());
                                                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                chooser.showSaveDialog(H2MigrationUI.this);
                                            }
                                        }
                                    }

                                    @Override
                                    protected void done() {
                                        try {
                                            List<File> files = get();
                                            int n = files.size();
                                            if (n > 0) {
                                                int[] selectedIndices = new int[n];
                                                textArea.append(
                                                        "\n" + n + " have not been migrated:\n");

                                                int i = 0;
                                                for (File f : files) {
                                                    textArea.append(f.getAbsolutePath() + "\n");
                                                    selectedIndices[i++] =
                                                            databaseFileModel.indexOf(f);
                                                }
                                                databaseFileList
                                                        .setSelectedIndices(selectedIndices);
                                            } else {
                                                textArea.append("\nReady without errors.");
                                            }
                                        } catch (InterruptedException | ExecutionException ex) {
                                            LOGGER.log(Level.SEVERE, null, ex);
                                        }
                                    }
                                };
                        executeAndWait(worker, H2MigrationUI.this, textArea);
                    }
                }
            };
    private final JButton migrateButton = new JButton(migrateAction);
    private final Action removeDatabaseFileAction =
            new AbstractAction("Remove Database File", LIST_REMOVE_ICON) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedIndices = databaseFileList.getSelectedIndices();
                    for (int i = selectedIndices.length - 1; i >= 0; i--) {
                        databaseFileModel.remove(selectedIndices[i]);
                    }
                }
            };
    private final Action verifyDatabaseFileAction =
            new AbstractAction("Verify Database File", DIALOG_WARNING_16_ICON) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    int[] selectedIndices = databaseFileList.getSelectedIndices();
                    int result = JOptionPane.NO_OPTION;

                    if (databaseFileList.getModel().getSize() > 0 && selectedIndices.length == 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Verify ALL listed H2 Database Files?",
                                        "Verification of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    } else if (selectedIndices.length > 0) {
                        result =
                                JOptionPane.showConfirmDialog(
                                        H2MigrationUI.this,
                                        "Verify only the " + selectedIndices.length
                                                + " selected H2 Database Files?",
                                        "Verification of the H2 Databases",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        DIALOG_QUESTION_64_ICON);
                    }

                    if (result == JOptionPane.YES_OPTION) {
                        TreeMap<File, Collection<Recommendation>> recommendations = new TreeMap<>();
                        SwingWorker worker =
                                new SwingWorker() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        Properties properties = new Properties();
                                        properties.setProperty("user", usernameField.getText());
                                        properties.setProperty("password", passwordField.getText());

                                        DriverRecord driverRecord =
                                                fromVersionList.getSelectedValue();

                                        Driver driver = H2MigrationTool.loadDriver("",
                                                driverRecord.getVersion());

                                        ArrayList<File> databaseFiles = new ArrayList<>();
                                        if (selectedIndices.length > 0) {
                                            for (int i : selectedIndices) {
                                                databaseFiles.add(databaseFileModel.get(i));
                                            }
                                        } else {
                                            databaseFiles.addAll(
                                                    Collections.list(databaseFileModel.elements()));
                                        }

                                        for (File f : databaseFiles) {
                                            String fileName = f.getAbsolutePath();
                                            if (fileName.toLowerCase().endsWith(".mv.db")) {
                                                fileName = fileName.substring(0,
                                                        fileName.length() - ".mv.db".length());
                                            }
                                            try (Connection con = driver
                                                    .connect("jdbc:h2:" + fileName, properties)) {
                                                Collection<Recommendation> r =
                                                        MetaDataTools.verifyDecimalPrecision(con);
                                                if (!r.isEmpty()) {
                                                    if (!recommendations.containsKey(f)) {
                                                        recommendations.put(f, r);
                                                    } else {
                                                        recommendations.get(f).addAll(r);
                                                    }
                                                }
                                            }
                                        }
                                        return recommendations;
                                    }

                                    @Override
                                    protected void done() {
                                        JTextArea textArea = new JTextArea(24, 72);
                                        textArea.setFont(MONOSPACED_FONT);
                                        for (Entry<File, Collection<Recommendation>> e : recommendations
                                                .entrySet()) {
                                            textArea.append(
                                                    "------------------------------------------------------------------------------------------------\n");
                                            textArea.append("-- ");
                                            textArea.append(e.getKey().getAbsolutePath());
                                            textArea.append("\n\n");

                                            for (Recommendation r : e.getValue()) {
                                                textArea.append("-- " + r.issue + "\n");
                                                textArea.append(r.recommendation + "\n");
                                            }
                                        }
                                        textArea.setCaretPosition(0);
                                        JOptionPane.showMessageDialog(H2MigrationUI.this,
                                                new JScrollPane(textArea));
                                    }
                                };
                        executeAndWait(worker, H2MigrationUI.this);
                    }
                }
            };
    private final JTextField connectionParameterField =
            new JTextField(";MODE=Oracle;CACHE_SIZE=8192", 22);
    private final JTextField usernameField = new JTextField("SA", 8);
    private final JTextField passwordField = new JTextField("", 8);
    private final DefaultListModel<DriverRecord> listModel = new DefaultListModel<>();
    private final JList<DriverRecord> fromVersionList = new JList<>();
    private final JList<DriverRecord> toVersionList = new JList<>();
    private final JComboBox<String> compressionBox =
            new JComboBox<>(new String[] {"", "ZIP", "GZIP"});
    private final JComboBox<String> repairModeBox =
            new JComboBox<>(new String[] {"", "REPAIR", "WORK-AROUND"});
    private final JCheckBox varbinaryBox = new JCheckBox("Convert BINARY to VARBINARY", false);
    private final JCheckBox quirksModeBox = new JCheckBox("Quirks Mode", true);
    private final JCheckBox overwriteBox = new JCheckBox("Overwrite", false);
    private final Action addDatabaseFileAction =
            new AbstractAction("Add Database File", LIST_ADD_ICON) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ArrayList<Exception> exceptions = new ArrayList<>();

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.addChoosableFileFilter(H2MigrationTool.H2_DATABASE_FILE_FILTER);
                    fileChooser.addChoosableFileFilter(H2MigrationTool.SQL_SCRIPT_FILE_FILTER);
                    fileChooser.setDialogTitle("Select the H2 Database Files and Directories");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setFileHidingEnabled(false);
                    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    fileChooser.setSelectedFile(H2MigrationTool.getAbsoluteFile("~"));

                    int result = fileChooser.showOpenDialog(H2MigrationUI.this);

                    if (JFileChooser.APPROVE_OPTION == result) {
                        File[] selectedFiles = fileChooser.getSelectedFiles();
                        if (selectedFiles.length == 0) {
                            File selectedFile = fileChooser.getSelectedFile();

                            if (selectedFile.isDirectory()) {
                                try {
                                    SwingWorker<Collection<Path>, Path> worker =
                                            new SwingWorker<Collection<Path>, Path>() {
                                                @Override
                                                protected Collection<Path> doInBackground()
                                                        throws Exception {
                                                    return H2MigrationTool.findH2Databases(
                                                            selectedFile.getAbsolutePath());
                                                }
                                            };

                                    executeAndWait(worker, H2MigrationUI.this);

                                    Collection<Path> h2DatabasePaths = worker.get();
                                    for (Path p : h2DatabasePaths) {
                                        databaseFileModel.addElement(p.toFile());
                                    }
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(H2MigrationUI.class.getName())
                                            .log(Level.SEVERE, null, ex);
                                } catch (ExecutionException ex) {
                                    Logger.getLogger(H2MigrationUI.class.getName())
                                            .log(Level.SEVERE, null, ex);
                                }
                            } else {
                                LOGGER.info(selectedFile.getAbsolutePath());
                                databaseFileModel.addElement(selectedFile);
                            }

                        } else {
                            for (File f : selectedFiles) {
                                LOGGER.info(f.getAbsolutePath());
                                databaseFileModel.addElement(f);
                            }
                        }

                        if (!exceptions.isEmpty()) {
                            Exception ex = new Exception("Could not read all files.");
                            for (Exception ex1 : exceptions) {
                                ex.addSuppressed(ex1);
                            }

                            LOGGER.log(Level.WARNING, "Failed to read some files.", ex);
                        }
                    }
                }
            };
    private final Action addDatabaseDriversAction =
            new AbstractAction("Add H2 Drivers", LIST_ADD_ICON) {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ArrayList<Exception> exceptions = new ArrayList<>();

                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Select the H2 Database Files and Directories");
                    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    fileChooser.setFileHidingEnabled(false);
                    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                    fileChooser.setSelectedFile(
                            H2MigrationTool.getAbsoluteFile("~/.m2/repository/com/h2database/h2"));

                    int result = fileChooser.showOpenDialog(H2MigrationUI.this);

                    if (JFileChooser.APPROVE_OPTION == result) {
                        File[] selectedFiles = fileChooser.getSelectedFiles();
                        if (selectedFiles.length == 0) {
                            File selectedFile = fileChooser.getSelectedFile();

                            if (selectedFile.isDirectory()) {
                                try {
                                    SwingWorker<Collection<Path>, Path> worker =
                                            new SwingWorker<Collection<Path>, Path>() {
                                                @Override
                                                protected Collection<Path> doInBackground()
                                                        throws Exception {
                                                    return H2MigrationTool.findH2Drivers(
                                                            selectedFile.getAbsolutePath());
                                                }
                                            };

                                    executeAndWait(worker, H2MigrationUI.this);

                                    Collection<Path> h2DriverPaths = worker.get();
                                    for (Path p : h2DriverPaths) {
                                        try {
                                            H2MigrationTool.readDriverRecord(p);
                                        } catch (Exception ex) {
                                            exceptions.add(ex);
                                        }
                                    }

                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, null, ex);
                                }
                            } else {
                                LOGGER.info(selectedFile.getAbsolutePath());
                                try {
                                    H2MigrationTool.readDriverRecord(selectedFile.toPath());
                                } catch (Exception ex) {
                                    exceptions.add(ex);
                                }
                            }

                        } else {
                            for (File f : selectedFiles) {
                                LOGGER.info(f.getAbsolutePath());
                                try {
                                    H2MigrationTool.readDriverRecord(f.toPath());
                                } catch (Exception ex) {
                                    exceptions.add(ex);
                                }
                            }
                        }

                        if (!exceptions.isEmpty()) {
                            Exception ex = new Exception("Could not read all Driver files.");
                            for (Exception ex1 : exceptions) {
                                ex.addSuppressed(ex1);
                            }

                            LOGGER.log(Level.WARNING, "Failed to read some files.", ex);
                        }
                    }

                    listModel.clear();
                    listModel.addAll(H2MigrationTool.getDriverRecords());
                }
            };
    private final Action helpAction =
            new AbstractAction("Help") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    ErrorDialog.show(
                            H2MigrationUI.this,
                            new UnsupportedOperationException("Not supported yet."));
                }
            };
    private final Action exitAction =
            new AbstractAction("Exit") {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    setVisible(false);
                }
            };
    private final JButton helpButton = new JButton(helpAction);
    private final JButton exitButton = new JButton(exitAction);

    public H2MigrationUI() {
        super("H2 Database Migration Tool");
    }

    public static JDialog getWorkerWaitDialog(Component component) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(component);
        JOptionPane p =
                new JOptionPane(
                        "Please wait while the data are collected in the background.\nThis will take a few minutes...",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        DIALOG_INFORMATION_64_ICON);

        JDialog dialog =
                new JDialog(windowAncestor, "Operation in progress",
                        Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setLocationByPlatform(true);
        dialog.setAlwaysOnTop(true);
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dialog.add(p);
        dialog.pack();

        return dialog;
    }

    public static void executeAndWait(SwingWorker<?, ?> worker, JDialog dialog) {
        worker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
        worker.execute();

        // the dialog will be visible until the SwingWorker is done
        dialog.setVisible(true);
    }

    public static void executeAndWait(SwingWorker<?, ?> worker, Component component,
            JTextArea textArea) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(component);

        JDialog dialog =
                new JDialog(windowAncestor, "Operation in progress",
                        Dialog.ModalityType.APPLICATION_MODAL);

        final Action closeAction =
                new AbstractAction("Close") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (worker.isDone()) {
                            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            dialog.setVisible(false);
                            dialog.dispose();
                        }
                    }
                };
        closeAction.setEnabled(false);

        final Action cancelAction =
                new AbstractAction("Cancel", DIALOG_CANCEL_16_ICON) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!worker.isDone()) {
                            worker.cancel(true);
                        }

                        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        dialog.setVisible(false);
                        dialog.dispose();
                    }
                };
        cancelAction.setEnabled(true);

        JLabel iconLabel = new JLabel(DIALOG_INFORMATION_64_ICON);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        iconLabel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 2));
        buttonPanel.add(new JButton(cancelAction));
        buttonPanel.add(new JButton(closeAction));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(320, 180));
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setFocusable(false);

        textArea.setEditable(false);

        dialog.setLayout(new BorderLayout(6, 6));

        dialog.add(iconLabel, BorderLayout.WEST);
        dialog.add(scrollPane);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setLocationByPlatform(true);
        dialog.setAlwaysOnTop(true);
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        dialog.pack();

        worker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent event) {
                        if ("state".equals(event.getPropertyName())
                                && SwingWorker.StateValue.DONE == event.getNewValue()) {
                            closeAction.setEnabled(true);
                            cancelAction.setEnabled(false);
                            dialog.setCursor(Cursor.getDefaultCursor());
                        }
                    }
                });
        worker.execute();

        dialog.setVisible(true);
    }

    public static void executeAndWait(SwingWorker<?, ?> worker, Component component) {
        Window windowAncestor = SwingUtilities.getWindowAncestor(component);
        JOptionPane p =
                new JOptionPane(
                        "Please wait while the data are collected in the background.\nThis will take a few minutes...",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        DIALOG_INFORMATION_64_ICON);

        JDialog dialog =
                new JDialog(windowAncestor, "Operation in progress",
                        Dialog.ModalityType.APPLICATION_MODAL);

        dialog.setLocationByPlatform(true);
        dialog.setAlwaysOnTop(true);
        dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        dialog.add(p);
        dialog.pack();

        worker.addPropertyChangeListener(new SwingWorkerCompletionWaiter(dialog));
        worker.execute();

        // the dialog will be visible until the SwingWorker is done
        dialog.setVisible(true);
    }

    private final String getLabel(String label, int annotation) {
        switch (annotation) {
            case 1:
                return "<html><u>" + label + "</u><font color='red'>*</font>:</html>";
            case 2:
                return "<html>" + label + "<font color='blue'>*</font>:</html>";
            default:
                return label + ":";
        }
    }

    public void buildUI(boolean visible) {
        setLayout(new BorderLayout(6, 6));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                LOGGER.info("Exiting the VM when Frame as been closed.");
                System.exit(0);
            }
        });

        Color selectedTextBackGround =
                UIManager.getLookAndFeel().getDefaults()
                        .getColor("Table[Enabled+Selected].textBackground");
        Color selectedTextForeGround =
                UIManager.getLookAndFeel().getDefaults()
                        .getColor("Table[Enabled+Selected].textForeground");

        String headerText =
                "<html>"
                        + "<p>Migrate existing H2 databases to a newer H2 Version:</p>"
                        + "<ol>"
                        + "<li>Select the <b>H2 Database Files</b> (single H2 DB file or a directory)</li>"
                        + "<li>Select the  <b>Version FROM</b> and <b>Version TO</b></li>"
                        + "<li>Start <b>Migrate</b></li>"
                        + "</ol>"
                        + "</html";

        JLabel headerTextArea = new JLabel(headerText);
        headerTextArea.setBackground(selectedTextBackGround);
        headerTextArea.setForeground(selectedTextForeGround);
        headerTextArea.setOpaque(true);
        headerTextArea.setBorder(new EmptyBorder(6, 6, 6, 6));
        add(headerTextArea, BorderLayout.NORTH);

        databaseFileList.setPrototypeCellValue(new File(String.copyValueOf(new char[255])));

        listModel.clear();
        listModel.addAll(H2MigrationTool.getDriverRecords());

        fromVersionList.setModel(listModel);
        fromVersionList.setSelectedValue(
                H2MigrationTool.getDriverRecord(H2MigrationTool.getDriverRecords(), 1, 4), true);

        toVersionList.setModel(listModel);
        toVersionList.setSelectedValue(
                H2MigrationTool.getDriverRecord(H2MigrationTool.getDriverRecords(), 2, 0), true);

        compressionBox.setSelectedIndex(1);

        migrateButton.setDefaultCapable(true);

        GridBagConstraints constraints =
                new GridBagConstraints(
                        0,
                        0,
                        1,
                        1,
                        1.0,
                        10.0,
                        GridBagConstraints.BASELINE_LEADING,
                        GridBagConstraints.HORIZONTAL,
                        new Insets(6, 6, 0, 0),
                        0,
                        0);

        JPanel centerNorthPanel = new JPanel(new GridBagLayout());

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.weighty = 1;
        JLabel databaseFileLabel = new JLabel(getLabel("Database Files", 1));
        databaseFileLabel.setLabelFor(databaseFileList);
        databaseFileLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(databaseFileLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 5.0;
        constraints.gridwidth = 3;
        constraints.gridheight = 2;
        constraints.fill = GridBagConstraints.BOTH;
        centerNorthPanel.add(new JScrollPane(databaseFileList), constraints);

        constraints.gridx += 3;
        constraints.weightx = 1.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets.left = 0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;

        JToolBar databaseFileActionsBar = new JToolBar("Database File Actions", JToolBar.VERTICAL);
        databaseFileActionsBar.setBorderPainted(false);
        databaseFileActionsBar.setFloatable(false);
        databaseFileActionsBar.add(addDatabaseFileAction).setHideActionText(true);
        databaseFileActionsBar.add(removeDatabaseFileAction).setHideActionText(true);
        databaseFileActionsBar.add(verifyDatabaseFileAction).setHideActionText(true);
        centerNorthPanel.add(databaseFileActionsBar, constraints);

        JLabel scriptLabel = new JLabel(getLabel("SQL Script Files", 2));
        scriptLabel.setHorizontalAlignment(JLabel.TRAILING);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        constraints.weighty = 100;
        databaseFileLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(scriptLabel, constraints);

        // connectionParameterField
        constraints.insets.left = 6;
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel connectionParameterLabel = new JLabel(getLabel("Con. Param.", 2));
        connectionParameterLabel.setLabelFor(connectionParameterField);
        connectionParameterLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(connectionParameterLabel, constraints);

        constraints.gridx++;
        constraints.gridwidth = 3;
        centerNorthPanel.add(connectionParameterField, constraints);


        constraints.insets.left = 6;
        constraints.gridx = 0;
        constraints.gridy++;
        constraints.weighty = 1;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel usernameLabel = new JLabel(getLabel("Username", 2));
        usernameLabel.setLabelFor(usernameField);
        usernameLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(usernameLabel, constraints);

        constraints.gridx++;
        centerNorthPanel.add(usernameField, constraints);

        constraints.gridx++;
        JLabel passwordLabel = new JLabel(getLabel("Password", 2));
        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(passwordLabel, constraints);

        constraints.gridx++;
        centerNorthPanel.add(passwordField, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.weightx = 3.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        JLabel versionListLabel = new JLabel(getLabel("Versions", 2));
        versionListLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(versionListLabel, constraints);

        JToolBar databaseDriverActionsBar =
                new JToolBar("Database Driver Actions", JToolBar.VERTICAL);
        databaseDriverActionsBar.setBorderPainted(false);
        databaseDriverActionsBar.setFloatable(false);
        databaseDriverActionsBar.add(addDatabaseDriversAction).setHideActionText(true);

        JScrollPane fromVersionListScrollPane = new JScrollPane(fromVersionList);

        JLabel fromVersionPaneLabel = new JLabel("From H2 Version");
        fromVersionPaneLabel.setHorizontalAlignment(JLabel.CENTER);
        fromVersionPaneLabel.setFont(fromVersionPaneLabel.getFont().deriveFont(Font.BOLD, 9f));

        JPanel fromVersionListPane = new JPanel(new BorderLayout());
        fromVersionListPane.add(fromVersionPaneLabel, BorderLayout.NORTH);
        fromVersionListPane.add(fromVersionListScrollPane, BorderLayout.CENTER);

        JScrollPane toVersionListScrollPane = new JScrollPane(toVersionList);

        JLabel toVersionPaneLabel = new JLabel("To H2 Version");
        toVersionPaneLabel.setHorizontalAlignment(JLabel.CENTER);
        toVersionPaneLabel.setFont(toVersionPaneLabel.getFont().deriveFont(Font.BOLD, 9f));

        JPanel toVersionListPane = new JPanel(new BorderLayout());
        toVersionListPane.add(toVersionPaneLabel, BorderLayout.NORTH);
        toVersionListPane.add(toVersionListScrollPane, BorderLayout.CENTER);

        JPanel centerCenterPanel = new JPanel(new GridLayout(1, 0, 6, 6));
        centerCenterPanel.add(fromVersionListPane);
        centerCenterPanel.add(toVersionListPane);

        constraints.gridx++;
        constraints.weightx = 10.0;
        constraints.gridwidth = 3;
        constraints.fill = GridBagConstraints.BOTH;
        centerNorthPanel.add(centerCenterPanel, constraints);

        constraints.gridx += 3;
        constraints.weightx = 1.0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.NONE;
        constraints.insets.left = 0;
        constraints.anchor = GridBagConstraints.FIRST_LINE_START;

        centerNorthPanel.add(databaseDriverActionsBar, constraints);

        add(centerNorthPanel, BorderLayout.CENTER);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        JLabel compressionLabel = new JLabel(getLabel("Compression", 0));
        compressionLabel.setLabelFor(compressionBox);
        compressionLabel.setHorizontalAlignment(JLabel.TRAILING);
        centerNorthPanel.add(compressionLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 1.0;
        constraints.gridwidth = 2;
        constraints.fill = GridBagConstraints.NONE;
        centerNorthPanel.add(compressionBox, constraints);

        JLabel checkBoxPaneLabel = new JLabel("Other Options");
        checkBoxPaneLabel.setHorizontalAlignment(JLabel.CENTER);
        checkBoxPaneLabel.setFont(checkBoxPaneLabel.getFont().deriveFont(Font.BOLD, 9f));

        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        checkBoxPanel.add(checkBoxPaneLabel);
        checkBoxPanel.add(varbinaryBox);
        checkBoxPanel.add(quirksModeBox);
        checkBoxPanel.add(overwriteBox);

        constraints.gridx++;
        constraints.gridheight = 4;
        constraints.gridwidth = 2;
        centerNorthPanel.add(checkBoxPanel, constraints);

        constraints.gridy++;
        constraints.gridx = 0;
        constraints.gridwidth = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.BASELINE_LEADING;
        constraints.weightx = 1.0;
        JLabel repairModeLabel = new JLabel(getLabel("Repair Mode", 0));
        repairModeLabel.setLabelFor(repairModeBox);
        repairModeLabel.setHorizontalAlignment(JLabel.TRAILING);
        // centerNorthPanel.add(repairModeLabel, constraints);

        constraints.gridx++;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.NONE;
        // centerNorthPanel.add(repairModeBox, constraints);

        JPanel southEastPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        southEastPanel.add(helpButton);

        JPanel southWestPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 2));
        southWestPanel.add(recoverButton);
        southWestPanel.add(migrateButton);
        southWestPanel.add(exitButton);

        JPanel southPanel = new JPanel(new GridLayout(1, 2));
        southPanel.add(southEastPanel);
        southPanel.add(southWestPanel);

        add(southPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(480, 720));
        pack();
        setMinimumSize(getSize());
        setVisible(visible);
    }

    private static class SwingWorkerCompletionWaiter implements PropertyChangeListener {

        private final JDialog dialog;

        public SwingWorkerCompletionWaiter(JDialog dialog) {
            this.dialog = dialog;
        }

        public void propertyChange(PropertyChangeEvent event) {
            if ("state".equals(event.getPropertyName())
                    && SwingWorker.StateValue.DONE == event.getNewValue()) {
                dialog.setCursor(Cursor.getDefaultCursor());
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
}
