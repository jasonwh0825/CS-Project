package Main;

import engine.GameLoop;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // 1. 建立遊戲的戰鬥畫布 (Pane)
        Pane gamePane = new Pane();
        gamePane.setPrefSize(800, 700); // 設定視窗寬 800，高 700

        // 2. 初始化核心遊戲引擎 (GameLoop)
        // 這裡暫時帶入帳號等級 1，等夥伴 B 的登入系統做好後，再從登入資料傳進來
        int temporaryAccountLevel = 1;
        GameLoop gameLoop = new GameLoop(gamePane, temporaryAccountLevel);

        // 3. 建立 JavaFX 視現場 (Scene) 並放入視窗 (Stage)
        Scene scene = new Scene(gamePane);
        scene.setOnMouseClicked(event -> {
            // 只有點擊空白處（不是按鈕）才射擊
            if (!(event.getTarget() instanceof javafx.scene.control.Button)) {
                gameLoop.playerShoot(event.getX(), event.getY());
            }
        });
        // 在 MainApp.java 的 start 方法中
        scene.setOnKeyReleased(event -> {
            // 使用 switch 可以方便未來擴充其他按鍵（如切換武器）
            switch (event.getCode()) {
                case SPACE:
                    gameLoop.castUltimate();
                    break;

                case DIGIT1:
                    // 範例：未來可以按 1 切換電系武器
                    // gameLoop.switchWeapon(WeaponType.ELECTRIC);
                    break;

                case DIGIT2:
                    // 範例：未來可以按 2 切換冰系武器
                    // gameLoop.switchWeapon(WeaponType.ICE);
                    break;
            }
        });
        primaryStage.setTitle("Project");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // 固定視窗大小防止跑版
        primaryStage.show();

        // 4. 正式啟動遊戲主迴圈，怪物會開始往下掉！
        gameLoop.start();
    }

    public static void main(String[] args) {
        // JavaFX 的標準啟動指令，它會自動呼叫上面的 start 方法
        launch(args);
    }
}