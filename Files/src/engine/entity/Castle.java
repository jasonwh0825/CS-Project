package engine.entity;

import javafx.animation.Animation;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.ColorAdjust;
import javafx.animation.ScaleTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.shape.Shape;

public class Castle extends Entity {
    private double gold = 0;
    private double exp = 0;
    private double ultEnergy = 0;
    private final double MAX_ULT_ENERGY = 100.0;

    private int atkLevel = 1;
    private int hpLevel = 1;
    public double currentAtkDamage = 0.0; //現在這是增加量

    public Castle(double x, double y, int accountLevel) {
        // 修改：初始顏色給一個基礎藍，稍後會被漸層覆蓋
        super(new Rectangle(800, 50, Color.BLUE), x, y, 100 + (accountLevel * 20));
        hpBar.setVisible(false);
        hpBarBg.setVisible(false);

        applyVisualEffects();
    }

    private void applyVisualEffects() {
        if (this.sprite instanceof Shape) {
            Shape body = (Shape) this.sprite;

            // ⭐ 換成「暗岩黑」到「金屬灰」的漸層，更有厚重感
            Stop[] stops = new Stop[] {
                    new Stop(0, Color.web("#2c3e50")), // 深藍黑
                    new Stop(0.5, Color.web("#4b79a1")), // 金屬藍
                    new Stop(1, Color.web("#283e51"))  // 回到深色
            };
            LinearGradient gradient = new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE, stops);
            body.setFill(gradient);

            // ⭐ 外框線：增加一條細細的金邊，增加質感
            body.setStroke(Color.web("#f1c40f", 0.5));
            body.setStrokeWidth(2);

            // ⭐ 發光效果：換成更有壓迫感的橙色或淡紫色核心光暈
            DropShadow glow = new DropShadow();
            glow.setColor(Color.web("#f39c12", 0.6)); // 暖橘光
            glow.setRadius(30);
            glow.setSpread(0.1);
            body.setEffect(glow);

            // 呼吸動畫維持，但幅度調小一點點，看起來更沉穩
            ScaleTransition breath = new ScaleTransition(Duration.seconds(2.5), body);
            breath.setByY(0.05);
            breath.setAutoReverse(true);
            breath.setCycleCount(Animation.INDEFINITE);
            breath.play();
        }
    }

    // 獲得獎勵：僅增加數值，不觸發升級與加血
    public void addReward(double g, double e) {
        this.gold += g;
        this.exp += e;
    }

    // 大招被動蓄力
    public void passiveChargeUlt(double amount) {
        if (this.ultEnergy < MAX_ULT_ENERGY) {
            this.ultEnergy += amount;
            if (this.ultEnergy > MAX_ULT_ENERGY) this.ultEnergy = MAX_ULT_ENERGY;
        }
    }

    public void useUlt() { this.ultEnergy = 0; }


    // 依最大生命值的百分比補血
    public void healByPercentage(double percent) {
        double healAmount = this.maxHp * percent;
        this.hp += healAmount;

        // 確保補血不會超過上限
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }

        // 更新實體的血條顯示
        if (hpBar != null && maxHp > 0) {
            hpBar.setWidth(sprite.getBoundsInLocal().getWidth() * (hp / maxHp));
        }
    }

    // 商店手動升級
    public boolean upgradeAttack() {
        double cost = getAtkUpgradeCost();
        if (gold >= cost) {
            gold -= cost;
            atkLevel++;
            currentAtkDamage += 15;
            return true;
        }
        return false;
    }

    public void takeDamage(double damage) {
        this.hp -= damage;
        if (this.hp < 0) this.hp = 0;

        // --- 方案 B: 受擊閃白特效 ---
        playHitFlash();

        if (hpBar != null && maxHp > 0) {
            hpBar.setWidth(sprite.getBoundsInLocal().getWidth() * (hp / maxHp));
        }
    }

    private void playHitFlash() {
        ColorAdjust flash = new ColorAdjust();
        flash.setBrightness(0.8); // 瞬間變亮

        // 保留原本的發光效果並疊加閃白
        if (sprite.getEffect() != null) {
            flash.setInput(sprite.getEffect());
        }
        sprite.setEffect(flash);

        // 0.1 秒後恢復
        PauseTransition pause = new PauseTransition(Duration.millis(100));
        pause.setOnFinished(e -> {
            // 恢復成原本的 Glow 效果
            applyVisualEffects();
        });
        pause.play();
    }

    public boolean upgradeMaxHp() {
        double cost = getHpUpgradeCost();
        if (gold >= cost) {
            gold -= cost;
            hpLevel++;
            maxHp = Math.floor((maxHp*1.3)/10)*10;
            hp = maxHp; // 商店購買才回滿血
            return true;
        }
        return false;
    }

    public double getAtkUpgradeCost() {
        double raw = 50 * Math.pow(1.5, atkLevel - 1);
        return Math.round(raw / 10.0) * 10.0;
    }

    public double getHpUpgradeCost() {
        double raw = 80 * Math.pow(1.6, hpLevel - 1);
        return Math.round(raw / 10.0) * 10.0;
    }

    public double getGold() { return gold; }
    public double getExp() { return exp; }
    public double getUltEnergy() { return ultEnergy; }
    public double getCurrentAtkDamage() { return currentAtkDamage; }
    public int getAtkLevel() { return atkLevel; }
    public int getHpLevel() { return hpLevel; }


    @Override
    public void update() { }
}