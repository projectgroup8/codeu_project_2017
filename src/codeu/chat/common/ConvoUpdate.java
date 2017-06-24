package codeu.chat.common;

import java.util.HashSet;

public class ConvoUpdate implements Update{
    private HashSet<ConversationHeader> conversationsCreated;
    private HashSet<ConversationHeader> conversationsModified;


    public ConvoUpdate(){
        conversationsCreated = new HashSet<ConversationHeader>();
        conversationsModified = new HashSet<ConversationHeader>();;
    }

    public void newConversation(ConversationHeader conversation){
        if(!conversationsCreated.contains(conversation)){
            conversationsCreated.add(conversation);
        }

    }

    public void newMessage(ConversationHeader conversation){
        if(!conversationsModified.contains(conversation)){
            conversationsModified.add(conversation);
        }
    }

    public HashSet<ConversationHeader> getConversationsCreated(){
        return conversationsCreated;
    }

    public HashSet<ConversationHeader> getConversationsModified(){
        return conversationsModified;
    }

    @Override
    public void clear() {
        conversationsCreated.clear();
        conversationsModified.clear();
    }
}
