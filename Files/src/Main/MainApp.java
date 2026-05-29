package Main;

import engine.GameLoop;
import engine.entity.Castle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import engine.entity.WeaponType;


public class MainApp extends Application {
    private Stage primaryStage;
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Project");
        primaryStage.setResizable(false); // 固定視窗大小防止跑版
        // showLoginScene();
        primaryStage.show();
        showGameScene();
    }

/*
    public void showLoginScene() {
        // 這裡你可以建立一個簡單的 VBox 或是載入 login.fxml
        // 裡面放 TextField (帳號) 和 Button (登入)
        // 點擊登入後，呼叫 showGameScene()
        VBox login=new VBox();
        login.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("歡迎進入遊戲");
        TextField usernameField = new TextField();
        usernameField.setPromptText("輸入帳號");
        usernameField.setMaxWidth(200);

        Button loginButton = new Button("登入");
        loginButton.setOnAction(e -> {
                showGameScene();
        });

        login.getChildren().addAll(titleLabel, usernameField,loginButton);

        Scene loginScene = new Scene(login, 800, 700);

        // 將其顯示在 Stage 上
        primaryStage.setScene(loginScene);
    }
*/

    public void showGameScene() {
        // 1. 建立遊戲的戰鬥畫布 (Pane)
        Pane gamePane = new Pane();
        gamePane.setPrefSize(1000, 700); // 設定視窗寬 800，高 700

        // 2. 初始化核心遊戲引擎 (GameLoop)
        // 這裡暫時帶入帳號等級 1，等夥伴 B 的登入系統做好後，再從登入資料傳進來
        int temporaryAccountLevel = 1;
        GameLoop gameLoop = new GameLoop(gamePane, temporaryAccountLevel);

        // 3. 建立 JavaFX 視現場 (Scene) 並放入視窗 (Stage)
        Scene scene = new Scene(gamePane);
        scene.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof javafx.scene.control.Button)) {
                gameLoop.playerShoot(event.getX(), event.getY());
            }
        });
        // 在 MainApp.java 的 start 方法中
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case SPACE:
                    gameLoop.castUltimate();
                    break;
                case DIGIT1: // 按下鍵盤 1
                    gameLoop.switchWeapon(WeaponType.NORMAL);
                    break;
                case DIGIT2: // 按下鍵盤 2
                    gameLoop.switchWeapon(WeaponType.ICE);
                    break;
                case DIGIT3: // 按下鍵盤 3
                    gameLoop.switchWeapon(WeaponType.HEAVY);
                    break;
                case KeyCode.Q:
                    gameLoop.atklevelup();
                    break;
                case KeyCode.E:
                    gameLoop.hplevelup();
                    break;
            }
        });

        primaryStage.setScene(scene);
        // 4. 正式啟動遊戲主迴圈，怪物會開始往下掉！
        gameLoop.start();
    }
    public static void main(String[] args) {
        // JavaFX 的標準啟動指令，它會自動呼叫上面的 start 方法
        launch(args);
    }
}