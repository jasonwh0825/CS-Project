package engine.entity.enemy;

import engine.entity.Castle;
import engine.entity.Entity;
import engine.entity.Bullet;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

public abstract class Enemy extends Entity {
    protected double speed;
    protected double rewardGold;
    protected double rewardExp;
    protected double baseDamage;
    protected int stunTimer = 0;
    private double speedMultiplier = 1.0;
    private int slowTimer = 0;
    private boolean isSummoned = false;
    private double originalWidth = -1;

    public Enemy(Node sprite, double x, double y, double maxHp, double speed, double rewardGold, double rewardExp , double baseDamage) {
        super(sprite, x, y, maxHp);
        this.speed = speed;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
        this.baseDamage = baseDamage;

        applyEnemyEffects();
    }

    @Override
    public void syncHpBar() {
        if (hpBar == null || sprite == null) return;

        // 不要用 getBoundsInLocal().getWidth()，因為動畫會干擾它
        // 直接根據怪物類型給予固定寬度
        double barWidth = (this instanceof BossEnemy) ? 100 : 50;

        double percent = Math.max(0, hp / maxHp);
        hpBar.setWidth(barWidth * percent);

        // 位置同步：放在怪物頭頂上 10 像素
        hpBar.setTranslateX(sprite.getTranslateX());
        hpBar.setTranslateY(sprite.getTranslateY() - 15);
        hpBarBg.setTranslateX(sprite.getTranslateX());
        hpBarBg.setTranslateY(sprite.getTranslateY() - 15);
    }

    private void applyEnemyEffects() {
        if (this.sprite instanceof Shape) {
            Shape s = (Shape) this.sprite;

            // --- 方案 A: 立體陰影 ---
            DropShadow shadow = new DropShadow();
            shadow.setRadius(10);
            shadow.setOffsetY(5);
            shadow.setColor(Color.color(0, 0, 0, 0.4));
            s.setEffect(shadow);

            // --- 方案 B: 出生彈出動畫 (Pop-in) ---
            s.setScaleX(0);
            s.setScaleY(0);
            ScaleTransition pop = new ScaleTransition(Duration.millis(300), s);
            pop.setToX(1.0);
            pop.setToY(1.0);
            pop.play();
        }
    }

    public void playHitFlash() {
        ColorAdjust flash = new ColorAdjust();
        flash.setBrightness(0.7);

        Node s = this.sprite;
        javafx.scene.effect.Effect originalEffect = s.getEffect();
        flash.setInput(originalEffect);
        s.setEffect(flash);

        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(e -> s.setEffect(originalEffect));
        pause.play();
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
    public boolean isSummoned() {return isSummoned;}

    public void setSummoned(boolean summoned) {isSummoned = summoned;}

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