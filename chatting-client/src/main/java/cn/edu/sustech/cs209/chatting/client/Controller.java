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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.net.URL;
import java.util.*;
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

            ObservableList<Message> messageObservableList =
                    FXCollections.observableArrayList(currentChat.getMessages());
            chatContentListView.setItems(messageObservableList);
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
        // TODO: if the current user already chatted with the selected user,
        //  just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel,
        //  the title should be the selected user's name

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

        List<String> targetUsers = new ArrayList<>();
        Stage stage = new Stage();

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        List<String> onlineUsers = new ArrayList<>();
        for (User usr : onlineUserList) {
            if (!usr.equals(this.user))
                onlineUsers.add(usr.getUserName());
        }

        List<CheckBox> checkBoxes = new ArrayList<>();
        for (int i = 0; i < onlineUsers.size(); i++) {
            CheckBox checkBox = new CheckBox(onlineUsers.get(i));
            checkBox.setAllowIndeterminate(false);
            checkBoxes.add(checkBox);
            gridPane.add(checkBox, 0, i);
        }

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
//            targetUsers.set(userSelectionBox.getSelectionModel().getSelectedItem());
//            stage.close();
//            client.creatPrivateChat(String.valueOf(targetUsers));
            for (int i = 0; i < checkBoxes.size(); i++) {
                if (checkBoxes.get(i).selectedProperty().get())
                    targetUsers.add(onlineUsers.get(i));
            }
            targetUsers.add(user.getUserName());
            stage.close();
            client.creatGroupChat(targetUsers);
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 120, 20, 20));  // TODO: 调整GUI大小
        box.getChildren().addAll(gridPane, okBtn);

//        stage.setScene(new Scene(gridPane, 500, 300));
        stage.setScene(new Scene(box));
        stage.showAndWait();

    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        if (currentChat == null) {
            Client.showInfoDialog("There's no current chat!");
            return;
        }
        if (inputArea.getText() == null || inputArea.getText().isEmpty()) {
            Client.showInfoDialog("Can't send empty message!");
            return;
        }
        client.sendMessage(inputArea.getText(), currentChat);
        inputArea.clear();
    }

    @FXML
    public void doSendEmoji() {
        if (currentChat == null) {
            Client.showInfoDialog("There's no current chat!");
            return;
        }

        // "\uD83D\uDE04" laugh
        // "\uD83D\uDE22" sad
        // "\uD83D\uDE09" wink
        // "\uD83D\uDE18" kiss
        // "\uD83D\uDE02" crying_laugh

        AtomicReference<String> emoji = new AtomicReference<>();
        Stage stage = new Stage();

        ComboBox<String> emojiSelectionBox = new ComboBox<>();
        emojiSelectionBox.getItems().addAll("\uD83D\uDE04", "\uD83D\uDE22",
                "\uD83D\uDE09", "\uD83D\uDE18", "\uD83D\uDE02");

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            if (emojiSelectionBox.getSelectionModel().isEmpty()) {
                Client.showInfoDialog("No emoji is chosen!");
                return;
            }
            emoji.set(emojiSelectionBox.getSelectionModel().getSelectedItem());
            stage.close();
            client.sendMessage(String.valueOf(emoji), currentChat);
        });

        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 120, 20, 20));  // TODO: 调整GUI大小
        box.getChildren().addAll(emojiSelectionBox, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

    }

    @FXML
    public void doSendFile() {
        if (currentChat == null) {
            Client.showInfoDialog("There's no current chat!");
            return;
        }

        Optional<String> input = Client.showChoosingFileDialog();
        if (!input.isPresent())
            return;
        if (input.get().isEmpty()) {
            Client.showInfoDialog("File name can't be empty!");
            return;
        }
        String path = "user_files/" + user.getUserName() + "/" + input.get();
        File file = new File(path);

        if (!file.exists()) {
            Client.showInfoDialog("Can't find this file!");
            return;
        }
        client.sendFile(file, currentChat);
    }

    @FXML
    public void changeCurrentChat() {
        Chat chat = chatListView.getSelectionModel().getSelectedItem();
        client.changeCurrentChat(chat);
    }




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

//                    System.out.println("MessageCell updated.");
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

                    if (user.equals(msg.getSentBy())) {
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

//                    System.out.println("UserCell updated.");
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

//                    System.out.println("ChatNameCell updated.");
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
            if (chat.getParticipants().size() == 3) {
                chat.getParticipants().sort(Comparator.comparing(User::getUserName));
                chatName = chat.getParticipants().get(0).getUserName() + ", " +
                        chat.getParticipants().get(1).getUserName() + ", " +
                        chat.getParticipants().get(2).getUserName();
            }
            else if (chat.getParticipants().size() > 3) {
                chat.getParticipants().sort(Comparator.comparing(User::getUserName));
                chatName = chat.getParticipants().get(0).getUserName() + ", " +
                        chat.getParticipants().get(1).getUserName() + ", " +
                        chat.getParticipants().get(2).getUserName() + "...";
            }
            else
                throw new RuntimeException("Unexpected branch");
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
                chat = new Chat(currentChat);
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
