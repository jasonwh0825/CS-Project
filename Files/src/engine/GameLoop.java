package engine;

import engine.entity.Bullet;
import engine.entity.Castle;
import engine.entity.enemy.*;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import static engine.entity.WeaponType.*;

public class GameLoop extends AnimationTimer {
    private Pane gamePane;
    public Castle castle;
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
    private WeaponType currentWeapon = NORMAL;
    private int currentWave = 1;

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
        this.currentWave= 1;
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
            e.takeDamage(80+castle.currentAtkDamage*6);
            e.applyStun(120);
        }
    }

    public void atklevelup(){
        castle.upgradeAttack();
    }

    public void hplevelup(){
        castle.upgradeMaxHp();
    }

    public void switchWeapon(WeaponType newWeapon) {
        this.currentWeapon = newWeapon;
        System.out.println("切換武器：" + newWeapon.getName());
    }

    public void playerShoot(double targetX, double targetY) {
        if (isGameOver) return;
        // 把最後一個參數換成 currentWeapon
        Bullet bullet = new Bullet(castle.getX() + 400, castle.getY(), targetX, targetY,
                castle.getCurrentAtkDamage() + currentWeapon.getBaseDamage(), 8.0, false, currentWeapon);
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
            Rectangle waveFlash = new Rectangle(800, 700, Color.RED);
            waveFlash.setOpacity(0.3);
            gamePane.getChildren().add(waveFlash);
            new Thread(() -> {
                try { Thread.sleep(200); } catch (InterruptedException ex) {}
                Platform.runLater(() -> gamePane.getChildren().remove(waveFlash));
            }).start();
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
                    castle.takeDamage(e.getBaseDamage());
                } else {
                    killCount++;
                    castle.addReward(e.getRewardGold(), e.getRewardExp());// 正常給予獎勵
                    if(currentWeapon.getName() == HEAL.getName() && castle.getHp()+10<=castle.getMaxHp()){
                        castle.takeDamage(-5*castle.getAtkLevel());
                    }
                }

                if (e instanceof BossEnemy) {
                    isBossActive = false;
                    currentWave++; // 【關鍵】打完 BOSS，正式進入下一波！

                    // 【特效】通關閃爍金光，提示玩家進入下一波
                    Rectangle waveFlash = new Rectangle(1000, 700, Color.GOLD);
                    waveFlash.setOpacity(0.4);
                    gamePane.getChildren().add(waveFlash);
                    new Thread(() -> {
                        try { Thread.sleep(200); } catch (InterruptedException ex) {}
                        Platform.runLater(() -> gamePane.getChildren().remove(waveFlash));
                    }).start();
                }

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
                        // 1. 造成傷害
                        e.takeDamage(b.getDamage());

                        // 2. 處理武器特殊效果
                        if (b.getWeaponType() == ICE && Math.random() < 0.3) {

                            if (e instanceof BossEnemy) {
                                continue;
                            }

                            e.applyStun(15);// 冰凍效果：麻痺 15 幀


                        }

                        else if (b.getWeaponType() == HEAVY && Math.random() < 0.2) {

                            if (e instanceof BossEnemy) {
                                continue;
                            }
                            Timeline y=new Timeline(new KeyFrame(Duration.seconds(0.01), event->{e.setY(e.getY()-1);}));
                            y.setCycleCount(30);
                            y.play();
                            Timeline x=new Timeline(new KeyFrame(Duration.seconds(0.01), event->{e.setY(e.getY());}));
                            x.setCycleCount(10);
                            x.play();

                            // 為了視覺流暢，立即更新圖案位置
                            e.updateSpritePosition();



                        }

                        // 3. 子彈撞擊後消失
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
        // 計算目前的難度倍率
        double difficulty = 1.0 + (currentWave - 1) * 0.5;

        // 確保包含了：波數、難度、金幣、經驗、能量、武器、擊殺、HP
        hudLabel.setText(String.format(
                "【 第 %d 波 】\n" +
                        " 難度: %.1f 倍\n\n" +
                        " 金幣: %.0f g\n\n" +
                        " 經驗: %.0f exp\n\n" +  // <-- 經驗值回來了！
                        " 能量: %.1f%%\n\n" +
                        " 武器: %s\n\n" +
                        " 擊殺: %d 隻\n\n" +
                        " 血量: %.0f / %.0f",
                currentWave,
                difficulty,
                castle.getGold(),
                castle.getExp(),        // 對應 castle.getExp()
                castle.getUltEnergy(),
                currentWeapon.getName(),
                killCount,
                castle.getHp(),
                castle.getMaxHp()
        ));

        // 升級按鈕邏輯保持不變
        atkUpgradeBtn.setText(String.format("升級攻擊 (Lv.%d): %.0fg", castle.getAtkLevel(), castle.getAtkUpgradeCost()));
        hpUpgradeBtn.setText(String.format("增加血量 (Lv.%d): %.0fg", castle.getHpLevel(), castle.getHpUpgradeCost()));
        atkUpgradeBtn.setDisable(castle.getGold() < castle.getAtkUpgradeCost());
        hpUpgradeBtn.setDisable(castle.getGold() < castle.getHpUpgradeCost());
    }

    private void spawnNormalEnemy() {
        double rx = Math.random() * 650 + 50;
        int type = (int)(Math.random() * 3);
        Enemy e = (type == 0) ? new MeleeEnemy(rx, 0) : (type == 1 ? new RangedEnemy(rx, 0) : new ShamanEnemy(rx, 0));

        // 直接使用 currentWave 來計算強度
        if (currentWave > 1) {
            double multiplier = 1.0 + (currentWave - 1) * 0.5;
            e.enhanceStats(multiplier);
        }

        enemies.add(e);
        gamePane.getChildren().add(e.getSprite());
    }

    private void spawnBoss() {
        isBossActive = true;
        BossEnemy boss = new BossEnemy(350, -100);

        // 第一個 BOSS 出現時 currentWave 還是 1，所以會是正常的基礎強度！
        if (currentWave > 1) {
            double multiplier = 1.0 + (currentWave - 1) * 0.8;
            boss.enhanceStats(multiplier);
        }

        enemies.add(boss);
        gamePane.getChildren().add(boss.getSprite());
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