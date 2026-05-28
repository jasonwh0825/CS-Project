package engine.entity;

public enum WeaponType {
    NORMAL("普通機槍", 15, "全螢幕彈幕"),
    ICE("冰凍雷電", 10, "全場敵人緩速 5 秒"),
    HEAVY("擊退重炮", 10, "強力震盪波（全場擊退）");

    private final String name;
    private final double baseDamage;
    private final String ultimateName;

    WeaponType(String name, double baseDamage, String ultimateName) {
        this.name = name;
        this.baseDamage = baseDamage;
        this.ultimateName = ultimateName;
    }

    public String getName() { return name; }
    public double getBaseDamage() { return baseDamage; }
    public String getUltimateName() { return ultimateName; }
}