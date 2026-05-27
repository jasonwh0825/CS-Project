package engine.entity.enemy;

import engine.entity.Castle;
import engine.entity.Entity;
import engine.entity.Bullet;
import javafx.scene.Node;

public abstract class Enemy extends Entity {
    protected double speed;
    protected double rewardGold;
    protected double rewardExp;
    protected int stunTimer = 0;

    public Enemy(Node sprite, double x, double y, double maxHp, double speed, double rewardGold, double rewardExp) {
        super(sprite, x, y, maxHp);
        this.speed = speed;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
    }

    // ==========================================
    // 【新增這裡】覆寫 Entity 的 update() 來解除報錯
    // ==========================================
    @Override
    public void update() {
        // 因為怪物的行為我們改用下方的 updateBehavior(Castle) 來處理，
        // 所以這裡只要留空即可，但必須寫出來滿足 Java 的規定。
    }

    public void applyStun(int duration) {
        this.stunTimer = duration;
    }

    public boolean isStunned() {
        return stunTimer > 0;
    }

    public void updateStun() {
        if (stunTimer > 0) stunTimer--;
    }

    public abstract Bullet updateBehavior(Castle targetCastle);

    public double getRewardGold() { return rewardGold; }
    public double getRewardExp() { return rewardExp; }

    public void enhanceStats(double multiplier) {
        // 血量乘上倍率
        this.maxHp *= multiplier;
        this.hp = this.maxHp;

        // 打死後掉落的金幣與經驗也隨之增加，給玩家更多獎勵
        this.rewardGold *= multiplier;
        this.rewardExp *= multiplier;

        // 速度只微幅增加（例如倍率多 0.5，速度只多 10%），以免後期怪物快到像閃電
        this.speed *= (1.0 + (multiplier - 1) * 0.2);
    }
}