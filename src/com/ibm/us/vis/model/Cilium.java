package com.ibm.us.vis.model;
import java.util.ArrayList;
import com.ibm.us.data.model.Record;
import com.ibm.us.data.model.Ethread;
import com.ibm.us.vis.alg.Map;
import com.ibm.us.vis.geo.color;
import com.ibm.us.vis.geo.pt;
import com.ibm.us.vis.geo.vec;
import com.ibm.us.vis.view.DetailView;
import processing.core.PApplet;

public class Cilium {
	//contact point of the cilium
	pt contact=null; 
	//position of the head
	pt pos = null; 
	vec offset = null; 
	//angle of the cilium 
	float angle; 
	Cell cell;
	Ethread thread; 
	float height; //height represent duration
	float radius; //radius of the head represent thread size
	public static float max_r=20, min_r=5; 
	public static float max_h=120, min_h=0; 
	public static float db = 1.5f; 
	float[] width; 	//width represent attachment of each email 
	public ArrayList<Plastid> plastids = new ArrayList<Plastid>(); 
	color head_color; 
	
	public Cilium(Cell c){ 
		cell = c; 
		//max_r = Cell.radius/5; min_r = Cell.radius/12; 
		max_r = Cell.radius/5; min_r = Cell.radius/16; 
		max_h = Cell.radius; min_h = Cell.radius/5; 
		//max_h = Cell.radius; min_h = Cell.radius/6; 
	}
	public Cilium setData(Ethread s){ 
		thread = s; return this; 
	}
	public Cilium setAngle(float angle){
		this.angle=angle;
		setPosition(angle); 
		return this; 
	}
	public void resetPosition(){
		setPosition(angle); 
	}
	public void setPosition(float ang){
		float cx = Cell.center.x+Cell.radius*PApplet.sin(ang);
		float cy = Cell.center.y-Cell.radius*PApplet.cos(ang); 
		contact = new pt(cx, cy); 
		float kx = Cell.center.x + (Cell.radius+height+this.radius)*PApplet.sin(ang); 
		float ky = Cell.center.y - (Cell.radius+height+this.radius)*PApplet.cos(ang);
		pos = new pt(kx, ky); 
		offset = new vec(); 
	}
	
	public Cilium setParameters(){
		this.setHeight().setWidth().setRadius().setColor(); 
		return this; 
	}
	private Cilium setHeight(){
		long d = thread.getDuration();
		System.out.println("duration: "+d); 
		long year = 31536000730L; 
		//float dr = (float)(d-cell.user.min_thread_duration)/(float)(cell.user.max_thread_duration-cell.user.min_thread_duration); 
		height = Map.LinearMap((float)d/(float)year, min_h, max_h); 
		return this; 
	}
	
	private Cilium setWidth(){
		width = new float[thread.records.size()]; 
		int k=0; 
		for (Record r:thread.records){
			width[k++] = r.attach*2; 
		}
		return this; 
	}
	
	private Cilium setRadius(){
		int d = thread.members.size();
		System.out.println("contact: "+d); 
		//float dr = (float)(d-cell.user.min_contact_size)/(float)(cell.user.max_contact_size-cell.user.min_contact_size); 
		radius = Map.LinearMap((float)d/(float)40, min_r, max_r); 
		return this;
	}
	
	private Cilium setColor(){
		int total = plastids.size(); 
		int abnormal = 0; 
		for (Plastid p:plastids){
			if (!p.normal) abnormal ++; 
		}
		if (abnormal==0) head_color = new color(250, 250, 250);
		else{
			float s = (float) abnormal/ (float) total; 
			head_color = new color(255, 255-(int)(255*s), 255 - (int)(255*s)) ; 
			//head_color = color.s(new color(200, 200, 200), s, new color(255, 0, 0)); 
		}
		//System.out.println(s);
		//System.out.println(head_color.r+","+head_color.g+","+head_color.b); 
		return this; 
	}
	
