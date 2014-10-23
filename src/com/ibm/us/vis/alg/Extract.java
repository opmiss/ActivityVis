package com.ibm.us.vis.alg;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import com.ibm.us.data.model.Record;
import com.ibm.us.data.model.User;
import com.ibm.us.data.util.FileWrite;

public class Extract {
	
	public static void extractPairs(ArrayList<User> users){
		ArrayList<ArrayList<String>> pairs = new ArrayList<ArrayList<String>>(); 
		for (int i=0; i<users.size()-1; i++){
			for (int j=i+1;j<users.size();j++){
				String name1 = users.get(i).Name;
				String name2 = users.get(j).Name; 
				if (users.get(j).contacts.contains(name1) && users.get(i).contacts.contains(name2)){
					ArrayList<String> pair = new ArrayList<String>(); 
					pair.add(users.get(i).UserID); 
					pair.add(users.get(j).UserID); 
					pairs.add(pair); 
				}
			}
		}
		FileWrite fr = new FileWrite("data/pairs.csv");  
		for (ArrayList<String> pair:pairs){
			String line = pair.get(0)+","+pair.get(1);
			fr.writeLine(line);
		}	
		fr.close(); 
	}
	
	public static void extractNums(ArrayList<User> users){
		int num = users.size(); 
		int[] ne = new int[num];
		int[] na = new int[num]; 
		int[] nc = new int[num];
		int k=0; 
		for (User u:users){
			if (u.records != null) {
				int n_e = u.records.size();
				int n_a = 0;
				Set<String> contacts = new HashSet<String>();
				for (Record r : u.records) {
					n_a += r.attach;
					contacts.addAll(r.to);
				}
				na[k] = n_a;
				nc[k] = contacts.size();
				ne[k] = n_e;
				k++;
			}
		}
		FileWrite fr = new FileWrite("data/nums.csv");  
		for (int i=0; i<num; i++){
			String line = nc[i]+","+ne[i]+","+na[i];
			fr.writeLine(line);
		}	
		fr.close();
	} 
	
	
	public static void extractBins(ArrayList<User> users){
		int[] Y = new int[12]; for (int i=0; i<12; i++) Y[i]=0; 
		int[] M = new int[31];  for (int i=0; i<31; i++) M[i]=0; 
		int[] D = new int[24];  for (int i=0; i<24; i++) D[i]=0; 
		for (User u : users) {
			if (u.records != null) {
				for (Record r : u.records) {
					// System.out.println(Record.sdf.format(r.time.getTime()));
					int y = r.time.get(Calendar.MONTH);
					int m = r.time.get(Calendar.DAY_OF_MONTH)-1;
					int d = r.time.get(Calendar.HOUR_OF_DAY);
					// System.out.println(y+","+m+","+d);
					Y[y]++;
					M[m]++;
					D[d]++;
				}
			}
		}
		FileWrite fr = new FileWrite("data/bins.csv");  
		StringBuffer yb = new StringBuffer(); 
		StringBuffer mb = new StringBuffer(); 
		StringBuffer db = new StringBuffer(); 
		for (int y:Y) yb.append(y).append(','); 
		for (int m:M) mb.append(m).append(',');
		for (int d:D) db.append(d).append(','); 
		fr.writeLine(new String(yb));
		fr.writeLine(new String(mb));
		fr.writeLine(new String(db));
		fr.close();
	}
	
	public static int[] Y = new int[12]; 
	public static int[] M = new int[31]; 
	public static int[] W = new int[7]; 
	public static int[] D = new int[24]; 
	
	public static void initYMWD(){
		for (int i=0; i<12; i++) Y[i] = 0;  
		for (int i=0; i<31; i++) M[i] = 0;  
		for (int i=0; i<7; i++) W[i] = 0; 
		for (int i=0; i<24; i++) D[i] = 0;  
	}
	
	public static void extractYMWD(User user){
		for (Record r : user.records) {
			int y = r.time.get(Calendar.MONTH); Y[y]++;
			int m = r.time.get(Calendar.DAY_OF_MONTH)-1; M[m]++;
			int w = r.time.get(Calendar.DAY_OF_WEEK); 
			if (w == Calendar.MONDAY) W[0]++; 
			if (w == Calendar.TUESDAY) W[1]++; 
			if (w == Calendar.WEDNESDAY) W[2]++; 
			if (w == Calendar.THURSDAY) W[3]++; 
			if (w == Calendar.FRIDAY) W[4]++; 
			if (w == Calendar.SATURDAY) W[5]++; 
			if (w == Calendar.SUNDAY) W[6]++; 
			int d = r.time.get(Calendar.HOUR_OF_DAY); D[d]++;
		}
	}
	
	public static int[] extractNum(User user){
		int[] ret = new int[3]; 
		if (user.records != null){
			ret[0] = user.records.size();
			ret[1] = 0;
			for (Record r: user.records){
				ret[1] += r.attach; 
			}
			ret[2] = user.contacts.size(); 
		}
		return ret; 
	}
	
	public static void extract(){
		File file = new File("data/records"); 
		File[] files = file.listFiles();
		ArrayList<int[]> num = new ArrayList<int[]>(); 
		initYMWD(); 
		for (File f:files){
			User u = (new User()).loadFile(f).summarize();
			if (u.records!=null){
				System.out.println(u.UserID); 
				num.add(extractNum(u));
				extractYMWD(u);
			}
		}
		FileWrite fr = new FileWrite("data/nums.csv");  
		for (int[] n:num) {
			String line = n[0]+","+n[1]+","+n[2];
			fr.writeLine(line);
		}	
		fr.close();
		fr = new FileWrite("data/bins.csv");  
		StringBuffer yb = new StringBuffer(); 
		StringBuffer mb = new StringBuffer(); 
		StringBuffer wb = new StringBuffer(); 
		StringBuffer db = new StringBuffer(); 
		for (int y:Y) yb.append(y).append(','); 
		for (int m:M) mb.append(m).append(',');
		for (int w:W) wb.append(w).append(','); 
		for (int d:D) db.append(d).append(','); 
		fr.writeLine(new String(yb));
		fr.writeLine(new String(mb));
		fr.writeLine(new String(wb)); 
		fr.writeLine(new String(db));
		fr.close();
	}
	
	public static void main(String[]args){
		extract(); 
	}
}
