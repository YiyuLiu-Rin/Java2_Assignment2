package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Chat;
import cn.edu.sustech.cs209.chatting.common.Request;
import cn.edu.sustech.cs209.chatting.common.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

    public Service(Socket socket, List<User> userList, List<Chat> chatList) {
        this.socket = socket;
        this.userList = userList;
        this.chatList = chatList;
    }

    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            while (true) {
                Object obj = in.readObject();
                if (obj.getClass() == Request.class) {
                    Request request = (Request)obj;
                    System.out.println("Server received a request: " + request.requestType +
                            ", by: " + request.getUser().getUserName());
                    switch (request.requestType) {
                        case LOG_IN: {
                            if (!userList.contains(request.getUser())) {
                                // 无此用户
                                out.writeObject(1);
                                break;
                            }
                            User user = userList.get(userList.indexOf(request.getUser()));
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
                            request.getUser().setOnline(true);
                            userList.add(request.getUser());
                            out.writeObject(request.getUser());
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Client lost connection.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            System.out.println("!!!!!!!!!!!!!!!!");
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}

