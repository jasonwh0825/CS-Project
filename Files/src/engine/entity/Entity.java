package engine.entity;

import javafx.scene.Node;

public abstract class Entity {
    protected Node sprite;      // 畫面上的形狀 (暫時用 Rectangle 或 Circle，美編後換 Image)
    protected double x;
    protected double y;
    protected double hp;
    protected double maxHp;
    protected boolean isDead = false;

    public Entity(Node sprite, double x, double y, double hp) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;
        updateSpritePosition();
    }

    // 每一幀由 GameLoop 呼叫，處理移動、AI 或冷卻
    public abstract void update();

    public void takeDamage(double damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isDead = true;
        }
    }

    public void updateSpritePosition() {
        sprite.setTranslateX(x);
        sprite.setTranslateY(y);
    }

    // Getters & Setters
    public Node getSprite() { return sprite; }
    public boolean isDead() { return isDead; }
    public double getX() { return x; }
    public double getY() { return y; }
}
