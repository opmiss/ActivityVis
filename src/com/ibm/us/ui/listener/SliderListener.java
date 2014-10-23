package com.ibm.us.ui.listener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.ibm.us.ui.TableFrame;

public class SliderListener implements ChangeListener{
	TableFrame tf; 
	public SliderListener(TableFrame t){
		tf=t; 
	}
	public void stateChanged(ChangeEvent e) {
		tf.filterValue();
    }
}
    