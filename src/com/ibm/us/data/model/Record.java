package com.ibm.us.data.model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Record implements Comparable{
	
	public Calendar time; 
	public List<String> to; 
	public long size; 
	public int attach; 
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public String line; 
	
	private void set(Calendar d, String t, int s, int a){
		time = (Calendar)d.clone(); 
		to = Arrays.asList(t.split(";"));  
		size = s; attach = a; 
	}
	
	public boolean isNormal(){
		int hour = time.get(Calendar.HOUR_OF_DAY); 
		if (hour <7 || hour > 20) return false; 
		else return true; 
	}
	
	public Record(String line) throws ParseException{
		String[] fields = line.split(",");
		String ts = fields[1]; 
		Calendar cal = Calendar.getInstance();
	    cal.setTime(sdf.parse(ts));
		String t = fields[3];
		int s = Integer.parseInt(fields[8]); 
		int a = Integer.parseInt(fields[9]); 
		set(cal, t, s, a); 
		this.line = line; 
	}

	@Override
	public int compareTo(Object arg0) {
		Record r = (Record) arg0;
		return time.compareTo(r.time); 
	}
}
