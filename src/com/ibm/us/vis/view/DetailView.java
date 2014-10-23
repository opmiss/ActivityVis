package com.ibm.us.vis.view;
import java.util.ArrayList;
import com.ibm.us.data.model.User;
import com.ibm.us.vis.geo.pt;
import com.ibm.us.vis.model.Cell;
import com.ibm.us.vis.model.Cilium;
import com.ibm.us.vis.model.Plastid;
import processing.core.PApplet;

public class DetailView extends PApplet {
	  Cell cell=null;
	  public int height=600, width=600;
	  
	  public DetailView(int w, int h, User u, float alpha) {
		this.width = w; this.height = h;
		cell = (new Cell(this)).setCenter(h/2, w/2).setRadius(w/5)
				.setData(u).setAlpha(alpha).layoutCiliums().layoutPlastids();
	  }
	  public void setup() { 
	    size(this.width, this.height);
	    ellipseMode(CENTER);
	    ellipseMode(RADIUS);
	    //textAlign(CENTER, CENTER);
	    //textMode(PApplet.SHAPE); 
	    textSize(20); 		
	    smooth();
	    //noLoop();
		if (cell == null) {
			User user = (new User("KTG0001")).setPath("../data/records/").loadFile().merge().sort().summarize();
			long t = System.currentTimeMillis();
			cell = (new Cell(this)).setCenter(height/2, width/2).setRadius(width/5).setData(user).setAlpha(0.5f).layoutCiliums().layoutPlastids();
			System.out.println("render time: "+ (System.currentTimeMillis()-t) ); 
		}
		info.add("User ID: "+cell.user.UserID);
		info.add("Selected Contact: "); 
		info.add(""); 
		cell.setTimeMark();
	  }
	  public ArrayList<String> info = new ArrayList<String>();
	  int offsetX = 20, offsetY=22; 
	  public void drawLabel(){
		  this.textAlign(LEFT, TOP);
		  fill(50); 
		  int y = offsetY; 
		  textSize(20); 
		  text(info.get(0), offsetX, y);
		  y+=offsetY; 
		  textSize(16); 
		  text(info.get(1)+info.get(2), offsetX, height-offsetY);
		 /* fill(100);
		  y = height-offsetY-offsetY-offsetY-offsetY; 
		  float x = width-100; offsetY=18; 
		  text(info.get(3), x, y);
		  y+=offsetY; 
		  text(info.get(4), x, y);
		  y+=offsetY; 
		  text(info.get(5), x, y);
		  y+=offsetY; 
		  text(info.get(6), x, y);*/
	  }
	  public void draw() {
		  background(255); 
		  //cell.drawBlackCiliums().drawBlackPlastids().drawBlackTimeline(); 
		  drawLabel();
		//  cell.drawTimeRing(0).drawTimeline().drawCiliums().drawPlastids();
		 if (cell.contain(mouseX, mouseY))
			  cell.drawTimeRing(0).drawTimeline().drawCiliums().drawPlastids();
		  else
			  cell.drawTimeRing(0).drawTimeline().drawPlastids().drawCiliums();
		  if (selected != null){
			  selected.drawHighlighted(this);
		  }
	  }
	  Plastid selected = null; 
	  
	  public void highlight(String q){
		  selected = cell.search(q); 
	  }
	  public void mousePressed(){	
		if (key=='p') {
			Plastid.pack(cell, 5); 
		}
	  }
	  public void mouseMoved(){
		  if (key=='f'){
				cell.FisheyeDistortion(pt.mouse(this));
		  }
	  }
	  public void setAlpha(float alpha){
		  cell.setAlpha(alpha).layoutCiliums(); 
		  redraw(); 
	  }
	  public void keyPressed(){
		  if (key=='c') {
			  System.out.println("save a frame"); 
			  this.saveFrame("pic/pic-####.png"); 
		  }
		  if (key=='r'){
			  Cilium.unpack(cell);
			  cell.reset(); 
		  }
	  }
}