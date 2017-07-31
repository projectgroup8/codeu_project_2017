package codeu.chat.util;

import codeu.chat.common.ConversationHeader;

public class ConversationJson extends TransactionJson {
  public String owner;
  public String title;
  public byte defaultAccess;

  public ConversationJson(String action, ConversationHeader conversation) {
    super.action = action;
    super.uuid = conversation.id.toString();
    owner = conversation.owner.toString();
    super.creation = conversation.creation.toString();
    title = conversation.title;
    defaultAccess = conversation.defaultAccess.getStatus();
  }
}
