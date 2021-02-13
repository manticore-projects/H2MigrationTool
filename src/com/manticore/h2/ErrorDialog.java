/*
 *
 * Copyright (C) 2011 Andreas Reichel <andreas@manticore-projects.com>
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package com.manticore.h2;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ErrorDialog extends JDialog {

  public JTextArea messageField = new JTextArea(3, 42);
  public JScrollPane messageScrollPane =
                     new JScrollPane(
                             messageField,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

  public JTextArea traceField = new JTextArea(16, 42);
  public JScrollPane traceScrollPane =
                     new JScrollPane(
                             traceField,
                             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                             JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

  public Action closeAction =
                new AbstractAction("Close", H2MigrationUI.DIALOG_CANCEL_16_ICON) {
          @Override
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
          }
        };

  public Action traceAction =
                new AbstractAction("Show Details", H2MigrationUI.SEARCH_16_ICON) {
          @Override
          public void actionPerformed(ActionEvent e) {
            JToggleButton b = (JToggleButton) e.getSource();
            traceScrollPane.setVisible(b.isSelected());
            pack();
          }
        };

  public Action emailAction =
                new AbstractAction("Send E-Mail", H2MigrationUI.MAIL_NEW_16_ICON) {
          @Override
          public void actionPerformed(ActionEvent e) {
            Desktop desktop;
            if (Desktop.isDesktopSupported() &&
                     (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL))
              try {
                String mailTo = URLEncoder.encode("support@manticore-projects.com", "UTF-8");
                String cc = URLEncoder.encode("andreas@manticore-projects.com", "UTF-8");
                String subject = URLEncoder.encode(messageField.getText(), "UTF-8");
                String body = URLEncoder.encode(traceField.getText(), "UTF-8");

                final String mailURIStr =
                             String.format("mailto:%s?subject=%s&cc=%s&body=%s", mailTo, subject, cc, body);
                final URI mailURI = new URI(mailURIStr);

                desktop.mail(mailURI);
              } catch (Exception ex) {
                Logger.getLogger(ErrorDialog.class.getName())
                        .log(Level.SEVERE, "Sending Email through Desktop", ex);
              }
            else
              JOptionPane.showMessageDialog(ErrorDialog.this, "Desktop Extension is not supported.");
          }
        };

  public ErrorDialog(Dialog owner, Exception exception) {
    super(owner, owner != null
            ? "Error at " + owner.getTitle()
            : "An Error occured");
    buildUI(exception);
  }

  public ErrorDialog(Frame owner, Exception exception) {
    super(owner, owner != null
            ? "Error at " + owner.getTitle()
            : "An Error occured");
    buildUI(exception);
  }

  public ErrorDialog(Window owner, Exception exception) {
    super(
            owner,
            owner instanceof Dialog
                    ? "Error at " + ((Dialog) owner).getTitle()
                    : owner instanceof Frame
                            ? "Error at " + ((Frame) owner).getTitle()
                            : "An Error occured");
    buildUI(exception);
  }

  private void buildUI(Exception exception) {
    setModal(true);
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    setLocationByPlatform(true);
    setLayout(new BorderLayout(6, 6));

    JButton closeButton = new JButton(closeAction);
    closeButton.setDefaultCapable(true);
    this.getRootPane().setDefaultButton(closeButton);

    JToggleButton stackeTraceButton = new JToggleButton(traceAction);
    JButton emailButton = new JButton(emailAction);
    emailButton.setVisible(
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.MAIL));

    messageScrollPane.setBorder(new EmptyBorder(6, 6, 0, 6));
    traceScrollPane.setVisible(false);
    traceScrollPane.setBorder(new EmptyBorder(6, 6, 0, 6));

    messageField.setText(ExceptionUtils.getRootCauseMessage(exception));
    messageField.setEditable(false);
    messageField.setOpaque(false);
    messageField.setTabSize(4);
    messageField.setLineWrap(true);
    messageField.setWrapStyleWord(true);
    messageField.setCaretPosition(0);

    StringBuilder builder = new StringBuilder();
    for (String trace : ExceptionUtils.getRootCauseStackTrace(exception))
      builder.append(trace).append("\n");
    traceField.setText(builder.toString());
    traceField.setOpaque(false);
    traceField.setEditable(false);
    traceField.setFont( Font.getFont(Font.MONOSPACED));
    traceField.setTabSize(2);
    traceField.setCaretPosition(0);

    JLabel logoLabel2 = new JLabel(H2MigrationUI.DIALOG_ERROR_64_ICON);
    logoLabel2.setIconTextGap(12);
    logoLabel2.setPreferredSize(new Dimension(96, 96));
    logoLabel2.setHorizontalAlignment(JLabel.CENTER);
    
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
                               new Insets(2, 2, 2, 2),
                               0,
                               0);

    JPanel p1 = new JPanel(new GridBagLayout());
    
    constraints.fill=GridBagConstraints.NONE;
    constraints.anchor=GridBagConstraints.CENTER;
    p1.add(logoLabel2, constraints);
    
    constraints.gridx++;
    constraints.fill=GridBagConstraints.BOTH;
    constraints.anchor=GridBagConstraints.NORTHEAST;
    constraints.weightx=10;
    constraints.weighty=2;
    p1.add(messageScrollPane, constraints);
    
    constraints.gridx=0;
    constraints.gridy++;
    constraints.fill=GridBagConstraints.BOTH;
    constraints.anchor=GridBagConstraints.NORTHEAST;
    constraints.weightx=1;
    constraints.weighty=10;
    constraints.gridwidth=2;
    p1.add(traceScrollPane, constraints);
    
    add(p1, BorderLayout.CENTER);

    JPanel pl = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
    pl.add(emailButton);
    pl.add(stackeTraceButton);
    JPanel pr = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 6));
    pr.add(closeButton);

    JPanel p = new JPanel(new GridLayout(1, 0, 6, 6));
    p.add(pl);
    p.add(pr);
    add(p, BorderLayout.SOUTH);

    pack();
    setMaximumSize(new Dimension(640, 480));

    Logger.getLogger("").log(Level.SEVERE, null, exception);

    setVisible(true);
  }

  public static void show(Dialog owner, Exception exception) {
    ErrorDialog errorDialog = new ErrorDialog(owner, exception);
    errorDialog.dispose();
  }

  public static void show(Frame owner, Exception exception) {
    ErrorDialog errorDialog = new ErrorDialog(owner, exception);
    errorDialog.dispose();
  }

  public static void show(Window owner, Exception exception) {
    ErrorDialog errorDialog = new ErrorDialog(owner, exception);
    errorDialog.dispose();
  }

  public static void show(Component component, Exception exception) {
    Window owner = SwingUtilities.getWindowAncestor(component);
    ErrorDialog errorDialog = new ErrorDialog(owner, exception);
    errorDialog.dispose();
  }
}
