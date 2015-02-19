package com.ibm.us.vis.model;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

import org.joda.time.Period;

import com.ibm.us.data.model.Ethread;
import com.ibm.us.data.model.Record;
import com.ibm.us.data.model.User;
import com.ibm.us.vis.alg.Fisheye;
import com.ibm.us.vis.alg.Map;
import com.ibm.us.vis.geo.pt;
import com.ibm.us.vis.geo.vec;
import com.ibm.us.vis.view.DetailView;

import processing.core.*;

public class Cell {
	public static pt center = new pt(0,0); //center of the cell
	public static float radius=100;  //radius of the cell body 
	public static float r0 = radius-8, r2 = r0*r0; 
	float alpha=0; //mapping parameter
	int cid = -1;  //id of selected cilium 
	ArrayList<Cilium> ciliums=null; 
	ArrayList<Plastid> plastids = null; 
	DetailView view; 
	public User user; 
	public static float start_angle = PConstants.PI/18; 
	public static float end_angle = PConstants.TWO_PI-start_angle; 
	public boolean thread_selected = false; 
	
	public Cell(DetailView cv){
		view = cv;
	}
	
	public Cell setData(User u){
		user = u;
		ciliums = new ArrayList<Cilium>(); 
		plastids = new ArrayList<Plastid>(); 
		for (Ethread s:u.threads){
			ciliums.add((new Cilium(this)).setData(s));
		}
		for (String c:u.contacts){
			Plastid p = (new Plastid()).setLabel(c); 
			plastids.add(p); 
			for (Cilium l:ciliums){
				if (l.contains(p.label)){
					l.plastids.add(p); 
					p.ciliums.add(l); 
				}
			}
		}
		this.setTimelineAngle();
		return this; 
	}
	
	public boolean contain(float mx, float my){
		return (PApplet.sq(mx-center.x)+PApplet.sq(my-center.y) < r2);
	}
	
	public Cell setAlpha(float a){
		alpha = a; return this; 
	}
	
	public Cell setCenter(float x, float y){
		center.setTo(x, y); return this; 
	}
	
	public Cell setCenter(pt c){
		center.setTo(c); return this; 
	}
	
	public Cell setRadius(float r){
		radius =r; 
		r0=r-8; r2 = r0*r0;  
		return this; 
	}	
	
	public Cell drawCiliums(){
		view.stroke(10); 
		view.strokeWeight(1); 
		view.noFill(); 
		for (Cilium c:ciliums){
			c.drawCilium(view); 
		}
		return this; 
	}
	
	public Cell drawBlackCiliums(){
		view.stroke(0); 
		view.strokeWeight(1); 
		view.noFill(); 
		for (Cilium c:ciliums){
			c.drawBlackCilium(view); 
		}
		return this; 	
	}
	
	public Cell drawSimpleCiliums(){
		for (Cilium c:ciliums){
			c.drawSimpleCilium(view); 
		}
		return this;
	}
	
	public Cell drawPlastids(){
		view.stroke(10); 
		view.strokeWeight(1); 
		view.noFill(); 
		for (Plastid p:plastids){
			p.draw(view); 
		}
		return this; 
	}
	
	public Cell drawSimplePlastids(){
		view.stroke(10); 
		view.strokeWeight(1); 
		view.noFill(); 
		for (Plastid p:plastids){
			p.drawSimple(view); 
		}
		return this; 
	}
	
	public Cell drawBlackPlastids(){
		view.stroke(10); 
		view.strokeWeight(1); 
		view.noFill(); 
		for (Plastid p:plastids){
			p.drawBlack(view); 
		}
		return this; 
	}
	
	float[] timeline_angle = new float[13];
	public void setTimelineAngle(){
		float a=0;  
		for (int i=0; i<13; i++) {
			timeline_angle[i] = a; 
			a+=PApplet.PI/6; 
		}
	}
	
