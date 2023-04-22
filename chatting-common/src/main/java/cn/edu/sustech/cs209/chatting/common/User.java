package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;

public class User implements Serializable {

    private String userName;
    private String password;
    private boolean online;

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.online = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this.getClass() != obj.getClass()) return false;
        return this.userName.equals(((User)obj).userName);
    }


    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return online;
    }

    public String getUserName() {
        return userName;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
