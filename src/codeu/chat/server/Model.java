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

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import codeu.chat.common.*;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;
import codeu.chat.util.store.StoreAccessor;

public final class Model {

  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) { return 0; }

      if (a == null && b != null) { return -1; }

      if (a != null && b == null) { return 1; }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };

  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;

  private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  private final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, ConversationHeader> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Time, ConversationHeader> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, ConversationHeader> conversationByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, ConversationPayload> conversationPayloadById = new Store<>(UUID_COMPARE);

  private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);

  private final HashMap<Subscribable, HashSet<User>> userSubscriptions = new HashMap<Subscribable, HashSet<User>>();
  private final HashMap<User, HashSet<Update>> userUpdates = new HashMap<User, HashSet<Update>>();

  public void add(User user) {
    userById.insert(user.id, user);
    userByTime.insert(user.creation, user);
    userByText.insert(user.name, user);
  }

  public StoreAccessor<Uuid, User> userById() {
    return userById;
  }

  public StoreAccessor<Time, User> userByTime() {
    return userByTime;
  }

  public StoreAccessor<String, User> userByText() {
    return userByText;
  }

  public void add(ConversationHeader conversation) {
    conversationById.insert(conversation.id, conversation);
    conversationByTime.insert(conversation.creation, conversation);
    conversationByText.insert(conversation.title, conversation);
    conversationPayloadById.insert(conversation.id, new ConversationPayload(conversation.id));

  }

  public StoreAccessor<Uuid, ConversationHeader> conversationById() {
    return conversationById;
  }

  public StoreAccessor<Time, ConversationHeader> conversationByTime() {
    return conversationByTime;
  }

  public StoreAccessor<String, ConversationHeader> conversationByText() {
    return conversationByText;
  }

  public StoreAccessor<Uuid, ConversationPayload> conversationPayloadById() {
    return conversationPayloadById;
  }

  public void add(Message message) {
    messageById.insert(message.id, message);
    messageByTime.insert(message.creation, message);
    messageByText.insert(message.content, message);
  }

  public StoreAccessor<Uuid, Message> messageById() {
    return messageById;
  }

  public StoreAccessor<Time, Message> messageByTime() {
    return messageByTime;
  }

  public StoreAccessor<String, Message> messageByText() {
    return messageByText;
  }

  // methods to handle subscriptions.
  public void addUserSubscription(User user, UserSub sub){
    // this method adds a new User Subscription to the subscriptions map.
    if(!userSubscriptions.containsKey(sub)) {
        userSubscriptions.put(sub, new HashSet<>());
    }
    userSubscriptions.get(sub).add(user);
  }

  public void addConversationSub(User user, ConvoSub sub){
    // this method adds a new Conversation Subscription to the subscriptions map.
    if(!userSubscriptions.containsKey(sub)) {
        userSubscriptions.put(sub, new HashSet<>());
    }
    userSubscriptions.get(sub).add(user);
  }

  public void removeUserSub(User user, UserSub sub){
      if(userSubscriptions.get(sub).contains(user)){
          userSubscriptions.get(sub).remove(user);

      }
  }
  public void removeConvoSub(User user, ConvoSub sub){
      if(userSubscriptions.get(sub).contains(user)){
          userSubscriptions.get(sub).remove(user);
      }
  }

  public HashSet<User> getUsersOfSub(Subscribable sub){
    if(sub == null){
      return new HashSet<User>();
    }
    else{
      return userSubscriptions.get(sub);
    }
  }


  public Subscribable getSubcriptionKey(Subscribable sub){
    for(Subscribable s: userSubscriptions.keySet()){
      if (s.getId() == sub.getId()){
        return s;
      }
    }
    return null;
  }

  public Subscribable getSubscriptionKeyFromId(Uuid id){
    for(Subscribable s: userSubscriptions.keySet()){
      if(s.getId() == id){
        return s;
      }
    }
    return null;
  }

  public void update(User user, ConversationHeader conversation){
    // This updates the subscribers of the user when a new message
    // has been created from the user.

    // first update the people that are following the user.
    Subscribable subscription = getSubscriptionKeyFromId(user.id);
    HashSet<User> users = new HashSet<User>();
    if(subscription != null){
      users = getUsersOfSub(subscription);
      if(users.size() != 0){
        for(User u: users){
          // update the subscribers that the user has added a new message to the conversation.
          String update = ((UserSub) subscription).getUser().name + " has added a new message to conversation " + conversation.title;
          if(!userUpdates.containsKey(u)){
            userUpdates.put(u, new HashSet<Update>());
          }
          userUpdates.get(u).add(new Update(update));
          System.out.println("new message added to updates.");
        }
      }
    }

    // now update the people that are following this conversation.
    subscription = getSubscriptionKeyFromId(conversation.id);
    if(subscription != null){
      users = getUsersOfSub(subscription);
      if(users.size() != 0){
        for(User u: users){
          // update the subscribers that this conversation has a new unread message.
          String update = "Conversation " + ((ConvoSub) subscription).getConversation().title + " has a new unread message";
          if(!userUpdates.containsKey(u)){
            userUpdates.put(u, new HashSet<Update>());
          }
          userUpdates.get(u).add(new Update(update));
        }
      }
    }

  }

  public void updateNewConversation(User user, ConversationHeader conversation){
    // this updates the subscribers of the user when a new conversation has been
    // created by that user.
    Subscribable subscription = getSubscriptionKeyFromId(user.id);
    if(subscription != null){
      HashSet<User> users = getUsersOfSub(subscription);
      if(users.size() != 0){
        for(User u: users){
          String update = "User " + user.name + " has created a new conversation " + conversation.title;
          if(!userUpdates.containsKey(u)){
            userUpdates.put(u, new HashSet<Update>());
          }
          userUpdates.get(u).add(new Update(update));
        }
      }
    }
  }

  public HashSet<Update> getUpdates(User u){
    // retrieves the updates for the user.
    // we still have to iteratively match the uuids of the user with user u.

    for(User user: userUpdates.keySet()){
      if(u.id == user.id){
        return userUpdates.get(u);
      }
    }
    return new HashSet<Update>();
  }

  public void clearUpdates(User user){
    userUpdates.get(user).clear();
  }
}
