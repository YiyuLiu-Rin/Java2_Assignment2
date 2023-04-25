package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ChatItem implements Serializable, Comparable<ChatItem> {

    private Chat.ChatType chatType;
    private List<User> participants;
    private long lastActiveTime;

    private boolean isNew;

    @Override
    public int compareTo(ChatItem o) {
        Long l = this.lastActiveTime;
        return l.compareTo(o.lastActiveTime);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (this.getClass() != o.getClass()) return false;

        ChatItem chatItem = (ChatItem)o;
        if (this.chatType != chatItem.chatType ||
                this.participants.size() != chatItem.participants.size() ||
                this.isNew != chatItem.isNew)
            return false;
        // note: 这里的排序更改了原对象，但采用此设计
        this.participants.sort(Comparator.comparing(User::getUserName));
        chatItem.participants.sort(Comparator.comparing(User::getUserName));
        for (int i = 0; i < this.participants.size(); i++) {
            if (!this.participants.get(i).getUserName().equals(chatItem.participants.get(i).getUserName()))
                return false;
        }
        return true;
    }


    public ChatItem(Chat chat) {
        this.chatType = chat.getChatType();
        this.participants = new ArrayList<>(chat.getParticipants());
        this.lastActiveTime = chat.getLastActiveTime();
    }

    public Chat.ChatType getChatType() {
        return chatType;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

}
