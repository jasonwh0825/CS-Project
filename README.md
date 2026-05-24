src/main/java/com/mygame/
│
├── main/
│   └── MainApp.java             # 遊戲啟動點 (切換登入/選單/戰鬥 Scene)
│
├── auth/                        # 帳號系統
│   ├── UserManager.java         # 負責讀寫 JSON/TXT 檔（檢查帳號重複、存取等級經驗）
│   └── LoginController.java     # 登入與註冊介面的 UI 邏輯
│
├── ui/                          # 遊戲介面
│   ├── MenuController.java      # 主選單、帳號資訊、排行榜 UI
│   └── GameHUD.java             # 戰鬥中左/右側的資訊看板（金幣、技能升級按鈕、冷卻時間）
│
└── engine/                      # 核心遊戲引擎與戰鬥
    ├── GameLoop.java            # 核心 AnimationTimer (每秒刷新 60 次的計時器)
    ├── CollisionManager.java    # 碰撞偵測 (子彈打中怪物、護盾範圍判定)
    └── entity/                  # 所有的遊戲角色物件
        ├── Entity.java          # 所有會動、有血量物件的「爺爺類別」
        ├── Castle.java          # 主堡 (內含武器切換、大招能量、升級邏輯)
        ├── WeaponType.java          # 子彈類別 (可區分玩家子彈、敵人重炮、雷電效果)
        └── enemy/
            ├── Enemy.java       # 所有敵人的基底類別
            ├── MeleeEnemy.java  # 近戰怪 (實作左右蛇行演算法)
            ├── RangedEnemy.java # 遠程怪 (實作走到畫面中央停下蓄力)
            └── ShieldEnemy.java # 護盾/薩滿怪 (實作搜尋周邊隊友並加盾/補血)