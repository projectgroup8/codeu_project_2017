package codeu.chat.common;

import codeu.chat.util.Uuid;

public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";

  public Uuid version;

  public ServerInfo() {
    try {
      this.version = Uuid.parse(SERVER_VERSION);
    } catch (Exception e) {
      this.version = null;
      System.out.println("ERROR: Exception registering server version Uuid.");
    }
  }

  public ServerInfo(Uuid version) {
    this.version = version;
  }
}
