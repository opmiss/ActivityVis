package com.ibm.us.data.model;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.*; 

import com.ibm.us.vis.geo.vec;

public class Ethread implements Comparable{
	
	public long start; 
	public long end; 
	public Long size;
	public Integer num_emails, num_attachs; 
	public ArrayList<Record> records = null; 
	public Set<String> members=null;  
	public String ThreadID = null; 
	public boolean mask = true; //use for merge sessions
	
	public Ethread(Record r){
		start = r.time; 
		end = r.time; 
		records = new ArrayList<Record>(); 
		records.add(r); 
		members = new HashSet<String>();
		members.addAll(r.to);
		size = r.size; 
		num_emails = 1; 
		num_attachs = r.attach; 
	}
	
	public Ethread setID(String id){
		ThreadID = id;
		return this; 
	}
	
	public void print(){
		System.out.println("----------"); 
		System.out.println("number of records: "+records.size()); 
		System.out.println("start: "+start); 
		System.out.println("end: "+end); 
		for (String s:members) System.out.print(s+" ");
		System.out.println(); 
	}
	
	public static void main(String[]args){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
		Date d1 = new Date();  Date d2 = new Date(); 
		try {
			d1 = sdf.parse("2010-01-02 08:10:03");
			d2 = sdf.parse("2010-01-10 09:16:03"); 
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		Calendar c1 = new GregorianCalendar(); 
		Calendar c2 = new GregorianCalendar(); 
		c1.setTime(d1); c2.setTime(d2); 
		System.out.println(c1.getTimeInMillis()); 
	}
	
	public long getDuration(){
		return end-start; 
	}
	
	public Object[] digest(){
		DateTime s = new DateTime(start); 
		DateTime e = new DateTime(end); 
		Period p = new Period(s, e); 
		Object[] data = new Object[5];
		data[0] = start; 
		data[1] = User.YearsMonthsDaysHours.print(p);  
		data[2] = members.size(); 
		data[3] = num_emails; 
		data[4] = num_attachs; 
		/*Object[] data = new Object[8];
		data[0] = this.ThreadID; 
		data[1] = User.YearsMonthsDaysHours.print(p);  
		data[2] = start.getTime().toString(); 
		data[3] = end.getTime().toString(); 
		data[4] = num_emails; 
		data[5] = members.size();
		data[6] = size;
		data[7] = num_attachs; */
		return data; 
	}		
	public Ethread sort(){
		Collections.sort(records); 
		return this; 
	}
	public int compareTo(Object o) {
		Ethread s = (Ethread)o; 
		if (this.start>s.start) return 1; 
		else if (this.start<s.start) return -1; 
		return 0; 
	}
	public static boolean similar(Ethread s1, Ethread s2){
		int num =0; 
		for (String s:s1.members){
			if (s2.members.contains(s)) num++; 
		}
		int tnum = s1.members.size()+s2.members.size(); 
		if (num > 0.1*tnum) return true;
		return false; 
	}
	public static vec a_similarity(Ethread s1, Ethread s2){
		int num =0; 
		for (String s:s1.members){
			if (s2.members.contains(s)) num++; 
		}
		return new vec((float)num/(float)s1.members.size(), (float)num/(float)s2.members.size() ); 
	}
	public void merge(Ethread s){
		start = (start<s.start)?start:s.start; 
		end = (end>s.end)?end:s.end; 
		this.members.addAll(s.members); 
		this.records.addAll(s.records); 
		this.num_attachs += s.num_attachs; 
		this.num_emails += s.num_emails; 
		this.size += s.size; 
		s.mask = false; 
	}
}
