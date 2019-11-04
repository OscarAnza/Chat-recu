package model;

import java.io.Serializable;
import java.util.Arrays;

public class User implements Serializable {
    private String user;
    private boolean isOut;

    public User(String user) {
        this.user = user;
        this.isOut = false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public boolean isOut() {
        return isOut;
    }

    public void setOut(boolean out) {
        isOut = out;
    }

    @Override
    public String toString() {
        return user;
    }
}
