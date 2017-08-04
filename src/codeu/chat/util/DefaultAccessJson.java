package codeu.chat.util;

import codeu.chat.common.ConversationHeader;

public class DefaultAccessJson {
  String action;
  String conversation;
  byte defaultAccess;

  public DefaultAccessJson(String action, ConversationHeader conversation, AccessLevel defaultAl) {
    this.action = action;
    this.conversation = conversation.id.toString();
    this.defaultAccess = defaultAl.getStatus();
  }
}
