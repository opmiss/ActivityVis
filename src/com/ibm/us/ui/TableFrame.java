package com.ibm.us.ui;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import com.ibm.us.data.model.Ethread;
import com.ibm.us.data.model.User;
import com.ibm.us.ui.listener.TableFilterListener;
import com.ibm.us.ui.listener.TableRowListener;
import com.ibm.us.ui.listener.SliderListener;
import com.ibm.us.ui.listener.ViewControlListener;

public class TableFrame extends JInternalFrame {
	//table of users
	public MyTableModel userTableModel; 
	Object[][] userTableData; 
	public JTable userTable; 
	int num_user_table_cols = 6; 
	int num_users;  
	public ArrayList<User> user_list; 
	//table of threads
	public MyTableModel sessionTableModel; 
	Object[][] sessionTableData; 
	JTable sessionTable; 
	int num_session_table_cols = 5; 
	int num_sessions; 
	public Window window; 
	public User selected; 
	protected int frameid;
	static final int xoffset = 5, yoffset = 15;
	public JTextField filtertxt;
	public SliderPane sizePane;
	public long max_size= Long.MIN_VALUE; 
	public long min_size = Long.MAX_VALUE; 
	private TableRowSorter<MyTableModel> sorter;
	
	public TableFrame(ArrayList<User> ul, Window w){
		super("Browse Files",
	              false, //resizable
	              false, //closable
	              false, //maximizable
	              true); //iconifiable
		setSize(600,650);
		setLocation(xoffset, yoffset);
		this.setLayout(new GridBagLayout()); 
		this.window = w; 
		this.user_list = ul;
		userTableModel = new MyTableModel(user_list);
		selected = user_list.get(0); 
		sessionTableModel = new MyTableModel(selected); 
		init();
	}
	
