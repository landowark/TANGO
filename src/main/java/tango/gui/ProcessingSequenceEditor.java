package tango.gui;

import tango.gui.parameterPanel.PreFilterPanel;
import tango.gui.parameterPanel.PostFilterPanel;
import mcib3d.utils.exceptionPrinter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.IJ;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import tango.gui.parameterPanel.*;
import tango.gui.util.*;
import tango.helper.HelpManager;
import tango.helper.Helper;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
import tango.parameter.StructureParameter;
import tango.util.ProcessingChainsToText;
import tango.util.Utils;

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
public class ProcessingSequenceEditor implements ActionListener {

    protected JPanel controlPanel;
    protected JComboBox structure;
    int currentStructure = 0;
    BasicDBObject currentTemplate;
    protected JComboBox template;
    protected ConfigurationList<PreFilterPanel> preFilterPanel, templatePreFilterPanel;
    protected ConfigurationList<ChannelSegmenterPanel> structureSegmenterPanel, templateStructureSegmenterPanel;
    protected ConfigurationList<NucleiSegmenterPanel> nucleusSegmenterPanel, templateNucleusSegmenterPanel;
    protected ConfigurationList<PostFilterPanel> postFilterPanel, templatePostFilterPanel;
    protected ConfigurationListMaster master;
    protected Core core;
    protected BasicDBObject data;
    protected boolean populatingTemplates, populatingStructures;
    protected ProcessingSequenceEditorLayout layout;
    protected JButton copyFromTemplate, copyToTemplate, createNewTemplate, save, test;
    protected JLabel structureLabel, templateLabel;
    protected Helper ml;
    //private JButton text;

