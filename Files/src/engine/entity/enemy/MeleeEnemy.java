package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.Entity;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class MeleeEnemy extends Enemy {
    private double angle = 0;

    public MeleeEnemy(double x, double y) {
        super(new Circle(15, Color.RED), x, y, 30, 2.5, 10, 15);
    }

    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        y += speed;
        angle += 0.1;

        // 1. 先計算蛇行位移
        x += Math.sin(angle) * 3;

        // 2. 限制 X 座標範圍 (防止超出 0 ~ 800 戰場區)
        // 近戰怪半徑是 15，所以我們留一點空間
        if (x < 0) {
            x = 0;
        }
        if (x > 770) { // 800 寬度 - 怪物寬度 30
            x = 770;
        }

        updateSpritePosition();
        return null; // 近戰怪不發射子彈
    }
}