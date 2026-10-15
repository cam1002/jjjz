# 玻璃记账 GlassLedger

Android 记账 APP，iOS 26/27「液态玻璃（Liquid Glass）」视觉风格。
纯本地存储（Room / SQLite），**不涉及任何后端**。

## 功能对照

| 需求 | 实现 |
|---|---|
| 液态玻璃风格 | `GlassBackdrop` 彩色光斑背景 + `Modifier.liquidGlass` 半透明渐变/高光描边/柔和投影 |
| 底部悬浮 Tab | `GlassBottomBar` 悬浮玻璃胶囊：记账 / 资产 / 统计 |
| 记账（可选支出账户） | `LedgerScreen` + `AddRecordSheet`：支出/收入、分类、账户、日期、备注 |
| 资产（银行卡/微信/支付宝等） | `AssetsScreen` + `AddAccountSheet`：资金/信用账户、内置主流 APP 图标、自定义 icon 与底色 |
| 总资产展示 | 净资产 = 资金账户合计 + 信用账户（负数欠款），总览卡片显示资产总额/负债总额 |
| 统计（当周/当月、按类别） | `StatsScreen`：环形比例图（Canvas 自绘 `PieChart`）+ 类别占比明细条 |
| 数据全存本地 | Room 单例数据库 `glass_ledger.db`，首次启动自动写入默认分类与账户 |

## 工程结构

```
GlassLedger/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/libs.versions.toml            # 版本目录（AGP 8.7.3 / Kotlin 2.0.21 / Compose BOM 2024.12）
└── app/src/main/
    ├── AndroidManifest.xml
    ├── res/
    │   ├── drawable/                    # 15 个内置矢量图标：微信、支付宝、工/建/农/中/农商 + 通用
    │   ├── values/                      # strings / themes / colors
    │   └── mipmap-anydpi-v26/           # 自适应启动图标
    └── java/com/glass/ledger/
        ├── GlassLedgerApp.kt            # Application，持有 Repository
        ├── MainActivity.kt              # 入口：背景 + 三页切换 + 悬浮 + 按钮 + 底栏
        ├── data/
        │   ├── Models.kt                # Account/Category/Record 实体与视图模型
        │   ├── IconCatalog.kt           # 账户图标目录 + 自定义色板
        │   ├── LedgerRepository.kt      # 唯一读写出口（记账同步余额）
        │   └── local/                   # Daos.kt / AppDatabase.kt（含首次种子数据）
        └── ui/
            ├── MainViewModel.kt         # 月份流水、月概览、周/月统计聚合
            ├── UiUtils.kt               # 金额/日期格式化、周月区间
            ├── theme/                   # 液态玻璃配色与主题
            ├── components/              # LiquidGlass / GlassBackdrop / GlassBottomBar /
            │                            # GlassWidgets / PieChart / CategoryIcons
            └── screens/                 # Ledger / Assets / Stats 三页 + 两个记账/开户面板
```

## 构建运行

1. Android Studio（Iguana 以上）打开本目录，Sync 后运行到真机/模拟器（minSdk 26）。
2. 首次打开若提示缺少 Gradle Wrapper，可执行 `gradle wrapper --gradle-version 8.9` 生成。
3. 命令行构建：`./gradlew :app:assembleDebug`。

## 设计说明

- 记账时选择「支出账户」，保存后该账户余额实时扣减；删除记录自动回退余额。
- 信用账户（花呗/白条/信用卡）余额为负表示欠款，列表显示「可用额度 = 总额度 + 余额」。
- 长按账单记录可删除；长按资产账户可删除（级联删除其流水）。
- 新增账户可从 15 个内置图标中选择，并自定义 9 色底色。
- 统计页饼图数据来自 SQL `GROUP BY categoryId`，类别色与记账分类一致。
