package com.ibm.us.ui.listener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.ibm.us.ui.TableFrame;

public class TableFilterListener implements ActionListener{
    TableFrame tf; 
    public TableFilterListener(TableFrame t){
    	tf=t; 
    }
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Search ID")){
			System.out.println("search id");
			tf.filterUserID();
		}
		if (e.getActionCommand().equals("Search Name")){
			System.out.println("search name"); 		
			tf.filterName();
		}
	}
}