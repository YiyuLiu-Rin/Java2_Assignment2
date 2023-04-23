package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Chat;
import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

    @FXML
    Label ChatNameLabel;
    @FXML
    Label currentUserLabel;
    @FXML
    Label onlineAmountLabel;
    @FXML
    ListView<Chat> chatListView;
    @FXML
    ListView<User> onlineUserListView;
    @FXML
    ListView<Message> chatContentListView;

    // 不变信息
    private Client client;
    private User user;

    // 待维护信息
    private List<User> onlineUserList;
    private List<Chat> chatList;
    private Chat currentChat;
    private int onlineAmount;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chatContentListView.setCellFactory(new MessageCellFactory());
        onlineUserListView.setCellFactory(new OnlineUserCellFactory());

        onlineUserList = new ArrayList<>();
        chatList = new ArrayList<>();
        currentChat = null;
        onlineAmount = 0;
    }

    public void refresh() {
        setOnlineAmountLabel();

        // 更新 ListView
        ObservableList<User> userObservableList = FXCollections.observableArrayList(onlineUserList);
//        userObservableList.addAll(onlineUserList);
        System.out.println("1 ################");
        onlineUserListView.setItems(userObservableList);
        System.out.println("2 ################");
//        System.out.println(onlineUserList);  //
    }

    @FXML
    public void createPrivateChat() {

        AtomicReference<String> targetUser = new AtomicReference<>();
        Stage stage = new Stage();

        ComboBox<String> userSelectionBox = new ComboBox<>();
//        List<String> onlineUsers = onlineUserList.stream()
//                        .filter(usr -> !usr.getUserName().equals(this.user.getUserName()))
//                .map(User::getUserName).toList();
        List<String> onlineUsers = new ArrayList<>();
        for (User usr : onlineUserList) {
            if (!usr.getUserName().equals(this.user.getUserName()))
                onlineUsers.add(usr.getUserName());
        }
        userSelectionBox.getItems().addAll(onlineUsers);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            targetUser.set(userSelectionBox.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSelectionBox, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user


        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        List<String> participantNames = new ArrayList<>();
        participantNames.add(user.getUserName());
        participantNames.add(String.valueOf(targetUser));
        client.creatPrivateChat(participantNames);

    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() {
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
    }

    @FXML
    public void doSendEmoji() {}

    @FXML
    public void doSendFile() {}


    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        //setText(null);
                        //setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy().getUserName());
                    Label msgLabel = new Label(msg.getContent());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (user.getUserName().equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    private class OnlineUserCellFactory implements Callback<ListView<User>, ListCell<User>> {
        @Override
        public ListCell<User> call(ListView<User> param) {
            System.out.println("1 ###########################");
            return new ListCell<User>() {

                @Override
                public void updateItem(User usr, boolean empty) {
                    System.out.println("2 ###########################");
                    super.updateItem(usr, empty);
                    if (empty || Objects.isNull(usr)) {
                        //setText(null);
                        //setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
//                    Label nameLabel = new Label(usr.getSentBy().getUserName());
//                    Label msgLabel = new Label(usr.getContent());
                    Label nameLabel = new Label(usr.getUserName());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

//                    if (user.getUserName().equals(usr.getSentBy())) {
//                        wrapper.setAlignment(Pos.TOP_RIGHT);
//                        wrapper.getChildren().addAll(msgLabel, nameLabel);
//                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
//                    } else {
//                        wrapper.setAlignment(Pos.TOP_LEFT);
//                        wrapper.getChildren().addAll(nameLabel, msgLabel);
//                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
//                    }
                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel);
                    nameLabel.setPadding(new Insets(0, 0, 0, 20));

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }





    public void setCurrentUserLabel() {
        currentUserLabel.setText(user.getUserName());
    }

    public void setOnlineAmountLabel() {
        onlineAmountLabel.setText(String.valueOf(onlineAmount));
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setOnlineAmount(int onlineAmount) {
        this.onlineAmount = onlineAmount;
    }

    public void setOnlineUserList(List<User> onlineUserList) {
        this.onlineUserList = onlineUserList;
    }
}
