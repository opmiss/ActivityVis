package com.ibm.us.data.model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.lang.Object;
import com.ibm.us.vis.alg.Merging;
import org.joda.time.*;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class User {
	public String UserID; 
	public String Name; 
	public long start; //start time of monitoring
	public long end;  //end time of monitoring 
	public ArrayList<Ethread> threads = null;
	public ArrayList<Record> records = null;
	public Set<String> contacts = null;
	public Integer num_emails, num_attachs; 
	public Long size; 
	public static String data_path = "data/records/"; 
	public static String suffix ="csv"; 
	public static PeriodFormatter YearsMonthsDaysHours = 
			new PeriodFormatterBuilder().appendYears().appendSuffix(" year", " years").
			appendMonths().appendSuffix(" month", " months").appendSeparator(" and ").
			appendDays().appendSuffix(" day", " days").appendSeparator(" and ").
			appendHours().appendSuffix(" hour", " hours").toFormatter(); 
	
	public long max_thread_duration, min_thread_duration; 
	public long max_thread_size, min_thread_size; 
	public int max_contact_size, min_contact_size; 
	
	public User(){
		threads = new ArrayList<Ethread>(); 
	}
	
	public User(String id){
		UserID = id; 
		threads = new ArrayList<Ethread>(); 
	}
	
	public void print(){
		Object[] lines = this.digest(); 
		for (int i=0; i<lines.length; i++)
		System.out.print(lines[i]+", "); 
		System.out.println(); 
	}
	
	public static DateTime toDateTime(Calendar C){
		return new DateTime(C.getTimeInMillis()); 
	}
	
	private static String printPeriodYMD(Period p){
		String s = ""; 
		if (p.getYears()>0){
			if (p.getYears() > 1)
				s+=p.getYears()+" years"; 
			else s+=p.getYears()+" year"; 
		}
		if (p.getMonths()>0){
			s+=p.getMonths() +" months"; 
		}
		s+=p.getDays()+" days";
		return s; 
	}
	
	public Object[] digest(){
		/*DateTime s = toDateTime(start); 
		DateTime e = toDateTime(end); 
		Period p = new Period(s, e); */
		Object[] data = new Object[6];
		data[0] = this.UserID; 
		data[1] = this.Name; 
		data[2] = threads.size();  
		data[3] = contacts.size(); 
		data[4] = num_emails; 
		data[5] = num_attachs;
		/*Object[] data = new Object[7];
		data[0] = this.UserID; 
		data[1] = this.Name; 
		data[2] = YearsMonthsDaysHours.print(p);  
		data[3] = threads.size(); 
		data[4] = num_emails; 
		data[5] = size;
		data[6] = num_attachs; */
		return data; 
	}
	
	public ArrayList<Ethread> getSessions(){
		return threads; 
	}
	
	public User setPath(String path){
		data_path = path; 
		return this; 
	}
	
	public boolean isEmpty(){
		return (threads.size()==0); 
	}
	
	public User sort(){
		if (threads.size()==0) return this; 
		Collections.sort(threads); 
		start = threads.get(0).start; 
		end = threads.get(threads.size()-1).end; 
		int id =0; 
		for (Ethread s:threads){
			s.sort().setID(Integer.toString(id)); 
			id++; 
			
		}
		return this;
	}
	
	public void printTime(){
		System.out.println("start: "+start+", "+"end: "+end);
	}
	
	public User merge(){
		Merging.setInput(threads); 
		Merging.merge(6); 
		threads = Merging.getOutput(); 
		return this; 
	}
	
	public User summarize(){
		num_emails=0; size = (long) 0; num_attachs =0; 
		long max_d = Long.MIN_VALUE; long min_d = Long.MAX_VALUE; 
		long max_s = Long.MIN_VALUE; long min_s = Long.MAX_VALUE; 
		int max_c = Integer.MIN_VALUE; int min_c = Integer.MAX_VALUE; 
		contacts = new HashSet<String>(); 
		for (Ethread s:threads){
			long d = s.getDuration(); 
			if (d > max_d) max_d = d; 
			if (d < min_d) min_d = d; 
			long sz = s.size; 
			if (sz > max_s) max_s = sz; 
			if (sz < min_s) min_s = sz;  
			num_emails += s.records.size(); 
			size += s.size; 
			num_attachs += s.num_attachs;
			contacts.addAll(s.members); 
			int cz = s.members.size(); 
			if (cz > max_c) max_c = cz; 
			if (cz < min_c) min_c = cz; 
		} 
		max_thread_duration = max_d;  
		min_thread_duration = min_d; 
		max_thread_size = max_s; 
		min_thread_size = min_s; 
		max_contact_size = max_c; 
		min_contact_size = min_c; 
		return this; 
	}
	
	public static ArrayList<Record> filterRecords(ArrayList<Record> records){
		// the filter rules can be modified 
		ArrayList<Record> list = new ArrayList<Record>(); 
		for (Record r:records){
			if ((r.attach>0) && (r.to.size()>1)) list.add(r);   
		}
		return list; 
	}
	
	private User setThreads(ArrayList<Record> records){
		threads = new ArrayList<Ethread>(); 
		for (Record r:records){
			threads.add(new Ethread(r)); 
		}
		return this; 
	}
	
	public User loadFile(File file){ 
		String s = file.getAbsolutePath();
		if ( !s.substring(s.length()-3, s.length()).equals("csv")){
			System.out.println("not valid file: "+s.substring(s.length()-3, s.length()-1)); 
			return this; 
		}
		BufferedReader br=null; 
		String line = null; 
		num_emails = 0; size = (long) 0; num_attachs=0; 
		String[] parts = file.getName().split("\\."); 
		//for (int i=0; i<parts.length; i++) System.out.println(parts[i]);
		if (parts.length!=2) return this; 
		if (!parts[1].equals("csv")) return this; 
		UserID = parts[0]; 
		try {
			br = new BufferedReader(new FileReader(file));
			this.records = new ArrayList<Record>(); 
			while ((line = br.readLine()) != null) {
				if (Name == null){
					String[] lines = line.split(","); 
					Name = lines[6]; 
				}
				Record r = new Record(line); 
				records.add(r); 
			}
		//	this.records=User.filterRecords(records); 
			this.setThreads(this.records); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace(); 
		} 
		finally{
			if (br!=null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return this; 
	}
	
	public User loadFile(){
		if (UserID ==null) return this; 
		File file = new File(data_path+UserID+suffix); 
		return loadFile(file); 
	}
	
	public static void main(String[]args){
		User yx = (new User("AAC0070")).loadFile().merge().sort().summarize(); 
		yx.print();
		yx.printTime(); 
		DateTime sdt = new DateTime(yx.start);
		DateTime edt = new DateTime(yx.end);
		System.out.println(sdt.getDayOfWeek()+", "+sdt.getDayOfMonth()+", "+sdt.getDayOfYear()); 
		System.out.println(edt.getDayOfWeek()+", "+edt.getDayOfMonth()+", "+edt.getDayOfYear()); 
		Period p = new Period(sdt, edt); 
		System.out.println(YearsMonthsDaysHours.print(p)); 
	}
	
}
