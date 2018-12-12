package tango.gui;

import com.mongodb.BasicDBObject;
import ij.IJ;
import ij.Prefs;
import ij.plugin.BrowserLauncher;
import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.exceptionPrinter;
import tango.helper.HelpManager;
import tango.helper.Helper;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.mongo.MongoConnector;
import tango.parameter.*;
import tango.util.SystemEnvironmentVariable;
import tango.util.Utils;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Dimension;
import javax.swing.SwingConstants;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class Connector extends javax.swing.JPanel {

    Core core;
    boolean connecting;
    String currentUser;
    public DoubleParameter magnitude = new DoubleParameter("Zoom Magnitude:", "zoom", 4d, Parameter.nfDEC1);
    public static SliderParameter maxThreads = new SliderParameter("Max Threads:", "maxThreads", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static SliderParameter maxCellsProcess = new SliderParameter("Max Cells (process):", "maxCellsProcess", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static SliderParameter maxCellsMeasure = new SliderParameter("Max Cells (measure):", "maxCellsMeasure", 1, Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors());
    public static BooleanParameter recordWindowsPosition = new BooleanParameter("Record Cell Image Windows Position:", "windowPos", true);
    public static ChoiceParameter roiColor = new ChoiceParameter("ROI Color", "RoiColor", new String[]{"Red", "Yellow", "Blue", "Green"}, "Red"); //, "Structure Color"
    public static BooleanParameter openSegmentedImages = new BooleanParameter("Open Segmented Images:", "openSegmentedImages", true);
    
    //public BooleanParameter multithreadProcess = new BooleanParameter("Multithread Cell Process", "multithreadProcess", true);
    Parameter[] parameters = new Parameter[]{maxThreads, maxCellsProcess, maxCellsMeasure, magnitude,recordWindowsPosition, roiColor, openSegmentedImages};
    Parameter[] parametersGeneral = new Parameter[] {maxThreads, maxCellsProcess, maxCellsMeasure};
    Parameter[] parametersImage = new Parameter[]{magnitude,recordWindowsPosition, roiColor, openSegmentedImages};
    public GroupParameter options = new GroupParameter("Options", "options", parameters);
    
    public Connector(Core core) {
        this.core = core;
        initComponents();
        
        //options.setHelp("General options. Saved For the current user", true);
        magnitude.setHelp("Zoom magnitude factor when opening cell images (buttons overlay/ open structures)", true);
        maxThreads.setHelp("Limits the maximium number of threads used dureing process & measurements", true);
        maxCellsProcess.setHelp("During cell process: one cell is processed by one thread. If this causes out of memory errors, lower this value", true);
        maxCellsMeasure.setHelp("During cell quantifications: one cell is processed by one thread. If this causes out of memory errors, lower this value", true);
        recordWindowsPosition.setHelp("Record cell image windows position when browsing cells in the data tab.", true);
        roiColor.setHelp("Color of displayed ROIs (Region-Of-Interest, contours of objects) on the active image. ", true); //If Structure Color is selected, the color of the ROI will be the color of the corresponding structure
        openSegmentedImages.setHelp("If selected, when displaying an image both the raw and segmented images will be opened, if no only the raw image will be opened", true);
        toggleEnableButtons(false, false);
        //JPanel mainSettingsPanel = new JPanel(new FlowLayout());
        //options.addToContainer(mainSettingsPanel);
        //this.optionPanel.add(mainSettingsPanel);
        for (Parameter p : parametersGeneral) p.addToContainer(optionPanel);
        for (Parameter p : parametersImage) p.addToContainer(optionImagePanel);
        
    }
    
    private String trim(String s, int size) {
        System.out.println("trim:"+s+ " size:"+size);
        if (s.length()>size) return "..."+s.substring(size-3);
        else return s;
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(host, new ID(RetrieveHelp.connectPage, "Host"));
        hm.objectIDs.put(hostLabel, new ID(RetrieveHelp.connectPage, "Host"));
        hm.objectIDs.put(connect, new ID(RetrieveHelp.connectPage, "Connect_2"));
        hm.objectIDs.put(helpButton, new ID(RetrieveHelp.connectPage, "Help"));
        hm.objectIDs.put(userLabel, new ID(RetrieveHelp.connectPage, "User"));
        hm.objectIDs.put(usernames, new ID(RetrieveHelp.connectPage, "User"));
        hm.objectIDs.put(newUser, new ID(RetrieveHelp.connectPage, "New_User"));
        hm.objectIDs.put(deleteUsr, new ID(RetrieveHelp.connectPage, "Delete_User"));
        hm.objectIDs.put(importData, new ID(RetrieveHelp.connectPage, "Import_Data"));
        hm.objectIDs.put(exportData, new ID(RetrieveHelp.connectPage, "Export_Data"));
        hm.objectIDs.put(exportInput, new ID(RetrieveHelp.connectPage, "Export_Input_Images"));
        hm.objectIDs.put(exportOutput, new ID(RetrieveHelp.connectPage, "Export_Ouput_Images"));
        hm.objectIDs.put(importSettings, new ID(RetrieveHelp.connectPage, "Import_Processing_Chains_Templates"));
        hm.objectIDs.put(exportSettings, new ID(RetrieveHelp.connectPage, "Export_Processing_Chains_Templates"));
    }
    
    public static Color getRoiColor() {
        int i = roiColor.getSelectedIndex();
        switch (i) {
            case 0 : return Color.red;
            case 1 : return Color.yellow;
            case 2 : return Color.blue;
            case 3 : return Color.green;
        }
        return Color.white;
    }

    private void getUsers() {
        this.usernames.removeAllItems();
        usernames.addItem("");
        // login user
        for (String key : Core.mongoConnector.getUsers()) {
            usernames.addItem(key);
        }
    }

    public void toggleIsRunning(boolean isRunning) {
        this.connect.setEnabled(!isRunning);
        toggleEnableButtons(!isRunning, !isRunning);
    }

    private void toggleEnableButtons(boolean connected, boolean userSet) {
        if (!connected) {
            this.newUser.setEnabled(false);
            this.usernames.setEnabled(false);
            this.helpButton.setEnabled(false);
        }
        if (connected) {
            this.newUser.setEnabled(true);
            this.helpButton.setEnabled(true);
            this.usernames.setEnabled(true);
        }
        if (!connected || (connected && !userSet)) {
            this.deleteUsr.setEnabled(false);
            exportData.setEnabled(false);
            importData.setEnabled(false);
            exportSettings.setEnabled(false);
            importSettings.setEnabled(false);
        }
        if (connected && userSet) {
            this.deleteUsr.setEnabled(true);
            exportData.setEnabled(true);
            importData.setEnabled(true);
            exportSettings.setEnabled(true);
            importSettings.setEnabled(true);
        }
    }

    private void connect() {
        connecting = true;
        try {
            System.out.println(mongoUser.getText() + mongoPwd.getText());
            Core.mongoConnector = new MongoConnector(host.getText(), mongoUser.getText(), mongoPwd.getText());
            if (!Core.mongoConnector.isConnected()) {
                toggleEnableButtons(false, false);
                return;
            }
            toggleEnableButtons(true, false);
            SystemEnvironmentVariable mongoHost = new SystemEnvironmentVariable("mongoHost", host.getText(), false, false, false);
            mongoHost.writeToPrefs();
            getUsers();
            if (usernames.getItemCount() > 0) {
                SystemEnvironmentVariable mongoUser = new SystemEnvironmentVariable("mongoUser", null, false, false, false);
                String user = mongoUser.getValue();
                if (user!=null && user.length() != 0 && Utils.contains(usernames, user, true)) {
                    setUser(user);
                }
                mongoUser.writeToPrefs();
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
        connecting = false;
    }

    public String getHost() {
        String h = this.host.getText();
        if (h == null || h.equals("")) {
            h = "localhost";
        }
        return h;
    }

    private void setUser(String usr) {
        if (Utils.contains(usernames, usr, true)) {
            usernames.setSelectedItem(usr);
        }
        BasicDBObject user = Core.mongoConnector.setUser(usr, false);
        if (user != null) {
            currentUser = usr;
            Object userHost = user.get("options_" + this.getHost());
            if (userHost == null) {
                userHost = new BasicDBObject();
                user.append("options_" + this.getHost(), userHost);
            }
            options.dbGet((BasicDBObject) userHost);
            ImageHandler.defZoomFactor=magnitude.getDoubleValue(1);
            SystemEnvironmentVariable mongoUser = new SystemEnvironmentVariable("mongoUser", usr, false, false, false);
            mongoUser.writeToPrefs();
            core.connect();
            toggleEnableButtons(true, true);
        } else {
            currentUser = null;
            core.disableTabs();
            toggleEnableButtons(true, false);
        }
    }

    public void saveOptions() {
        if (currentUser != null) {
            BasicDBObject user = Core.mongoConnector.getUser();
            BasicDBObject userHost = new BasicDBObject();
            options.dbPut(userHost);
            user.append("options_" + this.getHost(), userHost);
            Core.mongoConnector.saveUser(user);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionPanel = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        host = new javax.swing.JTextField();
        host.setToolTipText("Address of MongoDB");
        mongoUserLabel = new javax.swing.JLabel();
        mongoUser = new javax.swing.JTextField();
        mongoPwdLabel = new javax.swing.JLabel();
        mongoPwdLabel.setHorizontalAlignment(SwingConstants.LEFT);
        mongoPwd = new javax.swing.JTextField();
        connect = new javax.swing.JButton();
        usernames = new javax.swing.JComboBox();
        userLabel = new javax.swing.JLabel();
        newUser = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        deleteUsr = new javax.swing.JButton();
        websiteButton = new javax.swing.JButton();
        ExportImportPanel = new javax.swing.JPanel();
        exportInput = new javax.swing.JCheckBox();
        exportOutput = new javax.swing.JCheckBox();
        exportData = new javax.swing.JButton();
        importData = new javax.swing.JButton();
        exportImportPCPanel = new javax.swing.JPanel();
        exportSettings = new javax.swing.JButton();
        importSettings = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        optionImagePanel = new javax.swing.JPanel();

        setMaximumSize(new java.awt.Dimension(1024, 600));
        setMinimumSize(new java.awt.Dimension(1024, 600));
        setPreferredSize(new Dimension(1132, 703));

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("MongoDB Connection"));

        hostLabel.setText("Host:");

        host.setText((String) Prefs.get(tango.mongo.MongoConnector.getPrefix() + "_mongoHost.String", tango.mongo.MongoConnector.defaultHost_DB));
        host.setMaximumSize(new java.awt.Dimension(152, 25));
        host.setMinimumSize(new java.awt.Dimension(152, 25));
        host.setPreferredSize(new java.awt.Dimension(152, 25));
        host.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostActionPerformed(evt);
            }
        });
        mongoUserLabel.setText("Mongo User:");
        mongoUser.setToolTipText("Authorized user of MongoDB");
        mongoUser.setMaximumSize(new java.awt.Dimension(152, 25));
        mongoUser.setMinimumSize(new java.awt.Dimension(152, 25));
        mongoUser.setPreferredSize(new java.awt.Dimension(152, 25));
        //mongoUser.setColumns(10);

        mongoPwdLabel.setText("Password:");
        mongoPwd.setToolTipText("Password of authorized MongoDB user");
        mongoPwd.setMaximumSize(new java.awt.Dimension(152, 25));
        mongoPwd.setMinimumSize(new java.awt.Dimension(152, 25));
        mongoPwd.setPreferredSize(new java.awt.Dimension(152, 25));
        //mongoPwd.setColumns(10);

        connect.setText("Connect");
        connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connectActionPerformed(evt);
            }
        });

        usernames.setMaximumSize(new java.awt.Dimension(152, 25));
        usernames.setMinimumSize(new java.awt.Dimension(152, 25));
        usernames.setPreferredSize(new java.awt.Dimension(152, 25));
        usernames.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                usernamesItemStateChanged(evt);
            }
        });

        userLabel.setText("User:");

        newUser.setText("Add user");
        newUser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newUserActionPerformed(evt);
            }
        });

        helpButton.setText("Help!");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        deleteUsr.setText("Delete User");
        deleteUsr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUsrActionPerformed(evt);
            }
        });

        websiteButton.setText("Website");
        websiteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                websiteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
                connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(connect, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                                                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                                                .addComponent(hostLabel)
                                                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                                                .addComponent(host, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                                                                                        .addComponent(mongoPwdLabel)
                                                                                        .addComponent(mongoUserLabel))
                                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                                                                                        .addGroup(Alignment.TRAILING, connectionPanelLayout.createSequentialGroup()
                                                                                                .addComponent(mongoUser, GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                                                                                                .addPreferredGap(ComponentPlacement.RELATED))
                                                                                        .addComponent(mongoPwd, GroupLayout.PREFERRED_SIZE, 108, Short.MAX_VALUE))))
                                                                .addGap(550))
                                                        .addComponent(newUser, GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE))
                                                .addContainerGap())
                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                                .addComponent(userLabel)
                                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                                .addComponent(usernames, 0, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(0, 571, Short.MAX_VALUE))
                                                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                                                .addComponent(helpButton, GroupLayout.PREFERRED_SIZE, 109, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18)
                                                                .addComponent(websiteButton, GroupLayout.DEFAULT_SIZE, 654, Short.MAX_VALUE))
                                                        .addComponent(deleteUsr, GroupLayout.DEFAULT_SIZE, 781, Short.MAX_VALUE))
                                                .addGap(20))))
        );
        connectionPanelLayout.setVerticalGroup(
                connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(connectionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(host, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(hostLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18)
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(mongoUser, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(mongoUserLabel))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(mongoPwdLabel)
                                        .addComponent(mongoPwd, GroupLayout.PREFERRED_SIZE, 26, Short.MAX_VALUE))
                                .addGap(6)
                                .addComponent(connect)
                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(usernames, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(userLabel))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(newUser)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(deleteUsr)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(helpButton)
                                        .addComponent(websiteButton))
                                .addGap(34))
        );

        ExportImportPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Import/Export Data"));

        exportInput.setSelected(true);
        exportInput.setText("Export Input Images");

        exportOutput.setSelected(true);
        exportOutput.setText("Export Ouput Images");

        exportData.setText("Export Data");
        exportData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDataActionPerformed(evt);
            }
        });

        importData.setText("Import Data");
        importData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDataActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ExportImportPanelLayout = new javax.swing.GroupLayout(ExportImportPanel);
        ExportImportPanel.setLayout(ExportImportPanelLayout);
        ExportImportPanelLayout.setHorizontalGroup(
                ExportImportPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(ExportImportPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(ExportImportPanelLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(exportData, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                                        .addGroup(ExportImportPanelLayout.createSequentialGroup()
                                                .addGroup(ExportImportPanelLayout.createParallelGroup(Alignment.LEADING)
                                                        .addComponent(exportOutput)
                                                        .addComponent(exportInput))
                                                .addGap(0, 78, Short.MAX_VALUE))
                                        .addComponent(importData, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                                .addContainerGap())
        );
        ExportImportPanelLayout.setVerticalGroup(
                ExportImportPanelLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(ExportImportPanelLayout.createSequentialGroup()
                                .addComponent(exportInput)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(exportOutput)
                                .addGap(52)
                                .addComponent(exportData)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(importData)
                                .addContainerGap(173, Short.MAX_VALUE))
        );
        ExportImportPanel.setLayout(ExportImportPanelLayout);
        exportImportPCPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Import/Export Processing Chains"));

        exportSettings.setText("Export Processing Chains");
        exportSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSettingsActionPerformed(evt);
            }
        });

        importSettings.setText("Import Processing Chains");
        importSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importSettingsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout exportImportPCPanelLayout = new javax.swing.GroupLayout(exportImportPCPanel);
        exportImportPCPanel.setLayout(exportImportPCPanelLayout);
        exportImportPCPanelLayout.setHorizontalGroup(
            exportImportPCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportImportPCPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(exportImportPCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(importSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        exportImportPCPanelLayout.setVerticalGroup(
            exportImportPCPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(exportImportPCPanelLayout.createSequentialGroup()
                .addComponent(exportSettings)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(importSettings))
        );

        optionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Options"));
        optionPanel.setLayout(new javax.swing.BoxLayout(optionPanel, javax.swing.BoxLayout.PAGE_AXIS));

        optionImagePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Image Display Options"));
        optionImagePanel.setLayout(new javax.swing.BoxLayout(optionImagePanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        layout.setHorizontalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(30)
                                .addComponent(connectionPanel, GroupLayout.PREFERRED_SIZE, 417, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING, false)
                                        .addComponent(ExportImportPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(exportImportPCPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, 457, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(optionImagePanel, GroupLayout.PREFERRED_SIZE, 457, GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(optionImagePanel, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
                                                .addGap(0, 379, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                                                        .addComponent(ExportImportPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(connectionPanel, GroupLayout.PREFERRED_SIZE, 416, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(ComponentPlacement.RELATED)
                                                .addComponent(exportImportPCPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(322))))
        );
        this.setLayout(layout);
    }// </editor-fold>//GEN-END:initComponents

    private void exportDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDataActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        ArrayList<String> folders = Core.mongoConnector.getProjects();
        Object[] choice = new Object[folders.size() + 1];
        choice[0] = "";
        for (int i = 0; i < folders.size(); i++) {
            choice[i + 1] = folders.get(i);
        }
        Object selectedFolder = JOptionPane.showInputDialog(
                this,
                "Choose Project to export :",
                "Tango",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choice,
                "");
        String folder = (String) selectedFolder;
        if (folder != null && folder.length() > 0) {
            File dir = Utils.chooseDir("Select Export directory", null);
            if (dir != null) {
                Core.mongoConnector.mongoDumpProject(folder, dir.getAbsolutePath(), this.exportInput.isSelected(), exportOutput.isSelected());
            }
        }
    }//GEN-LAST:event_exportDataActionPerformed

    private void importDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDataActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = Utils.chooseDir("Select Import directory containing .bson files", null);
        if (dir != null) {
            String name = JOptionPane.showInputDialog("Project name (no special char):");
            if (name==null) return;
            if (!Utils.isValid(name, false)) IJ.error("Invalid name");
            if (name.length()>30) IJ.error("Name length must be <30");
            if (!Core.mongoConnector.getProjects().contains(name)) {
                try {
                    Core.mongoConnector.mongoRestoreProject(dir.getAbsolutePath(), name);
                    connect();
                } catch (Exception e) {
                    exceptionPrinter.print(e, "", Core.GUIMode);
                }

            } else {
                IJ.error("Project already exists...");
            }
        }
    }//GEN-LAST:event_importDataActionPerformed

    private void exportSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSettingsActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = Utils.chooseDir("Select Export directory", null);
        if (dir != null) {
            Core.mongoConnector.mongoDumpSettings(dir.getAbsolutePath());
        }
    }//GEN-LAST:event_exportSettingsActionPerformed

    private void importSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importSettingsActionPerformed
        if (Core.mongoConnector == null || !Core.mongoConnector.isConnected()) {
            IJ.error("connect first");
            return;
        }
        File dir = Utils.chooseDir("Select Import directory containing .bson files", null);
        if (dir != null) {
            try {
                Core.mongoConnector.mongoRestoreSettings(dir.getAbsolutePath());
                connect();
            } catch (Exception e) {
                exceptionPrinter.print(e, "", Core.GUIMode);
            }
        }
    }//GEN-LAST:event_importSettingsActionPerformed

    private void websiteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_websiteButtonActionPerformed
        try {
            BrowserLauncher.openURL("http://biophysique.mnhn.fr/tango/HomePage");
        } catch (IOException ex) {
            Logger.getLogger(Connector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_websiteButtonActionPerformed

    private void deleteUsrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUsrActionPerformed
        if (this.currentUser == null) {
            ij.IJ.error("select user first");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Remove User:" + this.currentUser + " and Projects associated (all data will be lost)?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) {
            Core.mongoConnector.removeUser();
            this.usernames.removeItem(this.currentUser);
            this.setUser(null);
        }
    }//GEN-LAST:event_deleteUsrActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        if (Core.helper != null) {
            Core.helper.close();
        }
        Core.helper = new Helper(core);
    }//GEN-LAST:event_helpButtonActionPerformed

    private void newUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newUserActionPerformed
        String name = JOptionPane.showInputDialog("Username (no special char):");
        if (name==null) return;
        if (!Utils.isValid(name, false)) {
            IJ.error("Name should not contain any special character");
            return;
        }
        if (Utils.contains(usernames, name, false)) {
            IJ.error("Name should not contain any special character");
            return;
        }
        if (name.length()>15) {
            IJ.error("Name should be shorter than 15 characters");
            return;
        }
        Core.mongoConnector.setUser(name, true);
        getUsers();
        setUser(name);
    }//GEN-LAST:event_newUserActionPerformed

    private void usernamesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_usernamesItemStateChanged
        if (connecting) {
            return;
        }
        if (evt.getStateChange() == 1) {
            String name = (String) usernames.getSelectedItem();
            if (name != null) {
                setUser(name);
            }
        }
    }//GEN-LAST:event_usernamesItemStateChanged

    private void connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectActionPerformed
        connect();
    }//GEN-LAST:event_connectActionPerformed

    private void hostActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hostActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ExportImportPanel;
    private javax.swing.JButton connect;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JButton deleteUsr;
    private javax.swing.JButton exportData;
    private javax.swing.JPanel exportImportPCPanel;
    private javax.swing.JCheckBox exportInput;
    private javax.swing.JCheckBox exportOutput;
    private javax.swing.JButton exportSettings;
    private javax.swing.JButton helpButton;
    private javax.swing.JTextField host;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JButton importData;
    private javax.swing.JButton importSettings;
    private javax.swing.JButton newUser;
    private javax.swing.JPanel optionImagePanel;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JComboBox usernames;
    private javax.swing.JButton websiteButton;
    private javax.swing.JTextField mongoUser;
    private javax.swing.JTextField mongoPwd;
    private javax.swing.JLabel mongoUserLabel;
    private javax.swing.JLabel mongoPwdLabel;
    // End of variables declaration//GEN-END:variables
}
