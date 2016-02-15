/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tango.plugin.filter.FeatureJ;
import mcib3d.utils.ThreadRunner;
import imagescience.image.Aspects;
import imagescience.image.Axes;
import imagescience.image.Coordinates;
import imagescience.image.Dimensions;
import imagescience.image.FloatImage;
import imagescience.image.Image;
import imagescience.utility.Timer;
import java.util.Vector;

/** Computes Hessian eigenimages. */
public class FJHessian3D {

	/** Default constructor. */
	public FJHessian3D() { }

	/** Computes Hessian eigenimages of images.

		@param image the input image for which Hessian eigenimages need to be computed. If it is of type {@link FloatImage}, it will be used to store intermediate results. Otherwise it will be left unaltered. If the size of the image in the z-dimension equals {@code 1}, this method will compute, for every image element, the two-dimensional (2D) Hessian and its two eigenvalues. Otherwise it will compute for every image element the full three-dimensional (3D) Hessian and its three eigenvalues. These computations are performed on every x-y(-z) subimage in a 5D image.

		@param scale the smoothing scale at which the required image derivatives are computed. The scale is equal to the standard deviation of the Gaussian kernel used for differentiation and must be larger than {@code 0}. In order to enforce physical isotropy, for each dimension, the scale is divided by the size of the image elements (aspect-ratio value) in that dimension.

		@param absolute determines whether eigenvalues are compared in absolute sense.

		@return an array containing the eigenimages. The images are always of type {@link FloatImage}.<br>
		If only the two-dimensional (2D) Hessian and its two eigenvalues were computed for every image element, the returned array contains two eigenimages:<br>
		Element {@code 0} = the image with, for every element, the largest (absolute) eigenvalue,<br>
		Element {@code 1} = the image with, for every element, the smallest (absolute) eigenvalue.<br>
		If the full three-dimensional (3D) Hessian and its three eigenvalues were computed for every image element, the returned array contains three eigenimages:<br>
		Element {@code 0} = the image with, for every element, the largest (absolute) eigenvalue,<br>
		Element {@code 1} = the image with, for every element, the middle (absolute) eigenvalue,<br>
		Element {@code 2} = the image with, for every element, the smallest (absolute) eigenvalue.

		@exception IllegalArgumentException if {@code scale} is less than or equal to {@code 0}.

		@exception IllegalStateException if the size of the image elements (aspect-ratio value) is less than or equal to {@code 0} in the x-, y-, or z-dimension.

		@exception NullPointerException if {@code image} is {@code null}.
	*/
	public Vector<Image> run(final Image image, final double scale, int nbCPUs) {

		//messenger.log(ImageScience.prelude()+"Hessian");

		final Timer timer = new Timer();
		timer.start();

		// Initialize:
		if (scale <= 0) throw new IllegalArgumentException("Smoothing scale less than or equal to 0");

		final Dimensions dims = image.dimensions();

		final Aspects asps = image.aspects();
		if (asps.x <= 0) throw new IllegalStateException("Aspect-ratio value in x-dimension less than or equal to 0");
		if (asps.y <= 0) throw new IllegalStateException("Aspect-ratio value in y-dimension less than or equal to 0");
		if (asps.z <= 0) throw new IllegalStateException("Aspect-ratio value in z-dimension less than or equal to 0");

		final Image smoothImage = (image instanceof FloatImage) ? image : new FloatImage(image);
		Vector<Image> eigenimages = null;
		final String name = image.name();


		// Compute Hessian matrix and eigenimages:
		if (dims.z == 1) { // 2D case

			final double[] pls = {0, 0.32, 0.64, 0.96, 1}; int pl = 0;

			// Compute Hessian components:
			final Image Hxx = differentiator.run(smoothImage,scale,2,0,0, nbCPUs);
			final Image Hxy = differentiator.run(smoothImage,scale,1,1,0, nbCPUs);
			final Image Hyy = differentiator.run(smoothImage,scale,0,2,0, nbCPUs);

			// Compute eigenimages (Hxx and Hyy are reused to save memory):
			logstatus("Computing eigenimages");
			Hxx.axes(Axes.X); Hxy.axes(Axes.X); Hyy.axes(Axes.X);
			final double[] ahxx = new double[dims.x];
			final double[] ahxy = new double[dims.x];
			final double[] ahyy = new double[dims.x];
			final Coordinates coords = new Coordinates();

                        for (coords.c=0; coords.c<dims.c; ++coords.c)
                                for (coords.t=0; coords.t<dims.t; ++coords.t)
                                        for (coords.y=0; coords.y<dims.y; ++coords.y) {
                                                Hxx.get(coords,ahxx);
                                                Hxy.get(coords,ahxy);
                                                Hyy.get(coords,ahyy);
                                                for (int x=0; x<dims.x; ++x) {
                                                        final double b = -(ahxx[x] + ahyy[x]);
                                                        final double c = ahxx[x]*ahyy[x] - ahxy[x]*ahxy[x];
                                                        final double q = -0.5*(b + (b < 0 ? -1 : 1)*Math.sqrt(b*b - 4*c));
                                                        double h1, h2;
                                                        if (q == 0) {
                                                                h1 = 0;
                                                                h2 = 0;
                                                        } else {
                                                                h1 = q;
                                                                h2 = c/q;
                                                        }
                                                        if (h1 > h2) {
                                                                ahxx[x] = h1;
                                                                ahyy[x] = h2;
                                                        } else {
                                                                ahxx[x] = h2;
                                                                ahyy[x] = h1;
                                                        }
                                                }
                                                Hxx.set(coords,ahxx);
                                                Hyy.set(coords,ahyy);
                                        }
			

			Hxx.name(name+" largest Hessian eigenvalues");
			Hyy.name(name+" smallest Hessian eigenvalues");

			Hxx.aspects(asps.duplicate());
			Hyy.aspects(asps.duplicate());

			eigenimages = new Vector<Image>(2);
			eigenimages.add(Hxx);
			eigenimages.add(Hyy);

		} else { // 3D case

			final double[] pls = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 1}; int pl = 0;

			// Compute Hessian components:
			final Image Hxx = differentiator.run(smoothImage,scale,2,0,0, nbCPUs);
			final Image Hxy = differentiator.run(smoothImage,scale,1,1,0, nbCPUs);
			final Image Hxz = differentiator.run(smoothImage,scale,1,0,1, nbCPUs);
			final Image Hyy = differentiator.run(smoothImage,scale,0,2,0, nbCPUs);
			final Image Hyz = differentiator.run(smoothImage,scale,0,1,1, nbCPUs);
			final Image Hzz = differentiator.run(smoothImage,scale,0,0,2, nbCPUs);

			// Compute eigenimages (Hxx, Hyy, Hzz are reused to save memory):
			logstatus("Computing eigenimages");
			Hxx.axes(Axes.X); Hxy.axes(Axes.X); Hxz.axes(Axes.X);
			Hyy.axes(Axes.X); Hyz.axes(Axes.X); Hzz.axes(Axes.X);
			
			
			
                        final ThreadRunner tr = new ThreadRunner(0, dims.z, nbCPUs);
                        for (int i = 0; i<tr.threads.length; i++) {
                            tr.threads[i]= new Thread(
                                new Runnable() {
                                    public void run() {
                                        final double[] ahxx = new double[dims.x];
                                        final double[] ahxy = new double[dims.x];
                                        final double[] ahxz = new double[dims.x];
                                        final double[] ahyy = new double[dims.x];
                                        final double[] ahyz = new double[dims.x];
                                        final double[] ahzz = new double[dims.x];
                                        final Coordinates coords = new Coordinates();
                                        for (coords.z = tr.ai.getAndIncrement(); coords.z<dims.z; coords.z=tr.ai.getAndIncrement()) {
                                            for (coords.y=0; coords.y<dims.y; ++coords.y) {
                                                        Hxx.get(coords,ahxx);
                                                        Hxy.get(coords,ahxy);
                                                        Hxz.get(coords,ahxz);
                                                        Hyy.get(coords,ahyy);
                                                        Hyz.get(coords,ahyz);
                                                        Hzz.get(coords,ahzz);
                                                        for (coords.x=0; coords.x<dims.x; ++coords.x) {
                                                            final double fhxx = ahxx[coords.x];
                                                            final double fhxy = ahxy[coords.x];
                                                            final double fhxz = ahxz[coords.x];
                                                            final double fhyy = ahyy[coords.x];
                                                            final double fhyz = ahyz[coords.x];
                                                            final double fhzz = ahzz[coords.x];
                                                            final double a = -(fhxx + fhyy + fhzz);
                                                            final double b = fhxx*fhyy + fhxx*fhzz + fhyy*fhzz - fhxy*fhxy - fhxz*fhxz - fhyz*fhyz;
                                                            final double c = fhxx*(fhyz*fhyz - fhyy*fhzz) + fhyy*fhxz*fhxz + fhzz*fhxy*fhxy - 2*fhxy*fhxz*fhyz;
                                                            final double q = (a*a - 3*b)/9;
                                                            final double r = (a*a*a - 4.5*a*b + 13.5*c)/27;
                                                            final double sqrtq = (q > 0) ? Math.sqrt(q) : 0;
                                                            final double sqrtq3 = sqrtq*sqrtq*sqrtq;
                                                            double h1, h2, h3;
                                                            if (sqrtq3 == 0) {
                                                                    h1 = 0;
                                                                    h2 = 0;
                                                                    h3 = 0;
                                                            } else {
                                                                    final double rsqq3 = r/sqrtq3;
                                                                    final double angle = (rsqq3*rsqq3 <= 1) ? Math.acos(rsqq3) : Math.acos(rsqq3 < 0 ? -1 : 1);
                                                                    h1 = -2*sqrtq*Math.cos(angle/3) - a/3;
                                                                    h2 = -2*sqrtq*Math.cos((angle + TWOPI)/3) - a/3;
                                                                    h3 = -2*sqrtq*Math.cos((angle - TWOPI)/3) - a/3;
                                                            }
                                                            if (h2 < h3) { final double tmp = h2; h2 = h3; h3 = tmp; }
                                                            if (h1 < h2) { final double tmp1 = h1; h1 = h2; h2 = tmp1;
                                                            if (h2 < h3) { final double tmp2 = h2; h2 = h3; h3 = tmp2; }}
                                                            ahxx[coords.x] = h1;
                                                            ahyy[coords.x] = h2;
                                                            ahzz[coords.x] = h3;
                                                        }
                                                        coords.x=0;
                                                        Hxx.set(coords,ahxx);
                                                        Hyy.set(coords,ahyy);
                                                        Hzz.set(coords,ahzz);
                                                        //progressor.step();
                                                    }
                                                }
                                            }
                                        }
                                    );

                        }
                        tr.startAndJoin();

			Hxx.name(name+" largest Hessian eigenvalues");
			Hyy.name(name+" middle Hessian eigenvalues");
			Hzz.name(name+" smallest Hessian eigenvalues");

			Hxx.aspects(asps.duplicate());
			Hyy.aspects(asps.duplicate());
			Hzz.aspects(asps.duplicate());

			eigenimages = new Vector<Image>(3);
			eigenimages.add(Hxx);
			eigenimages.add(Hyy);
			eigenimages.add(Hzz);
		}


		timer.stop();

		return eigenimages;
	}

	private void logstatus(final String s) {

		//messenger.log(s);
		//messenger.status(s+"...");
	}

	/** The object used for message displaying. */
	//public final Messenger messenger = new Messenger();

	/** The object used for progress displaying. */
	//public final Progressor progressor = new Progressor();

	/** The object used for image differentiation. */
	public final FJDifferentiator3D differentiator = new FJDifferentiator3D();

	private static final double TWOPI = 2*Math.PI;

}

