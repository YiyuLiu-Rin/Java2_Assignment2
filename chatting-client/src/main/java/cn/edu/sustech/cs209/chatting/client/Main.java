package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Request;
import cn.edu.sustech.cs209.chatting.common.User;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.Optional;
import java.util.Scanner;

public class Main extends Application {
    // 运行: mvn javafx:run -pl chatting-client

    Controller controller;
    User user;
    Socket socket;
    ObjectInputStream in;
    ObjectOutputStream out;

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
        while (true) {
            Optional<String[]> input = showLoginDialog();
            if (!input.isPresent()) {
                // 无输入
                System.out.println("Login failed: No input.");
                Platform.exit();
                break;
            }
            if (input.get()[1].isEmpty() || input.get()[2].isEmpty()) {
                // 无效输入
                System.out.println("Login failed: Invalid input.");
                showInfoDialog("Invalid input!");
                continue;
            }
            if (input.get()[0].equals("LOGIN")) {
                Request request = new Request(Request.RequestType.LOG_IN, new User(input.get()[1], input.get()[2]));
                out.writeObject(request);
                Object obj = in.readObject();
                if (obj.getClass() == User.class) {
                    // 登录成功
                    user = (User)obj;
                    System.out.println("Log in succeeded. Username: " + user.getUserName());
                    break;
                }
                else if (obj.getClass() == Integer.class) {
                    Integer responseInfo = (Integer)obj;
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
                }
                else
                    throw new RuntimeException("Unexpected branch");
            }
            else if (input.get()[0].equals("SIGNUP")) {
                Request request = new Request(Request.RequestType.SIGN_UP, new User(input.get()[1], input.get()[2]));
                out.writeObject(request);
                Object obj = in.readObject();
                if (obj.getClass() == User.class) {
                    // 注册成功
                    user = (User)obj;
                    System.out.println("Sign up succeeded. Username: " + user.getUserName());
                    break;
                }
                else if (obj.getClass() == Integer.class) {
                    Integer responseInfo = (Integer)obj;
                    if (responseInfo == 1) {
                        // 已有用户
                        showInfoDialog("This username has been already used!");
                    }
                    else
                        throw new RuntimeException("Unexpected branch");
                }
                else
                    throw new RuntimeException("Unexpected branch");
            }
            else
                throw new RuntimeException("Unexpected branch");
        }

        // 加载主窗口
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();



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
            }
            else
                return null;
        });

        return dialog.showAndWait();
    }

    private static void showInfoDialog(String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(null);
        alert.setContentText(info);

        alert.showAndWait();
    }

}