	public float getCenterX(){
		return pos.x+offset.x; 
	}
	
	public float getCenterY(){
		return pos.y+offset.y; 
	}
	
	public void addO(float x, float y){
		offset.x+=x; offset.y+=y; 
	}
	
	public void subO(float x, float y){
		offset.x-=x; offset.y-=y;  
	}
	
	public Cilium drawStem(PApplet pa){
		float b = height/(width.length);
		float y = b; 
		pa.stroke(100, 155, 255);
		pa.strokeWeight(1); 
		pa.line(contact.x, contact.y, getCenterX(), getCenterY()); 
		float lx = getCenterX() - contact.x; 
		float ly = getCenterY() - contact.y; 
		float ang = PApplet.atan2(lx, -ly); 
		pa.pushMatrix(); 
		pa.translate(contact.x, contact.y); 
		pa.rotate(ang); 
		pa.noStroke(); 
		for (int i=0; i<width.length; i++){
			if (thread.records.get(i).isNormal()) pa.stroke(50, 178, 50);
			else pa.stroke(250, 0, 0);
			pa.line(-width[i], -y, 0, -y);
			/*if (thread.records.get(i).isNormal()) pa.fill(50, 178, 50);
			else pa.fill(250, 0, 0);
			pa.rect(-width[i], -y, width[i], db); */
			y+=b;
		}
		pa.popMatrix(); 
		return this; 
	}
	
	public Cilium drawBlackStem(PApplet pa){
		float b = height/(width.length);
		float y = b; 
		pa.stroke(0);
		pa.strokeWeight(1); 
		pa.line(contact.x, contact.y, getCenterX(), getCenterY()); 
		float lx = getCenterX() - contact.x; 
		float ly = getCenterY() - contact.y; 
		float ang = PApplet.atan2(lx, -ly); 
		pa.pushMatrix(); 
		pa.translate(contact.x, contact.y); 
		pa.rotate(ang); 
		pa.noFill(); 
		for (int i=0; i<width.length; i++){
			pa.line(-width[i], -y, 0, -y); 
			y+=b;
		}
		pa.popMatrix(); 
		return this; 
	}
	
	public void drawCilium(DetailView pa){
		this.drawStem(pa).drawHead(pa);
	}
	
	public void drawBlackCilium(DetailView pa){
		this.drawBlackStem(pa).drawBlackHead(pa);
	}
	
	public void drawSimpleCilium(DetailView pa){
		pa.stroke(50);
		pa.strokeWeight(1);
		pa.noFill(); 
		pa.line(contact.x, contact.y, getCenterX(), getCenterY()); 
		pa.ellipse(getCenterX(), getCenterY(), radius, radius);
	}
	public boolean contains(String s){
		return this.thread.members.contains(s); 
	}
	
	boolean out = true; 
	
	/*public void setOut(){
		out = true; 
	}*/
	
	public Cilium drawHead(DetailView pa){
		float tx = pa.mouseX-getCenterX(), ty = pa.mouseY-getCenterY(); 
		boolean in = ((tx*tx+ty*ty) < (this.radius*this.radius)); 
		pa.fill(head_color.r, head_color.g, head_color.b);
		if (in) {
			if (out){
				Cilium.pack(cell, 10);
				out = false; 
			}
			pa.stroke(0);
			pa.strokeWeight(4);
			this.drawHeadSelected(pa); 
			pa.stroke(0);
			pa.strokeWeight(2);
			for (Plastid p:plastids) p.drawHighlighted(pa); 
		}
		else {
			this.drawHeadUnSelected(pa);  
		}
		return this; 
	}
	
	public Cilium drawBlackHead(DetailView pa){
		if (out){
			Cilium.pack(cell, 10);
			out = false; 
			}
		pa.fill(255);
		pa.ellipse(getCenterX(), getCenterY(), radius, radius); 	
		return this; 
	}
	