	public Cell drawTimeline(){
		for (int i=0; i<12; i++){
			view.strokeWeight(6-i*6/12);
			//view.noFill(); 
			if (i%2==0) {
				view.stroke(100, 100, 255);
				//view.fill(200+i*5, 50+i*5, 50+i*5); 
				
			}
			else {
				view.stroke(255, 255, 0);
				//view.fill(250, 250, i*10); 
			}
			int ni = i+1;
			view.arc(center.x, center.y, radius, radius, 
					timeline_angle[i]-PConstants.HALF_PI, timeline_angle[ni]-PConstants.HALF_PI);
		}
		//plot a arrow at the start 
		view.fill(100, 100, 255);
		//view.strokeWeight(4);
		//view.stroke(200); 
		
		float h = 10, a =8; 
		float ang = timeline_angle[0]-0.04f; 
		view.triangle(
				center.x + (radius-a)*PApplet.sin(ang), 
				center.y - (radius-a)*PApplet.cos(ang),
				center.x + (radius+a)*PApplet.sin(ang), 
				center.y - (radius+a)*PApplet.cos(ang), 
				center.x + radius*PApplet.sin(ang) + h*PApplet.cos(ang), 
				center.y - radius*PApplet.cos(ang) + h*PApplet.sin(ang)
				); 
		//draw sector 
		//for (int i=0; i<1; i++)
		//view.line(center.x, center.y, center.x+Cilium.max_r*PApplet.sin(timeline_angle[i]), arg3);
		
		return this; 
	}
	
	public void drawTimeline(float start_ang, float end_ang){
		view.strokeWeight(4);
		view.stroke(100, 140, 255); 
		view.noFill();
		view.arc(center.x, center.y, radius, radius, start_ang-PConstants.HALF_PI, end_ang-PConstants.HALF_PI);
		view.noStroke();
		view.fill(100, 140, 255); 
		view.triangle(center.x - 0.18f*radius, center.y - radius*0.92f, 
				center.x - 0.20f*radius,  center.y - radius*1.04f,
					center.x,  center.y - radius);
		view.ellipse(center.x + 0.16f*radius, center.y-radius*0.98f, 6, 6); 
	}

	public Cell drawBlackTimeline(){
		view.strokeWeight(4);
		view.stroke(0); 
		view.noFill();
		view.arc(center.x, center.y, radius+4, radius+4, start_angle-PConstants.HALF_PI, end_angle-PConstants.HALF_PI);
		view.noStroke();
		view.fill(0); 
		view.triangle(center.x - 0.18f*radius, center.y - radius*0.92f-4, 
				center.x - 0.20f*radius,  center.y - radius*1.04f-4,
					center.x,  center.y - radius-4);
		view.ellipse(center.x + 0.16f*radius, center.y-radius*0.98f-4, 6, 6); 
		return this; 
	}
	
	public Cell drawSimpleTimeline(){
		view.strokeWeight(2);
		view.stroke(0); 
		view.noFill();
		view.arc(center.x, center.y, radius, radius, start_angle-PConstants.HALF_PI, end_angle-PConstants.HALF_PI);
		view.noStroke();
		view.fill(0); 
		view.triangle(center.x - 0.16f*radius, center.y - radius*0.92f, 
					center.x - 0.16f*radius,  center.y - radius*1.04f,
					center.x - 0.07f*radius,  center.y - radius);
		view.ellipse(center.x + 0.16f*radius, center.y - radius*0.98f, 3, 3); 
		return this; 
	}
	
	public Plastid search(String q){
		for (Plastid p:plastids){
			if (p.label.contains(q)) return p; 
		} 
		return null; 
	}
	
	public Cell drawTimeRing(float ang){
		view.textAlign(PApplet.CENTER, PApplet.CENTER);
		float rmin = Cilium.min_h + Cell.radius; 
		float rmax = Cilium.max_h + Cell.radius; 
		float dr = (rmax-rmin)/3; 
		view.stroke(200); 
		view.strokeWeight(1);
		float rr = rmin; 
		view.fill(150);
		//view.text("+0 months", center.x + rr*PApplet.sin(ang), center.y - rr*PApplet.cos(ang));
		view.noFill(); 
		view.ellipse(center.x, center.y, rr, rr);
		rr+=dr; 
		//view.text("+4 months", center.x + rr*PApplet.sin(ang), center.y - rr*PApplet.cos(ang));
		view.ellipse(center.x, center.y, rr, rr);
		rr+=dr; 
		//view.text("+8 months", center.x + rr*PApplet.sin(ang), center.y - rr*PApplet.cos(ang));
		view.ellipse(center.x, center.y, rr, rr);
		rr+=dr; 
		//view.text("+1 year", center.x + rr*PApplet.sin(ang), center.y - rr*PApplet.cos(ang));
		view.ellipse(center.x, center.y, rr, rr);
		return this; 
	}
	
