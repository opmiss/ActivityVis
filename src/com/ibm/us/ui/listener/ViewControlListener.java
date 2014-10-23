package com.ibm.us.ui.listener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import com.ibm.us.data.model.User;
import com.ibm.us.ui.TableFrame;
import com.ibm.us.vis.view.DetailView;

public class ViewControlListener implements ActionListener{
	TableFrame tf; 
	public ViewControlListener(TableFrame t){
		tf=t;
	}
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("Create Main View")){
			User user = tf.selectUser();
			long t1 = System.currentTimeMillis(); 
			if (user!=null) tf.window.createCellFrame(user);
			long t2 = System.currentTimeMillis(); 
			System.out.println("render time: "+(t2-t1)); 
		}
		else if (event.getActionCommand().equals("Create Multiple")){
			System.out.println("create multiple cell views"); 
			int[] selected = tf.userTable.getSelectedRows(); 
			for (int i=0; i<selected.length; i++) System.out.print(selected[i]+", ");
			ArrayList<User> users = new ArrayList<User>(); 
			for (int i = 0; i<selected.length; i++){
				users.add(tf.user_list.get(selected[i])); 
			} 
			tf.window.createCellFrame(users);
		}
		else if (event.getActionCommand().equals("Highlight User")){
			System.out.println("highlight user");
			if (tf.window.cframe == null) return; 
			DetailView cv = tf.window.cframe.cview;  
			String q = tf.filtertxt.getText(); 
			System.out.println(q); 
			cv.highlight(q);   
		}
	}
}