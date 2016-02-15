package tango.plugin.filter.FeatureJ;

import imagescience.image.Aspects;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Dimensions;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import imagescience.utility.Messenger;
import imagescience.utility.Progressor;
import imagescience.utility.Timer;
import java.util.Vector;
import mcib3d.utils.ThreadRunner;

/** Computes eigenimages of the structure tensor. */
public class FJStructure3D {
	
	/** Default constructor. */
	public FJStructure3D() { }
	
	/** Computes structure tensor eigenimages of images.
		
		@param image the input image for which structure-tensor eigenimages need to be computed. If it is of type {@link FloatImage}, it will be used to store intermediate results. Otherwise it will be left unaltered. If the size of the image in the z-dimension equals {@code 1}, this method will compute, for every image element, the two-dimensional (2D) structure tensor and its two eigenvalues. Otherwise it will compute for every image element the full three-dimensional (3D) structure tensor and its three eigenvalues. These computations are performed on every x-y(-z) subimage in a 5D image.
		
		@param sscale the smoothing scale at which the required image derivatives are computed. The scale is equal to the standard deviation of the Gaussian kernel used for differentiation and must be larger than {@code 0}. In order to enforce physical isotropy, for each dimension, the scale is divided by the size of the image elements (aspect-ratio value) in that dimension.
		
		@param iscale the integration scale. This scale is equal to the standard deviation of the Gaussian kernel used for integrating the components of the structure tensor and must be larger than {@code 0}. In order to enforce physical isotropy, for each dimension, the scale is divided by the size of the image elements (aspect-ratio value) in that dimension.
		
		@return an array containing the eigenimages. The images are always of type {@link FloatImage}.<br>
		If only the two-dimensional (2D) structure tensor and its two eigenvalues were computed for every image element, the returned array contains two eigenimages:<br>
		Element {@code 0} = the image with, for every element, the largest eigenvalue,<br>
		Element {@code 1} = the image with, for every element, the smallest eigenvalue.<br>
		If the full three-dimensional (3D) structure tensor and its three eigenvalues were computed for every image element, the returned array contains three eigenimages:<br>
		Element {@code 0} = the image with, for every element, the largest eigenvalue,<br>
		Element {@code 1} = the image with, for every element, the middle eigenvalue,<br>
		Element {@code 2} = the image with, for every element, the smallest eigenvalue.
		
		@exception IllegalArgumentException if {@code sscale} or {@code iscale} is less than or equal to {@code 0}.
		
		@exception IllegalStateException if the size of the image elements (aspect-ratio value) is less than or equal to {@code 0} in the x-, y-, or z-dimension.
		
		@exception NullPointerException if {@code image} is {@code null}.
	*/
	public Vector<Image> run(final Image image, final double sscale, final double iscale, int nbCPUs) {
		
		
		// Initialize:
		if (sscale <= 0) throw new IllegalArgumentException("Smoothing scale less than or equal to 0");
		if (iscale <= 0) throw new IllegalArgumentException("Integration scale less than or equal to 0");
		
		final Dimensions dims = image.dimensions();
		
		final Aspects asps = image.aspects();
		if (asps.x <= 0) throw new IllegalStateException("Aspect-ratio value in x-dimension less than or equal to 0");
		if (asps.y <= 0) throw new IllegalStateException("Aspect-ratio value in y-dimension less than or equal to 0");
		if (asps.z <= 0) throw new IllegalStateException("Aspect-ratio value in z-dimension less than or equal to 0");
		
		final Image smoothImage = (image instanceof FloatImage) ? image : new FloatImage(image);
		Vector<Image> eigenimages = null;
		final String name = image.name();
		
		//differentiator.messenger.log(messenger.log());
		//differentiator.progressor.parent(progressor);
		
		// Compute structure tensor and eigenimages:
		if (dims.z == 1) { // 2D case
			
			final double[] pls = {0, 0.2, 0.4, 0.45, 0.63, 0.80, 0.95, 1}; int pl = 0;
			
			// Compute structure tensor components:
			logstatus("Computing Ix"); 
			final Image Ix2 = differentiator.run(smoothImage.duplicate(),sscale,1,0,0, nbCPUs);
			logstatus("Computing Iy"); 
			final Image Iy2 = differentiator.run(smoothImage,sscale,0,1,0, nbCPUs);
			
			final Image IxIy = Ix2.duplicate();
			logstatus("Computing IxIy"); IxIy.multiply(Iy2); 
			logstatus("Squaring Ix"); Ix2.square(); 
			logstatus("Squaring Iy"); Iy2.square(); 
			
			// Integrate tensor components:
			differentiator.run(Ix2,iscale,0,0,0, nbCPUs);
			differentiator.run(IxIy,iscale,0,0,0, nbCPUs);
			differentiator.run(Iy2,iscale,0,0,0, nbCPUs);
			
			// Compute eigenimages (Ix2 and Iy2 are reused to save memory):
			logstatus("Computing eigenimages");
			Ix2.axes(Axes.X); IxIy.axes(Axes.X); Iy2.axes(Axes.X);
			final double[] axx = new double[dims.x];
			final double[] axy = new double[dims.x];
			final double[] ayy = new double[dims.x];
			final Coordinates coords = new Coordinates();
			for (coords.c=0; coords.c<dims.c; ++coords.c)
				for (coords.t=0; coords.t<dims.t; ++coords.t)
					for (coords.y=0; coords.y<dims.y; ++coords.y) {
						Ix2.get(coords,axx);
						IxIy.get(coords,axy);
						Iy2.get(coords,ayy);
						for (int x=0; x<dims.x; ++x) {
							final double b = -(axx[x] + ayy[x]);
							final double c = axx[x]*ayy[x] - axy[x]*axy[x];
							final double q = -0.5*(b + (b < 0 ? -1 : 1)*Math.sqrt(b*b - 4*c));
							double absh1, absh2;
							if (q == 0) {
								absh1 = 0;
								absh2 = 0;
							} else {
								absh1 = Math.abs(q);
								absh2 = Math.abs(c/q);
							}
							if (absh1 > absh2) {
								axx[x] = absh1;
								ayy[x] = absh2;
							} else {
								axx[x] = absh2;
								ayy[x] = absh1;
							}
						}
						Ix2.set(coords,axx);
						Iy2.set(coords,ayy);
					}
			
			Ix2.name(name+" largest structure eigenvalues");
			Iy2.name(name+" smallest structure eigenvalues");
			
			Ix2.aspects(asps.duplicate());
			Iy2.aspects(asps.duplicate());
			
			eigenimages = new Vector<Image>(2);
			eigenimages.add(Ix2);
			eigenimages.add(Iy2);
			
		} else { // 3D case
			
			final double[] pls = {0, 0.1, 0.2, 0.3, 0.34, 0.40, 0.46, 0.52, 0.58, 0.64, 0.7, 1}; int pl = 0;
			
			// Compute structure tensor components:
			final Image Ix2 = differentiator.run(smoothImage.duplicate(),sscale,1,0,0, nbCPUs);
			final Image Iy2 = differentiator.run(smoothImage.duplicate(),sscale,0,1,0, nbCPUs);
			final Image Iz2 = differentiator.run(smoothImage,sscale,0,0,1, nbCPUs);
			
			logstatus("Computing IxIy"); final Image IxIy = Ix2.duplicate(); IxIy.multiply(Iy2);
			logstatus("Computing IxIz"); final Image IxIz = Ix2.duplicate(); IxIz.multiply(Iz2);
			logstatus("Computing IyIz"); final Image IyIz = Iy2.duplicate(); IyIz.multiply(Iz2);
			logstatus("Squaring Ix"); Ix2.square(); 
			logstatus("Squaring Iy"); Iy2.square(); 
			logstatus("Squaring Iz"); Iz2.square(); 
			
			// Integrate tensor components:
			differentiator.run(Ix2,iscale,0,0,0, nbCPUs);
			differentiator.run(IxIy,iscale,0,0,0, nbCPUs);
			differentiator.run(IxIz,iscale,0,0,0, nbCPUs);
			differentiator.run(Iy2,iscale,0,0,0, nbCPUs);
			differentiator.run(IyIz,iscale,0,0,0, nbCPUs);
			differentiator.run(Iz2,iscale,0,0,0, nbCPUs);
			
			// Compute eigenimages (Ix2, Iy2, Iz2 are reused to save memory):
			logstatus("Computing eigenimages");
			Ix2.axes(Axes.X); IxIy.axes(Axes.X); IxIz.axes(Axes.X);
			Iy2.axes(Axes.X); IyIz.axes(Axes.X); Iz2.axes(Axes.X);
			
                        final ThreadRunner tr = new ThreadRunner(0, dims.z, nbCPUs);
                        for (int i = 0; i<tr.threads.length; i++) {
                                tr.threads[i]= new Thread(
                                new Runnable() {
                                    public void run() {
					final double[] axx = new double[dims.x];
                                        final double[] axy = new double[dims.x];
                                        final double[] axz = new double[dims.x];
                                        final double[] ayy = new double[dims.x];
                                        final double[] ayz = new double[dims.x];
                                        final double[] azz = new double[dims.x];
                                        final Coordinates coords = new Coordinates();
                                        for (coords.z = tr.ai.getAndIncrement(); coords.z<dims.z; coords.z=tr.ai.getAndIncrement()) {
						for (coords.y=0; coords.y<dims.y; ++coords.y) {
							Ix2.get(coords,axx);
							IxIy.get(coords,axy);
							IxIz.get(coords,axz);
							Iy2.get(coords,ayy);
							IyIz.get(coords,ayz);
							Iz2.get(coords,azz);
							for (int x=0; x<dims.x; ++x) {
								final double fxx = axx[x];
								final double fxy = axy[x];
								final double fxz = axz[x];
								final double fyy = ayy[x];
								final double fyz = ayz[x];
								final double fzz = azz[x];
								final double a = -(fxx + fyy + fzz);
								final double b = fxx*fyy + fxx*fzz + fyy*fzz - fxy*fxy - fxz*fxz - fyz*fyz;
								final double c = fxx*(fyz*fyz - fyy*fzz) + fyy*fxz*fxz + fzz*fxy*fxy - 2*fxy*fxz*fyz;
								final double q = (a*a - 3*b)/9;
								final double r = (a*a*a - 4.5f*a*b + 13.5f*c)/27f;
								final double sqrtq = (q > 0) ? Math.sqrt(q) : 0;
								final double sqrtq3 = sqrtq*sqrtq*sqrtq;
								double absh1, absh2, absh3;
								if (sqrtq3 == 0) {
									absh1 = 0;
									absh2 = 0;
									absh3 = 0;
								} else {
									final double rsqq3 = r/sqrtq3;
									final double angle = (rsqq3*rsqq3 <= 1) ? Math.acos(rsqq3) : Math.acos(rsqq3 < 0 ? -1 : 1);
									absh1 = Math.abs(-2*sqrtq*Math.cos(angle/3) - a/3);
									absh2 = Math.abs(-2*sqrtq*Math.cos((angle + TWOPI)/3) - a/3);
									absh3 = Math.abs(-2*sqrtq*Math.cos((angle - TWOPI)/3) - a/3);
								}
								if (absh2 < absh3) { final double tmp = absh2; absh2 = absh3; absh3 = tmp; }
								if (absh1 < absh2) { final double tmp1 = absh1; absh1 = absh2; absh2 = tmp1;
								if (absh2 < absh3) { final double tmp2 = absh2; absh2 = absh3; absh3 = tmp2; }}
								axx[x] = absh1;
								ayy[x] = absh2;
								azz[x] = absh3;
							}
							Ix2.set(coords,axx);
							Iy2.set(coords,ayy);
							Iz2.set(coords,azz);
							//progressor.step();
                                                    }
                                                }
                                            }
                                        }
                                    );

                        }
                        tr.startAndJoin();
			
			Ix2.name(name+" largest structure eigenvalues");
			Iy2.name(name+" middle structure eigenvalues");
			Iz2.name(name+" smallest structure eigenvalues");
			
			Ix2.aspects(asps.duplicate());
			Iy2.aspects(asps.duplicate());
			Iz2.aspects(asps.duplicate());
			
			eigenimages = new Vector<Image>(3);
			eigenimages.add(Ix2);
			eigenimages.add(Iy2);
			eigenimages.add(Iz2);
		}
		
		
		return eigenimages;
	}
	
	private void logstatus(final String s) {
		
	}
	
	/** The object used for image differentiation. */
	public final FJDifferentiator3D differentiator = new FJDifferentiator3D();
	
	private static final double TWOPI = 2*Math.PI;
	
}

