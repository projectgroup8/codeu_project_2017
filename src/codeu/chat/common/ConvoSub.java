package codeu.chat.common;

import codeu.chat.util.Uuid;

public class ConvoSub implements Subscribable{
    private final ConversationHeader conversation;

    public ConvoSub(ConversationHeader conversation){
        this.conversation = conversation;
    }

    public ConversationHeader getConversation(){
        return conversation;
    }

    @Override
    public Uuid getId() {
        return conversation.id;
    }
}
