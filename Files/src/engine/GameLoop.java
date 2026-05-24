package engine;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.enemy.Enemy;
import engine.entity.enemy.MeleeEnemy;
import engine.entity.enemy.RangedEnemy;
import engine.entity.enemy.ShamanEnemy;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

public class GameLoop extends AnimationTimer {
    private Pane gamePane;
    private Castle castle;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private Label hudLabel;

    private int frameCount = 0;
    private int accountLevel; // 記錄帳號等級，重新開始時會用到
    private boolean isGameOver = false; // 判斷是否已經結束，避免重複觸發

    public GameLoop(Pane gamePane, int accountLevel) {
        this.gamePane = gamePane;
        this.accountLevel = accountLevel;
        initGame();

        // 初始化左上角 UI
        hudLabel = new Label();
        hudLabel.setFont(new Font("Arial", 18));
        hudLabel.setTextFill(Color.BLACK);
        hudLabel.setTranslateX(20);
        hudLabel.setTranslateY(20);
        gamePane.getChildren().add(hudLabel);
    }

    // 將初始化主堡的動作抽出來，方便重新開始時呼叫
    private void initGame() {
        this.castle = new Castle(360, 600, accountLevel);
        gamePane.getChildren().add(castle.getSprite());
        isGameOver = false;
        frameCount = 0;
    }

    public void playerShoot(double targetX, double targetY) {
        if (isGameOver) return; // 遊戲結束後禁止射擊
        Bullet bullet = new Bullet(castle.getX() + 40, castle.getY(), targetX, targetY, 15, 8.0, false);
        bullets.add(bullet);
        gamePane.getChildren().add(bullet.getSprite());
    }

    @Override
    public void handle(long now) {
        if (isGameOver) return; // 雙重保險，結束就不再更新畫面

        frameCount++;
        castle.update();

        // 1. 生怪邏輯 (每 100 幀)
        if (frameCount % 100 == 0) {
            double randomX = Math.random() * 700 + 50;
            int type = (int)(Math.random() * 3);
            Enemy newEnemy;
            if (type == 0) newEnemy = new MeleeEnemy(randomX, 0);
            else if (type == 1) newEnemy = new RangedEnemy(randomX, 0);
            else newEnemy = new ShamanEnemy(randomX, 0);

            enemies.add(newEnemy);
            gamePane.getChildren().add(newEnemy.getSprite());
        }

        // 2. 更新怪物行為
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);

            Bullet enemyBullet = e.updateBehavior(castle);
            if (enemyBullet != null) {
                bullets.add(enemyBullet);
                gamePane.getChildren().add(enemyBullet.getSprite());
            }

            if (e instanceof ShamanEnemy) {
                ((ShamanEnemy) e).castHeal(enemies);
            }

            // 怪物撞主堡判定
            if (e.getY() >= castle.getY() - 20) {
                castle.takeDamage(10);
                gamePane.getChildren().remove(e.getSprite());
                enemies.remove(i);
                checkGameOver(); // 【新增】檢查是否死亡
            } else if (e.isDead()) {
                castle.addReward(e.getRewardGold(), e.getRewardExp());
                gamePane.getChildren().remove(e.getSprite());
                enemies.remove(i);
            }
        }

        // 3. 更新子彈與碰撞判定
        for (int j = bullets.size() - 1; j >= 0; j--) {
            Bullet b = bullets.get(j);
            b.update();

            if (b.isDead()) {
                gamePane.getChildren().remove(b.getSprite());
                bullets.remove(j);
                continue;
            }

            if (b.isEnemyBullet()) {
                if (b.getSprite().getBoundsInParent().intersects(castle.getSprite().getBoundsInParent())) {
                    castle.takeDamage(b.getDamage());
                    b.takeDamage(999);
                    checkGameOver(); // 【新增】檢查是否死亡
                }
            } else {
                for (Enemy e : enemies) {
                    if (b.getSprite().getBoundsInParent().intersects(e.getSprite().getBoundsInParent())) {
                        e.takeDamage(b.getDamage());
                        b.takeDamage(999);
                        break;
                    }
                }
            }
        }

        // 4. 更新 UI
        if (!isGameOver) {
            hudLabel.setText(String.format("金幣: %.0f  |  經驗值: %.0f  |  大招能量: %.1f%%",
                    castle.getGold(), castle.getExp(), castle.getUltEnergy()));
        }
    }

    // ==========================================
    //          【新增】遊戲結束與重置系統
    // ==========================================

    private void checkGameOver() {
        if (castle.isDead() && !isGameOver) {
            isGameOver = true;
            this.stop(); // 停止 AnimationTimer (凍結遊戲畫面)
            showGameOverScreen();
        }
    }

    private void showGameOverScreen() {
        // 建立一個垂直排列的 UI 容器 (VBox)
        VBox gameOverBox = new VBox(25);
        gameOverBox.setAlignment(Pos.CENTER);

        // 設定半透明黑色的背景遮罩，蓋滿整個 800x700 視窗
        gameOverBox.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        gameOverBox.setPrefSize(800, 700);

        // Game Over 標題文字
        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setFont(new Font("Arial", 60));
        gameOverLabel.setTextFill(Color.RED);

        // 重新開始按鈕
        Button restartBtn = new Button("重新開始");
        restartBtn.setFont(new Font("Arial", 20));
        restartBtn.setPrefWidth(150);
        restartBtn.setOnAction(e -> restartGame(gameOverBox));

        // 退出遊戲按鈕
        Button exitBtn = new Button("退出遊戲");
        exitBtn.setFont(new Font("Arial", 20));
        exitBtn.setPrefWidth(150);
        exitBtn.setOnAction(e -> Platform.exit()); // 安全關閉 JavaFX 程式

        // 將元件放入 VBox，再將 VBox 放進畫布最上層
        gameOverBox.getChildren().addAll(gameOverLabel, restartBtn, exitBtn);
        gamePane.getChildren().add(gameOverBox);
    }

    private void restartGame(VBox gameOverBox) {
        // 1. 移除結算畫面
        gamePane.getChildren().remove(gameOverBox);

        // 2. 清除畫布上所有殘留的敵人、子彈與舊主堡
        for (Enemy e : enemies) gamePane.getChildren().remove(e.getSprite());
        for (Bullet b : bullets) gamePane.getChildren().remove(b.getSprite());
        gamePane.getChildren().remove(castle.getSprite());

        // 3. 清空陣列清單
        enemies.clear();
        bullets.clear();

        // 4. 重新生成主堡並重置狀態
        initGame();

        // 5. 重新啟動遊戲迴圈
        this.start();
    }
}