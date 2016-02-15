/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.plugin.filter;

import java.util.HashMap;
import mcib3d.geom.Object3D;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageInt;
import tango.dataStructure.InputCellImages;
import tango.dataStructure.InputImages;
import tango.parameter.BooleanParameter;
import tango.parameter.ConditionalParameter;
import tango.parameter.DoubleParameter;
import tango.parameter.IntParameter;
import tango.parameter.Parameter;

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
public class SizeFilter implements PostFilter {

    boolean debug;
    int nbCPUs = 1;
    int min, max, edgeXY, edgeZ, limX, limY, limZ;
    DoubleParameter minVox = new DoubleParameter("Min volume:", "minVox", 1.0, DoubleParameter.nfDEC2);
    DoubleParameter maxVox = new DoubleParameter("Max volume:", "maxVox", 0.0, DoubleParameter.nfDEC2);
    BooleanParameter useUnits = new BooleanParameter("Size in calibrated units:", "useUnits", false);
    BooleanParameter max_P = new BooleanParameter("Use max volume", "useMax", false);
    BooleanParameter edge_PXY = new BooleanParameter("Remove", "edgeXY", false);
    BooleanParameter edge_PZ = new BooleanParameter("Remove", "edgeZ", false);
    IntParameter edgeSurf_PXY = new IntParameter("Min nb of Voxels touching XY edges:", "edgeSurfXY", 1);
    IntParameter edgeSurf_PZ = new IntParameter("Min nb of Voxels touching Z edges:", "edgeSurfZ", 1);
    BooleanParameter outside = new BooleanParameter("Delete outside nucleus", "outside", false);
    //SliderParameter minPc = new SliderParameter("Min % coloc to remove", "coloc", 1, 100, 50);
    HashMap<Object, Parameter[]> mapXY = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{edgeSurf_PXY});
        }
    };
    HashMap<Object, Parameter[]> mapZ = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{edgeSurf_PZ});
        }
    };
    ConditionalParameter edgeCondXY = new ConditionalParameter("Objects touching edges XY", edge_PXY, mapXY);
    ConditionalParameter edgeCondZ = new ConditionalParameter("Objects touching edges Z", edge_PZ, mapZ);
    HashMap<Object, Parameter[]> map2 = new HashMap<Object, Parameter[]>() {
        {
            put(true, new Parameter[]{maxVox});
        }
    };
