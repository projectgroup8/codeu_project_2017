//package codeu.chat.util;

import java.util.LinkedList;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;

public class TransactionLogger {

	private LinkedList<String> toLog;
	private long lastWrite;

	public TransactionLogger() {
		toLog = new LinkedList<String>();
		lastWrite = System.currentTimeMillis();
	}

	public void logAddUser() {
		log("Add user");
	}

	// All log writes pass through this function
	// Checks whether queue to file is needed
	public void log(String transaction) {
		toLog.add(transaction);
		long now = System.currentTimeMillis();
		if (now-lastWrite > 120000) { //two minutes
			System.out.println(now-lastWrite);
			appendToLog();
			lastWrite = now;
		}
	}

	// Writes all transactions in toLog to file
	public void appendToLog() {
		try {
			String logFile = "transactionLog.txt";
			FileWriter fw = new FileWriter(logFile,true);
			for (int i=0; i<toLog.size(); i++) {
				fw.write(toLog.pop());
				fw.write("\n");
			}
			fw.close();
		} catch (IOException ioe) {
			System.out.println("IO Exception writing transaction log: " + ioe.getMessage());
		}
	}

	public static void main(String[] args) {
		TransactionLogger e = new TransactionLogger();
		e.logAddUser();
	}
}