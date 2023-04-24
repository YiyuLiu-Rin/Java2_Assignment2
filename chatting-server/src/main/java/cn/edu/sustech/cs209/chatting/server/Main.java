package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        // 维护的服务器端数据
        List<User> userList = new ArrayList<>();
        List<Chat> chatList = new ArrayList<>();

        // 启动服务器并等待连接
        final int PORT = 8888;
        ServerSocket server = new ServerSocket(PORT);
        System.out.println("Server is started, waiting for clients to connect...");
        while (true) {
            Socket socket = server.accept();
            System.out.println("A client is connected.");

            Thread t = new Thread(new Service(socket, userList, chatList));
            t.start();
        }

    }

}

class Service implements Runnable {

    private final List<User> userList;
    private final List<Chat> chatList;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private User user;

    private Chat currentChat;

    public Service(Socket socket, List<User> userList, List<Chat> chatList) {
        this.socket = socket;
        this.userList = userList;
        this.chatList = chatList;

        this.currentChat = null;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            boolean flag = true;
            while (flag) {
                Object obj = in.readObject();
                if (obj.getClass() == Request.class) {
                    Request request = (Request)obj;
                    if (request.requestType == RequestType.LOG_IN || request.requestType == RequestType.SIGN_UP ||
                            request.requestType == RequestType.DISCONNECT) {
                        System.out.println("Server received a request: " + request.requestType +
                                " @" + request.getUser().getUserName());
                    }
                    switch (request.requestType) {
                        case LOG_IN: {
                            if (!userList.contains(request.getUser())) {
                                // 无此用户
                                out.writeObject(1);
                                break;
                            }
                            user = userList.get(userList.indexOf(request.getUser()));
                            if (!request.getUser().getPassword().equals(user.getPassword())) {
                                // 密码错误
                                out.writeObject(2);
                                break;
                            }
                            if (user.isOnline()) {
                                // 已登录
                                out.writeObject(3);
                                break;
                            }
                            // 成功登录
                            System.out.println("Log in succeeded. @" + user.getUserName());
                            user.setOnline(true);
                            out.writeObject(user);
                            break;
                        }
                        case SIGN_UP: {
                            if (userList.contains(request.getUser())) {
                                // 已有用户
                                out.writeObject(1);
                                break;
                            }
                            // 成功注册
                            user = request.getUser();
                            System.out.println("Sign up succeeded. @" + user.getUserName());
                            user.setOnline(true);
                            userList.add(user);
                            out.writeObject(user);
                            break;
                        }
                        case GET_ONLINE_USER_LIST: {
//                            List<User> onlineUserList = userList.stream()
//                                    .filter(User::isOnline).toList();
                            List<User> onlineUserList = new ArrayList<>();
                            for (User user : userList) {
                                if (user.isOnline())
                                    onlineUserList.add(user);
                            }
                            out.writeObject(new Response(RequestType.GET_ONLINE_USER_LIST, onlineUserList));
                            break;
                        }
                        case GET_CHAT_LIST: {
                            List<Chat> hisChatList = new ArrayList<>();
                            for (Chat chat : chatList) {
                                for (User usr : chat.getParticipants()) {
                                    if (usr.getUserName().equals(request.getUser().getUserName())) {
                                        hisChatList.add(chat);
                                        break;
                                    }
                                }
                            }
                            Collections.sort(hisChatList, Comparator.reverseOrder());
                            out.writeObject(new Response(RequestType.GET_CHAT_LIST, hisChatList));
                            break;
                        }
                        case GET_CURRENT_CHAT: {
                            out.writeObject(new Response(RequestType.GET_CURRENT_CHAT, currentChat));
                            break;
                        }
                        case GET_ONLINE_AMOUNT: {
                            Integer amount = (int)(userList.stream()
                                    .filter(User::isOnline).count());
                            out.writeObject(new Response(RequestType.GET_ONLINE_AMOUNT, amount));
                            break;
                        }
                        case CREAT_PRIVATE_CHAT: {
                            List<User> participants = new ArrayList<>();
                            for (String userName : request.getParticipantNames()) {
                                participants.add(findUser(userName));
                            }
                            Chat chat = new Chat(participants);
                            if (!chatList.contains(chat)) {
                                chatList.add(chat);
                                currentChat = chat;
                            }
                            else {
                                currentChat = chatList.get(chatList.indexOf(chat));
                            }
                            out.writeObject(new Response(RequestType.CREAT_PRIVATE_CHAT, 0));
                            break;
                        }
                        case CREAT_GROUP_CHAT: {
                            //TODO
                            break;
                        }
                        case DISCONNECT: {
                            System.out.println("A client closed connection.");
                            user.setOnline(false);
                            flag = false;
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("A client lost connection by accident.");
            user.setOnline(false);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("A service is closed: " + this);
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private User findUser(String userName) {
        for (User user : userList) {
            if (user.getUserName().equals(userName))
                return user;
        }
        return null;
    }

}

