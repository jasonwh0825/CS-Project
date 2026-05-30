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
    private int currentAccountLevel = 1;
    private double currentAccountExp = 0;

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
        Label armoryTitle = new Label("武器庫 (隨等級解鎖)");
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
        VBox menuButtonsBox = new VBox(15); // 按鈕之間的間距為 15 像素
        menuButtonsBox.setAlignment(Pos.CENTER_RIGHT); // 靠右對齊

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
        exitBtn.setPrefSize(180, 50); // 高度稍微矮一點，讓視覺上有主次之分

        // 點擊後關閉 JavaFX 視窗並結束程式
        exitBtn.setOnAction(e -> javafx.application.Platform.exit());

        // 把兩個按鈕放進 VBox 盒子裡
        menuButtonsBox.getChildren().addAll(startBtn, exitBtn);

        // 將整個按鈕盒子固定在右下角
        AnchorPane.setBottomAnchor(menuButtonsBox, 20.0);
        AnchorPane.setRightAnchor(menuButtonsBox, 20.0);

        // 將四個區塊加入畫面
        root.getChildren().addAll(userInfoBox, leaderboardPane, armoryBox, menuButtonsBox);

        Scene menuScene = new Scene(root, 800, 700);
        primaryStage.setScene(menuScene);
    }

    // 正確的切換至遊戲畫面方法
    // 正確的切換至遊戲畫面方法
    public void showGameScene() {
        primaryStage.setTitle("Project - 遊戲中");

        Pane gamePane = new Pane();
        GameLoop gameLoop = new GameLoop(gamePane, currentAccountLevel, (earnedExp) -> {
            addExpAndLevelUp(earnedExp); // 1. 計算經驗值並判斷是否升級
            showMainMenuScene();         // 2. 重新顯示首頁 (畫面會自動刷新等級與武器解鎖狀態)
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