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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.*;

/**
 * @author Andreas Reichel <andreas@manticore-projects.com>
 */
public class H2MigrationUI extends JFrame {

  private final ImageIcon LIST_ADD_ICON =
                          new ImageIcon(
                                  ClassLoader.getSystemResource("com/manticore/icons/16/list-add.png"),
                                  "Add to the list.");
  private final ImageIcon LIST_REMOVE_ICON =
                          new ImageIcon(
                                  ClassLoader.getSystemResource("com/manticore/icons/16/list-remove.png"),
                                  "Remove from the list.");
  private final ImageIcon EDIT_FIND_ICON =
                          new ImageIcon(
                                  ClassLoader.getSystemResource("com/manticore/icons/16/edit-find.png"), "Find a File.");

  private final JTextField resourceField = new JTextField(16);
  private final JList<String> databaseFileList = new JList<>();
  private final DefaultListModel<DriverRecord> listModel = new DefaultListModel<>();
  ;
  private final JList<DriverRecord> fromVersionList = new JList<>();
  private final JList<DriverRecord> toVersionList = new JList<>();

  private final JComboBox<String> compressionBox =
                                  new JComboBox<>(new String[]{"", "ZIP", "GZIP"});
  private final JComboBox<String> repairModeBox =
                                  new JComboBox<>(new String[]{"", "REPAIR", "WORK-AROUND"});

  private final JCheckBox varbinaryBox = new JCheckBox("Convert BINARY to VARBINARY", true);
  private final JCheckBox truncateLengthBox = new JCheckBox("Truncate large lengths", true);
  private final JCheckBox overwriteBox = new JCheckBox("Overwrite", true);

  public H2MigrationUI() {
    super("H2 Database Migration Tool");
  }

