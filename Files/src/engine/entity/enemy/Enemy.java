package engine.entity.enemy;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.Entity;
import javafx.scene.Node;

public abstract class Enemy extends Entity {
    protected double speed;
    protected double rewardGold;
    protected double rewardExp;

    public Enemy(Node sprite, double x, double y, double hp, double speed, double gold, double exp) {
        super(sprite, x, y, hp);
        this.speed = speed;
        this.rewardGold = gold;
        this.rewardExp = exp;
    }

    // 因為 Entity 有 abstract void update()，我們在這裡把它實作掉，
    // 改成呼叫子類別的 updateBehavior (如果不需要子類別實作 update，這招很好用)
    @Override
    public final void update() {
        // 這裡留空，因為我們改用 updateBehavior 來處理
    }

    // 這才是我們真正要子類別實作的核心邏輯
    public abstract Bullet updateBehavior(Castle targetCastle);

    public double getRewardGold() { return rewardGold; }
    public double getRewardExp() { return rewardExp; }
}