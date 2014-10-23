package com.ibm.us.vis.alg;

public class Map {
	
	//map input data to an output range specified by max and min
	/*long[] input; //assume input is sorted
	float min; 
	float max; 
	float alpha = 0.5f; 
	float[] output; */
	
	public static float[] HDRMap(long[] input, float rmin, float rmax, float alpha){
		float[] output = new float[input.length];
		hdr_map(0, input.length-1, rmin, rmax, alpha, input, output);
		return output; 
	}
	
	public static float LinearMap(float input, float min, float max){
		return min + input*(max-min); 
	}
	
	public static float[] LinearMap(float[] input, float min, float max){
		float[] output = new float[input.length]; 
		for (int i=0 ; i <input.length; i++){
			output[i] = min + input[i]*(max-min); 
		}
		return output; 
	}
	
	public static long LinearMap(float input, long min, long max){
		return min+ (long)(input*(float)(max-min)); 
	}
	
	//public static 
	private static void hdr_map(int start, int end, float wmin, float wmax, float alpha, long[] input, float[] output){
		//System.out.println("Start: "+start+", End: "+end+", min: "+wmin+", max:"+wmax); 
	    float wmean = (wmax+wmin)/2; 
	    if (end<=start) { 
	    	if (end == 0 ) output[0] = wmin; 
	    	else if (end == output.length-1) output[end] = wmax; 
	    	else output[end] = wmean; 
	       //System.out.println("data "+end+":"+input[end]+" map to: "+output[end]);
	      return;
	    }
	    float Mean = (input[start]+input[end])/2; 
	    float Median = input[(start+end)/2];
	    float Cut = alpha*Median+(1-alpha)*Mean; 
	    int CI = id(Cut, start, end, input); 
	    //System.out.println("mean: "+Mean+", median: "+Median+", cut id: "+CI); 
	    if (CI==start) {
	    	output[start]= wmin;
	    	//System.out.println("data "+start+":"+input[start]+" map to: "+wmin); 
	    	hdr_map(CI+1, end, wmean, wmax, alpha, input, output);
	    }
	    else if (CI == end){ 
	    	output[end] = wmax;
	    	//System.out.println("data "+end+":"+input[end]+" map to: "+wmax);
	    	hdr_map(start, CI-1, wmin, wmean, alpha, input, output); 
	    }
	    else if ( CI<end && CI>start){
	    	hdr_map(start, CI-1, wmin, wmean, alpha, input, output);
	    	hdr_map(CI, end, wmean, wmax, alpha, input, output); 
	    }
	    else System.out.println("map error!");
	}
	
	private static int id(float value, long[] input){
		for (int i = 0; i < input.length; i++) {
			if (value < input[i]) return i;
			}
		return input.length-1;
	}
	
	private static int id(float value, int start, int end, long[] input){
		for (int i = start; i < end; i++) {
			if (value < input[i]) return i;
		}
		return end;
	}
	
}
