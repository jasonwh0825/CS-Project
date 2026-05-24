package engine.entity.enemy;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class RangedEnemy extends Enemy {
    private int attackCooldown = 0;

    public RangedEnemy(double x, double y) {
        // 綠色方形，速度中等
        super(new Rectangle(30, 30, Color.GREEN), x, y, 50, 1.2, 20, 25);
    }

    @Override
    public void update() {
        // 畫面中央假設是 y = 300。沒到中央就往下走，到了就停下來蓄力射擊
        if (y < 300) {
            y += speed;
        } else {
            // 定點蓄力行為
            attackCooldown++;
            if (attackCooldown >= 120) { // 假設 60 幀為 1 秒，這代表 2 秒射擊一次
                shootHeavyCannon();
                attackCooldown = 0;
            }
        }
        updateSpritePosition();
    }

    private void shootHeavyCannon() {
        System.out.println("遠程怪發射了高傷害重炮！");
        // TODO: 在 GameLoop 的敵人子彈串列中新增一顆子彈
    }
}