package Main;

import engine.GameLoop;
import engine.entity.WeaponType;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javafx.animation.ParallelTransition;

public class MainApp extends Application {
    private Stage primaryStage;


    // --- 玩家資料庫系統 ---
    private Map<String, UserAccount> userDatabase = new HashMap<>();
    private final String DATA_FILE = "users.txt";

    // --- 當前登入的玩家資料 ---
    private String currentPlayerName = "";
    private int currentAccountLevel = 1;
    private double currentAccountExp = 0;
    private int levelBeforeGame = 1;

    // 內部類別：用來封裝玩家資料
    class UserAccount {
        String username;
        String password;
        int level;
        double exp;

        UserAccount(String username, String password, int level, double exp) {
            this.username = username;
            this.password = password;
            this.level = level;
            this.exp = exp;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("城堡防衛戰");
        primaryStage.setResizable(false);

        // 1. 啟動時先讀取本地帳號資料
        loadUserData();

        // 2. 顯示登入畫面
        showLoginScene();
        primaryStage.show();
    }

    // ==========================================
    // 💾 資料庫讀寫邏輯
    // ==========================================
    private void loadUserData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return; // 沒有檔案就算了，等註冊時會自動產生

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String user = parts[0];
                    String pass = parts[1];
                    int lvl = Integer.parseInt(parts[2]);
                    double exp = Double.parseDouble(parts[3]);
                    userDatabase.put(user, new UserAccount(user, pass, lvl, exp));
                }
            }
        } catch (IOException e) {
            System.out.println("讀取存檔失敗: " + e.getMessage());
        }
    }

    private void saveUserData() {
        // 如果目前有玩家登入，先更新當前玩家的記憶體資料
        if (!currentPlayerName.isEmpty() && userDatabase.containsKey(currentPlayerName)) {
            UserAccount acc = userDatabase.get(currentPlayerName);
            acc.level = currentAccountLevel;
            acc.exp = currentAccountExp;
        }

        // 將所有玩家資料寫入 txt
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (UserAccount acc : userDatabase.values()) {
                writer.write(acc.username + "," + acc.password + "," + acc.level + "," + acc.exp);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("存檔失敗: " + e.getMessage());
        }
    }

    // ==========================================
    // 🔐 登入與註冊畫面
    // ==========================================
    public void showLoginScene() {
        // 原本的登入介面內容
        VBox loginContent = new VBox(20);
        loginContent.setAlignment(Pos.CENTER);
        loginContent.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50;");

        Label title = new Label("城堡防衛戰 - 登入");
        title.setTextFill(Color.GOLD);
        title.setFont(Font.font("System", FontWeight.BOLD, 36));

        // 輸入框區域
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);

        Label userLabel = new Label("使用者名稱:");
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font(16));
        TextField userField = new TextField();

        Label passLabel = new Label("密碼:");
        passLabel.setTextFill(Color.WHITE);
        passLabel.setFont(Font.font(16));
        PasswordField passField = new PasswordField();

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);

        // ⭐ 準備一個 StackPane，等一下要傳給動畫方法
        StackPane mainContainer = new StackPane();

        // 連結區域 (創建帳戶 / 忘記密碼)
        HBox linksBox = new HBox(20);
        linksBox.setAlignment(Pos.CENTER);

        Hyperlink createAccLink = new Hyperlink("創建帳戶?");
        createAccLink.setTextFill(Color.LIGHTSKYBLUE);
        createAccLink.setOnAction(e -> showRegisterScene());

        Hyperlink forgotPassLink = new Hyperlink("忘記密碼?");
        forgotPassLink.setTextFill(Color.LIGHTGRAY);
        // ⭐ 修改這裡：點擊忘記密碼觸發飄動動畫
        forgotPassLink.setOnAction(e -> showFloatingWarning(mainContainer, "我才不會給你重設密碼咧 ಠ_ಠ"));

        linksBox.getChildren().addAll(createAccLink, forgotPassLink);

        // 按鈕區域
        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("登入");
        loginBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            if (userDatabase.containsKey(u) && userDatabase.get(u).password.equals(p)) {
                currentPlayerName = u;
                currentAccountLevel = userDatabase.get(u).level;
                currentAccountExp = userDatabase.get(u).exp;
                levelBeforeGame = currentAccountLevel;
                showMainMenuScene();
            } else {
                // 登入失敗也可以用這個酷酷的動畫！
                showFloatingWarning(mainContainer, "使用者名稱或密碼錯誤！");
            }
        });

        Button guestBtn = new Button("遊客登入");
        guestBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 16px;");
        // ⭐ 修改這裡：點擊遊客登入觸發飄動動畫
        guestBtn.setOnAction(e -> showFloatingWarning(mainContainer, "沒有帳號就去辦一個帳號吧 (╯°□°）╯︵ ┻━┻"));

        btnBox.getChildren().addAll(loginBtn, guestBtn);
        loginContent.getChildren().addAll(title, grid, linksBox, btnBox);

        // ⭐ 將原本的登入內容裝進 StackPane 的底層
        mainContainer.getChildren().add(loginContent);

        // 設定 Scene 綁定新的 mainContainer
        primaryStage.setScene(new Scene(mainContainer, 800, 700));
    }

    public void showRegisterScene() {
        VBox registerContent = new VBox(20);
        registerContent.setAlignment(Pos.CENTER);
        registerContent.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 50;");

        Label title = new Label("創建新帳號");
        title.setTextFill(Color.LIGHTGREEN);
        title.setFont(Font.font("System", FontWeight.BOLD, 36));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);

        Label userLabel = new Label("使用者名稱:");
        userLabel.setTextFill(Color.WHITE);
        TextField userField = new TextField();

        Label passLabel = new Label("密碼:");
        passLabel.setTextFill(Color.WHITE);
        PasswordField passField = new PasswordField();

        Label confirmLabel = new Label("確認密碼:");
        confirmLabel.setTextFill(Color.WHITE);
        PasswordField confirmField = new PasswordField();

        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passLabel, 0, 1);
        grid.add(passField, 1, 1);
        grid.add(confirmLabel, 0, 2);
        grid.add(confirmField, 1, 2);

        // ⭐ 準備 StackPane，用來承載背景與飄動動畫
        StackPane mainContainer = new StackPane();

        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button createBtn = new Button("創建");
        createBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        createBtn.setFocusTraversable(false); // 避免空白鍵誤觸
        createBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p1 = passField.getText();
            String p2 = confirmField.getText();

            // ⭐ 全面改用 Floating 動畫！
            if (u.isEmpty() || p1.isEmpty()) {
                showFloatingWarning(mainContainer, "欄位不能為空！");
            } else if (userDatabase.containsKey(u)) {
                showFloatingWarning(mainContainer, "這個使用者名稱已經被註冊過了！");
            } else if (!p1.equals(p2)) {
                showFloatingWarning(mainContainer, "兩次密碼輸入不一致！");
            } else {
                // 註冊成功
                userDatabase.put(u, new UserAccount(u, p1, 1, 0.0));
                saveUserData();

                // 呼叫綠色的成功動畫
                showFloatingSuccess(mainContainer, "帳號創建成功，請重新登入！");

                // ⭐ 使用 PauseTransition 延遲 2 秒再切換畫面，讓玩家把成功動畫看完
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> showLoginScene());
                pause.play();
            }
        });

        Button backBtn = new Button("返回");
        backBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-font-size: 16px;");
        backBtn.setFocusTraversable(false); // 避免空白鍵誤觸
        backBtn.setOnAction(e -> showLoginScene());

        btnBox.getChildren().addAll(createBtn, backBtn);
        registerContent.getChildren().addAll(title, grid, btnBox);

        // 將註冊畫面內容加到 StackPane 底部
        mainContainer.getChildren().add(registerContent);

        primaryStage.setScene(new Scene(mainContainer, 800, 700));
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    // ==========================================
    // 🎮 遊戲核心與主選單 (與原本相同，加上存檔邏輯)
    // ==========================================

    private void addExpAndLevelUp(double earnedExp) {
        currentAccountExp += earnedExp;
        double requiredExp = currentAccountLevel * 100;

        while (currentAccountExp >= requiredExp) {
            currentAccountExp -= requiredExp;
            currentAccountLevel++;
            requiredExp = currentAccountLevel * 100;
            System.out.println("恭喜升級！目前等級: Lv." + currentAccountLevel);
        }
        // ⭐ 經驗值與等級變動後，自動存檔
        saveUserData();
    }

    public void showMainMenuScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: #2b2b2b;");

        // 玩家資訊區
        VBox userInfoBox = new VBox(5);
        userInfoBox.setPadding(new Insets(15));
        userInfoBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;");

        Label nameLabel = new Label("玩家名稱: " + currentPlayerName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label levelLabel = new Label("帳號等級: Lv." + currentAccountLevel +
                " (EXP: " + (int)currentAccountExp + " / " + (currentAccountLevel * 100) + ")");
        levelLabel.setTextFill(Color.GOLD);
        levelLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        userInfoBox.getChildren().addAll(nameLabel, levelLabel);
        AnchorPane.setTopAnchor(userInfoBox, 20.0);
        AnchorPane.setLeftAnchor(userInfoBox, 20.0);

        // 排行榜區
        VBox leaderboardContent = new VBox(5);
        leaderboardContent.setPadding(new Insets(10));

        java.util.List<UserAccount> allUsers = new java.util.ArrayList<>(userDatabase.values());

        allUsers.sort((u1, u2) -> {
            if (u1.level != u2.level) {
                return Integer.compare(u2.level, u1.level); // 等級高的排前面
            } else {
                return Double.compare(u2.exp, u1.exp);      // 經驗高的排前面
            }
        });

        int displayCount = Math.min(allUsers.size(), 5);
        for (int i = 0; i < displayCount; i++) {
            UserAccount acc = allUsers.get(i);
            int rank = i + 1;

            // 幫當前登入的玩家加上一個星號標記
            String isMe = acc.username.equals(currentPlayerName) ? " (你)" : "";
            Label rankLabel = new Label(rank + ". " + acc.username + isMe + " - Lv." + acc.level);

            // 前三名給予金、銀、銅專屬顏色
            if (rank == 1) {
                rankLabel.setTextFill(Color.GOLD);
                rankLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            } else if (rank == 2) {
                rankLabel.setTextFill(Color.web("#B3B3B3"));
            } else if (rank == 3) {
                rankLabel.setTextFill(Color.web("#CD7F32")); // 銅色
            } else {
                rankLabel.setTextFill(Color.BLACK);
            }

            leaderboardContent.getChildren().add(rankLabel);
        }
        if (allUsers.isEmpty()) {
            Label emptyLabel = new Label("尚無玩家資料");
            emptyLabel.setTextFill(Color.GRAY);
            leaderboardContent.getChildren().add(emptyLabel);
        }
        TitledPane leaderboardPane = new TitledPane("🏆 經驗值排行榜", leaderboardContent);
        // ⭐ 改成預設展開，這樣一進大廳就看得到排行榜！
        leaderboardPane.setExpanded(true);
        leaderboardPane.setPrefWidth(200);
        AnchorPane.setTopAnchor(leaderboardPane, 20.0);
        AnchorPane.setRightAnchor(leaderboardPane, 20.0);

        // 武器庫區
        VBox armoryBox = new VBox(10);
        armoryBox.setPadding(new Insets(15));
        armoryBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
        Label armoryTitle = new Label("武器庫（隨等級解鎖）");
        armoryTitle.setTextFill(Color.LIGHTGRAY);
        armoryTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        armoryBox.getChildren().add(armoryTitle);

        VBox weaponList = new VBox(10);
        for (WeaponType type : WeaponType.values()) {
            boolean unlocked = type.isUnlocked(currentAccountLevel);
            Label wLabel = new Label((unlocked ? "✅ " : "🔒 ") + type.getName() + " (Lv." + type.getUnlockLevel() + " 解鎖)");
            wLabel.setTextFill(unlocked ? Color.LIGHTGREEN : Color.GRAY);
            weaponList.getChildren().add(wLabel);
        }
        armoryBox.getChildren().add(weaponList);
        AnchorPane.setBottomAnchor(armoryBox, 20.0);
        AnchorPane.setLeftAnchor(armoryBox, 20.0);

        // 按鈕區
        VBox menuButtonsBox = new VBox(15);
        menuButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        Button startBtn = new Button("開始遊戲 ▶");
        startBtn.setFont(Font.font("System", FontWeight.BOLD, 24));
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15;");
        startBtn.setPrefSize(180, 60);
        startBtn.setOnAction(e -> showGameScene());

        Button fullScreenBtn = new Button(primaryStage.isFullScreen() ? "退出全螢幕 🗗" : "進入全螢幕 🖥️");
        fullScreenBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        fullScreenBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 15;");
        fullScreenBtn.setPrefSize(180, 45);

        primaryStage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            fullScreenBtn.setText(newVal ? "退出全螢幕 🗗" : "進入全螢幕 🖥️");
        });
        fullScreenBtn.setOnAction(e -> {
            primaryStage.setFullScreen(!primaryStage.isFullScreen());
        });

        Button logoutBtn = new Button("登出帳號 🚪");
        logoutBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        logoutBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 15;");
        logoutBtn.setPrefSize(180, 45);
        logoutBtn.setOnAction(e -> showLoginScene()); // ⭐ 登出返回登入畫面

        Button exitBtn = new Button("退出遊戲 ❌");
        exitBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        exitBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 15;");
        exitBtn.setPrefSize(180, 45);
        exitBtn.setOnAction(e -> {
            saveUserData(); // 退出前最後存檔確保安全
            javafx.application.Platform.exit();
        });

        menuButtonsBox.getChildren().addAll(startBtn, fullScreenBtn, logoutBtn, exitBtn);
        AnchorPane.setBottomAnchor(menuButtonsBox, 20.0);
        AnchorPane.setRightAnchor(menuButtonsBox, 20.0);
        root.getChildren().addAll(userInfoBox, leaderboardPane, armoryBox, menuButtonsBox);

        // ⭐ 1. 把原本的選單裝進一個固定大小的容器 (800x700)
        StackPane menuArea = new StackPane(root);
        menuArea.setMaxSize(800, 700);
        menuArea.setMinSize(800, 700);

        // ⭐ 2. 建立最外層的黑邊容器 (用來填滿全螢幕的空白處)
        StackPane rootContainer = new StackPane(menuArea);
        rootContainer.setStyle("-fx-background-color: black;");

        // ⭐ 3. 自動等比例縮放邏輯
        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            if (rootContainer.getWidth() > 0 && rootContainer.getHeight() > 0) {
                double scale = Math.min(rootContainer.getWidth() / 800.0, rootContainer.getHeight() / 700.0);
                menuArea.setScaleX(scale);
                menuArea.setScaleY(scale);
            }
        };
        rootContainer.widthProperty().addListener(resizeListener);
        rootContainer.heightProperty().addListener(resizeListener);

        // ⭐ 4. 套用 Scene 並維持全螢幕狀態
        boolean isFullScreen = primaryStage.isFullScreen();
        primaryStage.setScene(new Scene(rootContainer, 800, 700));
        primaryStage.setFullScreen(isFullScreen);

        if (currentAccountLevel > levelBeforeGame) {
            // ⭐ 注意這裡的彈窗改加在 menuArea，這樣彈窗才會跟著放大！
            showLevelUpNotification(menuArea, currentAccountLevel);
            levelBeforeGame = currentAccountLevel;
        }

        startBtn.setFocusTraversable(false);
        fullScreenBtn.setFocusTraversable(false);
        logoutBtn.setFocusTraversable(false);
        exitBtn.setFocusTraversable(false);
    }

    public void showGameScene() {
        levelBeforeGame = currentAccountLevel;
        primaryStage.setTitle("Project - 遊戲中");

        Pane gamePane = new Pane();
        gamePane.setStyle("-fx-background-color: white;");
        GameLoop gameLoop = new GameLoop(gamePane, currentAccountLevel, (earnedExp) -> {
            addExpAndLevelUp(earnedExp);
            showMainMenuScene();
        });

        // ==========================================
        // 🛑 確認離開選單 & ⏸️ 暫停選單 (沿用之前的邏輯)
        // ==========================================
        VBox confirmExitOverlay = new VBox(25);
        confirmExitOverlay.setAlignment(Pos.CENTER);
        confirmExitOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        confirmExitOverlay.setVisible(false);

        Label confirmTitle = new Label("確認離開戰場？");
        confirmTitle.setTextFill(Color.RED);
        confirmTitle.setFont(Font.font("System", FontWeight.BOLD, 40));

        Label confirmDesc = new Label("現在離開的話，這場遊戲的經驗值都不會保留喔！\n(っ °Д °;)っ");
        confirmDesc.setTextFill(Color.WHITE);
        confirmDesc.setFont(Font.font("System", FontWeight.BOLD, 22));
        confirmDesc.setAlignment(Pos.CENTER);
        confirmDesc.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        HBox confirmBtnsBox = new HBox(20);
        confirmBtnsBox.setAlignment(Pos.CENTER);

        Button yesBtn = new Button("確定離開");
        yesBtn.setFocusTraversable(false);
        yesBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        yesBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 10;");
        yesBtn.setPrefSize(160, 45);
        yesBtn.setOnAction(e -> {
            gameLoop.stop();
            showMainMenuScene();
        });

        Button noBtn = new Button("點錯了，回去");
        noBtn.setFocusTraversable(false);
        noBtn.setFont(Font.font("System", FontWeight.BOLD, 18));
        noBtn.setStyle("-fx-background-color: #757575; -fx-text-fill: white; -fx-background-radius: 10;");
        noBtn.setPrefSize(160, 45);

        confirmBtnsBox.getChildren().addAll(yesBtn, noBtn);
        confirmExitOverlay.getChildren().addAll(confirmTitle, confirmDesc, confirmBtnsBox);

        VBox pauseOverlay = new VBox(30);
        pauseOverlay.setAlignment(Pos.CENTER);
        pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
        pauseOverlay.setVisible(false);

        Label pauseLabel = new Label("遊戲暫停");
        pauseLabel.setTextFill(Color.GOLD);
        pauseLabel.setFont(Font.font("System", FontWeight.BOLD, 48));

        Button continueBtn = new Button("繼續遊戲 ▶");
        continueBtn.setFocusTraversable(false);
        continueBtn.setFont(Font.font("System", FontWeight.BOLD, 22));
        continueBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 10;");
        continueBtn.setPrefSize(220, 50);
        continueBtn.setOnAction(e -> {
            pauseOverlay.setVisible(false);
            gameLoop.start();
        });

        Button backToMenuBtn = new Button("回到主畫面 🏠");
        backToMenuBtn.setFocusTraversable(false);
        backToMenuBtn.setFont(Font.font("System", FontWeight.BOLD, 20));
        backToMenuBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 10;");
        backToMenuBtn.setPrefSize(220, 50);
        backToMenuBtn.setOnAction(e -> {
            pauseOverlay.setVisible(false);
            confirmExitOverlay.setVisible(true);
        });

        noBtn.setOnAction(e -> {
            confirmExitOverlay.setVisible(false);
            pauseOverlay.setVisible(true);
        });

        pauseOverlay.getChildren().addAll(pauseLabel, continueBtn, backToMenuBtn);

        Button pauseBtn = new Button("⏸ 暫停");
        pauseBtn.setFont(Font.font("System", FontWeight.BOLD, 16));
        pauseBtn.setFocusTraversable(false);
        pauseBtn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 5;");
        pauseBtn.setLayoutX(900);
        pauseBtn.setLayoutY(640);
        pauseBtn.setViewOrder(-2);
        pauseBtn.setOnAction(e -> {
            gameLoop.stop();
            pauseOverlay.setVisible(true);
        });

        gamePane.getChildren().add(pauseBtn);

        // ==========================================
        // ⭐ 全螢幕縮放核心邏輯
        // ==========================================

        // 1. 把遊戲畫面和選單疊進一個固定大小的 1000x700 容器
        StackPane gameArea = new StackPane();
        gameArea.setMaxSize(1000, 700);
        gameArea.setMinSize(1000, 700);
        gameArea.getChildren().addAll(gamePane, pauseOverlay, confirmExitOverlay);

        // 2. 建立最外層黑邊容器
        StackPane rootContainer = new StackPane(gameArea);
        rootContainer.setStyle("-fx-background-color: black;");

        // 3. 自動縮放邏輯
        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
            if (rootContainer.getWidth() > 0 && rootContainer.getHeight() > 0) {
                double scale = Math.min(rootContainer.getWidth() / 1000.0, rootContainer.getHeight() / 700.0);
                gameArea.setScaleX(scale);
                gameArea.setScaleY(scale);
            }
        };
        rootContainer.widthProperty().addListener(resizeListener);
        rootContainer.heightProperty().addListener(resizeListener);

        // 4. 切換 Scene 並維持全螢幕狀態
        boolean isFullScreen = primaryStage.isFullScreen();
        Scene scene = new Scene(rootContainer, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(isFullScreen);

        // ==========================================
        // 🖱️ 滑鼠與鍵盤事件
        // ==========================================

        // ⭐ 注意：滑鼠事件改綁在 gameArea 上！這樣 event.getX() 才會自動縮放換算
        gameArea.setOnMousePressed(event -> {
            if (!pauseOverlay.isVisible() && !confirmExitOverlay.isVisible() && event.getX() < 800) {
                gameLoop.setShootingState(true, event.getX(), event.getY());
            }
        });
        gameArea.setOnMouseDragged(event -> {
            if (!pauseOverlay.isVisible() && !confirmExitOverlay.isVisible() && event.getX() < 800) {
                gameLoop.updateMousePosition(event.getX(), event.getY());
            }
        });
        gameArea.setOnMouseReleased(event -> {
            if (!pauseOverlay.isVisible() && !confirmExitOverlay.isVisible()) {
                gameLoop.setShootingState(false, event.getX(), event.getY());
            }
        });

        // 鍵盤事件不用座標，所以還是綁在 scene 上
        scene.setOnKeyReleased(event -> {
            if (pauseOverlay.isVisible() || confirmExitOverlay.isVisible()) return;

            switch (event.getCode()) {
                case SPACE: gameLoop.castUltimate(); break;
                case DIGIT1: gameLoop.switchWeapon(WeaponType.NORMAL);break;
                case DIGIT2: gameLoop.switchWeapon(WeaponType.SPEED_DOWN);break;
                case DIGIT3: gameLoop.switchWeapon(WeaponType.ICE);break;
                case DIGIT4: gameLoop.switchWeapon(WeaponType.HEAVY);break;
                case DIGIT5: gameLoop.switchWeapon(WeaponType.HEAL);break;
                case DIGIT6: gameLoop.switchWeapon(WeaponType.FIRE);break;
                case Q: gameLoop.atklevelup(); break;
                case E: gameLoop.hplevelup(); break;
            }
        });

        gameLoop.start();
    }

    private void showFloatingWarning(StackPane rootContainer, String message) {
        Label warningLabel = new Label(message);
        warningLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        warningLabel.setTextFill(Color.RED);
        // 加一點半透明黑底讓紅字在任何背景下都能看清楚
        warningLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 15px; -fx-background-radius: 10px;");

        // 因為我們用 StackPane，它預設會在正中央，我們可以稍微往下調一點作為起始位置
        warningLabel.setTranslateY(50);

        // 加入到畫面的最上層
        rootContainer.getChildren().add(warningLabel);

        // 1. 漸隱動畫 (2秒內透明度從 1.0 變成 0.0)
        FadeTransition fade = new FadeTransition(Duration.seconds(2), warningLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        // 2. 位移動畫 (2秒內往上移動 50 像素)
        TranslateTransition translate = new TranslateTransition(Duration.seconds(2), warningLabel);
        translate.setByY(-80); // 往上飄動的幅度大一點點

        // 3. 把兩個動畫綁在一起同時播放
        ParallelTransition parallelTransition = new ParallelTransition(fade, translate);

        // 4. 動畫播完後，記得把這個 Label 從畫面上徹底移除
        parallelTransition.setOnFinished(e -> rootContainer.getChildren().remove(warningLabel));

        // 播放動畫！
        parallelTransition.play();
    }

    private void showFloatingSuccess(StackPane rootContainer, String message) {
        Label successLabel = new Label(message);
        successLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        successLabel.setTextFill(Color.LIGHTGREEN); // 成功改成漂亮的亮綠色
        successLabel.setStyle("-fx-background-color: rgba(0,0,0,0.7); -fx-padding: 15px; -fx-background-radius: 10px;");

        successLabel.setTranslateY(50);
        rootContainer.getChildren().add(successLabel);

        FadeTransition fade = new FadeTransition(Duration.seconds(2), successLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        TranslateTransition translate = new TranslateTransition(Duration.seconds(2), successLabel);
        translate.setByY(-80);

        ParallelTransition parallelTransition = new ParallelTransition(fade, translate);
        parallelTransition.setOnFinished(e -> rootContainer.getChildren().remove(successLabel));
        parallelTransition.play();
    }

    private void showLevelUpNotification(StackPane rootContainer, int newLevel) {
        VBox popup = new VBox(15);
        popup.setStyle("-fx-background-color: rgba(20, 20, 20, 0.9); -fx-background-radius: 15; -fx-padding: 30; -fx-border-color: #FFD700; -fx-border-width: 4; -fx-border-radius: 15;");
        popup.setAlignment(Pos.CENTER);
        popup.setMaxSize(350, 180);

        Label titleLabel = new Label("🎊 恭喜升級 🎊");
        titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label infoLabel = new Label("目前帳號等級: Lv." + newLevel);
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        popup.getChildren().addAll(titleLabel, infoLabel);
        rootContainer.getChildren().add(popup);

        popup.setTranslateY(-300);
        popup.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), popup);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), popup);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), popup);
        fadeOut.setDelay(Duration.seconds(3.0));
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootContainer.getChildren().remove(popup));

        slideIn.play();
        fadeIn.play();
        fadeOut.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}