package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.Entity;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class MeleeEnemy extends Enemy {
    private double angle = 0;

    public MeleeEnemy(double x, double y) {
        super(new Circle(15, Color.RED), x, y, 30, 2.5, 10, 15 ,10);
    }

    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 1. 取得當前實際速度 (考慮了減速與強化)
        double currentSpeed = getActualSpeed();

        // 2. 更新垂直位移
        y += currentSpeed;

        // 3. 更新蛇行角度
        // 如果想要更細緻，角度增加的速度也可以乘以倍率，讓擺動也變慢
        angle += 0.1 * (currentSpeed / speed);

        // 4. 計算蛇行位移 (原本的 3 也乘以倍率)
        x += Math.sin(angle) * (3 * (currentSpeed / speed));

        // 5. 限制 X 座標範圍 (防止超出 800 戰場區)
        if (x < 0) x = 0;
        if (x > 770) x = 770;

        updateSpritePosition();
        return null;
    }
}