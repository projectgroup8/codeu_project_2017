package codeu.chat.common;

import codeu.chat.util.Uuid;
import codeu.chat.util.Time;

public final class ServerInfo {
  private final static String SERVER_VERSION = "1.0.0";
  public final Time startTime;
  public Uuid version;

  public ServerInfo() {
    this.startTime = Time.now();

    try {
      this.version = Uuid.parse(SERVER_VERSION);
    } catch (Exception e) {
      this.version = null;
      System.out.println("ERROR: Exception registering server version Uuid.");
    }
  }

  public ServerInfo(Uuid version, Time startTime) {
    this.version = version;
    this.startTime = startTime;
  }
}