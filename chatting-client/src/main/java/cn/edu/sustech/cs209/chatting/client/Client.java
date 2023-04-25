package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Client extends Application {
    // 运行: mvn javafx:run -pl chatting-client

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private Controller controller;
    private User user;

    private RoutineRequestThread request;
    private ReceiveResponseThread receive;
    Thread requestThread, receiveThread;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException, ClassNotFoundException {

        // 将当前客户端连接到服务器端
        try {
            final int PORT = 8888;
            socket = new Socket("localhost", PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 根据Login页面的输入情况决定是否加载主窗口
        boolean load = true;
        while (true) {
            Optional<String[]> input = showLoginDialog();
            if (!input.isPresent()) {
                // 无输入
                System.out.println("Login failed: No input.");
                load = false;
                break;
            }
            if (input.get()[1].isEmpty() || input.get()[2].isEmpty()) {
                // 无效输入
                System.out.println("Login failed: Invalid input.");
                showInfoDialog("Invalid input!");
                continue;
            }
            if (input.get()[0].equals("LOGIN")) {
                Request request = new Request(RequestType.LOG_IN, new User(input.get()[1], input.get()[2]));
                out.writeObject(request);
                Object obj = in.readObject();
                if (obj.getClass() == User.class) {
                    // 登录成功
                    user = (User) obj;
                    System.out.println("Log in succeeded. @" + user.getUserName());
                    break;
                } else if (obj.getClass() == Integer.class) {
                    Integer responseInfo = (Integer) obj;
                    switch (responseInfo) {
                        case 1: {
                            // 无此用户
                            showInfoDialog("There is no such user!");
                            break;
                        }
                        case 2: {
                            // 密码错误
                            showInfoDialog("Wrong password!");
                            break;
                        }
                        case 3: {
                            // 已登录
                            showInfoDialog("This user is already online!");
                            break;
                        }
                        default:
                            throw new RuntimeException("Unexpected branch");
                    }
                } else
                    throw new RuntimeException("Unexpected branch");
            } else if (input.get()[0].equals("SIGNUP")) {
                User createdUser = new User(input.get()[1], input.get()[2]);
                Request request = new Request(RequestType.SIGN_UP, createdUser);
                out.writeObject(request);
                Object obj = in.readObject();
                if (obj.getClass() == User.class) {
                    // 注册成功
                    user = (User) obj;
                    System.out.println("Sign up succeeded. @" + user.getUserName());
                    break;
                } else if (obj.getClass() == Integer.class) {
                    Integer responseInfo = (Integer) obj;
                    if (responseInfo == 1) {
                        // 已有用户
                        showInfoDialog("This username has been already used!");
                    } else
                        throw new RuntimeException("Unexpected branch");
                } else
                    throw new RuntimeException("Unexpected branch");
            } else
                throw new RuntimeException("Unexpected branch");
        }

        // 初始化Controller并加载主窗口
        if (load) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
            stage.setScene(new Scene(fxmlLoader.load()));
            stage.setTitle("Chatting Client");
            stage.setOnCloseRequest(e -> {
                System.out.println("A client is closed. @" + user.getUserName());
                try {
                    out.writeObject(new Request(RequestType.DISCONNECT));
//                    in.close();
//                    out.close();
//                    socket.close();
                    receiveThread.interrupt();
                    requestThread.interrupt();
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
            controller = fxmlLoader.getController();
            controller.setClient(this);
            controller.setUser(user);
            controller.setCurrentUserLabel();
            stage.show();

            request = new RoutineRequestThread(in, out);
            receive = new ReceiveResponseThread(in, out);
            request.receiveThread = receive;
            requestThread = new Thread(request);
            receiveThread = new Thread(receive);
            requestThread.start();
            receiveThread.start();

        }

    }

    public void creatPrivateChat(String targetUser) {
        System.out.println("trying to create a private chat. @" + user.getUserName());
        List<String> participantNames = new ArrayList<>();
        participantNames.add(user.getUserName());
        participantNames.add(targetUser);
        try {
            out.writeObject(new Request(RequestType.CREAT_PRIVATE_CHAT, participantNames));
//            Object obj = in.readObject();
//            if (obj.getClass() != Response.class ||
//                    ((Response)obj).responseType != RequestType.CREAT_PRIVATE_CHAT ||
//                    ((Response)obj).getObj().getClass() != Integer.class)
//                throw new RuntimeException("Unexpected branch");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void creatGroupChat(List<String> participantNames) {
        System.out.println("trying to create a group chat. @" + user.getUserName());
        if (participantNames.size() < 3)
            showInfoDialog("Group chat should have at least 3 participants!");
        else {
            try {
                out.writeObject(new Request(RequestType.CREAT_GROUP_CHAT, participantNames));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(String text, Chat chat) {
        System.out.println("trying to send a message. @" + user.getUserName());
        Message message = new Message(System.currentTimeMillis(), user, text);
        Request request = new Request(RequestType.SEND_MESSAGE, chat, message);
        try {
            out.writeObject(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFile(File file, Chat chat) {
        System.out.println("trying to send a file. @" + user.getUserName());
        UploadedFile uploadedFile = new UploadedFile(System.currentTimeMillis(), user, file);
        Request request = new Request(RequestType.SEND_FILE, chat, uploadedFile);
        try {
            out.writeObject(request);
            sendMessage("#notice_from_server#\nI send a file: " + file.getName(), chat);
            showInfoDialog("Sending file succeeded!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeCurrentChat(Chat chat) {
        System.out.println("trying to change current chat. @" + user.getUserName());
        try {
            out.writeObject(new Request(RequestType.CHANGE_CURRENT_CHAT, chat));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static Optional<String[]> showLoginDialog() {

        // 创建对话框
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);

        // 创建并设置面板
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        //
        // 创建用户名与密码的文本框和标签，并加入面板
        TextField username = new TextField();
        username.setPromptText("Username");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        //
        // 设置ButtonType
        ButtonType loginButtonType = new ButtonType("登录", ButtonBar.ButtonData.OK_DONE);
        ButtonType signupButtonType = new ButtonType("注册", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, signupButtonType, ButtonType.CANCEL);
        //
        // 将面板加入对话框
        dialog.getDialogPane().setContent(grid);

        // 创建返回内容
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType || dialogButton == signupButtonType) {
                String[] result = new String[3];
                result[1] = username.getText();
                result[2] = password.getText();
                if (dialogButton == loginButtonType)
                    result[0] = "LOGIN";
                else
                    result[0] = "SIGNUP";
                return result;
            } else
                return null;
        });

        return dialog.showAndWait();
    }

    public static void showInfoDialog(String info) {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice");
        alert.setHeaderText(null);
        alert.setContentText(info);

        alert.showAndWait();
    }

    public static Optional<String> showChoosingFileDialog() {

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("choose file");
        dialog.setHeaderText(null);
        dialog.setContentText("Enter the file name");

        return dialog.showAndWait();
    }


    /**
     * 不断向服务器发送请求，获取在线用户列表、在线人数、
     */
    private class RoutineRequestThread implements Runnable {

        ObjectInputStream in;
        ObjectOutputStream out;
        ReceiveResponseThread receiveThread;

        public RoutineRequestThread(ObjectInputStream in, ObjectOutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            System.out.println("RoutineRequestThread started.");
            while (true) {
                try {
                    out.writeObject(new Request(RequestType.GET_ONLINE_USER_LIST));
                    out.writeObject(new Request(RequestType.GET_CHAT_LIST));
                    out.writeObject(new Request(RequestType.GET_CURRENT_CHAT));
                    out.writeObject(new Request(RequestType.GET_ONLINE_AMOUNT));
                    while (true) {
                        Thread.sleep(10);
                        if (receiveThread.refreshed) {
                            receiveThread.refreshed = false;
                            break;
                        }
                    }
                    Thread.sleep(300);
                } catch (IOException | InterruptedException e) {
//                        e.printStackTrace();
//                        System.out.println("RoutineRequestThread is killed.");
                    System.exit(0);
                    break;
                }
            }
        }
    }

    /**
     * 接收服务器收到请求后返回的回应
     */
    private class ReceiveResponseThread implements Runnable {

        ObjectInputStream in;
        ObjectOutputStream out;
        boolean refreshed;

        public ReceiveResponseThread(ObjectInputStream in, ObjectOutputStream out) {
            this.in = in;
            this.out = out;
            this.refreshed = true;
        }

        @Override
        public void run() {
            System.out.println("ReceiveResponseThread started.");

            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj.getClass() == Response.class) {
                        Response response = (Response) obj;
//                            System.out.println("A client received a response: " +
//                            response.responseType +
//                                    " @" + user.getUserName());
                        switch (response.responseType) {
                            case GET_ONLINE_USER_LIST: {
                                controller.setOnlineUserList((List<User>) response.getObj());
                                break;
                            }
                            case GET_CHAT_LIST: {
                                controller.setChatList((List<Chat>) response.getObj());
                                break;
                            }
                            case GET_CURRENT_CHAT: {
                                controller.setCurrentChat((Chat) response.getObj());
                                break;
                            }
                            case GET_ONLINE_AMOUNT: {
                                controller.setOnlineAmount((Integer) response.getObj());
                                Platform.runLater(() -> {
                                    controller.refresh();
                                    refreshed = true;
                                });
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    } else {
                        throw new RuntimeException("Unexpected branch");
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (EOFException e) {
                // 关闭客户端造成
//                e.printStackTrace();
//                System.out.println("ReceiveResponseThread is killed.");
                System.exit(0);
            } catch (SocketException e) {
                // 服务器崩溃造成
//                e.printStackTrace();
                System.out.println("The server crashed down.");
                Platform.runLater(() -> {
                    showInfoDialog("The server crashed down!");
                    System.exit(1);
                });
                // TODO: 处理异常
            } catch (IOException ignored) {
            }

        }
    }

}

