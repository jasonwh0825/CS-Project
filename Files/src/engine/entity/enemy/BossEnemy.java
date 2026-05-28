package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.WeaponType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class BossEnemy extends Enemy {
    private int attackCooldown = 0;
    private boolean isEnraged = false;

    public BossEnemy(double x, double y) {
        // 體型 100x100，血量 500，速度極慢 0.5，獎勵 500g
        super(new Rectangle(100, 100, Color.DARKRED), x, y, 500, 0.5, 500, 200 ,30);
    }

    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 1. 狂暴判定：血量低於 50% 速度提升
        if (!isEnraged && this.hp < (this.maxHp / 2)) {
            isEnraged = true;
            this.speed *= 2.0;
            ((Rectangle)sprite).setFill(Color.ORANGERED);
        }

        // 2. 移動：緩慢逼近主堡
        y += speed;
        updateSpritePosition();

        // 3. 攻擊：每 1.5 秒射出一顆強力子彈
        attackCooldown++;
        if (attackCooldown >= 90) {
            attackCooldown = 0;
            // 射向主堡中心
            return new Bullet(
                    this.x + 50, this.y + 100,
                    this.x, targetCastle.getY(),
                    this.baseDamage, 4.0, true, WeaponType.NORMAL // <== 補上最後這個參數
            );
        }
        return null;
    }
}
