package com.ibm.us.vis.alg;
import com.ibm.us.vis.geo.vec;

import processing.core.*;

public class Fisheye {
	
	public static final float FISHEYE_D = 1.0f;
	public static final float FISHEYE_HALF_RANGE = 0.05f;
	public static final float FISHEYE_P = 0.5f;
	
	public static double[] Transformation(double x[], double max,
			double min, double d, double p) {
		int length = x.length;
		double mid = (max + min) / 2;
		double range = max - min;
		// run the transformation
		double[] f = new double[x.length];
		for (int i = 0; i < length; i++) {
			double v = (x[i] - mid) / (range / 2);
			if (v < 1.0 && v > -1.0)
				f[i] = fisheye(v, d, p);
			else
				f[i] = v;
			f[i] = f[i] * range / 2 + mid;
		}
		return f;
	}
	public static double computeNormalizedAngle(double cx, double cy,
			double px, double py) {
		double a = px - cx;
		double b = py - cy;
		double angle = -(((a == 0) ? Math.PI / 2 : Math.atan(b / a)) / Math.PI / 2);
		if (a < 0)
			angle = 0.5 + angle;
		else if (b > 0)
			angle = 1.0 + angle;

		return angle;
	}
	
	public static float computeAngle(vec v){
		//compute the angle from negative y axis to input vector 
		float a= PApplet.atan2(v.x, -v.y); 
		//make sure output is in the range [0, 2PI] 
		if (a<0) a+=PApplet.TWO_PI; 
		return a; 
	}
	
	public static float diff(float a0, float a){
		//compute the difference between a0 and a, inputs are in the range [0, 2PI] 
		float da = a-a0;
		//make sure output is in the range [-PI, PI]
		if (da>PApplet.PI) da = da - PApplet.TWO_PI; 
		if (da<-PApplet.PI) da = da + PApplet.TWO_PI;    
		return da; 
	}
	
	public static float fisheye_scale(float a0, float a){
		float da = diff(a0, a); 
		float sda = fisheye(da/PApplet.PI) * PApplet.PI; 
		float sa = sda+a0; 
	//	if (sa < 0) sa += PApplet.TWO_PI; 
	//	if (sa > PApplet.TWO_PI) sa -= PApplet.TWO_PI; 
		return sa; 
	}
	
	public static float fisheye(float v){
		float r; 
		if (v<=0)
			r = (FISHEYE_P + FISHEYE_D) * v / (FISHEYE_P - FISHEYE_D * v);
		else 
			r = (FISHEYE_P + FISHEYE_D) * v / (FISHEYE_P + FISHEYE_D * v); 
		return r; 
	}

	public static double fisheye(double v, double d, double p) {
		double r;
		if (v <= 0)
			r = (p + d) * v / (p - d * v);
		else
			r = (p + d) * v / (p + d * v);
		return r;
	}
	
}
