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
        NORMAL,         // 多重彈幕型
        SWARM_MOTHER,   // 蜂群母體 (召喚型 + 單發子彈)
        VOID_BLINKER    // 虛空行者 (閃現型 + 單發子彈)
    }

    private BossType type;
    private int wave;

    private Consumer<Enemy> onSpawnMinion;
    private Consumer<Bullet> onShoot; // ⭐ 新增：用來發射(多發)子彈

    private int skillTimer = 0;       // 特殊技能計時器
    private int shootTimer = 0;       // 射擊計時器
    private boolean isEnraged = false;

    // ⭐ 建構子多加了一個 Consumer<Bullet>
    public BossEnemy(double x, double y, int wave, Consumer<Enemy> onSpawnMinion, Consumer<Bullet> onShoot) {
        super(
                new javafx.scene.shape.Rectangle(100, 100, Color.DARKRED),
                x, y,
                500 + (wave * 250),   // hp
                0.5 + (wave * 0.05),  // speed
                500 + (wave * 100),   // rewardGold
                200 + (wave * 50),    // rewardExp
                30 + (wave * 10)      // baseDamage
        );

        this.wave = wave;
        this.onSpawnMinion = onSpawnMinion;
        this.onShoot = onShoot; // 綁定發射子彈的通道

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

        skillTimer++;
        shootTimer++;

        // 1. 狂暴判定 (NORMAL 專屬加速)
        if (type == BossType.NORMAL && !isEnraged && this.hp < (this.maxHp / 2)) {
            isEnraged = true;
            this.speed *= 1.5;
            setSpriteColor(Color.ORANGERED);
        }

        // 2. ⭐ 共通射擊邏輯 (所有 BOSS 都會開火)
        int shootInterval = (isEnraged && type == BossType.NORMAL) ? 60 : 90; // 狂暴時射更快
        if (shootTimer >= shootInterval) {
            performShoot(castle);
            shootTimer = 0;
        }

        // 3. ⭐ 特殊技能邏輯 (大幅提高頻率)
        switch (type) {
            case SWARM_MOTHER:
                // 召喚頻率提高：最短約 1 秒 (60幀) 召喚一次
                int spawnInterval = Math.max(60, 150 - (wave * 10));
                if (skillTimer >= spawnInterval) {
                    spawnMinions();
                    skillTimer = 0;
                }
                break;

            case VOID_BLINKER:
                // 瞬移頻率提高：最短不到 1 秒 (50幀) 瞬移一次
                int blinkInterval = Math.max(50, 120 - (wave * 10));
                if (skillTimer >= blinkInterval) {
                    blink();
                    skillTimer = 0;
                }
                break;

            case NORMAL:
                break; // NORMAL 的特殊能力是多重彈幕，寫在射擊邏輯裡了
        }

        // 因為子彈由 onShoot 處理了，這裡直接回傳 null 即可
        return null;
    }

    private void performShoot(Castle targetCastle) {
        if (onShoot == null) return;

        double startX = this.getX() + 50; // 從 BOSS 中間發射
        double startY = this.getY() + 80;
        double targetX = targetCastle.getX() + 400; // 瞄準主堡中心
        double targetY = targetCastle.getY();

        if (type == BossType.NORMAL) {
            // ⭐ NORMAL BOSS: 扇形多發子彈 (1~5 發)
            int bulletCount = Math.min(1 + (wave / 2), 5);
            double spreadAngle = 0.3; // 子彈散開的角度

            for (int i = 0; i < bulletCount; i++) {
                double offset = (bulletCount == 1) ? 0 : (i - (bulletCount - 1) / 2.0) * spreadAngle;
                double angle = Math.atan2(targetY - startY, targetX - startX) + offset;

                double vx = Math.cos(angle) * 100 + startX;
                double vy = Math.sin(angle) * 100 + startY;

                onShoot.accept(new Bullet(
                        startX, startY, vx, vy,
                        this.baseDamage, 5.0 + (wave * 0.2), true, WeaponType.NORMAL
                ));
            }
        } else {
            // ⭐ 其他 BOSS: 規矩地射單發子彈
            onShoot.accept(new Bullet(
                    startX, startY, targetX, targetY,
                    this.baseDamage, 4.0, true, WeaponType.NORMAL
            ));
        }
    }

    private void spawnMinions() {
        if (onSpawnMinion == null) return;
        int count = 2 + (wave / 3);
        for (int i = 0; i < count; i++) {
            double offsetX = (Math.random() - 0.5) * 120;
            double spawnX = Math.max(0, Math.min(780, this.getX() + offsetX));

            Enemy minion = new MeleeEnemy(spawnX, this.getY());
            minion.setSummoned(true);
            setupMinionAppearance(minion);
            minion.enhanceStats(0.5 + (wave * 0.1));
            onSpawnMinion.accept(minion);
        }
    }

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
            mShape.setFill(Color.LIGHTGREEN);
            if (mShape instanceof javafx.scene.shape.Circle) {
                ((javafx.scene.shape.Circle) mShape).setRadius(12.5);
            } else if (mShape instanceof javafx.scene.shape.Rectangle) {
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