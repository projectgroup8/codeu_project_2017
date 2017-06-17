package codeu.chat.util;

import codeu.chat.common.Message;

public class MessageJson extends TransactionJson {
  String previous;
  String author;
  String content;
  String next;

  public MessageJson(String action, Message message) {
    super.action = action;
    super.uuid = message.id.toString();
    previous = message.previous.toString();
    author = message.author.toString();
    content = message.content.toString();
    next = message.next.toString();
    super.creation = message.creation.toString();
  }
}
