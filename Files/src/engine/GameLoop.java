package engine;

import engine.entity.Castle;
import engine.entity.enemy.Enemy;
import engine.entity.enemy.MeleeEnemy;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.List;

public class GameLoop extends AnimationTimer {
    private Pane gamePane; // 遊戲戰鬥畫布
    private Castle castle;
    private List<Enemy> enemies = new ArrayList<>();

    private int frameCount = 0;

    public GameLoop(Pane gamePane, int accountLevel) {
        this.gamePane = gamePane;
        // 將主堡生成在畫面下方中央 (假設視窗寬度 800，高度 700)
        this.castle = new Castle(360, 600, accountLevel);
        gamePane.getChildren().add(castle.getSprite());
    }

    @Override
    public void handle(long now) {
        frameCount++;

        // 1. 更新主堡狀態
        castle.update();

        // 2. 簡單的一波波出怪邏輯 (每 3 秒生一隻近戰怪做測試)
        if (frameCount % 180 == 0) {
            double randomX = Math.random() * 700 + 50;
            Enemy newEnemy = new MeleeEnemy(randomX, 0);
            enemies.add(newEnemy);
            gamePane.getChildren().add(newEnemy.getSprite());
        }

        // 3. 更新所有怪物狀態
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update();

            // 如果怪物撞到主堡
            if (e.getY() >= castle.getY() - 20) {
                castle.takeDamage(10); // 主堡扣血
                gamePane.getChildren().remove(e.getSprite());
                enemies.remove(i);

                if (castle.isDead()) {
                    this.stop();
                    System.out.println("遊戲結束！主堡被摧毀。");
                }
                continue;
            }

            // 如果怪物死亡 (被子彈打死)
            if (e.isDead()) {
                castle.addReward(e.getRewardGold(), e.getRewardExp());
                gamePane.getChildren().remove(e.getSprite());
                enemies.remove(i);
            }
        }

        // 4. TODO: 更新子彈、進行碰撞偵測、更新 HUD 介面數值
    }
}