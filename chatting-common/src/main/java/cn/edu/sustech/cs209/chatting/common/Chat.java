package cn.edu.sustech.cs209.chatting.common;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Chat implements Serializable, Comparable<Chat> {

    private ChatType chatType;
    private List<User> participants;
    private long lastActiveTime;
    private List<Message> messages;
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

    public Chat(List<User> participants) {
        this.chatType = ChatType.PRIVATE_CHAT;
        this.participants = participants;
        this.lastActiveTime = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public Chat(List<User> participants, String groupChatName) {
        this.chatType = ChatType.GROUP_CHAT;
        this.participants = participants;
        this.lastActiveTime = System.currentTimeMillis();
        this.messages = new ArrayList<>();
        this.groupChatName = groupChatName;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public String getGroupChatName() {
        return groupChatName;
    }

    public enum ChatType implements Serializable {
        PRIVATE_CHAT, GROUP_CHAT
    }
}