//    HashMap<Object, Parameter[]> mapOut = new HashMap<Object, Parameter[]>() {
//        {
//            put(true, new Parameter[]{minPc});
//        }
//    };
    ConditionalParameter maxCond = new ConditionalParameter("Constraint on maximum size", max_P, map2);
    //ConditionalParameter outParam = new ConditionalParameter(outside, mapOut);
    Parameter[] parameters = new Parameter[]{minVox, maxCond, useUnits, edgeCondXY, edgeCondZ, outside};

    public SizeFilter() {
        minVox.setHelp("if an objects has less volume than this value, it is erased", false);
        maxVox.setHelp("if an objects has more volume than this value, it is erased. \nleave blank or 0 for no maximum value", false);
        maxVox.setCompulsary(false);
        edge_PXY.setHelp("Pixels touching the border of the image (XY border) will be erased", false);
        edge_PXY.setHelp("Pixels touching the border of the image (Z border) will be erased", false);
        edgeSurf_PXY.setHelp("Minimum number of edge-touching voxel per objects: if the objects too few voxels touching the edges, it won't be erased", false);
        edgeSurf_PZ.setHelp("Minimum number of edge-touching voxel per objects: if the objects too few voxels touching the edges, it won't be erased", false);
        outside.setHelp("Delete objects outside nucleus (only valid for structures)", false);
        useUnits.setHelp("The values for sizes are in calibrated units instead of voxels", true);
        //minPc.setHelp("Minimum % of structure colocalisation outside nucleus to remove it", false);
    }

    @Override
    public ImageInt runPostFilter(int currentStructureIdx, ImageInt in, InputImages images) {
        Object3D[] objects = in.getObjects3D();
        edgeXY = edgeSurf_PXY.getIntValue(1);
        if (edgeXY < 1) {
            edgeXY = 1;
        }
        edgeZ = edgeSurf_PZ.getIntValue(1);
        if (edgeZ < 1) {
            edgeZ = 1;
        }

        int min = 0, max = Integer.MAX_VALUE;
        double mind = minVox.getFloatValue(0);
        double maxd = maxVox.getFloatValue(Integer.MAX_VALUE);
        double volunit = in.getCalibration().pixelWidth * in.getCalibration().pixelHeight * in.getCalibration().pixelDepth;
        if (useUnits.isSelected()) {
            min = (int) (mind / volunit);
            if (max_P.isSelected()) {
                max = (int) (maxd / volunit);
            }
        } else {
            min = (int) mind;
            max = Integer.MAX_VALUE;
            if (max_P.isSelected()) {
                max = (int) maxd;
            }
        }

        if (max <= min) {
            max = Integer.MAX_VALUE;
        }
        limX = in.sizeX - 1;
        limY = in.sizeY - 1;
        limZ = in.sizeZ - 1;

        // NUCLEI
        if (images instanceof InputCellImages) {
            // IJ.log("Input Cell images");
            ImageInt mask = ((InputCellImages) images).getMask();
            for (Object3D o : objects) {
                if ((o.getVolumePixels() < min) || (o.getVolumePixels() > max)) {
                    in.draw(o, 0);
                } else if (edge_PXY.isSelected()) {
                    int count = 0;
                    for (Voxel3D v : o.getContours()) {
                        if (isOutsideMaskXY(v, mask)) {
                            count++;
                        }
                        if (count >= edgeXY) {
                            break;
                        }
                    }
                    if (count >= edgeXY) {
                        // IJ.log("Touch XY");
                        in.draw(o, 0);
                    }
                } else if (edge_PZ.isSelected()) {
                    int count = 0;
                    for (Voxel3D v : o.getContours()) {
                        if (isOutsideMaskZ(v, mask)) {
                            count++;
                        }
                        if (count >= edgeZ) {
                            break;
                        }
                    }
                    if (count >= edgeZ) {
                        in.draw(o, 0);
                        //  IJ.log("Touch Z");
                    }
                }
            }
            // OBJECTS
        } else {
            for (Object3D o : objects) {
                if ((o.getVolumePixels() < min) || (o.getVolumePixels() > max)) {
                    in.draw(o, 0);
                    //EDGE XY
                } else if (edge_PXY.isSelected() && (o.getXmax() == limX || o.getXmin() == 0 || o.getYmax() == limY || o.getYmin() == 0)) {
                    if (edgeXY > 1) {
                        //count touching voxels
                        int count = 0;
                        for (Voxel3D v : o.getContours()) {
                            if (v.getRoundX() == 0 || v.getRoundX() == limX || v.getRoundY() == 0 || v.getRoundY() == limY) {
                                count++;
                            }
                            if (count >= edgeXY) {
                                break;
                            }
                        }
                        if (count >= edgeXY) {
                            in.draw(o, 0);
                        }
                    } else {
                        in.draw(o, 0);
                    }
                    // EDGEZ
                } else if (edge_PZ.isSelected() && (o.getZmax() == limZ || o.getZmin() == 0)) {
                    if (edgeZ > 1) {
                        //count touching voxels
                        int count = 0;
                        for (Voxel3D v : o.getContours()) {
                            if (v.getRoundZ() == 0 || v.getRoundZ() == limZ) {
                                count++;
                            }
                            if (count >= edgeZ) {
                                break;
                            }
                        }
                        if (count >= edgeZ) {
                            in.draw(o, 0);
                        }
                    } else {
                        in.draw(o, 0);
                    }
                }
            }
        }
        if (outside.isSelected()) {
            in.intersectMask(images.getMask());
            //in.show("delete outside");
        }

        return in;
    }

    private boolean isOutsideMask(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundX() < limX) {
            if (mask.getPixelInt(vox.getRoundX() + 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundX() > 0) {
            if (mask.getPixelInt(vox.getRoundX() - 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() < limY) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() + 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() - 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() < limZ) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() + 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() - 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isOutsideMaskXY(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundX() < limX) {
            if (mask.getPixelInt(vox.getRoundX() + 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundX() > 0) {
            if (mask.getPixelInt(vox.getRoundX() - 1, vox.getRoundY(), vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() < limY) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() + 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundY() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY() - 1, vox.getRoundZ()) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    private boolean isOutsideMaskZ(Voxel3D vox, ImageInt mask) {
        if (vox.getRoundZ() < limZ) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() + 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        if (vox.getRoundZ() > 0) {
            if (mask.getPixelInt(vox.getRoundX(), vox.getRoundY(), vox.getRoundZ() - 1) == 0) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    public void setVerbose(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void setMultithread(int nbCPUs) {
        this.nbCPUs = nbCPUs;
    }

    @Override
    public Parameter[] getParameters() {
        return parameters;
    }

    @Override
    public String getHelp() {
        return "Erase Objects according to their sizes (in voxels). Also remove objects touching edges, and outside nuclei.";
    }
}
