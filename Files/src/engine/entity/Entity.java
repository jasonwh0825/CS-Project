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

    protected Rectangle hpBar;
    protected Rectangle hpBarBg;

    public void setY(double y) {
        this.y = y;
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

        // 這裡我們先隨便給個寬度 1，反正馬上就會呼叫 syncHpBar() 來校正
        hpBarBg = new Rectangle(1, 5, Color.GRAY);
        hpBar = new Rectangle(1, 5, Color.GREEN);

        container.getChildren().addAll(hpBarBg, hpBar);


        syncHpBar();
        updateSpritePosition();

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

        syncHpBar();
    }

    // ==========================================
    // 【新增這裡】自動對齊並計算血條長度的神仙方法
    // ==========================================
    public void syncHpBar() {
        // 使用 getBoundsInParent() 可以正確抓到圓形(負座標)和方形的真實邊界
        double width = sprite.getBoundsInParent().getWidth();
        double minX = sprite.getBoundsInParent().getMinX();
        double minY = sprite.getBoundsInParent().getMinY();

        // 1. 同步背景灰條 (長度 = 怪物的寬度)
        hpBarBg.setWidth(width);
        hpBarBg.setTranslateX(minX);
        hpBarBg.setTranslateY(minY - 10); // 浮在頭上 10px

        // 2. 同步綠色血條 (長度 = 比例 * 怪物的寬度)
        hpBar.setWidth((hp / maxHp) * width);
        hpBar.setTranslateX(minX);
        hpBar.setTranslateY(minY - 10);
    }

    public abstract void update();

    public Node getSprite() { return container; }
    public boolean isDead() { return isDead; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHp() { return hp; }
    public double getMaxHp() { return maxHp; }
}