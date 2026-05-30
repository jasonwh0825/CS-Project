package Main;

import engine.GameLoop;
import engine.entity.WeaponType;
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

public class MainApp extends Application {
    private Stage primaryStage;

    // 模擬的玩家資料 (未來可替換成讀取資料庫或存檔)
    private String currentPlayerName = "Guest_001";
    private int currentAccountLevel = 3; // 假設玩家目前等級是 3（可以手動改這個數字測試解鎖）

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Project - 首頁");
        primaryStage.setResizable(false);

        // 啟動時先顯示主畫面
        showMainMenuScene();
        primaryStage.show();
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

        Label levelLabel = new Label("帳號等級: Lv." + currentAccountLevel);
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
                new Label("3. 季軍高手 - Lv.42"),
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
        Label armoryTitle = new Label("⚔️ 武器庫 (隨等級解鎖)");
        armoryTitle.setTextFill(Color.LIGHTGRAY);
        armoryTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        armoryBox.getChildren().add(armoryTitle);

        // 建立武器列表 (簡單模擬解鎖邏輯：每升1級解鎖一把)
        int requiredLevel = 1;
        for (WeaponType weapon : WeaponType.values()) {
            boolean isUnlocked = currentAccountLevel >= requiredLevel;
            String statusText = isUnlocked ? "✅ 解鎖" : "🔒 需 Lv." + requiredLevel;
            Color statusColor = isUnlocked ? Color.LIGHTGREEN : Color.GRAY;

            Label weaponLabel = new Label(weapon.getName() + " - " + statusText);
            weaponLabel.setTextFill(statusColor);
            armoryBox.getChildren().add(weaponLabel);
            requiredLevel++; // 下一把武器需要的等級 +1
        }

        AnchorPane.setBottomAnchor(armoryBox, 20.0);
        AnchorPane.setLeftAnchor(armoryBox, 20.0);

        // ==========================================
        // 右下：開始遊戲按鈕 (Start Game)
        // ==========================================
        Button startBtn = new Button("開始遊戲 ▶");
        startBtn.setFont(Font.font("System", FontWeight.BOLD, 24));
        startBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15;");
        startBtn.setPrefSize(180, 60);

        // 點擊後跳轉到遊戲畫面
        startBtn.setOnAction(e -> showGameScene());

        AnchorPane.setBottomAnchor(startBtn, 20.0);
        AnchorPane.setRightAnchor(startBtn, 20.0);

        // 將四個區塊加入畫面
        root.getChildren().addAll(userInfoBox, leaderboardPane, armoryBox, startBtn);

        Scene menuScene = new Scene(root, 800, 700);
        primaryStage.setScene(menuScene);
    }

    // 正確的切換至遊戲畫面方法
    // 正確的切換至遊戲畫面方法
    public void showGameScene() {
        primaryStage.setTitle("Project - 遊戲中");

        Pane gamePane = new Pane();
        GameLoop gameLoop = new GameLoop(gamePane, currentAccountLevel);

        // 1. 【修正 UI 不見的問題】視窗寬度必須是 1000 (800 遊戲戰場 + 200 UI 側邊欄)
        Scene scene = new Scene(gamePane, 1000, 700);

        // 2. 【修正無法攻擊的問題】加入滑鼠點擊與拖曳的監聽事件
        scene.setOnMousePressed(event -> {
            // 確保只有點擊戰場區 (X < 800) 才發射子彈，避免點擊側邊欄升級按鈕時也開火
            if (event.getX() < 800) {
                gameLoop.playerShoot(event.getX(), event.getY());
            }
        });

        scene.setOnMouseDragged(event -> {
            if (event.getX() < 800) {
                gameLoop.playerShoot(event.getX(), event.getY());
            }
        });

        // 3. 恢復鍵盤監聽事件 (放大招、切換武器)
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case SPACE:
                    gameLoop.castUltimate();
                    break;
                case DIGIT1:
                    gameLoop.switchWeapon(WeaponType.NORMAL);
                    break;
                case DIGIT2:
                    gameLoop.switchWeapon(WeaponType.ICE);
                    break;
                case DIGIT3:
                    gameLoop.switchWeapon(WeaponType.HEAVY);
                    break;
                case DIGIT4:
                    gameLoop.switchWeapon(WeaponType.HEAL);
                    break;
                case DIGIT5:
                    gameLoop.switchWeapon(WeaponType.FIRE);
                    break;
                case DIGIT6:
                    gameLoop.switchWeapon(WeaponType.SPEED_DOWN);
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

    public static void main(String[] args) {
        launch(args);
    }
}