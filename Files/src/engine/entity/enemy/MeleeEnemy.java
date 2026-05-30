package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MeleeEnemy extends Enemy {

    private double sinTimer;
    private double swayAmplitude;
    private double swaySpeed; // 新增：扭動的頻率 (快慢)

    public MeleeEnemy(double x, double y) {
        super(
                new Circle(15, Color.RED),
                x, y,
                30, 1.5, 20, 10, 15
        );

        // 初始相位隨機
        this.sinTimer = Math.random() * Math.PI * 2;

        //1. 加大幅度：原本最多 3.0，現在調高到 3.0 ~ 7.0 之間
        this.swayAmplitude = 3.0 + Math.random() * 4.0;

        // 2. 隨機化扭動頻率：原本固定 0.1，現在改為 0.05 ~ 0.15 之間
        this.swaySpeed = 0.05 + Math.random() * 0.1;
    }

    @Override
    public Bullet updateBehavior(Castle targetCastle) {
        // 正常往下推進
        setY(getY() + getActualSpeed());

        // 3. 突變機制：每一幀有 2% 的機率，小怪會突然改變扭動的方式
        if (Math.random() < 0.02) {
            // 突然變大動作或小動作，甚至改變扭的快慢
            this.swayAmplitude = 2.0 + Math.random() * 6.0;
            this.swaySpeed = 0.05 + Math.random() * 0.15;
        }

        // 計算蛇行 X 座標
        sinTimer += swaySpeed;
        double newX = getX() + (Math.sin(sinTimer) * swayAmplitude);

        // 確保不會超出左右畫面邊界
        newX = Math.max(20, Math.min(760, newX));
        setX(newX);

        return null;
    }
}