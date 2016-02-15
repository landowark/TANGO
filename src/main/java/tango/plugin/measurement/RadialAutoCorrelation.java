package tango.plugin.measurement;

import ij.IJ;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageStats;
import tango.util.ImageUtils;

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

public class RadialAutoCorrelation {
    ImageInt maskResampled;
    ImageHandler intensityResampled;
    double sigma2;
    double meanValue;
    
    public RadialAutoCorrelation(ImageHandler intensity, ImageInt mask, int resample) {
        init(intensity, mask, resample);
    }
     
    protected void init(ImageHandler intensity, ImageInt mask, int resample) {
        if (resample>0) {
            int newZ = (int)(mask.sizeZ * mask.getScaleZ()/mask.getScaleXY()+0.5);
            maskResampled=mask.resample(newZ, ij.process.ImageProcessor.NEAREST_NEIGHBOR);
            intensityResampled=intensity.resample(newZ, resample==1? ij.process.ImageProcessor.BILINEAR : ij.process.ImageProcessor.BICUBIC);
        } else {
            maskResampled=mask;
            intensityResampled=intensity;
        }
        ImageStats stats = intensityResampled.getImageStats(maskResampled);
        meanValue=stats.getMean();
        sigma2 = Math.pow(stats.getStandardDeviation(), 2);
    }
    
    public double getCorrelation(float radius, float radiusZ) {
        int[][] neighbor=ImageUtils.getHalfNeighbourhood(radius, radiusZ, 1);
        double sum=0;
        double count=0;
        int zz, xx, yy, xy2;
        for (int z = 0; z<maskResampled.sizeZ; z++) {
            for (int y = 0; y<maskResampled.sizeY; y++) {
                for (int x= 0; x<maskResampled.sizeX; x++) {
                    int xy = x+y*maskResampled.sizeX;
                    if (maskResampled.getPixel(xy, z)!=0) {
                        double value = intensityResampled.getPixel(xy, z)-meanValue;
                        for (int i = 0; i<neighbor[0].length; i++) {
                            zz = z + neighbor[2][i];
                            if (zz<maskResampled.sizeZ && zz>=0) {
                                xx= neighbor[0][i]+x;
                                if (xx<maskResampled.sizeX && xx>=0) {
                                    yy= neighbor[1][i]+y;
                                    if (yy<maskResampled.sizeY && yy>=0) {
                                        xy2 = xx+yy*maskResampled.sizeX;
                                        if (maskResampled.getPixel(xy2, zz)!=0) {
                                            sum += value * (intensityResampled.getPixel(xy2, zz)-meanValue);
                                            count++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (count>0) sum/=count;
        if (sigma2>0) return sum/sigma2;
        return 0;
    }
}