  private final Action helpAction =
                       new AbstractAction("Help") {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                   throw new UnsupportedOperationException(
                           "Not supported yet."); // To change body of generated methods, choose Tools |
                   // Templates.
                 }
               };

  private final Action exitAction =
                       new AbstractAction("Exit") {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                   setVisible(false);
                 }
               };

  private final Action migrateAction =
                       new AbstractAction("Migrate") {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                 }
               };

  private final Action readDriverRecordsAction =
                       new AbstractAction("Read Drivers", EDIT_FIND_ICON) {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                   FileDialog fileDialog =
                              new FileDialog(H2MigrationUI.this, "H2 Library Folder", FileDialog.LOAD);
                   fileDialog.setFilenameFilter(
                           new FilenameFilter() {
                     @Override
                     public boolean accept(File file, String string) {
                       return string.toLowerCase().endsWith(".jar");
                     }
                   });
                   fileDialog.setMultipleMode(false);
                   fileDialog.setVisible(true);

                   File file = new File(fileDialog.getDirectory(), fileDialog.getFile());

                   if (file.isFile())
                     file = file.getParentFile();

                   resourceField.setText(file.getAbsolutePath());

                   String resourceStr = resourceField.getText();

                   try {
                     H2MigrationTool.readDriverRecords(resourceStr);
                   } catch (Exception ex) {

                   }

                   listModel.clear();
                   listModel.addAll(H2MigrationTool.driverRecords);
                 }
               };

  private final Action addDatabaseFileAction =
                       new AbstractAction("Add Database File", LIST_ADD_ICON) {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                   throw new UnsupportedOperationException(
                           "Not supported yet."); // To change body of generated methods, choose Tools |
                   // Templates.
                 }
               };

  private final Action removeDatabaseFileAction =
                       new AbstractAction("Remove Database File", LIST_REMOVE_ICON) {
                 @Override
                 public void actionPerformed(ActionEvent ae) {
                   throw new UnsupportedOperationException(
                           "Not supported yet."); // To change body of generated methods, choose Tools |
                   // Templates.
                 }
               };

  private final JButton helpButton = new JButton(helpAction);
  private final JButton exitButton = new JButton(exitAction);
  private final JButton migrateButton = new JButton(migrateAction);

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

    listModel.clear();
    listModel.addAll(H2MigrationTool.driverRecords);

    fromVersionList.setModel(listModel);
    toVersionList.setModel(listModel);

    migrateButton.setDefaultCapable(true);

    GridBagConstraints constraints =
                       new GridBagConstraints(
                               0,
                               0,
                               1,
                               1,
                               1.0,
                               1.0,
                               GridBagConstraints.BASELINE_LEADING,
                               GridBagConstraints.HORIZONTAL,
                               new Insets(2, 2, 2, 2),
                               0,
                               0);

    JPanel centerNorthPanel = new JPanel(new GridBagLayout());

    JLabel resourceLabel = new JLabel(getLabel("H2 Lib Folder", 1));
    resourceLabel.setLabelFor(resourceField);
    resourceLabel.setHorizontalAlignment(JLabel.TRAILING);
    centerNorthPanel.add(resourceLabel, constraints);

    constraints.gridx++;
    constraints.weightx = 10.0;
    constraints.gridwidth = 2;
    centerNorthPanel.add(resourceField, constraints);

    constraints.gridx += 2;
    constraints.weightx = 1.0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    JButton b = new JButton(readDriverRecordsAction);
    b.setHideActionText(true);
    // b.setBorderPainted(false);
    // b.setContentAreaFilled(false);
    centerNorthPanel.add(b, constraints);

    constraints.gridy++;
    constraints.gridx = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.BASELINE_LEADING;
    JLabel databaseFileLabel = new JLabel(getLabel("Database Files", 2));
    databaseFileLabel.setLabelFor(databaseFileList);
    databaseFileLabel.setHorizontalAlignment(JLabel.TRAILING);
    centerNorthPanel.add(databaseFileLabel, constraints);

    constraints.gridx++;
    constraints.weightx = 10.0;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.BOTH;
    centerNorthPanel.add(new JScrollPane(databaseFileList), constraints);

    constraints.gridx += 2;
    constraints.weightx = 1.0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.FIRST_LINE_START;
    JToolBar databaseFileActionsBar = new JToolBar("Database File Actions", JToolBar.VERTICAL);
    databaseFileActionsBar.setBorderPainted(false);
    databaseFileActionsBar.setFloatable(false);
    databaseFileActionsBar.add(addDatabaseFileAction).setHideActionText(true);
    databaseFileActionsBar.add(removeDatabaseFileAction).setHideActionText(true);
    centerNorthPanel.add(databaseFileActionsBar, constraints);

    constraints.gridy++;
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.BASELINE_LEADING;
    JLabel versionListLabel = new JLabel(getLabel("Versions", 2));
    versionListLabel.setHorizontalAlignment(JLabel.TRAILING);
    centerNorthPanel.add(versionListLabel, constraints);

    JScrollPane fromVersionListScrollPane = new JScrollPane(fromVersionList);

    JScrollPane toVersionListScrollPane = new JScrollPane(toVersionList);

    JPanel centerCenterPanel = new JPanel(new GridLayout(1, 0, 0, 0));
    centerCenterPanel.add(fromVersionListScrollPane);
    centerCenterPanel.add(toVersionListScrollPane);

    constraints.gridx++;
    constraints.weightx = 10.0;
    constraints.gridwidth = 2;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    centerNorthPanel.add(centerCenterPanel, constraints);

    add(centerNorthPanel, BorderLayout.CENTER);

    constraints.gridy++;
    constraints.gridx = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.BASELINE_LEADING;
    constraints.weightx = 1.0;
    constraints.insets = new Insets(2, 2, 2, 2);
    JLabel compressionLabel = new JLabel(getLabel("Compression", 0));
    compressionLabel.setLabelFor(compressionBox);
    compressionLabel.setHorizontalAlignment(JLabel.TRAILING);
    centerNorthPanel.add(compressionLabel, constraints);

    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.NONE;
    centerNorthPanel.add(compressionBox, constraints);

    constraints.gridx++;
    centerNorthPanel.add(varbinaryBox, constraints);

    constraints.gridy++;
    constraints.gridx = 0;
    constraints.gridwidth = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.BASELINE_LEADING;
    constraints.weightx = 1.0;
    constraints.insets = new Insets(2, 2, 2, 2);
    JLabel repairModeLabel = new JLabel(getLabel("Repair Mode", 0));
    repairModeLabel.setLabelFor(repairModeBox);
    repairModeLabel.setHorizontalAlignment(JLabel.TRAILING);
    centerNorthPanel.add(repairModeLabel, constraints);

    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.NONE;
    centerNorthPanel.add(repairModeBox, constraints);

    constraints.gridx++;
    centerNorthPanel.add(truncateLengthBox, constraints);

    constraints.gridy++;
    centerNorthPanel.add(overwriteBox, constraints);

    JPanel southEastPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
    southEastPanel.add(helpButton);

    JPanel southWestPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 2));
    southWestPanel.add(migrateButton);
    southWestPanel.add(exitButton);

    JPanel southPanel = new JPanel(new GridLayout(1, 2));
    southPanel.add(southEastPanel);
    southPanel.add(southWestPanel);

    add(southPanel, BorderLayout.SOUTH);

    pack();
    setVisible(visible);
  }
}