	private void init(){
		JPanel FilterPanel = initFilters(); 
		GridBagConstraints c = new GridBagConstraints(); 
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx = 0;     
		c.gridy = 0; 
		c.ipady=0; 
		c.weightx = 1; 
		add(FilterPanel, c); 
		//JPanel userPanel = new JPanel(); 
		userTable = new JTable(userTableModel);
		userTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); 
		ListSelectionModel lsm = userTable.getSelectionModel();
        lsm.addListSelectionListener(new TableRowListener(this)); 
		JScrollPane userPane = new JScrollPane(userTable);
		userPane.setBorder(BorderFactory.createTitledBorder("User Table"));
		c.gridy =1; 
		c.ipady=130; 
		add(userPane, c);
		sessionTable = new JTable(sessionTableModel);
		JScrollPane streamPane = new JScrollPane(sessionTable); 
		streamPane.setBorder(BorderFactory.createTitledBorder("Thread Table"));
		c.gridy =2; 
		c.ipady=210; 
		add(streamPane, c); 
		sorter = new TableRowSorter<MyTableModel>(userTableModel);
		userTable.setRowSorter(sorter); 
	}
	
	public User selectUser(){
		int rid = userTable.getSelectedRow(); 
		String id = (String) userTable.getValueAt(rid, 0); 
		for (User u:user_list){
			if (u.UserID.equals(id)) return u; 
		}
		return null; 
	}
	
	public ArrayList<User> selectUsers(){
		int[] rid = userTable.getSelectedRows();
		ArrayList<User> users = new ArrayList<User>(); 
		for (User u:user_list){
			for (int k=0; k<rid.length; k++){
				String id = (String) userTable.getValueAt(rid[k], 0); 
				if (u.UserID.equals(id)) users.add(u); 
			}
		}
		return users; 
	}
	
	public void updateSessionTable(){
	    selected = selectUser(); 
		sessionTableData = parse_user(selected); 
		sessionTableModel.setTableData(sessionTableData); 
	}
	
	private Object[][] parse_users(ArrayList<User> users){
		Object[][] tabdata = new Object[users.size()][num_user_table_cols]; 
		int i=0; 
		for(User u:users){
			tabdata[i++] = u.digest(); 
		}
		return tabdata; 
	}
	
	private Object[][] parse_user(User selected_user){
		ArrayList<Ethread> sessions = selected_user.getSessions(); 
		Object[][] tabdata = new Object[sessions.size()][num_session_table_cols];
		int i=0; 
		for(Ethread session:sessions){
			tabdata[i++] = session.digest(); 
		}
		return tabdata;
	}
	
	public void filterUserID() {
		RowFilter<MyTableModel, Object> rf = null;
		try {
			rf = RowFilter.regexFilter(filtertxt.getText(), 0);
		} catch (java.util.regex.PatternSyntaxException e) {
			e.printStackTrace(); 
			return;
		}
		sorter.setRowFilter(rf);
	}
	
	public void filterName(){
		RowFilter<MyTableModel, Object> rf = null;
		try {
			rf = RowFilter.regexFilter(filtertxt.getText(), 1);
		} catch (java.util.regex.PatternSyntaxException e) {
			e.printStackTrace(); 
			return;
		}
		sorter.setRowFilter(rf);
	}
	
	public void filterValue(){
		System.out.println("filter value"); 
		 RowFilter<MyTableModel,Integer> valueFilter = new RowFilter<MyTableModel,Integer>() {
			 public boolean include(Entry<? extends MyTableModel, ? extends Integer > entry) {
			     if ((int)entry.getValue(5) < Integer.parseInt(sizePane.getValue())) return true;
			     return false;
			     }
			 };
		sorter.setRowFilter(valueFilter);
	}
	
	public class MyTableModel extends AbstractTableModel implements TableModelListener {
		private String[] user_col_names = 
			{"UserID", "Name", "# of Threads", "# of Contacts", "# of Emails", "# of Attachments"}; 
		private String[] session_col_names = 
			{"Start Time", "Duration", "# of Participants", "# of Emails", "# of Attachments"};
		private String[] col_names; 
		private Object[][] data;
		public MyTableModel(Object input){
			if (input instanceof ArrayList<?>) {
				col_names = user_col_names; 
				data = parse_users((ArrayList<User>)input); 
			}
			else if (input instanceof User){
				col_names = session_col_names; 
				data = parse_user((User)input); 
			}
		}; 
		public int getColumnCount(){ return col_names.length; }
		public String getColumnName(int col) { return col_names[col]; }
		public int getRowCount(){ return data.length; }
		public Object getValueAt(int row, int col){ return data[row][col]; }
		public void tableChanged(TableModelEvent e) { System.out.println(e);}
		public void setTableData(Object[][] newdata) {
			data = newdata; 
			fireTableDataChanged();  
		}
	}
	private JPanel initFilters(){
		JPanel northPanel = new JPanel(new BorderLayout());
		JPanel filter = new JPanel(new GridLayout(2, 1));
		JPanel filter_user = new JPanel(new GridLayout(0, 2)); 
		filter_user.setBorder(BorderFactory.createTitledBorder("Filter Users"));
		filtertxt = new JTextField(15);
		filter_user.add(filtertxt);
		TableFilterListener tfl = new TableFilterListener(this); 
		JButton button1 = new JButton("Search ID");
		JButton button2 = new JButton("Search Name"); 
		button1.addActionListener(tfl); 
		button2.addActionListener(tfl); 
		JPanel filter_user_buttons = new JPanel(new GridLayout(2,1));
		filter_user_buttons.add(button1); 
		filter_user_buttons.add(button2); 
		filter_user.add(filter_user_buttons);
		filter.add(filter_user); 
		for(User u:user_list){
			max_size = Math.max(max_size, u.num_attachs); 
			min_size = Math.min(min_size, u.num_attachs); 
		}
		sizePane = new SliderPane("Filter Att Size", "Att: ", (int)min_size, (int) max_size, (int)max_size, false);
		sizePane.addListener(new SliderListener(this)); 
		filter.add(sizePane); 
		northPanel.add(filter, BorderLayout.WEST);
		JPanel filter_view = new JPanel(new BorderLayout());
		filter_view.setBorder(BorderFactory.createTitledBorder("View Control"));
		filter_view.setLayout(new GridLayout(3, 1));
		ViewControlListener vcl = new ViewControlListener(this); 
		JButton single = new JButton("Create Main View");
		JButton multiple = new JButton("Create Multiple");
		JButton search = new JButton("Highlight User");
		single.addActionListener(vcl);
		multiple.addActionListener(vcl);
		search.addActionListener(vcl);
		filter_view.add(single);
		filter_view.add(multiple); 
		filter_view.add(search); 
		northPanel.add(filter_view, BorderLayout.EAST); 
		return northPanel; 
	}
}
