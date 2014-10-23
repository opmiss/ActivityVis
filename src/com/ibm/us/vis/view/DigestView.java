package com.ibm.us.vis.view;
import com.ibm.us.data.model.User;
import com.ibm.us.vis.model.Cell;
import com.ibm.us.vis.model.Plastid;

public class DigestView extends DetailView {
	
	  public DigestView(int w, int h, User u, float alpha) {
		super(w, h, u, alpha); 
	  }
	  
	  public void draw() {
		  background(255); 
		  textSize(10); 
		  fill(0); 
		  text(info.get(0), width/3, height/2);
		  cell.drawSimpleCiliums().drawSimpleTimeline();	  
	  }
	  
	  public void mousePressed(){
	  }
}

	  