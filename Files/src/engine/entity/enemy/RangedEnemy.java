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
        super(new Rectangle(30, 30, Color.GREEN), x, y, 50, 1.2, 20, 25 , 10);
    }

    // 就是這個方法！必須與 Enemy.java 中的定義完全一致
    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 1. 取得實際速度
        double currentSpeed = getActualSpeed();

        // 2. 移動邏輯：改用 currentSpeed
        if (y < 250) {
            y += currentSpeed;
        } else {
            // 3. 蓄力攻擊邏輯
            // 如果你希望「緩速」也影響「攻速」，可以根據速度倍率來增加冷卻值
            // 如果被緩速（假設倍率 0.5），每一幀只加 0.5，攻速就會變慢一倍
            double slowFactor = currentSpeed / speed;

            // 注意：因為 attackCooldown 是 int，我們要轉型處理
            // 或者簡單點：只有在隨機機率下才增加，或是乾脆保持原樣
            attackCooldown++;

            // 每 120 幀 (約 2 秒) 發射一次重炮
            if (attackCooldown >= 120) {
                attackCooldown = 0;
                // 發射子彈，傷害使用已經被 enhanceStats 強化過的 baseDamage
                return new Bullet(
                        this.x + 15, this.y + 30, // 調整一下槍口位置
                        this.x + 15, targetCastle.getY(),
                        this.baseDamage, 5.0, true, WeaponType.NORMAL
                );
            }
        }
        updateSpritePosition();
        return null;
    }
}