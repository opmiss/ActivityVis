package com.ibm.us.data.util;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.ibm.us.data.model.Ethread;
import com.ibm.us.data.model.Record;
import com.ibm.us.data.model.User;

public class JsonFileWrite {
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMDDHHmm");
	public static JSONObject toJson(User u){
		JSONObject u_obj = new JSONObject();
		u_obj.put("id", u.UserID);
		u_obj.put("start", sdf.format(u.start));
		u_obj.put("end", sdf.format(u.end)); 
		JSONArray threads = new JSONArray(); 
		for (Ethread t:u.threads){
			JSONObject t_obj = new JSONObject(); 
			t_obj.put("id", t.ThreadID); 
			t_obj.put("start", sdf.format(t.start));
			t_obj.put("end", sdf.format(t.end));
			JSONArray emails = new JSONArray(); 
			for (Record r:t.records){
				JSONObject r_obj = new JSONObject(); 
				r_obj.put("time", sdf.format(r.time));
				r_obj.put("attachments", r.attach);
				r_obj.put("normal", Boolean.toString(r.isNormal())); 
				JSONArray members = new JSONArray(); 
				for (String m:r.to){
					members.add(m); 
				}
				r_obj.put("members", members); 
				emails.add(r_obj); 
			}
			t_obj.put("emails", emails);
			threads.add(t_obj);
		}
		u_obj.put("threads", threads); 
		return u_obj; 
	}
	public static void main(String[] args){
		System.out.println("save to Json Object"); 
		User yx = (new User("AAC0070")).loadFile().merge().sort().summarize(); 
		yx.print();
		JSONObject yxo = JsonFileWrite.toJson(yx); 
		FileWriter file = null;
	    try {
	    	file = new FileWriter("data/json/"+yx.UserID+".json");
	    	file.write(yxo.toJSONString());
	    	System.out.println("Successfully Copied JSON Object to File...");
	    	System.out.println("\nJSON Object: " + yxo);
	    	file.flush(); 
	    	file.close(); 
	    	} catch (IOException e) {
	            e.printStackTrace();
	        }
	} 
}
