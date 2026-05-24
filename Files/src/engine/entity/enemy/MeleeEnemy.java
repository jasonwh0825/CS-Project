package engine.entity.enemy;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

public class MeleeEnemy extends Enemy {
    private double angle = 0; // 用於計算正弦波（蛇行）

    public MeleeEnemy(double x, double y) {
        // 紅色圓形，速度快 (2.5)
        super(new Circle(15, Color.RED), x, y, 30, 2.5, 10, 15);
    }

    @Override
    public void update() {
        y += speed; // 由上而下前進

        // 左右蛇行：利用 Math.sin 讓 X 座標在原點附近左右擺動
        angle += 0.1;
        x += Math.sin(angle) * 3;

        updateSpritePosition();
    }
}
