package codeu.chat.util;

import codeu.chat.server.Model;
import com.google.gson.Gson;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.lang.System;

import codeu.chat.common.User;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import com.google.gson.JsonObject;


public class TransactionLogger {

	private LinkedList<String> toLog; //Implements Queue
	private long lastWrite;

	public TransactionLogger() {
		toLog = new LinkedList<String>();
		lastWrite = System.currentTimeMillis();
	}

  // methods to serialize commands.
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

    // methods to deserialze commands.
    public User getUser(JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            String name = jsonObject.get("name").getAsString();

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            return new User(id, name, creation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ConversationHeader getConversation(JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            Uuid owner = Uuid.parse(jsonObject.get("owner").getAsString());

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            String title = jsonObject.get("title").getAsString();
            return new ConversationHeader(id, owner, creation, title);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Message getMessage(JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            Uuid next = Uuid.parse(jsonObject.get("next").getAsString());
            Uuid previous = Uuid.parse(jsonObject.get("previous").getAsString());

            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            Uuid author = Uuid.parse(jsonObject.get("author").getAsString());
            String content = jsonObject.get("content").getAsString();
            return new Message(id, next, previous, creation, author, content);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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

	// Reads the transactions from the log and executes them.
	public void readLog(Model model){
        try {
            BufferedReader br = new BufferedReader(new FileReader("transactions.log"));
            try {
                for(String line; (line = br.readLine()) != null;) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(line, JsonObject.class);
                    // find what kind of command it is and execute it.
                    String action = jsonObject.get("action").getAsString();
                    if ("ADD-USER".equals(action)) {
                        model.add(getUser(jsonObject));
                    }
                    else if ("ADD-CONVERSATION".equals(action)) {
                        model.add(getConversation(jsonObject));
                    }
                    else if ("ADD-MESSAGE".equals(action)) {
                        model.add(getMessage(jsonObject));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
