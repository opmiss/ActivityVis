package com.ibm.us.ui.listener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.ibm.us.ui.TableFrame;

public class TableRowListener implements ListSelectionListener {
	
	 TableFrame tf; 
	 
	 public TableRowListener(TableFrame input){
		 tf =input; 
	 }
	 
	 @Override
	 public void valueChanged(ListSelectionEvent e) {
       // Ignore extra messages.
        if (e.getValueIsAdjusting()) return;
        tf.updateSessionTable();
     }
}