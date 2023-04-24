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
    Label chatNameLabel;
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
    @FXML
    TextArea inputArea;

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

        onlineUserListView.setCellFactory(new OnlineUserCellFactory());
        chatListView.setCellFactory(new ChatNameCellFactory());
        chatContentListView.setCellFactory(new MessageCellFactory());

        onlineUserList = new ArrayList<>();
        chatList = new ArrayList<>();
        currentChat = null;
        onlineAmount = 0;

    }

    public void refresh() {

        ObservableList<User> userObservableList = FXCollections.observableArrayList(onlineUserList);
        onlineUserListView.setItems(userObservableList);

        ObservableList<Chat> chatObservableList = FXCollections.observableArrayList(chatList);
        chatListView.setItems(chatObservableList);

        if (currentChat != null) {
            setChatNameLabel();
            // TODO: 设置 chatContentListView
//            ObservableList<Message> messageObservableList =
//                    FXCollections.observableArrayList(currentChat.getMessages());
//            chatContentListView.setItems(messageObservableList);
        }
        else {
            chatNameLabel.setText("null");
        }

        setOnlineAmountLabel();

    }

    @FXML
    public void createPrivateChat() {

        AtomicReference<String> targetUser = new AtomicReference<>();  // 一定要用这个吗？
        Stage stage = new Stage();

        ComboBox<String> userSelectionBox = new ComboBox<>();
        List<String> onlineUsers = new ArrayList<>();
        for (User usr : onlineUserList) {
            if (!usr.equals(this.user))
                onlineUsers.add(usr.getUserName());
        }
        userSelectionBox.getItems().addAll(onlineUsers);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            targetUser.set(userSelectionBox.getSelectionModel().getSelectedItem());
            stage.close();
            client.creatPrivateChat(String.valueOf(targetUser));
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 120, 20, 20));  // TODO: 调整GUI大小
        box.getChildren().addAll(userSelectionBox, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // 在别的地方判断了
        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name

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
        if (inputArea.getText() == null || inputArea.getText().equals("") || currentChat == null) return;
        client.sendMessage(inputArea.getText(), currentChat);
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

                    System.out.println("MessageCell updated.");
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
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
            return new ListCell<User>() {

                @Override
                public void updateItem(User usr, boolean empty) {

                    System.out.println("UserCell updated.");
                    super.updateItem(usr, empty);
                    if (empty || Objects.isNull(usr)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();

                    Label nameLabel = new Label(usr.getUserName());

                    nameLabel.setPrefSize(200, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel);
                    nameLabel.setPadding(new Insets(0, 0, 0, 20));

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    private class ChatNameCellFactory implements Callback<ListView<Chat>, ListCell<Chat>> {
        @Override
        public ListCell<Chat> call(ListView<Chat> param) {
            return new ListCell<Chat>() {

                @Override
                public void updateItem(Chat chat, boolean empty) {

                    System.out.println("ChatNameCell updated.");
                    super.updateItem(chat, empty);
                    if (empty || Objects.isNull(chat)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();

                    Label nameLabel = new Label(getChatName(chat));

                    nameLabel.setPrefSize(200, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    wrapper.setAlignment(Pos.TOP_LEFT);
                    wrapper.getChildren().addAll(nameLabel);
                    nameLabel.setPadding(new Insets(0, 0, 0, 20));

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }



    public String getChatName(Chat chat) {
        String chatName = null;
        if (chat.getChatType() == Chat.ChatType.PRIVATE_CHAT) {
            for (User usr : chat.getParticipants()) {
                if (!usr.getUserName().equals(user.getUserName())) {
                    chatName = usr.getUserName();
                    break;
                }
            }
        }
        else if (chat.getChatType() == Chat.ChatType.GROUP_CHAT) {
            chatName = chat.getGroupChatName();
        }
        else
            throw new RuntimeException("Unexpected branch");
        return chatName;
    }

    public void setChatNameLabel() {
        chatNameLabel.setText(getChatName(currentChat));
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

    public void setOnlineUserList(List<User> onlineUserList) {
        this.onlineUserList = onlineUserList;
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
    }

    public void setCurrentChat(Chat currentChat) {
//        this.currentChat = currentChat;
        if (currentChat == null) {
            this.currentChat = null;
            return;
        }
        for (Chat chat : chatList) {
            if (currentChat.equals(chat)) {
                this.currentChat = chat;
                return;
            }
        }
        throw new RuntimeException("Unexpected branch");
    }

    public void setOnlineAmount(int onlineAmount) {
        this.onlineAmount = onlineAmount;
    }


}