	public void setTimeMark(){
		float d = 1.0f/3; 
		for (float t = 0; t<=1; t+=d){
			long ms = Map.LinearMap(t, user.min_thread_duration, user.max_thread_duration); 
			Period p = new Period(ms); 
			view.info.add(User.YearsMonthsDaysHours.print(p)); 
		}
	}
	public Cell layoutCiliums(){
		//fill an array of times 
		float[] time = new float[ciliums.size()];
		Calendar cal = Calendar.getInstance();
	    try {
			cal.setTime(Record.sdf.parse("2010-01-01 00:00:00"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    int k=0;
		for(Cilium c:ciliums){
			time[k]=(float)(c.thread.start - cal.getTimeInMillis())/(float)31556926000L; 
			System.out.println("time "+k+": "+time[k]); 
			k++; 
		}
		//from upper right, in clock-wise direction 
		float[] angle = Map.LinearMap(time, start_angle, end_angle); 
		for (int i=0; i<angle.length; i++){
			ciliums.get(i).setParameters().setAngle(angle[i]); 
		}
		//ciliums.remove(angle.length-1);
		return this;
	}
	public void FisheyeDistortion(pt m) {
		vec v = new vec(center, m); 
		float a0 = Fisheye.computeAngle(v); 
		reset(); 
		//distort cilium angle
		float[] a = new float[ciliums.size()];
		for (int i = 0; i < ciliums.size(); i++) {
			a[i] = ciliums.get(i).angle; 
			a[i] = Fisheye.fisheye_scale(a0, a[i]); 
		}
		for (int i = 0; i < ciliums.size(); i++) {
			ciliums.get(i).setPosition(a[i]); 
		}
		//distort timeline angle
		for (int i=0; i<timeline_angle.length; i++){
			timeline_angle[i] = Fisheye.fisheye_scale(a0, timeline_angle[i]); 
			if (i>0){
				if (timeline_angle[i] < timeline_angle[i-1]) timeline_angle[i] +=PApplet.TWO_PI; 
			}
		}
	}
	
	public void reset(){
		for (int i = 0; i < ciliums.size(); i++) {
			ciliums.get(i).resetPosition(); 
		}
		setTimelineAngle(); 
	}
	
	public Cell layoutPlastids(){
		//set interpolated positions
		for (Plastid p:plastids){
			float[] weight = new float[ciliums.size()]; 
			int k=0;int sum=0; 
			for (Cilium c:ciliums){
				if (c.contains(p.label)) weight[k]=1; 
				else weight[k]=0;
				sum +=weight[k]; 
				k++; 
			}
			p.setRadius(sum);
			for (k = 0; k<weight.length; k++){
				weight[k]/=(float) sum; 
				p.addPos(weight[k], ciliums.get(k).contact); 
			}
		}
		//Plastid.pack(this, 10);
		return this;
	}
	
	public Cell simpleLayoutPlastids(){
		//set interpolated positions
		for (Plastid p:plastids){
			float[] weight = new float[ciliums.size()]; 
			int k=0;int sum=0; 
			for (Cilium c:ciliums){
				if (c.contains(p.label)) weight[k]=1; 
				else weight[k]=0;
				sum +=weight[k]; 
				k++; 
			}
			for (k = 0; k<weight.length; k++){
				weight[k]/=(float) sum; 
				p.addPos(weight[k], ciliums.get(k).contact); 
			}
		}
		Plastid.simplePack(this, 50);
		return this;
	}
	
	public static void main(String[] args){
		float dx = -0.1f; 
		float dy = -5; 
		float a = PApplet.atan2(dx, -dy);
		if (a<0) a+=PApplet.TWO_PI; 
		System.out.println(a); 
	}
}
