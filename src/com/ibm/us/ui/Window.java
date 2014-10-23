package com.ibm.us.ui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.undo.UndoManager;

import com.ibm.us.data.model.User;
import com.ibm.us.ui.listener.FileIOListener;

public class Window extends JFrame {
	JDesktopPane desktop;
	UndoManager undoredo;
	public TableFrame tbframe; 
	FileIOListener file_listener;
	public CellFrame cframe;  

	public Window(){
		super("ActivityVis");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		init();
	}
	
	private void init(){
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int inset = 100;
		setBounds(inset, inset, screenSize.width  - inset*2, screenSize.height - inset*2);
		desktop = new JDesktopPane();
		setContentPane(desktop);
		JMenuBar menubar = create_menubar();
		setJMenuBar(menubar);
		desktop.setBackground(Color.WHITE);
		desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
	}
	
	public void createTableFrame(ArrayList<User> users){
		TableFrame frame = new TableFrame(users, this);
		tbframe = frame; 
		frame.setVisible(true);
		frame.setBackground(Color.LIGHT_GRAY);
		desktop.add(frame);
	}
	
	public void createCellFrame(User user){
		if (cframe!=null) remove(cframe); 
		cframe = new CellFrame(user, 0.5f);
		cframe.setVisible(true);
        desktop.add(cframe);
	}
	
	public void createCellFrame(ArrayList<User> users){
		if (cframe!=null) remove(cframe); 
		cframe = new CellFrame(users, 0.5f);
		cframe.setVisible(true);
        desktop.add(cframe);
	}
	
	protected void removeCellFrame(){
		desktop.remove(cframe); 
	}
	
	private JMenuBar create_menubar(){
		JMenuBar menubar = new JMenuBar();
		file_listener = new FileIOListener(this); 
		undoredo = new UndoManager();
		JMenu file = new JMenu("File");
		JMenuItem fopendir = new JMenuItem("Select Users");
		fopendir.addActionListener(file_listener); 
		file.add(fopendir);
		JMenu help = new JMenu("Help");
		JMenuItem about = new JMenuItem("About"); 
		help.add(about); 
		menubar.add(file);
		menubar.add(help);
		return menubar;
	}
	
	public static void main(String[]args){
		JFrame.setDefaultLookAndFeelDecorated(true);
		Window window = new Window();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
}