	public Cilium drawHeadSelected(DetailView pa){
		//pa.strokeWeight(3); 
		//pa.stroke(0);
		pa.fill(head_color.r, head_color.g, head_color.b);
		pa.ellipse(getCenterX(), getCenterY(), radius, radius);
		return this; 
	}
	
	public Cilium drawHeadUnSelected(DetailView pa){
		pa.strokeWeight(1);
		pa.stroke(100, 155, 255);
	//	pa.noFill();
		pa.ellipse(getCenterX(), getCenterY(), radius, radius);
		return this; 
	}
	
	public boolean isIn(PApplet pa){
		float mx = pa.mouseX-contact.x-(height+radius)*PApplet.cos(angle);
		float my = pa.mouseY-contact.y-(height+radius)*PApplet.sin(angle); 
		if (mx*mx+my*my<radius*radius) return true; 
		return false; 
	}
	
	/*public void repelOne(ArrayList<Cilium> ciliums){
		for (Cilium c:ciliums){
			float dx = this.getCenterX() - c.getCenterX();
			float dy = this.getCenterY() - c.getCenterY(); 
			float dx2= dx*dx, dy2 = dy*dy, d2 = dx2+dy2; 
			float r = this.radius+c.radius; 
			float r2 = r*r; 
			if (d2<r2&&d2>0){
				float d = (float) Math.sqrt(d2); 
				float f = (r-d)/r;
				float dcx = this.contact.x - c.contact.x;
				float dcy = this.contact.y - c.contact.y; 
				float dc = (float) Math.sqrt(dcx*dcx + dcy*dcy); 
				dcx = dcx/dc*d; dcy = dcx/dc*d; 			
				c.subO(f*dcx, f*dcy); 
			}
		}
	}*/
	
	public static void repel(ArrayList<Cilium> ciliums){
		for (int i=0; i<ciliums.size(); i++){
			for (int j=i+1; j<ciliums.size(); j++){
				float dx = ciliums.get(i).getCenterX() - ciliums.get(j).getCenterX();
				float dy = ciliums.get(i).getCenterY() - ciliums.get(j).getCenterY(); 
				float dx2= dx*dx, dy2 = dy*dy, d2 = dx2+dy2; 
				float r = ciliums.get(i).radius+ciliums.get(j).radius; 
				float r2 = r*r; 
				if (d2<r2){
					float d = (float) Math.sqrt(d2); 
					float fi = (r-d)*ciliums.get(j).radius/r2;
					float fj = (r-d)*ciliums.get(i).radius/r2;
				  /*float dcx = ciliums.get(i).contact.x - ciliums.get(j).contact.x;
					float dcy = ciliums.get(i).contact.y - ciliums.get(j).contact.y; 
					float dc = (float) Math.sqrt(dcx*dcx + dcy*dcy); 
					dcx = dcx/dc*d; dcy = dcx/dc*d; 
					ciliums.get(j).subO(fj*dcx, fj*dcy);
					ciliums.get(i).addO(fi*dcx, fi*dcy); */
					ciliums.get(j).subO(fj*dx, fj*dy);
					ciliums.get(i).addO(fi*dx, fi*dy);
				}
			}
		}
	}
	
	public void restrain(){
		float x = this.getCenterX() - Cell.center.x; 
		float y =  this.getCenterY() - Cell.center.y; 
		float l = PApplet.sqrt(x*x + y*y);
		float d = l-Cell.radius-this.height-this.radius; 
		this.subO( d/l*x , d/l*y); 
	}
	
	public static void pack(Cell cell, int num){
		for (int i=0; i<num; i++){
			 Cilium.repel(cell.ciliums); 
			 for (Cilium c:cell.ciliums) c.restrain(); 
		}
	}
	
	public static void unpack(Cell cell){
		 for (Cilium c:cell.ciliums) {
			 c.offset.setTo(0, 0);
			 c.out = true;
		 }
	}
}