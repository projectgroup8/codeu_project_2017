// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;


import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.ConversationPayload;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.common.UserSub;
import codeu.chat.common.ConvoSub;
import codeu.chat.util.*;
import com.google.gson.Gson;

import java.util.LinkedList;

    public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);
  private final static TransactionLogger transactionLogger = new TransactionLogger();
  private final LinkedList<TransactionJson> temporaryLog = new LinkedList<TransactionJson>();
  private boolean retrieveOn = true;

  private final Model model;
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name) {
    return newUser(createId(), name, Time.now());
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner, AccessLevel defaultAl) {
    return newConversation(createId(), title, owner, Time.now(), defaultAl);
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    final User foundUser = model.userById().first(author);
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);

    Message message = null;

    if (foundUser != null && foundConversation != null && isIdFree(id)) {

      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message);
      LOG.info("Message added: %s", message.id);

      if(!retrieveOn){
        transactionLogger.addMessage(conversation, message);
        transactionLogger.appendToLog();
        model.update(model.userById().first(author), model.conversationById().first(conversation));
      }

      // Find and update the previous "last" message so that it's "next" value
      // will point to the new message.

      if (Uuid.equals(foundConversation.lastMessage, Uuid.NULL)) {

        // The conversation has no messages in it, that's why the last message is NULL (the first
        // message should be NULL too. Since there is no last message, then it is not possible
        // to update the last message's "next" value.

      } else {
        final Message lastMessage = model.messageById().first(foundConversation.lastMessage);
        lastMessage.next = message.id;
      }

      // If the first message points to NULL it means that the conversation was empty and that
      // the first message should be set to the new message. Otherwise the message should
      // not change.

      foundConversation.firstMessage =
          Uuid.equals(foundConversation.firstMessage, Uuid.NULL) ?
          message.id :
          foundConversation.firstMessage;

      // Update the conversation to point to the new last message as it has changed.

      foundConversation.lastMessage = message.id;
    }

    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime);
      model.add(user);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

      if(!retrieveOn){
        transactionLogger.addUser(user);
        transactionLogger.appendToLog();
      }
    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public ConversationHeader newConversation(Uuid id, String title, Uuid owner, Time creationTime, AccessLevel defaultAl) {

    final User foundOwner = model.userById().first(owner);

    ConversationHeader conversation = null;

    if (foundOwner != null && isIdFree(id)) {
      conversation = new ConversationHeader(id, owner, creationTime, title, defaultAl);
      model.add(conversation);
      LOG.info("Conversation added: " + id);

      if(!retrieveOn){
          transactionLogger.addConversation(conversation);
          transactionLogger.appendToLog();
          model.updateNewConversation(model.userById().first(owner),conversation);
      }
    }

    return conversation;
  }

  @Override
  public void newUserSubscription(String name, Uuid user) {
    final User subscribingUser = model.userById().first(user); 
    final UserSub userSub = new UserSub(model.userByText().first(name)); 
    model.addUserSubscription(subscribingUser, userSub);
  }

  @Override
  public void newConversationSubscription(String title, Uuid user) {
    final User subscribingUser = model.userById().first(user); 
    final ConvoSub convoSub = new ConvoSub(model.conversationByText().first(title)); 
    model.addConversationSub(subscribingUser, convoSub);
  }

  @Override
  public void addMember(String user, Uuid conversation) {
    final User u = model.userByText().first(user);
    final ConversationHeader conversationHeader = model.conversationById().first(conversation);

    AccessLevel level = conversationHeader.getAccessLevel(u);

    if(level.hasMemberAccess()){
      System.out.println("The user is already a member.");
    }
    else if(level.hasOwnerAccess()){
      System.out.println("You cannot lower an owner's or creator's status.");
    }
    else{
      level.setMemberStatus();
    }
  }

  @Override
  public void addOwner(String user, Uuid conversation) {
    final User u = model.userByText().first(user);
    final ConversationHeader conversationHeader = model.conversationById().first(conversation);

    AccessLevel level = conversationHeader.getAccessLevel(u);

    if(level.hasOwnerAccess()){
      System.out.println("The user already has owner status.");
    }
    else if(level.hasCreatorAccess()){
      System.out.println("You cannot lower a creator's status.");
    }
    else{
      level.setOwnerStatus();
    }
  }

  @Override
  public void defaultAccess(Uuid conversation, AccessLevel defaultAl) {
    final ConversationHeader conversationHeader = model.conversationById().first(conversation);
    conversationHeader.defaultAccess = defaultAl;

    if(!retrieveOn){
      transactionLogger.addDefaultAccess(conversationHeader, defaultAl);
      transactionLogger.appendToLog();
    }
  }

  @Override
  public void clearUpdates(Uuid user) {
    model.clearUpdates(model.userById().first(user));
  }

  private Uuid createId() {

    Uuid candidate;

    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

     // Assuming that "randomUuid" is actually well implemented, this
     // loop should never be needed, but just incase make sure that the
     // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    return model.messageById().first(id) != null ||
           model.conversationById().first(id) != null ||
           model.userById().first(id) != null;
  }

  private boolean isIdFree(Uuid id) { return !isIdInUse(id); }

  public void saveUser(User user){
    temporaryLog.add(new UserJson("ADD-USER", user));
  }

  public void saveConversation(ConversationHeader conversation){
    temporaryLog.add(new ConversationJson("ADD-CONVERSATION", conversation));

  }

  public void saveMessage(Message message, Uuid conversation){
    final ConversationPayload foundConversation = model.conversationPayloadById().first(conversation);
    temporaryLog.add(new MessageJson("ADD-MESSAGE", foundConversation.id, message));
  }

  public void appendTransactions(){
    for(TransactionJson json: temporaryLog){
      Gson gson = new Gson();
      if (json instanceof UserJson){
        transactionLogger.log(gson.toJson(((UserJson)json)));
        transactionLogger.appendToLog();
      }
      else if (json instanceof MessageJson){
        transactionLogger.log(gson.toJson(((MessageJson)json)));
        transactionLogger.appendToLog();
      }
      else if (json instanceof ConversationJson){
        transactionLogger.log(gson.toJson(((ConversationJson)json)));
        transactionLogger.appendToLog();
      }
    }
  }

  public boolean isRetrieving(){
    return retrieveOn;
  }

  public void deserializeCommands(){
    retrieveOn = true; // tell the model we're retrieving stuff right now.
    transactionLogger.readLog(this); // retrieve the data.
    retrieveOn = false; // tell the model we're done retrieving data.
    LOG.info("Retrieving data has been completed.");
    // now append stuff that might've not been appended because the log was being read.
    appendTransactions();
  }

}