    public ProcessingSequenceEditor(Core main) {
        this.core = main;
        this.layout = new ProcessingSequenceEditorLayout(main, "Current Structures");
        this.master = new ConfigurationListMaster(layout, layout.choicePanel);
        try {
            init();
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(copyFromTemplate, new ID(RetrieveHelp.editPSPage, "Copy_From_Template"));
        hm.objectIDs.put(copyToTemplate, new ID(RetrieveHelp.editPSPage, "Copy_To_Template"));
        hm.objectIDs.put(createNewTemplate, new ID(RetrieveHelp.editPSPage, "Create_New_Template"));
        hm.objectIDs.put(save, new ID(RetrieveHelp.editPSPage, "Save"));
        hm.objectIDs.put(test, new ID(RetrieveHelp.editPSPage, "Test"));
        hm.objectIDs.put(structureLabel, new ID(RetrieveHelp.editPSPage, "Choose_Structure"));
        hm.objectIDs.put(templateLabel, new ID(RetrieveHelp.editPSPage, "Choose_Template"));
        hm.objectIDs.put(layout.preFilterPanel, new ID(RetrieveHelp.editPSPage, "Pre-filtering"));
        hm.objectIDs.put(layout.segPanel, new ID(RetrieveHelp.editPSPage, "Segmentation"));
        hm.objectIDs.put(layout.postFilterPanel, new ID(RetrieveHelp.editPSPage, "Post-filtering"));
    }

    public void register(Helper ml) {
        if (this.ml != null && this.ml != ml) {
            unRegister(this.ml);
        }
        this.ml = ml;
        register();
    }

    public void register() {
        if (preFilterPanel != null) {
            preFilterPanel.register(ml);
        }
        if (currentStructure > 0 && structureSegmenterPanel != null) {
            structureSegmenterPanel.register(ml);
        }
        if (currentStructure == 0 && nucleusSegmenterPanel != null) {
            nucleusSegmenterPanel.register(ml);
        }
        if (postFilterPanel != null) {
            postFilterPanel.register(ml);
        }
    }

    public void unRegister(Helper ml) {
        if (this.ml == ml) {
            this.ml = null;
        }
        if (preFilterPanel != null) {
            preFilterPanel.unRegister(ml);
        }
        if (structureSegmenterPanel != null) {
            structureSegmenterPanel.unRegister(ml);
        }
        if (nucleusSegmenterPanel != null) {
            nucleusSegmenterPanel.unRegister(ml);
        }
        if (postFilterPanel != null) {
            postFilterPanel.unRegister(ml);
        }
    }

    private void init() throws Exception {
        controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setPreferredSize(layout.controlDim);
        structureLabel = new JLabel("Choose structure:");
        controlPanel.add(structureLabel);
        structure = new JComboBox();
        for (String s : StructureParameter.getStructures()) {
            structure.addItem(s);
        }
        if (structure.getItemCount() > 0) {
            structure.setSelectedIndex(0);
        }
        structure.addActionListener(this);
        structure.setAlignmentX(0);
        structure.setMaximumSize(new Dimension(layout.controlDim.width, structure.getPreferredSize().height));
        Utils.addHorizontalScrollBar(structure);
        controlPanel.add(structure);
        templateLabel = new JLabel("Choose template:");
        controlPanel.add(templateLabel);
        template = new JComboBox();
        this.getTemplates(currentStructure == 0);
        Utils.addHorizontalScrollBar(template);
        template.addActionListener(this);
        template.setAlignmentX(0);
        template.setMaximumSize(new Dimension(layout.controlDim.width, template.getPreferredSize().height));
        controlPanel.add(template);
        copyFromTemplate = new JButton("Copy from Template");
        copyFromTemplate.addActionListener(this);
        controlPanel.add(copyFromTemplate);
        copyFromTemplate.setAlignmentX(0);
        Dimension buttonDim = new Dimension(layout.controlDim.width, copyFromTemplate.getPreferredSize().height);
        copyFromTemplate.setMinimumSize(buttonDim);
        copyFromTemplate.setMaximumSize(buttonDim);
        copyFromTemplate.setPreferredSize(buttonDim);
        copyToTemplate = new JButton("Copy To Template");
        copyToTemplate.addActionListener(this);
        copyToTemplate.setAlignmentX(0);
        controlPanel.add(copyToTemplate);
        copyToTemplate.setMinimumSize(buttonDim);
        copyToTemplate.setMaximumSize(buttonDim);
        copyToTemplate.setPreferredSize(buttonDim);
        createNewTemplate = new JButton("Create new Template");
        createNewTemplate.addActionListener(this);
        createNewTemplate.setAlignmentX(0);
        createNewTemplate.setMinimumSize(buttonDim);
        createNewTemplate.setMaximumSize(buttonDim);
        createNewTemplate.setPreferredSize(buttonDim);
        controlPanel.add(createNewTemplate);
        test = new JButton("Test");
        test.addActionListener(this);
        test.setAlignmentX(0);
        controlPanel.add(test);
        test.setMinimumSize(buttonDim);
        test.setMaximumSize(buttonDim);
        test.setPreferredSize(buttonDim);
        save = new JButton("Save");
        save.addActionListener(this);
        save.setAlignmentX(0);
        controlPanel.add(save);
        save.setMinimumSize(buttonDim);
        save.setMaximumSize(buttonDim);
        save.setPreferredSize(buttonDim);
        layout.controlPanel.add(controlPanel);

        // TEST THOMAS
//        text = new JButton("Text");
//        text.addActionListener(this);
//        text.setAlignmentX(0);
//        controlPanel.add(text);
//        text.setMinimumSize(buttonDim);
//        text.setMaximumSize(buttonDim);

        setStructure(0);
    }

    public JPanel getPanel() {
        return layout;
    }

    public void refreshParameters() {
        if (preFilterPanel != null) {
            this.preFilterPanel.refreshParameters();
        }
        if (postFilterPanel != null) {
            this.postFilterPanel.refreshParameters();
        }
        if (structureSegmenterPanel != null) {
            this.structureSegmenterPanel.refreshParameters();
        }
        if (nucleusSegmenterPanel != null) {
            this.nucleusSegmenterPanel.refreshParameters();
        }
        //refresh structures:
        String selectedStructure = Utils.getSelectedString(structure);
        populatingStructures = true;
        structure.removeAllItems();
        for (String s : StructureParameter.getStructures()) {
            structure.addItem(s);
        }
        if (Utils.contains(structure, selectedStructure, true)) {
            structure.setSelectedItem(selectedStructure);
        } else {
            this.setStructure(0);
        }
        populatingStructures = false;
        //refresh templates
        String t = Utils.getSelectedString(template);
        this.getTemplates(structure.getSelectedIndex() == 0);
        populatingTemplates = true;
        if (Utils.contains(template, t, true)) {
            template.setSelectedItem(t);
        }
        populatingTemplates = false;
    }

    protected void getTemplates(boolean nucleus) {
        populatingTemplates = true;
        template.removeAllItems();
        if (nucleus) {
            template.addItem(" ");
            for (String item : Core.mongoConnector.getNucSettings()) {
                template.addItem(item);
            }
        } else {
            for (String item : Core.mongoConnector.getChannelSettings()) {
                template.addItem(item);
            }
        }
        populatingTemplates = false;
    }

    protected BasicDBObject getProcessingSequence(int structureIdx) {
        if (structureIdx >= 0 && structureIdx < structure.getItemCount()) {
            return Core.getExperiment().getProcessingChain(structureIdx);
        } else {
            return null;
        }
    }

    protected void createMultiPanels() {
        if (preFilterPanel != null) {
            preFilterPanel.flushList();
        }
        if (structureSegmenterPanel != null) {
            structureSegmenterPanel.flushList();
        }
        if (nucleusSegmenterPanel != null) {
            nucleusSegmenterPanel.flushList();
        }
        if (postFilterPanel != null) {
            postFilterPanel.flushList();
        }
        layout.flush();
        master.flush();
        //ij.IJ.log("prefilters dataPF:"+(dataPF.get("preFilters")));
        try {
            this.preFilterPanel = new ConfigurationList<PreFilterPanel>(core, getPreFilters(), master, layout.preFilterList, layout.preFilterButtonPanel, false, false, true, PreFilterPanel.class);
            if (this.currentStructure > 0) {
                this.structureSegmenterPanel = new ConfigurationList<ChannelSegmenterPanel>(core, getSegmentation(), master, layout.segList, layout.segButtonPanel, true, true, true, ChannelSegmenterPanel.class);
            } else {
                this.nucleusSegmenterPanel = new ConfigurationList<NucleiSegmenterPanel>(core, getSegmentation(), master, layout.segList, layout.segButtonPanel, true, true, true, NucleiSegmenterPanel.class);
            }
            this.postFilterPanel = new ConfigurationList<PostFilterPanel>(core, getPostFilters(), master, layout.postFilterList, layout.postFilterButtonPanel, false, false, true, PostFilterPanel.class);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    protected void record() {

        Core.getExperiment().save();
        if (Core.GUIMode) {
            IJ.log("XP saved!");
        }
    }

    private void setStructure(int structureIdx) {
        try {
            if (this.ml != null) {
                unRegister(ml);
            }
            data = getProcessingSequence(structureIdx);
            populatingStructures = true;
            if (structureIdx > 0 && currentStructure == 0) {
                getTemplates(false);
            } else if (structureIdx == 0 && currentStructure > 0) {
                getTemplates(true);
            }
            currentStructure = structureIdx;
            createMultiPanels();
            if (data != null && data.containsField("name") && Utils.contains(template, data.get("name"), true)) {
                setCurrentTemplate(data.getString("name"));
            } else {
                populatingTemplates = true;
                template.setSelectedIndex(0);
                populatingTemplates = false;
                currentTemplate = null;
            }
            register(Core.helper);
            //layout.hidePanel();
            //layout.showListPanels(this.preFilterPanel.getPanel(), currentStructure==0?this.nucleusSegmenterPanel.getPanel():this.structureSegmenterPanel.getPanel(), this.postFilterPanel.getPanel());
            populatingStructures = false;
        } catch (Exception e) {
            exceptionPrinter.print(e, "", Core.GUIMode);
        }
    }

    protected void save(boolean record) {
        try {
            data = new BasicDBObject("name", Utils.getSelectedString(template));
            data.append("preFilters", preFilterPanel.save());
            if (currentStructure == 0) {
                data.append("segmentation", nucleusSegmenterPanel.save());
            } else {
                data.append("segmentation", structureSegmenterPanel.save());
            }
            data.append("postFilters", postFilterPanel.save());
            data.append("tango_version", Core.VERSION);
            Core.getExperiment().setProcessingChain(structure.getSelectedIndex(), data);
            if (record) {
                record();
            }
        } catch (Exception e) {
            exceptionPrinter.print(e, null, Core.GUIMode);
        }
    }

    protected DBObject getPreFilters() {
        if (data == null) {
            return null;
        }
        Object dataPF = this.data.get("preFilters");
        if (dataPF != null) {
            return (DBObject) dataPF;
        } else {
            return null;
        }
    }

    protected DBObject getPostFilters() {
        if (data == null) {
            return null;
        }
        Object dataPF = this.data.get("postFilters");
        if (dataPF != null) {
            return (DBObject) dataPF;
        } else {
            return null;
        }
    }

    protected DBObject getSegmentation() {
        if (data == null) {
            return null;
        }
        Object dataS = this.data.get("segmentation");
        if (dataS != null) {
            return (DBObject) dataS;
        } else {
            return null;
        }
    }

    protected BasicDBObject getTemplate(String name) {
        if (currentStructure == 0) {
            return Core.mongoConnector.getNucSettings(name);
        } else {
            return Core.mongoConnector.getChannelSettings(name);
        }
    }

    protected void setCurrentTemplate(String name) {
        System.out.println("Setting template:" + name);
        if (name == null) {
            currentTemplate = null;
            populatingTemplates = true;
            template.setSelectedIndex(0);
            populatingTemplates = false;
            return;
        }
        populatingTemplates = true;
        template.setSelectedItem(name);
        populatingTemplates = false;

        if (name.length() == 0) {
            currentTemplate = null;
        } else {
            currentTemplate = getTemplate(name);
        }
        if (data != null) {
            data.append("name", name);
        }
        if (currentTemplate != null) {
            this.templatePreFilterPanel = new ConfigurationList<PreFilterPanel>(core, (DBObject) currentTemplate.get("preFilters"), null, null, null, false, false, true, PreFilterPanel.class);
            if (this.currentStructure > 0) {
                this.templateStructureSegmenterPanel = new ConfigurationList<ChannelSegmenterPanel>(core, (DBObject) currentTemplate.get("segmentation"), null, null, null, true, true, true, ChannelSegmenterPanel.class);
            } else {
                this.templateNucleusSegmenterPanel = new ConfigurationList<NucleiSegmenterPanel>(core, (DBObject) currentTemplate.get("segmentation"), null, null, null, true, true, true, NucleiSegmenterPanel.class);
            }
            this.templatePostFilterPanel = new ConfigurationList<PostFilterPanel>(core, (DBObject) currentTemplate.get("postFilters"), null, null, null, false, false, true, PostFilterPanel.class);
        } else {
            templatePreFilterPanel = null;
            templateStructureSegmenterPanel = null;
            templateNucleusSegmenterPanel = null;
            templatePostFilterPanel = null;
        }
        if (preFilterPanel != null) {
            this.preFilterPanel.setTemplate(templatePreFilterPanel);
        }
        if (postFilterPanel != null) {
            this.postFilterPanel.setTemplate(templatePostFilterPanel);
        }
        if (this.currentStructure > 0 && structureSegmenterPanel != null) {
            this.structureSegmenterPanel.setTemplate(templateStructureSegmenterPanel);
        } else if (currentStructure == 0 && nucleusSegmenterPanel != null) {
            this.nucleusSegmenterPanel.setTemplate(templateNucleusSegmenterPanel);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (e.getSource() == structure && !populatingStructures) {
            setStructure(structure.getSelectedIndex());
        } else if (e.getSource() == template && !populatingTemplates) {
            String name = Utils.getSelectedString(template);
            if (name == null || name.length() == 0) {
                return;
            }
            setCurrentTemplate(name);
        } else if (source == copyFromTemplate) {
            if (currentTemplate == null) {
                return;
            }
            data = (BasicDBObject) currentTemplate.copy();
            data.removeField("_id");
            data.append("name", Utils.getSelectedString(template));
            Core.getExperiment().setProcessingChain(currentStructure, data);
            setStructure(currentStructure);
        } else if (source == copyToTemplate) {
            if (template.getSelectedIndex() <= 0) {
                IJ.log("Select template first");
            }
            if (JOptionPane.showConfirmDialog(this.layout, "Override selected template?", "TANGO", JOptionPane.OK_CANCEL_OPTION) == 0) {
                save(false);
                if (currentStructure == 0) {
                    Core.mongoConnector.saveNucSettings(data);
                } else {
                    Core.mongoConnector.saveStructureProcessingChain(data);
                }
                core.updateSettings();
                setCurrentTemplate(Utils.getSelectedString(template));
            }
        } else if (source == createNewTemplate) {
            String name = JOptionPane.showInputDialog("Template Name");
            if (name == null) {
                return;
            }
            if (Utils.isValid(name, false) && !Utils.contains(template, name, false)) {
                save(false);
                data.append("name", name);
                if (currentStructure == 0) {
                    Core.mongoConnector.saveNucSettings(data);
                } else {
                    Core.mongoConnector.saveStructureProcessingChain(data);
                }
                core.updateSettings();
                getTemplates(currentStructure == 0);
                setCurrentTemplate(name);
            } else {
                IJ.error("Invalid Name/Processing Chain already exists");
            }

        } else if (source == save) {
            save(true);
        } else if (source == test) {
            save(false);
            if (structure.getSelectedIndex() > 0) {
                core.getCellManager().test(true, false, false, this.structure.getSelectedIndex());
            } else {
                core.getFieldManager().test();
            }
        } 
    }

    public void test(int step, int subStep) {
        save(false);
        System.out.println("Test:" + step + " " + subStep + " structure" + structure.getSelectedIndex());
        if (structure.getSelectedIndex() > 0) {
            core.getCellManager().testProcess(this.structure.getSelectedIndex(), step, subStep);
        } else {
            core.getFieldManager().testProcess(step, subStep);
        }
    }

   

}
