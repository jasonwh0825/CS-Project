package engine.entity.enemy;

import engine.entity.Castle;
import engine.entity.Entity;
import engine.entity.Bullet;
import javafx.scene.Node;

public abstract class Enemy extends Entity {
    protected double speed;
    protected double rewardGold;
    protected double rewardExp;
    protected double baseDamage;
    protected int stunTimer = 0;
    private double speedMultiplier = 1.0;
    private int slowTimer = 0;

    public Enemy(Node sprite, double x, double y, double maxHp, double speed, double rewardGold, double rewardExp , double baseDamage) {
        super(sprite, x, y, maxHp);
        this.speed = speed;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
        this.baseDamage = baseDamage;
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
        this.maxHp *= multiplier;
        this.hp = this.maxHp;
        this.rewardGold *= multiplier;
        this.rewardExp *= multiplier;
        this.baseDamage *= multiplier;
        this.speed *= (1.0 + (multiplier - 1) * 0.2);
    }

    public double getBaseDamage() { return baseDamage; }

    public void updateSlow() {
        if (slowTimer > 0) {
            slowTimer--;
            speedMultiplier = 0.5; // 減速 1/2
        } else {
            speedMultiplier = 1.0; // 恢復正常
        }
    }

    // 接受緩速效果 (傳入持續的幀數)
    public void applySlow(int duration) {
        // 如果怪物身上已經有緩速，就取時間比較長的那一個覆蓋
        this.slowTimer = Math.max(this.slowTimer, duration);
    }


    // 取得實際速度的方法
    public double getActualSpeed() {
        return speed * speedMultiplier;
    }
}