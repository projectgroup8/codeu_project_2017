package codeu.chat.util;

import codeu.chat.util.Uuid;
import codeu.chat.common.Message;

public class MessageJson extends TransactionJson {
  public String author;
  public String content;
  public String conversation;

  public MessageJson(String action, Uuid conversation, Message message) {
    super.action = action;
    super.uuid = message.id.toString();
    this.author = message.author.toString();
    this.content = message.content.toString();
    super.creation = message.creation.toString();
    this.conversation = conversation.toString();
  }
}
