package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.WeaponType;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class RangedEnemy extends Enemy {
    private int attackCooldown = 0;

    public RangedEnemy(double x, double y) {
        // 綠色方形，速度中等(1.2)，血量(50)
        super(new Rectangle(30, 30, Color.GREEN), x, y, 50, 1.2, 20, 25);
    }

    // 就是這個方法！必須與 Enemy.java 中的定義完全一致
    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 1. 移動邏輯：如果 Y 小於 250，就往下走
        if (y < 250) {
            y += speed;
        } else {
            // 2. 蓄力攻擊邏輯：到了定點後停下，開始計時
            attackCooldown++;
            // 每 120 幀 (約 2 秒) 發射一次重炮
            if (attackCooldown >= 120) {
                attackCooldown = 0;
                // 發射一顆瞄準主堡的敵方子彈 (高傷害，速度較慢)
                return new Bullet(
                        this.x + 20, this.y + 20,
                        this.x, targetCastle.getY(),
                        10, 5.0, true, WeaponType.NORMAL // <== 補上最後這個參數
                );
            }
        }
        updateSpritePosition();
        return null; // 沒發射子彈時回傳 null
    }
}