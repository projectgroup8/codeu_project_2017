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
//

package codeu.chat;

import java.io.IOException;
import java.io.File;

import codeu.chat.server.Server;
import codeu.chat.util.Logger;
import codeu.chat.util.RemoteAddress;
import codeu.chat.util.SerializedExecutor;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.ClientConnectionSource;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;
import codeu.chat.util.connections.ServerConnectionSource;

final class ServerMain {

  private static final Logger.Log LOG = Logger.newLog(ServerMain.class);

  public static void main(String[] args) {

    Logger.enableConsoleOutput();

    int port = -1;

    try {
      port = Integer.parseInt(args[0]);
    } catch (Exception ex) {
      LOG.error(ex, "Failed to parse port from %s", args[0]);
      System.exit(1);
    }

    LOG.info("============================= START OF LOG =============================");

    // This is the directory where it is safe to store data accross runs
    // of the server.
    File persistentPath = null;

    try {
      port = Integer.parseInt(args[0]);
      persistentPath = new File(args[1]);
    } catch (Exception ex) {
      LOG.error(ex, "Failed to parse persistent path from %s", args[1]);
      System.exit(1);
    }

    if (!persistentPath.isDirectory()) {
      LOG.error("Persistent path %s is not a directory", args[1]);
      System.exit(1);
    }

    final Server server = new Server();
    LOG.verbose("Successfully create server instance");

    final SerializedExecutor<Connection> executor = new SerializedExecutor<Connection>() {
      @Override
      public void onValue(Connection value) {
        try {
          server.handleConnection(value);
        } catch (Exception ex) {
          LOG.error(ex, "Unhandled exception while handling connection");
        }
      }
    };

    // Start the executor on a new thread. As there is no clean way to end the server and there will
    // be no way shutdown the executor, there is no reason to keep a reference to the thread.
    new Thread(executor).start();

    try (final ConnectionSource serverSource = ServerConnectionSource.forPort(port)) {

      LOG.verbose("Bound to port %d", port);

      while (true) {
        try {
          executor.add(serverSource.connect());
          LOG.verbose("Connection established.");
        } catch (IOException ex) {
          LOG.error(ex, "Failed to connect to incoming connection");
        }
      }
    } catch (IOException ex) {
      LOG.error(ex, "Failed to bind to port %d", port);
    }
  }
}
