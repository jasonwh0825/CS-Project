package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import javafx.scene.shape.Polygon;
import javafx.scene.paint.Color;
import java.util.List;

public class ShamanEnemy extends Enemy {
    private int healCooldown = 0;

    public ShamanEnemy(double x, double y) {
        // 藍色三角形代表薩滿，速度慢(1.0)，血量較高(60)
        super(createTriangle(), x, y, 60, 1.0, 30, 30 , 10);
    }

    // 建立三角形外觀
    private static Polygon createTriangle() {
        Polygon p = new Polygon();
        p.getPoints().addAll(15.0, 0.0, 0.0, 30.0, 30.0, 30.0);
        p.setFill(Color.AQUA);
        return p;
    }

    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 1. 修正：改用 getActualSpeed()，否則緩速槍對它無效
        y += getActualSpeed();
        updateSpritePosition();
        return null;
    }

    public void castHeal(List<Enemy> allEnemies) {
        // 2. 進階：如果你希望緩速也能影響薩滿的補血頻率
        // 可以改用 healCooldown += (isSlowed() ? 0.5 : 1); (需將 cooldown 改為 double)
        healCooldown++;

        if (healCooldown >= 180) {
            healCooldown = 0;

            for (Enemy e : allEnemies) {
                // 不補自己，且不補已經死的，也不補滿血的
                if (e == this || e.isDead() || e.getHp() >= e.getMaxHp()) continue;

                double dx = e.getX() - this.x;
                double dy = e.getY() - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= 150) {
                    // 3. 修正補血邏輯：
                    // 原本的 e.getHp()-e.getMaxHp() 會得到負數，傳進 takeDamage(-負數) 會變成扣血！
                    // 我們改成直接判斷：
                    double healAmount = 20;
                    if (e.getHp() + healAmount > e.getMaxHp()) {
                        // 如果補下去會爆表，就只補到滿
                        e.takeDamage(-(e.getMaxHp() - e.getHp()));
                    } else {
                        e.takeDamage(-healAmount);
                    }
                    System.out.println("薩滿補血！目標: " + e.getClass().getSimpleName());
                }
            }
        }
    }
}