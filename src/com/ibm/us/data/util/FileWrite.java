package com.ibm.us.data.util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileWrite {
	BufferedWriter bw; 
	
	public FileWrite(String filename){
        try {
			bw = new BufferedWriter(new FileWriter(new File(filename)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void writeLine(String line) {
    	try {
    		bw.write(line);
    		bw.newLine();
    	} catch (IOException e) {
    		e.printStackTrace(); 
    	}
    }
    public void close() {
    	try {
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }
}