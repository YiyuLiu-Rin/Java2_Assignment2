package cn.edu.sustech.cs209.chatting.common;

import javafx.collections.ObservableList;

import java.util.List;

public class Chat implements Comparable<Chat> {

    private ChatType chatType;
    private List<User> participants;
    private long lastActiveTime;
    private ObservableList<Message> messages;
    private String groupChatName;

    @Override
    public int compareTo(Chat o) {
        Long l = this.lastActiveTime;
        return l.compareTo(o.lastActiveTime);
    }

/*    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (this.getClass() != o.getClass()) return false;
        Chat ch = (Chat)o;
        if (this.chatType != ch.chatType) return false;
        if (this.chatType == ChatType.PRIVATE_CHAT) {

        }
    }*/

    public Chat() {}


    public enum ChatType {
        PRIVATE_CHAT, GROUP_CHAT
    }
}

