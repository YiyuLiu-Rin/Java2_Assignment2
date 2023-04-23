package cn.edu.sustech.cs209.chatting.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Collections;
import java.util.Comparator;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (this.getClass() != o.getClass()) return false;

        Chat chat = (Chat)o;
        if (this.chatType != chat.chatType) return false;
        if (this.chatType == ChatType.PRIVATE_CHAT) {
            this.participants.sort(Comparator.comparing(User::getUserName));
            chat.participants.sort(Comparator.comparing(User::getUserName));
            for (int i = 0; i < this.participants.size(); i++) {
                if (!this.participants.get(i).getUserName().equals(chat.participants.get(i).getUserName()))
                    return false;
            }
            return true;
        }
        else {
            return this.groupChatName.equals(chat.groupChatName);
        }
    }

    public Chat(ChatType chatType, List<User> participants, String groupChatName) {
        this.chatType = chatType;
        this.participants = participants;
        this.lastActiveTime = System.currentTimeMillis();
        this.messages = FXCollections.observableArrayList();
        if (chatType == ChatType.GROUP_CHAT)
            this.groupChatName = groupChatName;
    }

    public enum ChatType {
        PRIVATE_CHAT, GROUP_CHAT
    }
}

