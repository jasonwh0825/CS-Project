package engine;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.enemy.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import java.util.ArrayList;
import java.util.List;
import engine.entity.WeaponType;

public class GameLoop extends AnimationTimer {
    private Pane gamePane;
    private Castle castle;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private Label hudLabel;
    private VBox upgradePanel;
    private Button atkUpgradeBtn, hpUpgradeBtn;
    private int frameCount = 0;
    private int killCount = 0;
    private int accountLevel;
    private boolean isGameOver = false;
    private boolean isBossActive = false;
    private WeaponType currentWeapon = WeaponType.NORMAL;

    public GameLoop(Pane gamePane, int accountLevel) {
        this.gamePane = gamePane;
        this.accountLevel = accountLevel;
        initGame();
        initHUD();
        initUpgradeUI();
    }

    private void initGame() {
        this.castle = new Castle(0, 600, accountLevel);
        gamePane.getChildren().add(castle.getSprite());
        isGameOver = false;
        isBossActive = false;
        killCount = 0;
        frameCount = 0;
    }

    public void castUltimate() {
        if (castle.getUltEnergy() < 100) return;
        castle.useUlt();
        Rectangle flash = new Rectangle(800, 700, Color.WHITE);
        flash.setOpacity(0.6);
        gamePane.getChildren().add(flash);
        new Thread(() -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            Platform.runLater(() -> gamePane.getChildren().remove(flash));
        }).start();
        for (Enemy e : enemies) {
            e.takeDamage(80);
            e.applyStun(120);
        }
    }

    public void switchWeapon(WeaponType newWeapon) {
        this.currentWeapon = newWeapon;
        System.out.println("切換武器：" + newWeapon.getName());
    }

    public void playerShoot(double targetX, double targetY) {
        if (isGameOver) return;
        // 把最後一個參數換成 currentWeapon
        Bullet bullet = new Bullet(castle.getX() + 400, castle.getY(), targetX, targetY,
                castle.getCurrentAtkDamage(), 8.0, false, currentWeapon);
        bullets.add(bullet);
        gamePane.getChildren().add(bullet.getSprite());
    }

    @Override
    public void handle(long now) {
        if (isGameOver) return;
        frameCount++;

        // 大招隨時間充能 (每幀 0.1%，約 16 秒滿)
        castle.passiveChargeUlt(0.1);

        if (!isBossActive && killCount > 0 && killCount % 20 == 0) {
            spawnBoss();
        } else if (!isBossActive && frameCount % 100 == 0) {
            spawnNormalEnemy();
        }

        updateEnemies();
        updateBullets();
        updateUI();
    }

    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            if (e.isDead() || e.getY() >= castle.getY() - 20) {
                if (e.getY() >= castle.getY() - 20) {
                    castle.takeDamage(10);
                } else {
                    killCount++;
                    castle.addReward(e.getRewardGold(), e.getRewardExp()); // 正常給予獎勵
                }
                if (e instanceof BossEnemy) isBossActive = false;
                removeEnemy(i);
                checkGameOver();
                continue;
            }

            if (e.isStunned()) {
                e.updateStun();
            } else {
                Bullet eb = e.updateBehavior(castle);
                if (eb != null) {
                    bullets.add(eb);
                    gamePane.getChildren().add(eb.getSprite());
                }
                if (e instanceof ShamanEnemy) ((ShamanEnemy) e).castHeal(enemies);
            }
        }
    }

    private void updateBullets() {
        for (int j = bullets.size() - 1; j >= 0; j--) {
            Bullet b = bullets.get(j);
            b.update();
            if (b.isDead()) {
                gamePane.getChildren().remove(b.getSprite());
                bullets.remove(j);
                continue;
            }
            if (!b.isEnemyBullet()) {
                for (Enemy e : enemies) {
                    if (b.getSprite().getBoundsInParent().intersects(e.getSprite().getBoundsInParent())) {
                        e.takeDamage(b.getDamage());
                        if (b.getWeaponType() == WeaponType.ICE && Math.random() < 0.3) {
                            e.applyStun(15);
                        }
                        b.takeDamage(999);
                        break;
                    }
                }
            } else {
                if (b.getSprite().getBoundsInParent().intersects(castle.getSprite().getBoundsInParent())) {
                    castle.takeDamage(b.getDamage());
                    b.takeDamage(999);
                    checkGameOver();
                }
            }
        }
    }

    private void updateUI() {
        // 利用 \n 讓資訊在側欄垂直整齊排列
        hudLabel.setText(String.format(
                "【 基地狀態 】\n\n" +
                        " 金幣: %.0f g\n\n" +
                        " 經驗: %.0f exp\n\n" +
                        " 武器: %s\n\n" +
                        " 能量: %.1f%%\n\n" +
                        " 擊殺: %d 隻\n\n" +
                        " 血量: %.0f / %.0f",
                castle.getGold(),
                castle.getExp(),
                currentWeapon.getName(), // 順便把當前武器名稱接上來！
                castle.getUltEnergy(),
                killCount,
                castle.getHp(),
                castle.getMaxHp()
        ));

        // 底下的升級按鈕邏輯維持不變
        atkUpgradeBtn.setText(String.format("升級攻擊 (Lv.%d): %.0fg", castle.getAtkLevel(), castle.getAtkUpgradeCost()));
        hpUpgradeBtn.setText(String.format("增加血量 (Lv.%d): %.0fg", castle.getHpLevel(), castle.getHpUpgradeCost()));
        atkUpgradeBtn.setDisable(castle.getGold() < castle.getAtkUpgradeCost());
        hpUpgradeBtn.setDisable(castle.getGold() < castle.getHpUpgradeCost());
    }

    private void spawnBoss() {
        isBossActive = true;
        BossEnemy boss = new BossEnemy(350, -100);
        enemies.add(boss);
        gamePane.getChildren().add(boss.getSprite());
    }

    private void spawnNormalEnemy() {
        double rx = Math.random() * 650 + 50; // 限制在 50~700 之間
        int type = (int)(Math.random() * 3);
        Enemy e = (type == 0) ? new MeleeEnemy(rx, 0) : (type == 1 ? new RangedEnemy(rx, 0) : new ShamanEnemy(rx, 0));

        // 計算目前是第幾波 (0~19隻是第1波，20~39隻是第2波...)
        int wave = (killCount / 20) + 1;
        if (wave > 1) {
            // 每多一波，小兵屬性增加 50% (例如第 2 波 1.5 倍，第 3 波 2.0 倍)
            double multiplier = 1.0 + (wave - 1) * 0.5;
            e.enhanceStats(multiplier);
        }

        enemies.add(e);
        gamePane.getChildren().add(e.getSprite());
    }

    private void removeEnemy(int index) {
        gamePane.getChildren().remove(enemies.get(index).getSprite());
        enemies.remove(index);
    }

    private void checkGameOver() {
        if (castle.isDead() && !isGameOver) {
            isGameOver = true;
            this.stop();
            showGameOverScreen();
        }
    }

    private void initHUD() {
        Rectangle sidebarBg = new Rectangle(200, 700, Color.web("#2c3e50")); // 深灰色側欄背景
        sidebarBg.setX(800);
        gamePane.getChildren().add(sidebarBg);
        hudLabel = new Label();
        hudLabel.setFont(new Font(16)); // 16 號字大小剛剛好
        hudLabel.setTranslateX(815);    // 移到右側側欄起點
        hudLabel.setTranslateY(20);     // 從上方 20 像素開始往下排
        hudLabel.setTextFill(Color.WHITE); // 如果你加了深色側欄背景，字體改白色會很清晰
        gamePane.getChildren().add(hudLabel);
    }

    private void initUpgradeUI() {
        upgradePanel = new VBox(20);
        upgradePanel.setTranslateX(810);
        upgradePanel.setTranslateY(350);

        atkUpgradeBtn = new Button();
        hpUpgradeBtn = new Button();
        atkUpgradeBtn.setPrefWidth(180);
        hpUpgradeBtn.setPrefWidth(180);

        atkUpgradeBtn.setOnAction(e -> castle.upgradeAttack());
        hpUpgradeBtn.setOnAction(e -> castle.upgradeMaxHp());

        // 【重點檢查】：確保只有下面這一行在添加按鈕，不要有其他的 addAll 或是 add
        Label titleLabel = new Label("--- 基地強化 ---");
        titleLabel.setTextFill(Color.WHITE); // 如果背景是深色，字體可以改白色
        upgradePanel.getChildren().addAll(titleLabel, atkUpgradeBtn, hpUpgradeBtn);

        gamePane.getChildren().add(upgradePanel);

        atkUpgradeBtn.setFocusTraversable(false);
        hpUpgradeBtn.setFocusTraversable(false);
    }

    private void showGameOverScreen() {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
        box.setPrefSize(800, 700);
        Label l = new Label("GAME OVER");
        l.setFont(new Font(50)); l.setTextFill(Color.RED);
        Button r = new Button("重新開始");
        r.setOnAction(e -> restartGame(box));
        box.getChildren().addAll(l, r);
        gamePane.getChildren().add(box);
    }

    private void restartGame(VBox box) {
        gamePane.getChildren().remove(box);
        for (Enemy e : enemies) gamePane.getChildren().remove(e.getSprite());
        for (Bullet b : bullets) gamePane.getChildren().remove(b.getSprite());
        gamePane.getChildren().remove(castle.getSprite());
        enemies.clear(); bullets.clear();
        initGame();
        this.start();
    }
}