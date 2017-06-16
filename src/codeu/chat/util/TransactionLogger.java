package codeu.chat.util;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.HashMap;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;

public class TransactionLogger {

	private LinkedList<String> toLog; //Implements Queue
	private long lastWrite;

	public TransactionLogger() {
		toLog = new LinkedList<String>();
		lastWrite = System.currentTimeMillis();
	}

	// All log writes pass through this function
	// Checks whether queue to file is needed
	// transaction: String already made by Gson
	public void log(String transaction) {
		toLog.add(transaction); //add to queue
		long now = System.currentTimeMillis();
		if (now-lastWrite > 120000) { //don't write within 2 minutes
			System.out.println(now-lastWrite);
			appendToLog();
			lastWrite = now;
		}
	}

	// Writes all transactions in toLog to file
	public void appendToLog() {
		try {
			String logFile = "transactionLog.txt";
			FileWriter fw = new FileWriter(logFile,true /*append*/);
			for (int i=0; i<toLog.size(); i++) {
				fw.write(toLog.pop());
				fw.write("\n");
			}
			fw.close();
		} catch (IOException ioe) {
			System.out.println("IO Exception writing transaction log: " + ioe.getMessage());
		}
	}
}