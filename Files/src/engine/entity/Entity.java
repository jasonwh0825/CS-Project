package engine.entity;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class Entity {
    protected Pane container;
    protected Node sprite;
    protected double x, y;
    protected double hp, maxHp;
    protected boolean isDead = false;

    // 血條組件
    protected Rectangle hpBar;
    protected Rectangle hpBarBg;

    // 在 Entity.java 裡面加入
    public void setY(double y) {
        this.y = y;
        // 重要：設定完座標後，要同步更新圖案(Sprite)的位置
        updateSpritePosition();
    }

    public void setX(double x) {
        this.x = x;
        updateSpritePosition();
    }

    public Entity(Node sprite, double x, double y, double maxHp) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.hp = maxHp;
        this.maxHp = maxHp;

        container = new Pane();
        container.getChildren().add(sprite);

        // 初始化血條背景 (灰色)
        hpBarBg = new Rectangle(sprite.getBoundsInLocal().getWidth(), 5, Color.GRAY);
        hpBarBg.setTranslateY(-10);

        // 初始化血條 (綠色)
        hpBar = new Rectangle(sprite.getBoundsInLocal().getWidth(), 5, Color.GREEN);
        hpBar.setTranslateY(-10);

        container.getChildren().addAll(hpBarBg, hpBar);
        updateSpritePosition();

        // --- 修正 Bug 1: 如果是子彈(maxHp <= 1)，隱藏血條 ---
        if (maxHp <= 1.0) {
            hpBar.setVisible(false);
            hpBarBg.setVisible(false);
        }
    }

    public void updateSpritePosition() {
        container.setTranslateX(x);
        container.setTranslateY(y);
    }

    public void takeDamage(double damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            isDead = true;
        }
        // 更新血條長度
        hpBar.setWidth((hp / maxHp) * sprite.getBoundsInLocal().getWidth());
    }

    public abstract void update();

    public Node getSprite() { return container; }
    public boolean isDead() { return isDead; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
}