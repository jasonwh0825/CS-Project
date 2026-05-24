package engine.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends Entity {
    private double speed;
    private double damage;
    private double dirX;
    private double dirY;

    // 【新增】分辨子彈陣營
    private boolean isEnemyBullet;

    public Bullet(double startX, double startY, double targetX, double targetY, double damage, double speed, boolean isEnemyBullet) {
        // 如果是敵方子彈用紫色，玩家子彈用黃色
        super(new Circle(5, isEnemyBullet ? Color.PURPLE : Color.YELLOW), startX, startY, 1);
        this.damage = damage;
        this.speed = speed;
        this.isEnemyBullet = isEnemyBullet;

        this.hpBgBar.setVisible(false);
        this.hpFgBar.setVisible(false);

        double dx = targetX - startX;
        double dy = targetY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            this.dirX = dx / distance;
            this.dirY = dy / distance;
        } else {
            this.dirX = 0;
            this.dirY = 1; // 預設往下飛
        }
    }

    @Override
    public void update() {
        x += dirX * speed;
        y += dirY * speed;
        updateSpritePosition();

        if (x < 0 || x > 800 || y < 0 || y > 700) {
            this.isDead = true;
        }
    }

    public double getDamage() { return damage; }
    // 【新增】取得子彈陣營
    public boolean isEnemyBullet() { return isEnemyBullet; }
}