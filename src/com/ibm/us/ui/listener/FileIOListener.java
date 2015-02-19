package com.ibm.us.ui.listener;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import com.ibm.us.data.model.User;
import com.ibm.us.ui.Window;

public class FileIOListener extends JPanel implements ActionListener {
	JFileChooser jfc;
	JTextArea txtarea;
	public ArrayList<User> user_list;
	//public Pcaps pcaps;
	Window window;
	public FileIOListener(Window w) {
		window = w;
		txtarea = new JTextArea(5, 20);
		txtarea.setMargin(new Insets(5, 5, 5, 5));
		txtarea.setEditable(false);
		jfc = new JFileChooser();
		try {
			File f = new File(new File(".").getCanonicalPath());
			jfc.setCurrentDirectory(f);
		} catch (IOException e) {
			e.printStackTrace();
		}
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("Select Users")) {
			int retval = jfc.showOpenDialog(FileIOListener.this);
			if (retval == JFileChooser.APPROVE_OPTION) {
				File file = jfc.getSelectedFile();
				populateTable(file);
			} else {
				txtarea.append("Open File command failed");
			}
			txtarea.setCaretPosition(txtarea.getDocument().getLength());
		}
	}
	
	public void populateTable(File file) {
		if(window == null){
			System.err.println("Initialize SaveOpenListener before use");
			System.exit(1);
		}
		System.out.println("Creating new frame"); 
		ArrayList<User> users = new ArrayList<User>(); 
		if(file.isDirectory()){
			File[] files = file.listFiles();
			for(File f:files){
				System.out.println("load file: "+f.getName()); 
				long t1 = System.currentTimeMillis(); 
				User u = (new User()).loadFile(f).merge().sort().summarize();
				long t2 = System.currentTimeMillis(); 
				System.out.println("process time: "+(t2-t1)); 
				if (!u.isEmpty()) users.add(u);
				System.out.println(users.size()); 
			}
		}else{
			User u = (new User()).loadFile(file).merge().sort().summarize();
			users.add(u);
		}
		window.createTableFrame(users); 
	}
	
}