package com.ibm.us.data.model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Record implements Comparable{
	
	public long time; 
	public List<String> to; 
	public long size; 
	public int attach; 
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public String line; 
	
	private void set(String d, String t, int s, int a){
		try {
			time = sdf.parse(d).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		to = Arrays.asList(t.split(";"));  
		size = s; attach = a; 
	}
	
	public boolean isNormal(){
		Calendar cal = Calendar.getInstance(); 
		cal.setTimeInMillis(time);
		int hour = cal.get(Calendar.HOUR_OF_DAY); 
		if (hour <7 || hour > 20) return false; 
		else return true; 
	}
	
	public Record(String line) {
		String[] fields = line.split(",");
		String ts = fields[1].replace("\"", ""); 
		String t = fields[3].replace("\"", "");
		int s, a; 
		if (fields[7].isEmpty()){
			s = Integer.parseInt(fields[8].replace("\"", "")); 
			a = Integer.parseInt(fields[9].replace("\"", "")); 
		}
		else{
			s = Integer.parseInt(fields[7].replace("\"", "")); 
			a = Integer.parseInt(fields[8].replace("\"", "")); 
		}
		set(ts, t, s, a); 
		this.line = line; 
	}

	@Override
	public int compareTo(Object arg0) {
		Record r = (Record) arg0;
		if (this.time>r.time) return 1; 
		else if (this.time<r.time) return -1; 
		return 0; 
	}

}
