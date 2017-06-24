package codeu.chat.common;

public class ConvoSub implements Subscribable{
    private final ConversationHeader conversation;

    public ConvoSub(ConversationHeader conversation){
        this.conversation = conversation;
    }

    public ConversationHeader getConversation(){
        return conversation;
    }
}
