package com.ibm.us.data.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Search {
	private Connection conn = null;
	private PreparedStatement ps = null;
	public Search() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(
					"jdbc:mysql://L3-6.pok.ibm.com/SYNTHETIC", "adams",
					"passw0rd");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public ResultSet getRecords(String userId){
		ResultSet rs = null; 
		try {
			Statement s = conn.createStatement();
			rs = s.executeQuery("select * from CSV_EMAIL where USERID='"+userId+"'");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rs; 
	}
	public void getRecordsByUserId(String userId) {
		try {
			Statement s = conn.createStatement();
			int k=0; 
			ResultSet rs = s.executeQuery("select * from CSV_EMAIL where USERID='"+userId+"'");
			while (rs.next()) {
				//String f1 = rs.getString(0); 
				String f2 = rs.getString(1); 
				String f3 = rs.getString(2);
				String f4 = rs.getString(3);
				String f5 = rs.getString(5);
				String f6 = rs.getString(6); 
				String f7 = rs.getString(7); 
				String f8 = rs.getString(8);
				String f9 = rs.getString(9); 
				String f10 = rs.getString(10); 
				String f11 = rs.getString(11); 
				k++; 
				System.out.println(f2+","+f3+","+f4+","+f5+","+f6+","+f7+","+f8+","+f9+","+f10+","+f11);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveRecordsByUserId(String userId){
		FileWrite fr = new FileWrite("data/records/"+userId+".csv"); 
		ResultSet rs = getRecords(userId); 
		int k=0; 
		try {
		
			while(rs.next()){
				String f2 = rs.getString(1);
				String f3 = rs.getString(2);
				String f4 = rs.getString(3);
				String f5 = rs.getString(5);
				String f6 = rs.getString(6); 
				String f7 = rs.getString(7); 
				String f8 = rs.getString(8);
				String f9 = rs.getString(9); 
				String f10 = rs.getString(10); 
				String f11 = rs.getString(11); 
				String line = f2+","+f3+","+f4+","+f5+","+f6+","+f7+","+f8+","+f9+","+f10+","+f11;
				k++; 
				fr.writeLine(line); 		
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			fr.close(); 
			System.out.println("number of records: "+k); 
		}
	}
	
	public void saveRecords(){
		BufferedReader br=null; 
		String line = null; 
		try {
			br = new BufferedReader(new FileReader(new File("data/USERID.csv")));
			br.readLine(); 
			long start = System.currentTimeMillis(); 
			int k=0; 
			while ((line = br.readLine()) != null) {
				String[] lines = line.split(","); 
				if (lines[0].charAt(0)=='Z'){
					System.out.println("save: "+lines[0]); 
					saveRecordsByUserId(lines[0]); 
					k++; 
				}
			}
			long stop = System.currentTimeMillis(); 
			System.out.println("number of records: "+k+ ", time: "+(stop-start));
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
	}
	
	public static void main(String[] args) {
		Search s = new Search();
		s.saveRecords(); 
		//s.getRecordsByUserId("AAC0070");
		//s.saveRecordsByUserId("AAC0070"); 
	}
}