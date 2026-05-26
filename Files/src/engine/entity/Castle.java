package engine.entity;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Castle extends Entity {
    private double gold = 0;
    private double exp = 0; // 僅作為分數或資源累積
    private double ultEnergy = 0;
    private final double MAX_ULT_ENERGY = 100.0;

    private int atkLevel = 1;
    private int hpLevel = 1;
    private double currentAtkDamage = 15.0;

    public Castle(double x, double y, int accountLevel) {
        super(new Rectangle(800, 50, Color.BLUE), x, y, 100 + (accountLevel * 20));
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

    // 商店手動升級
    public boolean upgradeAttack() {
        double cost = getAtkUpgradeCost();
        if (gold >= cost) {
            gold -= cost;
            atkLevel++;
            currentAtkDamage += 5.0;
            return true;
        }
        return false;
    }

    public boolean upgradeMaxHp() {
        double cost = getHpUpgradeCost();
        if (gold >= cost) {
            gold -= cost;
            hpLevel++;
            maxHp += 100;
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