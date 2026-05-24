package engine.entity;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class Entity {
    protected Group container;       // 【修改】改用 Group 當作畫面的主要容器
    protected Node sprite;
    protected Rectangle hpBgBar;     // 血條紅底
    protected Rectangle hpFgBar;     // 血條綠色血量

    protected double x;
    protected double y;
    protected double hp;
    protected double maxHp;
    protected boolean isDead = false;

    private final double BAR_WIDTH = 40; // 血條預設寬度

    public Entity(Node sprite, double x, double y, double hp) {
        this.sprite = sprite;
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.maxHp = hp;

        // 建立血條 (紅底、綠量)
        this.hpBgBar = new Rectangle(BAR_WIDTH, 4, Color.RED);
        this.hpFgBar = new Rectangle(BAR_WIDTH, 4, Color.GREEN);

        // 將血條稍微往上移，漂浮在角色頭頂 (假設角色上方 -10 像素的位置)
        hpBgBar.setTranslateY(-10);
        hpFgBar.setTranslateY(-10);

        // 把外觀跟血條包進同一個 Group
        this.container = new Group(sprite, hpBgBar, hpFgBar);

        updateSpritePosition();
    }

    public abstract void update();

    public void takeDamage(double damage) {
        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isDead = true;
        }
        // 更新血條長度
        double healthRatio = this.hp / this.maxHp;
        hpFgBar.setWidth(BAR_WIDTH * healthRatio);
    }

    public void updateSpritePosition() {
        // 直接移動整個 Group，外觀跟血條就會一起跟著走
        container.setTranslateX(x);
        container.setTranslateY(y);
    }

    // 【修改】外部改拿 container 渲染到畫面上
    public Node getSprite() { return container; }
    public boolean isDead() { return isDead; }
    public double getX() { return x; }
    public double getY() { return y; }
}