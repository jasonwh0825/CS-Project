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
        x += Math.sin(angle) * 3;
        updateSpritePosition();
        return null; // 近戰怪不發射子彈
    }
}