
package tango.gui.util;

import java.awt.Dimension;
import java.util.ArrayList;
import tango.dataStructure.Object3DGui;
import tango.gui.ObjectManager;
import tango.helper.HelpManager;
import tango.helper.ID;
import tango.helper.RetrieveHelp;
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
 * /**
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

public class ObjectManagerLayout extends javax.swing.JPanel {
    ObjectManager objectManager;
    boolean populateKeys;
    String lastKey;
    public ObjectManagerLayout(ObjectManager objectManager) {
        this.objectManager=objectManager;
        initComponents();
        this.sortByCB.setMaximumSize(new Dimension(sortByPanel.getPreferredSize().width, sortByCB.getPreferredSize().height));
        Utils.addHorizontalScrollBar(sortByCB);
    }
    
    public void toggleIsRunning(boolean isRunning) {
        this.selectAll.setEnabled(!isRunning);
        this.selectNone.setEnabled(!isRunning);
        this.invertSelection.setEnabled(!isRunning);
        this.delete.setEnabled(!isRunning);
        this.manual.setEnabled(!isRunning);
        this.list.setEnabled(!isRunning);
        this.merge.setEnabled(!isRunning);
        this.shiftIndexes.setEnabled(!isRunning);
        this.showROIs.setEnabled(!isRunning);
        this.viewMeasurements.setEnabled(!isRunning);
        this.split.setEnabled(!isRunning);
        if (isRunning) {
            this.unableSortKeys();
        } else {
            this.ascendingOrder.setEnabled(true);
            this.sortByCB.setEnabled(true);
        }
        
    }
    
    public void registerComponents(HelpManager hm) {
        hm.objectIDs.put(this.selectAll, new ID(RetrieveHelp.objectPage, "Select_All"));
        hm.objectIDs.put(this.selectNone, new ID(RetrieveHelp.objectPage, "Select_None"));
        hm.objectIDs.put(this.showROIs, new ID(RetrieveHelp.objectPage, "ROIs"));
        hm.objectIDs.put(this.viewMeasurements, new ID(RetrieveHelp.objectPage, "Quantifications"));
        hm.objectIDs.put(this.shiftIndexes, new ID(RetrieveHelp.objectPage, "Shift_Indexes"));
        hm.objectIDs.put(this.delete, new ID(RetrieveHelp.objectPage, "Delete"));
        hm.objectIDs.put(this.merge, new ID(RetrieveHelp.objectPage, "Merge"));
        hm.objectIDs.put(this.split, new ID(RetrieveHelp.objectPage, "Split"));
        hm.objectIDs.put(this.manual, new ID(RetrieveHelp.objectPage, "Manual_Segmentation"));
    }
    
    public String getSortKey() {
        return Utils.getSelectedString(sortByCB);
    }
    
    public boolean getAscendingOrder() {
        return this.ascendingOrder.isSelected();
    }
    
    public void setKeys(ArrayList<String> keys) {
        this.populateKeys=true;
        this.ascendingOrder.setEnabled(true);
        this.sortByCB.setEnabled(true);
        lastKey = getSortKey();
        this.sortByCB.removeAllItems();
        Dimension dim = new Dimension(sortByPanel.getPreferredSize().width, sortByCB.getPreferredSize().height);
        this.sortByCB.addItem("idx");
        if (keys!=null) {
            for (String key : keys) sortByCB.addItem(key);
        }
        if (Utils.contains(sortByCB, lastKey, true)) sortByCB.setSelectedItem(lastKey);
        else this.sortByCB.setSelectedIndex(0);
        this.sortByCB.setMaximumSize(dim);
        this.populateKeys=false;
    }
    
    public void unableSortKeys() {
        setKeys(null);
        this.ascendingOrder.setEnabled(false);
        this.sortByCB.setEnabled(false);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        selectAll = new javax.swing.JButton();
        selectNone = new javax.swing.JButton();
        invertSelection = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        showROIs = new javax.swing.JToggleButton();
        viewMeasurements = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        shiftIndexes = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        merge = new javax.swing.JButton();
        split = new javax.swing.JButton();
        manual = new javax.swing.JButton();
        splitDistPanel = new javax.swing.JPanel();
        listScroll = new javax.swing.JScrollPane();
        list = new javax.swing.JList();
        sortByPanel = new javax.swing.JPanel();
        sortByCB = new javax.swing.JComboBox();
        ascendingOrder = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Objects 3D"));
        setMaximumSize(new java.awt.Dimension(409, 600));
        setMinimumSize(new java.awt.Dimension(409, 600));
        setPreferredSize(new java.awt.Dimension(409, 600));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Selection"));

        selectAll.setText("Select All");
        selectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllActionPerformed(evt);
            }
        });

        selectNone.setText("Select None");
        selectNone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneActionPerformed(evt);
            }
        });

        invertSelection.setText("Invert");
        invertSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectNone, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
            .addComponent(selectAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(invertSelection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(selectAll)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectNone)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(invertSelection))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("View"));

        showROIs.setText("ROIs");
        showROIs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showROIsActionPerformed(evt);
            }
        });

        viewMeasurements.setText(">Measurements>");
        viewMeasurements.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMeasurementsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(showROIs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(viewMeasurements, javax.swing.GroupLayout.DEFAULT_SIZE, 141, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(showROIs)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(viewMeasurements))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Adjust"));

        shiftIndexes.setText("Shift Indexes");
        shiftIndexes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shiftIndexesActionPerformed(evt);
            }
        });

        delete.setText("Delete");
        delete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteActionPerformed(evt);
            }
        });

        merge.setText("Merge");
        merge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeActionPerformed(evt);
            }
        });

        split.setText("Split");
        split.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitActionPerformed(evt);
            }
        });

        manual.setText("Manual...");
        manual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(shiftIndexes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(delete, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(merge, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(split, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(manual, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(splitDistPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(shiftIndexes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(delete)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(merge)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(split)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitDistPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(manual))
        );

        listScroll.setViewportView(list);

        sortByPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sort By Value"));

        sortByCB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "idx" }));
        sortByCB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sortByCBActionPerformed(evt);
            }
        });

        ascendingOrder.setText("ascending order");
        ascendingOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ascendingOrderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sortByPanelLayout = new javax.swing.GroupLayout(sortByPanel);
        sortByPanel.setLayout(sortByPanelLayout);
        sortByPanelLayout.setHorizontalGroup(
            sortByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sortByCB, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(ascendingOrder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sortByPanelLayout.setVerticalGroup(
            sortByPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sortByPanelLayout.createSequentialGroup()
                .addComponent(sortByCB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ascendingOrder)
                .addGap(0, 12, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sortByPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addGap(6, 6, 6))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sortByPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addComponent(listScroll)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllActionPerformed
        objectManager.selectAll();
    }//GEN-LAST:event_selectAllActionPerformed

    private void selectNoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneActionPerformed
        objectManager.selectNone();
    }//GEN-LAST:event_selectNoneActionPerformed

    private void showROIsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showROIsActionPerformed
        objectManager.toggleShowROIs(this.showROIs.isSelected());
    }//GEN-LAST:event_showROIsActionPerformed

    private void viewMeasurementsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMeasurementsActionPerformed
        objectManager.toggleShowMeasurements();
    }//GEN-LAST:event_viewMeasurementsActionPerformed

    private void shiftIndexesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shiftIndexesActionPerformed
        objectManager.shift();
    }//GEN-LAST:event_shiftIndexesActionPerformed

    private void deleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteActionPerformed
        objectManager.deleteSelectedObjects();
    }//GEN-LAST:event_deleteActionPerformed

    private void mergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeActionPerformed
        objectManager.mergeSelectedObjects();
    }//GEN-LAST:event_mergeActionPerformed

    private void splitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitActionPerformed
        objectManager.splitObjects();
    }//GEN-LAST:event_splitActionPerformed

    private void manualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualActionPerformed
        objectManager.manualSegmentation();
    }//GEN-LAST:event_manualActionPerformed

    private void ascendingOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ascendingOrderActionPerformed
        Object3DGui.setAscendingOrger(this.ascendingOrder.isSelected());
        this.objectManager.populateObjects();
    }//GEN-LAST:event_ascendingOrderActionPerformed

    private void sortByCBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sortByCBActionPerformed
        if (this.populateKeys) return;
        this.objectManager.populateObjects();
    }//GEN-LAST:event_sortByCBActionPerformed

    private void invertSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertSelectionActionPerformed
        objectManager.invertSelection();
    }//GEN-LAST:event_invertSelectionActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox ascendingOrder;
    private javax.swing.JButton delete;
    private javax.swing.JButton invertSelection;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    public javax.swing.JList list;
    private javax.swing.JScrollPane listScroll;
    private javax.swing.JButton manual;
    private javax.swing.JButton merge;
    private javax.swing.JButton selectAll;
    private javax.swing.JButton selectNone;
    private javax.swing.JButton shiftIndexes;
    public javax.swing.JToggleButton showROIs;
    private javax.swing.JComboBox sortByCB;
    private javax.swing.JPanel sortByPanel;
    private javax.swing.JButton split;
    public javax.swing.JPanel splitDistPanel;
    public javax.swing.JToggleButton viewMeasurements;
    // End of variables declaration//GEN-END:variables


    
}
