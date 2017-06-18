package codeu.chat.util;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.HashMap;
import java.lang.System;
import java.io.FileWriter;
import java.io.IOException;

import codeu.chat.common.User;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;

public class TransactionLogger {

	private LinkedList<String> toLog; //Implements Queue
	private long lastWrite;

	public TransactionLogger() {
		toLog = new LinkedList<String>();
		lastWrite = System.currentTimeMillis();
	}

  public void addUser(User user) {
    UserJson userJson = new UserJson("ADD-USER", user);
    Gson gson = new Gson();
    log(gson.toJson(userJson));
  }

  public void addConversation(ConversationHeader conversation) {
    ConversationJson convJson = new ConversationJson("ADD-CONVERSATION", conversation);
    Gson gson = new Gson();
    log(gson.toJson(convJson));
  }

  public void addMessage(Message message) {
    MessageJson messageJson = new MessageJson("ADD-MESSAGE", message);
    Gson gson = new Gson();
    log(gson.toJson(messageJson));  
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
			String logFile = "transactions.log";
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
