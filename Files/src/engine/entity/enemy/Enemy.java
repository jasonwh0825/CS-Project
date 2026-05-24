package engine.entity.enemy;

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

    public double getRewardGold() { return rewardGold; }
    public double getRewardExp() { return rewardExp; }
}