package engine;

import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
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
import java.util.function.Consumer;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.scene.text.FontWeight;

import static engine.entity.WeaponType.*;

public class GameLoop extends AnimationTimer {
    private ProgressBar hpBar, energyBar;
    private Label hpNumLabel, energyNumLabel;
    private Label waveLabel, killLabel, weaponLabel, goldLabel, expLabel;
    private Consumer<Double> onReturnToMenu;
    private Pane gamePane;
    public Castle castle;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Enemy> pendingEnemies = new ArrayList<>(); // 暫存小怪避免崩潰
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
    private boolean hasSpawnedCurrentWaveBoss = false;
    private int lastShootFrame = -60; // 記錄上次射擊的幀數 (預設負數確保遊戲一開始就能馬上開火)
    private int shootCooldown = 15;// 射擊冷卻幀數 (60幀大約是一秒，15幀代表一秒最多射4發)
    private boolean isMouseShooting = false;
    private double targetMouseX = 0;
    private double targetMouseY = 0;

    public GameLoop(Pane gamePane, int accountLevel ,Consumer<Double> onReturnToMenu) {
        this.gamePane = gamePane;
        this.accountLevel = accountLevel;
        this.onReturnToMenu = onReturnToMenu;
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
        this.lastShootFrame = -shootCooldown;
    }

    public void castUltimate() {
        // 1. 檢查能量是否滿 100
        if (castle.getUltEnergy() < 100) {
            System.out.println("大招能量不足！目前: " + (int)castle.getUltEnergy());
            return;
        }

        // 2. 消耗能量
        castle.useUlt();

        // 3. 取得加成變數
        int atkLv = castle.getAtkLevel();
        double baseDmg = currentWeapon.getBaseDamage() + castle.currentAtkDamage;

        // 4. 根據當前武器施放對應大招
        switch (currentWeapon) {
            case NORMAL:
                // 【狂熱彈幕】：扇形發射大量子彈，數量隨等級上升
                int bulletCount = 10 + (atkLv * 2); // 預設 12 發起跳
                double angleStep = Math.PI / (bulletCount - 1);

                for (int i = 0; i < bulletCount; i++) {
                    // 角度從 180度(左) 到 360度(右) 呈扇形往上打
                    double angle = Math.PI + (i * angleStep);

                    // 計算子彈朝向的虛擬目標點
                    double startX = castle.getX() + 400; // 假設主堡寬 800，從正中央發射
                    double startY = castle.getY();
                    double targetX = startX + Math.cos(angle) * 100;
                    double targetY = startY + Math.sin(angle) * 100;

                    Bullet b = new Bullet(
                            startX, startY, targetX, targetY,
                            baseDmg, 12.0, false, WeaponType.NORMAL
                    );
                    bullets.add(b);
                    gamePane.getChildren().add(b.getSprite());
                }
                showWarningMessage("狂熱彈幕！發射 " + bulletCount + " 發子彈！");
                break;

            case ICE:
                // 【絕對零度】：全體凍結，控制時間隨攻擊等級上升
                int freezeDuration = 120 + (atkLv * 30); // 基礎2秒(120幀) + 每級加0.5秒
                for (Enemy e : enemies) {
                    if (e instanceof BossEnemy) {
                        e.applyStun(freezeDuration / 2);// Boss 控制時間減半
                        e.takeDamage(80+atkLv*10);
                    } else {
                        e.applyStun(freezeDuration);
                        e.takeDamage(80+atkLv*10);
                    }
                }
                showWarningMessage("絕對零度！全場凍結！");
                break;

            case HEAVY:
                // 【引力震盪波】：全體擊退
                for (Enemy e : enemies) {
                    double knockback = (e instanceof BossEnemy) ? 40 : 150; // Boss 擊退抗性
                    // 將怪物 Y 座標往上推，且不允許被推到畫面外 (最小為 0)
                    e.setY(Math.max(0, e.getY() - knockback));
                }
                showWarningMessage("引力震盪波！退避！");
                break;

            case HEAL:
                // 【緊急修復】：依等級恢復最大生命值，20% 起跳，上限 50%
                double healPercent = Math.min(0.5, 0.2 + (atkLv * 0.02));
                castle.healByPercentage(healPercent);
                showWarningMessage("緊急修復！恢復 " + (int)(healPercent * 100) + "% 生命值！");
                break;

            case FIRE:
                // 【烈焰風暴】：全體真實傷害，傷害隨攻擊等級大幅上升
                double fireDamage = 300 + (atkLv * 50);
                for (Enemy e : enemies) {
                    if (e instanceof BossEnemy) {
                        e.takeDamage(fireDamage * 0.5); // Boss 受傷減半
                    } else {
                        e.takeDamage(fireDamage);
                    }
                }
                showWarningMessage("烈焰風暴！造成 " + (int)fireDamage + " 點傷害！");
                break;

            case SPEED_DOWN:
                // 【時間泥沼】：全體極度緩速，持續時間隨等級上升
                int slowDuration = 300 + (atkLv * 60); // 基礎 5秒(300幀) + 每級加1秒
                for (Enemy e : enemies) {
                    if (e instanceof BossEnemy) {
                        e.applySlow(slowDuration / 2); // Boss 緩速時間減半
                        e.takeDamage(80+atkLv*10);
                    } else {
                        e.applySlow(slowDuration);
                        e.takeDamage(80+atkLv*10);
                    }
                }
                showWarningMessage("時間泥沼！全場動作遲緩！");
                break;
        }
    }

