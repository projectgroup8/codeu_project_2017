package codeu.chat.common;

public class UserUpdate implements Update{
    private int messagesCreated;

    public UserUpdate(){
        messagesCreated = 0;
    }

    public void addMessage(){
        ++messagesCreated;
    }

    public int getUpdate(){
        return messagesCreated;
    }

    @Override
    public void clear() {
        messagesCreated = 0;
    }
}
