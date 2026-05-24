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
        super(createTriangle(), x, y, 60, 1.0, 30, 30);
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
        y += speed; // 緩慢往下走
        updateSpritePosition();
        return null; // 薩滿不發射子彈
    }

    // 範圍補血邏輯：由 GameLoop 每幀呼叫並傳入當前所有敵人清單
    public void castHeal(List<Enemy> allEnemies) {
        healCooldown++;
        if (healCooldown >= 180) { // 約 3 秒一次 (60fps * 3)
            healCooldown = 0;

            for (Enemy e : allEnemies) {
                // 不補自己，且不補已經死的
                if (e == this || e.isDead()) continue;

                // 計算兩者之間的距離
                double dx = e.getX() - this.x;
                double dy = e.getY() - this.y;
                double distance = Math.sqrt(dx * dx + dy * dy);

                // 補血範圍：150 像素
                if (distance <= 150) {
                    // 呼叫 takeDamage 傳入負值即為補血
                    e.takeDamage(-20);
                    System.out.println("薩滿補血！目標距離: " + (int)distance);
                }
            }
        }
    }
}