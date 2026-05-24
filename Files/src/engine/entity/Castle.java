package engine.entity;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class Castle extends Entity {
    private int accountLevel;       // 登入帳號的外部等級
    private double gold = 0;
    private double exp = 0;

    // 技能等級
    private int dmgSkillLevel = 1;
    private int hpSkillLevel = 1;

    // 武器與大招
    private WeaponType currentWeapon = WeaponType.NORMAL;
    private double ultEnergy = 0;   // 0 到 100
    private final double MAX_ULT_ENERGY = 100;

    public Castle(double x, double y, int accountLevel) {
        // 畫面下方的主堡，暫時用一個 80x50 的藍色矩形代表
        super(new Rectangle(80, 50, Color.BLUE), x, y, 100 + (accountLevel * 20));
        this.accountLevel = accountLevel;
    }

    @Override
    public void update() {
        // 大招隨時間自然緩慢恢復，或透過擊殺增加
        if (ultEnergy < MAX_ULT_ENERGY) {
            ultEnergy += 0.05; // 每一幀加一點點
        }
    }

    // 消耗金幣升級攻擊力
    public void upgradeDamage() {
        double cost = dmgSkillLevel * 50;
        if (gold >= cost) {
            gold -= cost;
            dmgSkillLevel++;
        }
    }

    // 釋放大招
    public void castUltimate() {
        if (ultEnergy >= MAX_ULT_ENERGY) {
            System.out.println("施放了大招：" + currentWeapon.getUltimateName());
            ultEnergy = 0; // 重置能量
            // TODO: 在 GameLoop 中觸發相對應的全場效果 (如全場緩速或擊退)
        }
    }

    // 切換武器
    public void switchWeapon(WeaponType type) {
        this.currentWeapon = type;
    }

    // 擊殺怪物時由外部呼叫來獲得金幣/經驗
    public void addReward(double g, double e) {
        this.gold += g;
        this.exp += e;
    }

    // Getters
    public double getGold() { return gold; }
    public double getExp() { return exp; }
    public double getUltEnergy() { return ultEnergy; }
}
