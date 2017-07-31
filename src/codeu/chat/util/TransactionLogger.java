package codeu.chat.util;

import codeu.chat.server.Controller;
import com.google.gson.Gson;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.lang.System;

import codeu.chat.common.User;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.util.AccessLevel;
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
    ConversationJson conversationJson = new ConversationJson("ADD-CONVERSATION", conversation);
    Gson gson = new Gson();
    log(gson.toJson(conversationJson));
  }

  public void addMessage(Uuid conversation, Message message) {
    MessageJson messageJson = new MessageJson("ADD-MESSAGE", conversation, message);
    Gson gson = new Gson();
    log(gson.toJson(messageJson));  
  }

    // methods to deserialze commands.
    public void retrieveUser(Controller controller, JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            String name = jsonObject.get("name").getAsString();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            controller.newUser(id, name, creation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void retrieveConversation(Controller controller, JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            Uuid owner = Uuid.parse(jsonObject.get("owner").getAsString());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            String title = jsonObject.get("title").getAsString();
            AccessLevel defaultAl = new AccessLevel( jsonObject.get("defaultAccess").getAsByte() );
            controller.newConversation(id, title, owner, creation, defaultAl);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void retrieveMessage(Controller controller, JsonObject jsonObject){
        try {
            Uuid id = Uuid.parse(jsonObject.get("uuid").getAsString());
            Uuid conversation = Uuid.parse(jsonObject.get("conversation").getAsString());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss.SSS");
            Time creation = Time.fromMs(formatter.parse(jsonObject.get("creation").getAsString()).getTime());
            Uuid author = Uuid.parse(jsonObject.get("author").getAsString());
            String content = jsonObject.get("content").getAsString();
            controller.newMessage(id, author, conversation, content, creation);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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
	public void readLog(Controller controller){
        try {
            BufferedReader br = new BufferedReader(new FileReader("transactions.log"));
            try {
                for(String line; (line = br.readLine()) != null;) {
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(line, JsonObject.class);
                    // find what kind of command it is and execute it.
                    String action = jsonObject.get("action").getAsString();
                    switch(action){
                        case "ADD-USER":
                            retrieveUser(controller, jsonObject);
                            break;
                        case "ADD-CONVERSATION":
                            retrieveConversation(controller, jsonObject);
                            break;
                        case "ADD-MESSAGE":
                            retrieveMessage(controller, jsonObject);
                            break;
                        default: break;

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
