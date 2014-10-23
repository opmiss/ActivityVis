package com.ibm.us.vis.model;
import java.util.ArrayList;
import com.ibm.us.vis.geo.pt;
import com.ibm.us.vis.geo.vec;
import com.ibm.us.vis.view.DetailView;

public class Plastid {
	pt pos; 
	vec offset;  
	//display parameters 
	public float radius; 
	public static final float r=2, dr2=4, dr22=16; 
	public float[] weight = null; 
	public String label; 
	boolean show_label=false;
	boolean normal = true; 
	public ArrayList<Cilium> ciliums = new ArrayList<Cilium>(); 
	
	public Plastid(){
		pos = new pt(0, 0); offset = new vec(); 
	}
	
	public Plastid setLabel(String l){
		label =l; 
		String[] parts = l.split("@");
		if (parts.length !=2){normal = false; return this;}
		normal = parts[1].equals("dtaa.com")?true:false; 
		return this; 
	}
	
	public Plastid addPos(float w, pt p){
		pos.addScaledPt(w, p);
		return this; 
	}
	
	public float getX(){
		return pos.x+offset.x;  
	}
	
	public float getY(){
		return pos.y+offset.y; 
	}
	
	public pt getP(){
		return new pt(this.getX(), this.getY()); 
	}
	
	public void addO(float x, float y){
		offset.x+=x; offset.y+=y; 
	}
	
	public void subO(float x, float y){
		offset.x-=x; offset.y-=y;  
	}
	
	public void sink(){
		offset.x*=0.998f; offset.y*=0.998f; 
	}
	
	public void setRadius(int num){
		float r=4; 
		for (int i=1; i<num; i++){
			r = r/1.5f; 
		}
		radius = 8-r; 
	}
	
	public void drawSelected(DetailView pa){
		pa.stroke(150);
		pa.strokeWeight(2); 
		if (normal){
			this.drawNormal(pa);
		}
		else{
			this.drawAbnormal(pa);
		}
	}
	
	public void drawSimple(DetailView pa){
		pa.stroke(0);
		pa.strokeWeight(1);
		pa.fill(255);
		pa.ellipse(getX(), getY(), r, r);
	}
	
	public void drawPlain(DetailView pa){
		pa.stroke(0);
		pa.strokeWeight(1);
		pa.fill(255);
		pa.ellipse(getX(), getY(), radius, radius);
	}
	

	public void draw(DetailView pa) {
		boolean in = pt.mouse(pa).disTo(this.getP()) < radius+1; 
		if (in){
			pa.stroke(100, 155, 255);
			pa.strokeWeight(4);
			for (Cilium c:ciliums) c.drawHeadSelected(pa); 
			pa.info.set(2, this.label); 
			pa.stroke(100, 100, 255);
		}
		else {
			pa.stroke(255);
			pa.strokeWeight(1);
		}
		if (normal){
			this.drawNormal(pa);
		}
		else{
			this.drawAbnormal(pa);
		}
	}
	
	public void drawBlack(DetailView pa){
		pa.stroke(0);
		pa.strokeWeight(1);
		pa.fill(255);
		pa.ellipse(getX(), getY(), radius, radius);
	}
	
	public void drawAbnormal(DetailView cv){
		cv.fill(255, 100, 100);
		cv.ellipse(getX(), getY(), radius, radius);
	}
	
	public void drawNormal(DetailView cv){
		cv.fill(170);
		cv.ellipse(getX(), getY(), radius, radius);
	}
	
	public void drawTransparent(DetailView cv){
		cv.noFill();
		cv.ellipse(getX(), getY(), radius, radius);
	}
	
	public void drawHighlighted(DetailView cv){
		//cv.stroke(0);
		//cv.strokeWeight(2);
		if (normal){
			this.drawNormal(cv);
		}
		else{
			this.drawAbnormal(cv);
		}
	}
	
	public static void repel(ArrayList<Plastid> plastids, boolean simple){
		for (int i=0; i<plastids.size(); i++){
			for (int j=i+1; j<plastids.size(); j++){
				float dx = plastids.get(i).getX() - plastids.get(j).getX();
				float dy = plastids.get(i).getY() - plastids.get(j).getY(); 
				float dx2= dx*dx; 
				float dy2 = dy*dy; 
				float d2 = dx2+dy2; 
				float r; 
				if (simple){
					r = dr2; 
				}
				else{
					r = plastids.get(i).radius+ plastids.get(j).radius; 
				}
				float r2 = r*r; 
				if ( d2 < r2){
					float d = (float)Math.sqrt(d2); 
					float f = (r-d)/r; 
					float fx = f*dx; float fy = f*dy; 
					if (d==0){
						dx = (float) (Math.random()-0.5); dx2= dx*dx; 
						dy = (float) (Math.random()-0.5); dy2 = dy*dy;
						fx = (float) (Math.sqrt(dx2/(dx2+dy2))*r); 
						if (dx<0) fx =-fx; 
						fy = (float) (Math.sqrt(dy2/(dx2+dy2))*r); 
						if (dy<0) fy =-fy; 
					}
					plastids.get(j).subO(fx, fy); 
					plastids.get(i).addO(fx, fy); 
				}
			}
		}
	}
	
	public static void confine(ArrayList<Plastid> plastids, Cell cell){
		for (Plastid p:plastids){
			float dx = p.getX() - Cell.center.x;
			float dy = p.getY() - Cell.center.y; 
			float dx2= dx*dx, dy2 = dy*dy; 
			float d2 = dx2+dy2; 
			if ( d2 > Cell.r2){
				float d = (float)Math.sqrt(d2); 
				float f = (d-Cell.radius)/d; 
				float fx = f*dx, fy = f*dy; 
				p.subO(fx, fy); 
			}
		}
	}
	
	public static void sink(Cell cell){
		for (Plastid p:cell.plastids) {
			p.sink(); 
			float dx = p.getX() - Cell.center.x;
			float dy = p.getY() - Cell.center.y; 
			float dx2= dx*dx, dy2 = dy*dy; 
			float d2 = dx2+dy2; 
			if ( d2 > Cell.r2){
				float d = (float)Math.sqrt(d2); 
			//	System.out.println(Cell.r0); 
				float f = (d-Cell.r0)/d; 
				float fx = f*dx, fy = f*dy; 
				p.subO(fx, fy); 
			}
		}
	}
	
	public static void pack(Cell cell, int num){
		for (int i=0; i<num; i++){
			 Plastid.repel(cell.plastids, false); 
			 Plastid.sink(cell); 
		}
	}
	public static void simplePack(Cell cell, int num){
		for (int i=0; i<num; i++){
			 Plastid.repel(cell.plastids, true); 
			 Plastid.sink(cell); 
		}
	}

}
