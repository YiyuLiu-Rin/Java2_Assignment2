package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {

    public RequestType requestType;
    private User user;  // 发出请求的客户端对应的用户
    private List<User> participants;  // 创建聊天时的用户列表

    public Request(RequestType requestType, User user) {
        this.requestType = requestType;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

}