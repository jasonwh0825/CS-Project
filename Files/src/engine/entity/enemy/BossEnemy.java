package engine.entity.enemy;

import javafx.scene.paint.Color;
import java.util.function.Consumer;
import engine.entity.Castle;
import engine.entity.Bullet;
import engine.entity.WeaponType;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

public class BossEnemy extends Enemy {

    public enum BossType {
        NORMAL,         // 原本的 Boss
        SWARM_MOTHER,   // 蜂群母體 (召喚型)
        VOID_BLINKER    // 虛空行者 (閃現型)
    }

    private BossType type;
    private int wave;
    private int actionTimer = 0;
    private Consumer<Enemy> onSpawnMinion;

    // 原本 Boss 專用變數
    private int attackCooldown = 0;
    private boolean isEnraged = false;

    public BossEnemy(double x, double y, int wave, Consumer<Enemy> onSpawnMinion) {
        super(
                new javafx.scene.shape.Rectangle(100, 100, Color.DARKRED),
                x, y,
                500 + (wave * 250),   // hp
                0.5 + (wave * 0.05),  // speed
                500 + (wave * 100),  // rewardGold
                200 + (wave * 50),   // rewardExp
                30 + (wave * 10)     // baseDamage
        );

        this.wave = wave;
        this.onSpawnMinion = onSpawnMinion;

        int randomPick = (int) (Math.random() * 3);
        this.type = BossType.values()[randomPick];

        // 根據類型初始化外觀
        switch (this.type) {
            case NORMAL:
                setSpriteColor(Color.DARKRED);
                break;
            case SWARM_MOTHER:
                setSpriteColor(Color.DARKGREEN);
                setSpriteSize(60, 60);
                break;
            case VOID_BLINKER:
                setSpriteColor(Color.PURPLE);
                setSpriteSize(60, 60);
                break;
        }
    }

    @Override
    public Bullet updateBehavior(Castle castle) {
        // 基本移動
        setY(getY() + getActualSpeed());
        actionTimer++;

        switch (type) {
            case NORMAL:
                // 執行原本 Boss 的專屬邏輯 (狂暴 + 射擊)
                return handleOriginalBossLogic(castle);

            case SWARM_MOTHER:
                // 只有這裡會產生 MeleeEnemy
                int spawnInterval = Math.max(120, 300 - (wave * 15));
                if (actionTimer >= spawnInterval) {
                    spawnMinions();
                    actionTimer = 0;
                }
                break;

            case VOID_BLINKER:
                int blinkInterval = Math.max(90, 240 - (wave * 10));
                if (actionTimer >= blinkInterval) {
                    blink();
                    actionTimer = 0;
                }
                break;
        }
        return null;
    }

    // 🎯 這是你原本 BOSS 的核心邏輯：狂暴與發射子彈
    private Bullet handleOriginalBossLogic(Castle targetCastle) {
        // 1. 狂暴判定
        if (!isEnraged && this.hp < (this.maxHp / 2)) {
            isEnraged = true;
            this.speed *= 2.0;
            setSpriteColor(Color.ORANGERED);
        }

        // 2. 攻擊計時
        attackCooldown++;
        if (attackCooldown >= 90) {
            attackCooldown = 0;
            // 射向主堡中心
            return new Bullet(
                    this.getX() + 50, this.getY() + 100,
                    this.getX(), targetCastle.getY(),
                    this.baseDamage, 4.0, true, WeaponType.NORMAL
            );
        }
        return null;
    }

    private void spawnMinions() {
        if (onSpawnMinion == null) return;
        int count = 2 + (wave / 3);
        for (int i = 0; i < count; i++) {
            double offsetX = (Math.random() - 0.5) * 120;
            double spawnX = Math.max(0, Math.min(780, this.getX() + offsetX));

            // ✅ 這裡會 Call MeleeEnemy
            Enemy minion = new MeleeEnemy(spawnX, this.getY());

            setupMinionAppearance(minion);
            minion.enhanceStats(0.5 + (wave * 0.1));
            onSpawnMinion.accept(minion);
        }
    }

    // 💡 修改這個方法來適應圓形小怪的縮小邏輯
    private void setupMinionAppearance(Enemy minion) {
        Node mNode = minion.getSprite();
        Shape mShape = null;
        if (mNode instanceof Pane && !((Pane)mNode).getChildren().isEmpty()) {
            Node firstChild = ((Pane)mNode).getChildren().get(0);
            if (firstChild instanceof Shape) mShape = (Shape) firstChild;
        } else if (mNode instanceof Shape) {
            mShape = (Shape) mNode;
        }

        if (mShape != null) {
            // 1. 雖然 MeleeEnemy 建構子是紅色，母體噴出來的我們還是強制給它淺綠色做區分
            mShape.setFill(Color.LIGHTGREEN);

            // 2. ⭐ 這裡修正：判斷如果是圓形 (Circle)，則縮小半徑
            if (mShape instanceof javafx.scene.shape.Circle) {
                // 原本半徑 15 (直徑30)，缩小的直徑設為 25，所以半徑為 12.5
                ((javafx.scene.shape.Circle) mShape).setRadius(12.5);
            }
            // 保留 Rectangle 的判斷，以防萬一以後召喚別種怪
            else if (mShape instanceof javafx.scene.shape.Rectangle) {
                ((javafx.scene.shape.Rectangle) mShape).setWidth(25);
                ((javafx.scene.shape.Rectangle) mShape).setHeight(25);
            }
        }
        minion.syncHpBar();
    }

    public String getBossName() {
        switch (type) {
            case NORMAL: return "巨獸 · 破壞者";
            case SWARM_MOTHER: return "異變蜂群 · 母體";
            case VOID_BLINKER: return "虛空行者 · 閃爍者";
            default: return "未知 Boss";
        }
    }

    private void setSpriteColor(Color color) {
        Node node = getSprite();
        if (node instanceof Pane) {
            Pane p = (Pane) node;
            if (!p.getChildren().isEmpty() && p.getChildren().get(0) instanceof Shape) {
                ((Shape) p.getChildren().get(0)).setFill(color);
            }
        } else if (node instanceof Shape) {
            ((Shape) node).setFill(color);
        }
    }

    private void setSpriteSize(double width, double height) {
        Node node = getSprite();
        javafx.scene.shape.Rectangle rect = null;
        if (node instanceof Pane) {
            Pane p = (Pane) node;
            if (!p.getChildren().isEmpty() && p.getChildren().get(0) instanceof javafx.scene.shape.Rectangle) {
                rect = (javafx.scene.shape.Rectangle) p.getChildren().get(0);
            }
        } else if (node instanceof javafx.scene.shape.Rectangle) {
            rect = (javafx.scene.shape.Rectangle) node;
        }
        if (rect != null) {
            rect.setWidth(width);
            rect.setHeight(height);
        }
        syncHpBar();
    }

    private void blink() {
        double direction = Math.random() > 0.5 ? 1 : -1;
        double newX = getX() + (direction * (100 + Math.random() * 100));
        newX = Math.max(20, Math.min(740, newX));
        setX(newX);
    }
}