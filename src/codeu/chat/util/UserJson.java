package codeu.chat.util;

import codeu.chat.common.User;

public class UserJson extends TransactionJson {
  public final String name;

  public UserJson(String action, User user) {
    super.action = action;
    super.uuid = user.id.toString(); // String representation of uuid
    name = user.name;
    super.creation = user.creation.toString();
  }
}
