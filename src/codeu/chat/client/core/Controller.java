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

package codeu.chat.client.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;

import codeu.chat.common.BasicController;
import codeu.chat.common.ConversationHeader;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.AccessLevel;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

final class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.STRING.write(connection.out(), body);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User newUser(String name) {

    User response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.nullable(User.SERIALIZER).read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public ConversationHeader newConversation(String title, Uuid owner, AccessLevel defaultAl)  {

    ConversationHeader response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), owner);
      AccessLevel.SERIALIZER.write(connection.out(), defaultAl);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(ConversationHeader.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public void newUserSubscription(String name, Uuid user) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_SUBSCRIPTION_REQUEST);
      Serializers.STRING.write(connection.out(), name);
      Uuid.SERIALIZER.write(connection.out(), user);
      LOG.info("newUserSubscription: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_SUBSCRIPTION_RESPONSE) {
        LOG.info("newUserSubscription: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    } 
  }

  @Override
  public void newConversationSubscription(String title, Uuid user) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_SUBSCRIPTION_REQUEST);
      Serializers.STRING.write(connection.out(), title);
      Uuid.SERIALIZER.write(connection.out(), user);
      LOG.info("newConversationSubscription: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_SUBSCRIPTION_RESPONSE) {
        LOG.info("newConversationSubscription: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    } 
  }

  @Override
  public void addMember(String user, Uuid conversation) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.ADD_MEMBER_REQUEST);
      Serializers.STRING.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      LOG.info("addMember: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.ADD_MEMBER_RESPONSE) {
        LOG.info("addMember: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
  }

  @Override
  public void addOwner(String user, Uuid conversation) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.ADD_OWNER_REQUEST);
      Serializers.STRING.write(connection.out(), user);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      LOG.info("addOwner: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.ADD_OWNER_RESPOND) {
        LOG.info("addOwner: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
  }

  @Override
  public void defaultAccess(Uuid conversation, AccessLevel defaultAl) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.DEFAULT_ACCESS_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      AccessLevel.SERIALIZER.write(connection.out(), defaultAl);
      LOG.info("defaultStatus: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.DEFAULT_ACCESS_RESPOND) {
        LOG.info("defaultStatus: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
  }

  @Override
  public void clearUpdates(Uuid user) {
    try (final Connection connection = source.connect()) {
      Serializers.INTEGER.write(connection.out(), NetworkCode.CLEAR_UPDATES_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), user);
      LOG.info("clearUpdates: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.CLEAR_UPDATES_RESPOND) {
        LOG.info("clearUpdates: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }
  }
}
