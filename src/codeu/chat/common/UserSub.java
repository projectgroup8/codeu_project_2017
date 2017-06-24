package codeu.chat.common;

public class UserSub implements Subscribable{
    private final User user;

    public UserSub(User user){
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
