package engine.entity;

public enum WeaponType {
    // 名稱, 基礎傷害, 大招名稱, 【解鎖等級】
    NORMAL("普通機槍", 15, "全螢幕轟炸", 1),
    SPEED_DOWN("緩速槍", 10, "緩速風暴", 3),
    ICE("冰凍雷電", 10, "極寒地獄", 8),
    HEAVY("擊退重炮", 10, "震盪轟擊", 12),
    HEAL("治療槍", 10, "生命回復", 16),
    FIRE("火焰槍", 10, "末日烈焰", 20);


    private final String name;
    private final double baseDamage;
    private final String ultimateName;
    private final int unlockLevel; // 新增變數

    WeaponType(String name, double baseDamage, String ultimateName, int unlockLevel) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.ultimateName = ultimateName;
        this.unlockLevel = unlockLevel;
    }

    public String getName() { return name; }
    public double getBaseDamage() { return baseDamage; }
    public String getUltimateName() { return ultimateName; }
    public int getUnlockLevel() { return unlockLevel; } // 新增 Getter

    // 新增：便利方法，用來判斷目前等級是否已解鎖此武器
    public boolean isUnlocked(int currentLevel) {
        return currentLevel >= this.unlockLevel;
    }
}