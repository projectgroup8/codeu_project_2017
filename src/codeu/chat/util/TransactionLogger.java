//package codeu.chat.util;

import java.util.LinkedList;
import java.util.HashMap;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;

public class TransactionLogger {

	private LinkedList<String> toLog;
	private long lastWrite;

	public TransactionLogger() {
		toLog = new LinkedList<String>();
		lastWrite = System.currentTimeMillis();
	}

	// All log writes pass through this function
	// Checks whether queue to file is needed
	public void log(HashMap<String,Object> transaction) {
		JSONObject jsonTransaction = new JSONObject();
		jsonTransaction.putAll(transaction);
		String strTransaction = transaction.toString();
		System.out.printf("JSON %s", transaction.toString() );

		toLog.add(strTransaction);
		long now = System.currentTimeMillis();
		if (now-lastWrite > 0) { //two minutes
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
		HashMap<String, Object> addUser = new HashMap<String, Object>();
		addUser.put("action", "ADD-USER");
		addUser.put("focus-uuid", 100);
		e.log(addUser);
	}
}