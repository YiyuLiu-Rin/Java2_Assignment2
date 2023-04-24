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
        if (this.chatType != chat.chatType || this.participants.size() != chat.participants.size()) return false;
        // note: 这里的排序更改了原对象，但采用此设计
        this.participants.sort(Comparator.comparing(User::getUserName));
        chat.participants.sort(Comparator.comparing(User::getUserName));
        for (int i = 0; i < this.participants.size(); i++) {
            if (!this.participants.get(i).getUserName().equals(chat.participants.get(i).getUserName()))
                return false;
        }
        return true;
    }

    public Chat(ChatType chatType, List<User> participants) {
        this.chatType = chatType;
        this.participants = participants;
        this.lastActiveTime = System.currentTimeMillis();
        this.messages = new ArrayList<>();
    }

    public Chat(Chat chat) {
        this.chatType = chat.chatType;
        this.participants = new ArrayList<>(chat.participants);
        this.lastActiveTime = chat.lastActiveTime;
        this.messages = new ArrayList<>(chat.messages);
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

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public enum ChatType implements Serializable {
        PRIVATE_CHAT, GROUP_CHAT
    }
}

