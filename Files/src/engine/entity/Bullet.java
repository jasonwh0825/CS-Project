package engine.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Bullet extends Entity {


    private double targetX, targetY;
    private double speed;
    private double damage;
    private boolean isEnemyBullet;
    private WeaponType weaponType;


    private double vx;
    private double vy;

    public Bullet(double startX, double startY, double targetX, double targetY,
                  double damage, double speed, boolean isEnemyBullet, WeaponType weaponType) {


        super(new Circle(weaponType == WeaponType.HEAVY ? 10 : 5,
                        weaponType == WeaponType.HEAVY ? Color.DARKSLATEGRAY :
                                (weaponType == WeaponType.ICE ? Color.CYAN : Color.YELLOW)),
                startX, startY, 1);
        this.targetX = targetX;
        this.targetY = targetY;
        this.damage = damage;
        this.speed = speed;
        this.isEnemyBullet = isEnemyBullet;
        this.weaponType = weaponType;

        // 計算向量
        double dx = targetX - startX;
        double dy = targetY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        this.vx = (dx / distance) * speed;
        this.vy = (dy / distance) * speed;


    }

    @Override
    public void update() {
        x += vx;
        y += vy;
        updateSpritePosition();

        // 超出螢幕判定為死亡
        if (x < 0 || x > 800 || y < 0 || y > 700) {
            takeDamage(999);
        }
    }

    public double getDamage() { return damage; }
    public boolean isEnemyBullet() { return isEnemyBullet; }
    public WeaponType getWeaponType() { return weaponType; }
}