    public void atklevelup(){
        castle.upgradeAttack();
    }

    public void hplevelup(){
        castle.upgradeMaxHp();
    }

    // 在 GameLoop.java 裡面
    public void switchWeapon(WeaponType type) {
        if (type.isUnlocked(this.accountLevel)) {
            this.currentWeapon = type;
            System.out.println("切換武器至: " + type.getName());
        } else {
            showWarningMessage("等級不足！解鎖 " + type.getName() + " 需要 Lv." + type.getUnlockLevel());
        }
    }

    // 在畫面上顯示一個會往上飄並消失的警告文字
    public void showWarningMessage(String message) {
        Label warningLabel = new Label(message);
        warningLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        warningLabel.setTextFill(Color.RED);
        // 加一點半透明黑底讓紅字在任何背景下都能看清楚
        warningLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 10px; -fx-background-radius: 5px;");

        // 將文字大概放在畫面正中央偏上方 (戰場寬度是 800，高度 700)
        warningLabel.setLayoutX(250);
        warningLabel.setLayoutY(300);

        gamePane.getChildren().add(warningLabel);

        // 1. 漸隱動畫 (2秒內透明度從 1.0 變成 0.0)
        FadeTransition fade = new FadeTransition(Duration.seconds(2), warningLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        // 2. 位移動畫 (2秒內往上移動 50 像素)
        TranslateTransition translate = new TranslateTransition(Duration.seconds(2), warningLabel);
        translate.setByY(-50);

        // 3. 把兩個動畫綁在一起同時播放
        ParallelTransition parallelTransition = new ParallelTransition(fade, translate);

        // 4. 動畫播完後，記得把這個 Label 從畫面上徹底移除，避免吃記憶體
        parallelTransition.setOnFinished(e -> gamePane.getChildren().remove(warningLabel));

        // 播放動畫！
        parallelTransition.play();
    }

    public void playerShoot(double targetX, double targetY) {
        if (isGameOver) return;

        //檢查冷卻時間：如果間隔太短，直接 return 取消這次射擊
        if (frameCount - lastShootFrame < shootCooldown) {
            return;
        }
        //成功射擊！更新最後開火的幀數為「現在這一幀」
        lastShootFrame = frameCount;
        // 原本的射擊邏輯保持不變
        Bullet bullet = new Bullet(castle.getX() + 400, castle.getY(), targetX, targetY,
                castle.getCurrentAtkDamage() + currentWeapon.getBaseDamage(), 8.0, false, currentWeapon);
        bullets.add(bullet);
        gamePane.getChildren().add(bullet.getSprite());
    }

    // 讓外部 (MainApp) 可以更新滑鼠狀態
    public void setShootingState(boolean isShooting, double x, double y) {
        this.isMouseShooting = isShooting;
        this.targetMouseX = x;
        this.targetMouseY = y;
    }

    public void updateMousePosition(double x, double y) {
        this.targetMouseX = x;
        this.targetMouseY = y;
    }

    @Override
    public void handle(long now) {
        if (isGameOver) return;
        frameCount++;

        if (isMouseShooting) {
            playerShoot(targetMouseX, targetMouseY);
        }

        castle.passiveChargeUlt(0.1);

        // 核心修正：檢查是否達到 20 擊殺且這波還沒生過 BOSS
        if (!isBossActive && !hasSpawnedCurrentWaveBoss && killCount >= currentWave * 20) {
            spawnBoss();
            hasSpawnedCurrentWaveBoss = true; // 標記已產生，防止重複觸發

            // 紅光特效
            Rectangle waveFlash = new Rectangle(800, 700, Color.RED);
            waveFlash.setOpacity(0.3);
            gamePane.getChildren().add(waveFlash);
            new Thread(() -> {
                try { Thread.sleep(200); } catch (InterruptedException ex) {}
                Platform.runLater(() -> gamePane.getChildren().remove(waveFlash));
            }).start();
        }
        // 只有在非 BOSS 戰期間才生小怪
        else if (!isBossActive && frameCount % 100 == 0) {
            spawnNormalEnemy();
        }

        updateEnemies();
        updateBullets();
        updateUI();
        enemies.addAll(pendingEnemies);
        pendingEnemies.clear();
    }

    private void updateEnemies() {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);

            // 1. 每幀更新怪物的緩速狀態計時器
            e.updateSlow();

            // 2. 判斷死亡或是碰到主堡
            if (e.isDead() || e.getY() >= castle.getY() - 20) {
                if (e.getY() >= castle.getY() - 20) {
                    // 怪物撞到主堡，主堡扣除對應攻擊力的血量
                    castle.takeDamage(e.getBaseDamage());
                } else {
                    // 怪物被擊殺
                    killCount++;
                    castle.addReward(e.getRewardGold(), e.getRewardExp()); // 正常給予獎勵

                    // 【修正】直接比對 Enum，且確認血量未滿時才補血
                    if (currentWeapon == WeaponType.HEAL && castle.getHp() < castle.getMaxHp()) {
                        castle.takeDamage(-5 * castle.getAtkLevel()); // 補血邏輯
                    }
                }

                // 3. 處理 BOSS 死亡與波數推進特效
                if (e instanceof BossEnemy) {
                    isBossActive = false;
                    hasSpawnedCurrentWaveBoss = false;
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

                // 移除死亡的怪物
                removeEnemy(i);
                checkGameOver();
                continue;
            }

            // 4. 處理存活怪物的行動 (麻痺優先，否則正常移動攻擊)
            if (e.isStunned()) {
                e.updateStun();
            } else {
                // 這裡會呼叫 e.updateBehavior()，子類別裡的速度請配合 isSlowed() 來減速
                Bullet eb = e.updateBehavior(castle);
                if (eb != null) {
                    bullets.add(eb);
                    gamePane.getChildren().add(eb.getSprite());
                }

                // 薩滿補血技能
                if (e instanceof ShamanEnemy) {
                    ((ShamanEnemy) e).castHeal(enemies);
                }
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
                        } else if (b.getWeaponType() == HEAVY && Math.random() < 0.2) {
                            if (e instanceof BossEnemy) {
                                continue;
                            }
                            Timeline y=new Timeline(new KeyFrame(Duration.seconds(0.01), event->{e.setY(e.getY()-1);}));
                            y.setCycleCount(30);
                            y.play();
                            Timeline x=new Timeline(new KeyFrame(Duration.seconds(0.01), event->{e.setY(e.getY());}));
                            x.setCycleCount(10);
                            x.play();
                            e.updateSpritePosition();// 為了視覺流暢，立即更新圖案位置
                        } else if (b.getWeaponType() == FIRE && Math.random() < 0.5) {
                            Timeline y=new Timeline(new KeyFrame(Duration.seconds(1),event->{e.takeDamage(castle.getAtkLevel()*5);}));
                            y.setCycleCount(2);
                            y.play();
                        } else if (b.getWeaponType() == SPEED_DOWN && Math.random() < 0.3){
                            e.applySlow(120);

                            e.getSprite().setOpacity(0.7);// 視覺回饋：可以讓怪物的透明度變低或變色，讓玩家知道他被緩速了

                            // 建立一個兩秒後恢復顏色的計時器
                            Timeline recoverColor = new Timeline(new KeyFrame(Duration.seconds(2), event -> {
                                e.getSprite().setOpacity(1.0);
                            }));
                            recoverColor.play();
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

        // ⭐ 這裡多傳入一個 Lambda，用來接收 BOSS 發射出來的子彈
        BossEnemy boss = new BossEnemy(Math.random() * 600 + 100, -100, currentWave,
                (minion) -> {
                    // 處理召喚的小怪
                    pendingEnemies.add(minion);
                    gamePane.getChildren().add(minion.getSprite());
                },
                (bullet) -> {
                    // 處理發射的子彈
                    bullets.add(bullet);
                    gamePane.getChildren().add(bullet.getSprite());
                }
        );

        enemies.add(boss);
        gamePane.getChildren().add(boss.getSprite());

        showWarningMessage("⚠️ 警告！【" + boss.getBossName() + "】來襲！");
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
        // 1. 右側深色背景
        Rectangle sidebarBg = new Rectangle(200, 700, Color.web("#2c3e50"));
        sidebarBg.setX(800);
        sidebarBg.setViewOrder(-1); // ⭐ 強制推到最上層
        gamePane.getChildren().add(sidebarBg); // 確保只 add 這一此

        // 2. 左上角：血量與能量條
        VBox topBarBox = new VBox(15);
        topBarBox.setTranslateX(15);
        topBarBox.setTranslateY(15);

        // 血條 HBox
        HBox hpBox = new HBox(10);
        hpBox.setAlignment(Pos.CENTER_LEFT);
        Label hpTitle = new Label("血量");
        hpTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        hpTitle.setTextFill(Color.BLACK);

        hpBar = new ProgressBar(1.0);
        hpBar.setPrefSize(200, 20);
        hpBar.setStyle("-fx-accent: #ff4d4d; -fx-control-inner-background: #eeeeee; -fx-box-border: #000000;");

        hpNumLabel = new Label("?/?");
        hpNumLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        hpNumLabel.setTextFill(Color.BLACK);
        hpBox.getChildren().addAll(hpTitle, hpBar, hpNumLabel);

        // 能量條 HBox
        HBox energyBox = new HBox(10);
        energyBox.setAlignment(Pos.CENTER_LEFT);
        Label energyTitle = new Label("能量");
        energyTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        energyTitle.setTextFill(Color.BLACK);

        energyBar = new ProgressBar(0.0);
        energyBar.setPrefSize(200, 20);
        energyBar.setStyle("-fx-accent: #00BFFF; -fx-control-inner-background: #eeeeee; -fx-box-border: #000000;");

        energyNumLabel = new Label("?%");
        energyNumLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        energyNumLabel.setTextFill(Color.BLACK);
        energyBox.getChildren().addAll(energyTitle, energyBar, energyNumLabel);

        topBarBox.getChildren().addAll(hpBox, energyBox);
        topBarBox.setViewOrder(-1); // ⭐ 強制推到最上層
        gamePane.getChildren().add(topBarBox); // 確保只 add 這一此

        // 3. 右側欄資訊：波數、擊殺、武器、金幣、經驗
        VBox rightStatsBox = new VBox(25);
        rightStatsBox.setTranslateX(815);
        rightStatsBox.setTranslateY(30);

        waveLabel = createSidebarLabel("第 ? 波", 28);
        killLabel = createSidebarLabel("擊殺: ? 隻", 20);
        weaponLabel = createSidebarLabel("武器: ???", 20);
        goldLabel = createSidebarLabel("$: ?????", 22);
        goldLabel.setTextFill(Color.GOLD);
        expLabel = createSidebarLabel("Exp: ?????", 22);
        expLabel.setTextFill(Color.LIGHTGREEN);

        rightStatsBox.getChildren().addAll(waveLabel, killLabel, weaponLabel, goldLabel, expLabel);
        rightStatsBox.setViewOrder(-1); // ⭐ 強制推到最上層
        gamePane.getChildren().add(rightStatsBox); // 確保只 add 這一此
    }

    // 建立側邊欄文字的小幫手
    private Label createSidebarLabel(String text, int fontSize) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        lbl.setTextFill(Color.WHITE);
        return lbl;
    }

    private void initUpgradeUI() {
        HBox upgradeBox = new HBox(10);
        upgradeBox.setTranslateX(815);
        upgradeBox.setTranslateY(450);

        atkUpgradeBtn = new Button();
        hpUpgradeBtn = new Button();

        atkUpgradeBtn.setPrefSize(80, 85);
        hpUpgradeBtn.setPrefSize(80, 85);

        String btnStyle = "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8;";
        atkUpgradeBtn.setStyle(btnStyle);
        hpUpgradeBtn.setStyle(btnStyle);

        atkUpgradeBtn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        hpUpgradeBtn.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        atkUpgradeBtn.setOnAction(e -> castle.upgradeAttack());
        hpUpgradeBtn.setOnAction(e -> castle.upgradeMaxHp());
        atkUpgradeBtn.setFocusTraversable(false);
        hpUpgradeBtn.setFocusTraversable(false);

        upgradeBox.getChildren().addAll(atkUpgradeBtn, hpUpgradeBtn);
        upgradeBox.setViewOrder(-1); // ⭐ 強制推到最上層
        gamePane.getChildren().add(upgradeBox); // 確保只 add 這一此
    }

    private void updateUI() {
        // 1. 更新左上角進度條
        hpBar.setProgress(castle.getHp() / castle.getMaxHp());
        hpNumLabel.setText(String.format("%.0f/%.0f", castle.getHp(), castle.getMaxHp()));

        energyBar.setProgress(castle.getUltEnergy() / 100.0);
        energyNumLabel.setText(String.format("%.1f%%", castle.getUltEnergy()));

        // 2. 更新右側欄資訊
        waveLabel.setText(String.format("第 %d 波", currentWave));
        killLabel.setText(String.format("擊殺: %d 隻", killCount));
        weaponLabel.setText(String.format("武器:\n%s", currentWeapon.getName()));
        goldLabel.setText(String.format("$: %.0f", castle.getGold()));
        expLabel.setText(String.format("Exp: %.0f", castle.getExp()));

        // 3. 更新升級按鈕文字
        atkUpgradeBtn.setText(String.format("攻擊\nLv.%d\n$%.0f", castle.getAtkLevel(), castle.getAtkUpgradeCost()));
        hpUpgradeBtn.setText(String.format("血量\nLv.%d\n$%.0f", castle.getHpLevel(), castle.getHpUpgradeCost()));

        // 4. 判斷錢夠不夠
        atkUpgradeBtn.setDisable(castle.getGold() < castle.getAtkUpgradeCost());
        hpUpgradeBtn.setDisable(castle.getGold() < castle.getHpUpgradeCost());
    }

    private void showGameOverScreen() {
        VBox box = new VBox(25); // 稍微增加間距
        box.setAlignment(Pos.CENTER);
        // 這裡的背景遮罩維持深色半透明，這樣在白色遊戲背景上會非常醒目
        box.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        box.setPrefSize(1000, 700);

        Label l = new Label("GAME OVER");
        l.setFont(Font.font("System", FontWeight.BOLD, 60)); // 加粗並放大
        l.setTextFill(Color.RED);

        // 顯示獲得的經驗值
        Label expLabel = new Label("獲得經驗值: " + (int)castle.getExp() + " EXP");
        expLabel.setFont(Font.font("System", FontWeight.BOLD, 30));
        expLabel.setTextFill(Color.GOLD);

        // ==========================================
        // ✨ 重新設計的按鈕 (對齊開始遊戲風格)
        // ==========================================
        Button r = new Button("結算並返回首頁");

        // 設定字體 (對齊開始遊戲的 BOLD 24)
        r.setFont(Font.font("System", FontWeight.BOLD, 24));

        // 設定樣式：使用與開始遊戲相似的綠色，並加上圓角與手型游標
        // 如果你希望更像「結束」的感覺，也可以把 #4CAF50 (綠) 改成 #FF9800 (橘)
        r.setStyle(
                "-fx-background-color: #4CAF50; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-cursor: hand; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 0);"
        );

        // 設定按鈕大小 (參考開始遊戲的 180x60，因為字較多，寬度稍微拉長到 240)
        r.setPrefSize(240, 60);

        // 滑鼠懸停效果：讓按鈕互動感更好
        r.setOnMouseEntered(e -> r.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;"));
        r.setOnMouseExited(e -> r.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 15;"));

        r.setOnAction(e -> {
            this.stop();
            if (onReturnToMenu != null) {
                onReturnToMenu.accept(castle.getExp());
            }
        });

        box.getChildren().addAll(l, expLabel, r);
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