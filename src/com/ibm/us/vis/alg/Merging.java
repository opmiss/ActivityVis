package com.ibm.us.vis.alg;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.ibm.us.data.model.Ethread;
import com.ibm.us.data.model.User;
import com.ibm.us.vis.geo.vec;

public class Merging {
	
	public static ArrayList<Ethread> input; 
	public static ArrayList<Ethread> output; 
	public static void setInput(ArrayList<Ethread> I){
		input = I;
	}
	
/*	public static void mergeOne(){
		float cor = -1; 
		int ii=-1, jj=-1; 
		for (int i=0; i<input.size(); i++){
			for (int j=0; j<input.size(); j++){
				if (input.get(i).mask && input.get(j).mask){
					float c = Ethread.similarity(input.get(i), input.get(j)); 
					if (cor <c){cor=c; ii=i; jj=j;}
				}
			}
		}
		input.get(ii).merge(input.get(jj)); 
	}*/
	
	public static void merge_old(){
		int nt = input.size(); 
		for (int i=0; i<nt-1; i++){
			for (int j=i+1; j<nt; j++){
				if ( mergable(input.get(i), input.get(j)) ){
					input.get(i).merge(input.get(j)); 
				}
			}
		}
	}

	public static void merge() {
		int nt = input.size();
		for (int i = 0; i < nt-1; i++) {
			if (input.get(i).mask) {
				int mid = -1;
				float score = 0;
				for (int j = i + 1; j < nt; j++) {
					if (can_merge(input.get(i), input.get(j))) {
						float s = similarity(input.get(i), input.get(j));
						if (score < s) {
							score = s;
							mid = j;
						}
					}
				}
				if (score >0.1) input.get(i).merge(input.get(mid));
			}
		}
	}
	
	public static float similarity(Ethread s1, Ethread s2){
		int num =0; 
		for (String s:s1.members){
			if (s2.members.contains(s)) num++; 
		}
		int tnum = s1.members.size()+s2.members.size(); 
		return (float)num/(float)tnum; 
	}
	
	public static boolean can_merge(Ethread a, Ethread b){ 
		if (!b.mask) return false;
		DateTime s = new DateTime(a.start); 
		DateTime e = new DateTime(b.end); 
		Period p = new Period(s, e); 
		if (p.getYears() >= 1) return false; 
		return true; 
	}
	
	public static boolean mergable(Ethread a, Ethread b){ 
		if (!a.mask || !b.mask) return false;
		DateTime s = new DateTime(a.start); 
		DateTime e = new DateTime(b.end); 
		Period p = new Period(s, e); 
		if (p.getYears() >= 1) return false; 
		boolean c = Ethread.similar(a, b); 
		if (!c) return false; 
		return true; 
	}
	
	/*public static boolean valid(Ethread t){
		if (!t.mask) return false; 
	}*/
	
	public static void a_merge(){
		for (int i=0; i<input.size()-1; i++){
			for (int j=i+1; j<input.size(); j++){
				if (input.get(i).mask && input.get(j).mask){
					vec sim = Ethread.a_similarity(input.get(i), input.get(j)); 
					if (sim.x >0.1){
						if (sim.x>sim.y){
							if (sim.x >0.2) input.get(i).merge(input.get(j));
						}
						else if (sim.y>0.2) input.get(j).merge(input.get(i));
					}
				}
			}
		}
	}
	
	public static void merge(int n){
		for (int k=0; k<n; k++){
			merge(); 
		}
	}
	
	public static ArrayList<Ethread> getOutput(){
		output = new ArrayList<Ethread>();
		for (Ethread s:input){
			if (s.mask) output.add(s); 
		}
		return output; 
	}
	
}
