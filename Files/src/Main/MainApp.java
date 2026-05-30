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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private Stage primaryStage;

    // 模擬的玩家資料 (未來可替換成讀取資料庫或存檔)
    private String currentPlayerName = "Guest_001";
    private int currentAccountLevel = 1;
    private double currentAccountExp = 0;

    // ⭐ 初始與帳號等級相同，避免一開遊戲就跳升級
    private int levelBeforeGame = 12;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Project - 首頁");
        primaryStage.setResizable(false);

        // 啟動時先顯示主畫面
        showMainMenuScene();
        primaryStage.show();
    }

    private void addExpAndLevelUp(double earnedExp) {
        currentAccountExp += earnedExp;
        double requiredExp = currentAccountLevel * 100; // 升級所需經驗值

        // 如果經驗值超過門檻，就升級 (使用 while 是怕一次獲得太多經驗值連升兩級)
        while (currentAccountExp >= requiredExp) {
            currentAccountExp -= requiredExp;
            currentAccountLevel++;
            requiredExp = currentAccountLevel * 100;
            System.out.println("恭喜升級！目前等級: Lv." + currentAccountLevel);
        }
    }

    public void showMainMenuScene() {
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: #2b2b2b;"); // 設定深色背景

        // ==========================================
        // 左上：玩家資訊 (User Info)
        // ==========================================
        VBox userInfoBox = new VBox(5);
        userInfoBox.setPadding(new Insets(15));
        userInfoBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 10;");

        Label nameLabel = new Label("玩家名稱: " + currentPlayerName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 18));

        // 顯示目前等級與經驗值進度
        Label levelLabel = new Label("帳號等級: Lv." + currentAccountLevel +
                " (EXP: " + (int)currentAccountExp + " / " + (currentAccountLevel * 100) + ")");
        levelLabel.setTextFill(Color.GOLD);
        levelLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        userInfoBox.getChildren().addAll(nameLabel, levelLabel);
        AnchorPane.setTopAnchor(userInfoBox, 20.0);
        AnchorPane.setLeftAnchor(userInfoBox, 20.0);

        // ==========================================
        // 右上：排行榜 (Leaderboard - 可展開 TitledPane)
        // ==========================================
        VBox leaderboardContent = new VBox(5);
        leaderboardContent.getChildren().addAll(
                new Label("1. 神手玩家 - Lv.50"),
                new Label("2. 亞軍大大 - Lv.45"),
                new Label("3. 季這次高手 - Lv.42"),
                new Label("... (載入中)")
        );
        TitledPane leaderboardPane = new TitledPane("🏆 經驗值排行榜", leaderboardContent);
        leaderboardPane.setExpanded(false); // 預設收合
        leaderboardPane.setPrefWidth(200);
        AnchorPane.setTopAnchor(leaderboardPane, 20.0);
        AnchorPane.setRightAnchor(leaderboardPane, 20.0);

        // ==========================================
        // 左下：武器庫 (Armory)
        // ==========================================
        VBox armoryBox = new VBox(10);
        armoryBox.setPadding(new Insets(15));
        armoryBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
        Label armoryTitle = new Label("武器庫（隨等級解鎖）");
        armoryTitle.setTextFill(Color.LIGHTGRAY);
        armoryTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        armoryBox.getChildren().add(armoryTitle);

        // 建立武器列表 (簡單模擬解鎖邏輯：每升1級解鎖一把)
        VBox weaponList = new VBox(10);
        for (WeaponType type : WeaponType.values()) {
            boolean unlocked = type.isUnlocked(currentAccountLevel); // 使用新加的方法

            Label wLabel = new Label(
                    (unlocked ? "✅ " : "🔒 ") + type.getName() +
                            " (Lv." + type.getUnlockLevel() + " 解鎖)"
            );

            wLabel.setTextFill(unlocked ? Color.LIGHTGREEN : Color.GRAY);
            weaponList.getChildren().add(wLabel);
        }
        armoryBox.getChildren().add(weaponList);
        AnchorPane.setBottomAnchor(armoryBox, 20.0);
        AnchorPane.setLeftAnchor(armoryBox, 20.0);

        // ==========================================
        // 右下：按鈕控制區 (開始遊戲 / 退出遊戲)
        // ==========================================
        VBox menuButtonsBox = new VBox(15);
        menuButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        // 1. 開始遊戲按鈕
        Button startBtn = new Button("開始遊戲 ▶");
        startBtn.setFont(Font.font("System", FontWeight.BOLD, 24));
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15;");
        startBtn.setPrefSize(180, 60);
        startBtn.setOnAction(e -> showGameScene());

        // 2. 退出遊戲按鈕
        Button exitBtn = new Button("退出遊戲 ❌");
        exitBtn.setFont(Font.font("System", FontWeight.BOLD, 20));
        exitBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-background-radius: 15;");
        exitBtn.setPrefSize(180, 50);

        exitBtn.setOnAction(e -> javafx.application.Platform.exit());
        menuButtonsBox.getChildren().addAll(startBtn, exitBtn);

        AnchorPane.setBottomAnchor(menuButtonsBox, 20.0);
        AnchorPane.setRightAnchor(menuButtonsBox, 20.0);
        root.getChildren().addAll(userInfoBox, leaderboardPane, armoryBox, menuButtonsBox);


        StackPane mainContainer = new StackPane(root);
        Scene menuScene = new Scene(mainContainer, 800, 700);
        primaryStage.setScene(menuScene);

        if (currentAccountLevel > levelBeforeGame) {
            showLevelUpNotification(mainContainer, currentAccountLevel);
            // 顯示完後更新記錄，避免重複彈出
            levelBeforeGame = currentAccountLevel;
        }
    }

    public void showGameScene() {
        levelBeforeGame = currentAccountLevel;

        primaryStage.setTitle("Project - 遊戲中");

        Pane gamePane = new Pane();
        GameLoop gameLoop = new GameLoop(gamePane, currentAccountLevel, (earnedExp) -> {
            addExpAndLevelUp(earnedExp); // 1. 計算經驗值並判斷是否升級
            showMainMenuScene();         // 2. 重新顯示首頁 (此時會觸發彈窗)
        });

        Scene scene = new Scene(gamePane, 1000, 700);
        scene.setOnMousePressed(event -> {
            if (event.getX() < 800) {
                gameLoop.setShootingState(true, event.getX(), event.getY());
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getX() < 800) {
                gameLoop.updateMousePosition(event.getX(), event.getY());
            }
        });

        scene.setOnMouseReleased(event -> {
            gameLoop.setShootingState(false, event.getX(), event.getY());
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case SPACE:
                    gameLoop.castUltimate();
                    break;
                case DIGIT1:
                    gameLoop.switchWeapon(WeaponType.NORMAL);
                    break;
                case DIGIT2:
                    gameLoop.switchWeapon(WeaponType.SPEED_DOWN);
                    break;
                case DIGIT3:
                    gameLoop.switchWeapon(WeaponType.ICE);
                    break;
                case DIGIT4:
                    gameLoop.switchWeapon(WeaponType.HEAVY);
                    break;
                case DIGIT5:
                    gameLoop.switchWeapon(WeaponType.HEAL);
                    break;
                case DIGIT6:
                    gameLoop.switchWeapon(WeaponType.FIRE);
                    break;
                case Q:
                    gameLoop.atklevelup();
                    break;
                case E:
                    gameLoop.hplevelup();
                    break;
            }
        });

        primaryStage.setScene(scene);
        gameLoop.start();
    }

    private void showLevelUpNotification(StackPane rootContainer, int newLevel) {
        VBox popup = new VBox(15);
        popup.setStyle("-fx-background-color: rgba(20, 20, 20, 0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 30; " +
                "-fx-border-color: #FFD700; " +
                "-fx-border-width: 4; " +
                "-fx-border-radius: 15;");
        popup.setAlignment(Pos.CENTER);
        popup.setMaxSize(350, 180);

        Label titleLabel = new Label("🎊 恭喜升級 🎊");
        titleLabel.setStyle("-fx-text-fill: #FFD700; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label infoLabel = new Label("目前帳號等級: Lv." + newLevel);
        infoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");

        popup.getChildren().addAll(titleLabel, infoLabel);

        // 加入到畫面上
        rootContainer.getChildren().add(popup);

        // 動畫設定
        popup.setTranslateY(-300);
        popup.setOpacity(0);

        TranslateTransition slideIn = new TranslateTransition(Duration.seconds(0.5), popup);
        slideIn.setToY(0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), popup);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), popup);
        fadeOut.setDelay(Duration.seconds(3.0)); // 停留 3 秒
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