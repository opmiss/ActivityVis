package com.ibm.us.ui;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.JInternalFrame;
import com.ibm.us.data.model.User;
import com.ibm.us.vis.view.DetailView;
import com.ibm.us.vis.view.DigestView;

public class CellFrame extends JInternalFrame {
	static final int xoffset = 600, yoffset = 10;
	static int width=600, height=600;
	public DetailView cview;
	public ArrayList<DetailView> cviews; 
	
	public CellFrame(User user, float alpha){
		super("Cell View",
	              false, //resizable
	              true,  //closable
	              false, //maximizable
	              false);//iconifiable
		setSize(width+20, height+50);
		setLocation(xoffset, yoffset);
		cview = new DetailView(width, height, user, alpha); 
		this.add(cview);    
		cview.init();
	}
	
	public CellFrame(ArrayList<User> users, float alpha){
		super("Multiple Cell View",
	              true, //resizable
	              true,  //closable
	              true, //maximizable
	              true);//iconifiable
		setSize(width+20, height+40);
		this.setLayout(new GridLayout(2, 2));
		setLocation(xoffset, yoffset);
		for (User u:users){
			DigestView dv = new DigestView(width/2, height/2, u, alpha); 
			this.add(dv); 
			dv.init(); 
		}
	}
	
}
