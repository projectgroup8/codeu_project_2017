package codeu.chat.common;

import codeu.chat.util.Uuid;

public class UserSub implements Subscribable{
    private final User user;

    public UserSub(User user){
        this.user = user;
    }

    public User getUser() {
        return user;
    }


    @Override
    public Uuid getId() {
        return user.id;
    }
}
