# 玻璃记账 GlassLedger

Android 记账 APP，iOS 26/27「液态玻璃（Liquid Glass）」视觉风格。
纯本地存储（Room / SQLite），**不涉及任何后端**。

## 功能对照

| 需求 | 实现 |
|---|---|
| 液态玻璃风格 | `GlassBackdrop` 彩色光斑背景 + `Modifier.liquidGlass` 半透明渐变/高光描边/柔和投影 |
| 底部悬浮 Tab | `GlassBottomBar` 悬浮玻璃胶囊：记账 / 资产 / 统计 |
| 记账（可选支出账户） | `LedgerScreen` + `AddRecordSheet`：支出/收入、分类、账户、日期、备注 |
| 资产（银行卡/微信/支付宝等） | `AssetsScreen` + `AddAccountSheet`：资金/信用/充值/投资/债务五类账户、内置主流 APP 图标；底色不再手选，跟随图标品牌色，选中图标自动填写账户名称 |
| 账户详情账单统计 | `AccountDetailScreen`：点击账户打开全屏详情页，含余额主卡、年份切换、流入/流出年度合计、月度余额趋势图（`BalanceLineChart` 自研玻璃曲线风）、月度账单表格、修改账户、「记一笔」预选该账户 |
| 账单详情页（收据样式） | `RecordDetailScreen` + `ReceiptCard`：点击记账页某笔流水进入。分类色通栏（含状态栏）+ 扇贝撕边白纸 + 压边分类徽标 + 点线明细（账户/日期/账本/备注）+ 底部三个圆钮（退款 / 修改 / 删除） |
| 退款 | 详情页点「退款」→ 该笔标记 `refunded` 并把金额原路回补账户余额（事务内完成）；标记后不再计入月概览、饼图、账户统计，列表中淡化显示「已退款」，退款按钮置灰 |
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
    │   ├── drawable/                    # 19 个内置立体渐变矢量图标：微信、支付宝、工/建/农/中、
    │   │                                # 花呗/白条/公交卡/饭卡/会员卡/理财/金币/借贷 + 通用卡/钱包/现金/手机
    │   │                                # 支付宝来自官方 SVG（圆形品牌标，路径原样保留，只按集内惯例加了渐变+高光）
    │   ├── values/                      # strings / themes / colors
    │   └── mipmap-anydpi-v26/           # 自适应启动图标
    └── java/com/glass/ledger/
        ├── GlassLedgerApp.kt            # Application，持有 Repository
        ├── MainActivity.kt              # 入口：背景 + 三页切换 + 悬浮 + 按钮 + 底栏
        ├── data/
        │   ├── Models.kt                # Account/Category/Record 实体与视图模型
        │   ├── IconCatalog.kt           # 账户图标目录（图标 id → 矢量资源 + 品牌色 + 适用类型）
        │   ├── LedgerRepository.kt      # 唯一读写出口（记账同步余额、退款回补、改账重放）
        │   └── local/                   # Daos.kt / AppDatabase.kt（首次种子数据 + v1→v2→v3 迁移）
        └── ui/
            ├── base/                    # ★ 设计基座：GlassTokens / GlassWindow /
            │                            #   GlassPage / GlassDialog（新界面一律从这里起步）
            ├── MainViewModel.kt         # 月份流水、月概览、周/月统计聚合、单笔流水详情流
            ├── UiUtils.kt               # 金额/日期格式化、周月区间、颜色混白
            ├── theme/                   # 液态玻璃配色与主题
            ├── components/              # LiquidGlass / GlassBackdrop / GlassBottomBar / GlassWidgets /
            │                            # PieChart / CategoryIcons / ReceiptCard（收据纸面·点线·徽标）
            └── screens/                 # Ledger / Assets / Stats / AccountDetail / RecordDetail
                                         #   + 记一笔、开户、日期滚轮等面板
```

## 设计规范（Base 层，`ui/base/`）

所有界面的状态栏、玻璃质感、圆角、间距、字号都收敛到这一层，**新增界面必须复用，不允许再写死数值**。

| 文件 | 提供什么 | 用法 |
|---|---|---|
| `GlassTokens.kt` | 唯一的设计数值来源：`CLARITY` 全局清晰度、`Corner/Elevation/Surface/Space/Type` 档位、`PageBackground/PanelBackground/BrandBrush/AccentText/BrandSoft` 颜色 | `GlassTokens.Corner.Field`、`GlassTokens.Surface.Card`、`GlassTokens.Elevation.Chip` |
| `GlassWindow.kt` | 系统栏唯一实现（沉浸式、透明状态栏/导航栏、浅色图标、禁用对比度遮罩）；`GlassDialogWindow()` 给 Dialog 窗口套用同一套 | Activity 在 `GlassLedgerTheme` 里调用；全屏页弹窗自动带上 |
| `GlassPage.kt` | `GlassPage`（页面骨架：底色渐变 + 光斑 + 状态栏留白 + 统一顶栏 + 左右留白 + 滚动/键盘避让，`topBand/topBandHeight` 可铺一条含状态栏的分类色带）、`GlassPageDialog` 的全屏版、`GlassTopBar`、`GlassIconButton`（圆钮，默认 40dp，详情页底部操作钮传 56dp）、`GlassBrandButton`（品牌渐变实心钮：FAB、「+」） | 新页面：`GlassPage(title, onBack/onClose) { ... }`；内容自己放 LazyColumn 时传 `scroll = false` |
| `GlassDialog.kt` | `GlassDialogPanel`（居中玻璃弹窗骨架：圆角卡 + 标题行 + 可选关闭 + 分隔线）、`GlassPageDialog`（全屏表单弹窗）、`GlassDialogOption`（弹窗选项行）、`GlassPrimaryButton`（主按钮：保存/确定，禁用态自动降级为玻璃）、`GlassFieldDivider` | 新弹窗：`GlassDialogPanel(title, onDismiss) { ... }` |

配套约定：

- `Modifier.liquidGlass(shape, alpha, elevation)` 是唯一玻璃实现，`alpha` 只传 `GlassTokens.Surface.*` 档位，最终透明度由 `surfaceAlpha()` + `CLARITY` 统一映射；想整体调浓/调淡只改 `GlassTokens.CLARITY`。
- 旧的 `GlassCornerLarge/Medium/Small/Pill` 只是 `GlassTokens.Corner.*` 的别名，新代码直接用 token。
- 主按钮的品牌渐变只能来自 `GlassTokens.BrandBrush`（`GlassPrimaryButton` / `GlassBrandButton` 内部使用），别再手写 `listOf(0xFF6C7CFF, 0xFF5B6EF5)`。
- 一级 Tab 页（记账/资产/统计）背景由 `MainActivity` 统一提供，因此它们只加 `contentPadding`；二级页、覆盖层、全屏弹窗一律走 `GlassPage` / `GlassPageDialog`，状态栏天然一致。

已接入 Base 层的界面：`LedgerScreen`、`AssetsScreen`、`StatsScreen`、`AccountDetailScreen`、`RecordDetailScreen`、`AddRecordSheet`、`AddAccountSheet`、`AccountTypePickerDialog`、`DateTimeWheelDialog`、`GlassConfirmDialog`、`GlassBottomBar`、`GlassCard/GlassSegmented`、`MainActivity` FAB。

收据专用件（`ui/components/ReceiptCard.kt`）：`ReceiptSurface`（扇贝撕边白纸 + 压边徽标）、`ReceiptCategoryBadge`、`DottedLeader`（点线/分隔线）。它们只负责"纸"的形状（Canvas 现算路径），按钮、弹窗、页面骨架、状态栏仍然全部走 Base 层，不另立一套风格。

## 退款与统计口径

- `records.refunded`（Room v3 新增列，`@ColumnInfo(defaultValue = "0")` 必须写，否则 `ALTER TABLE ... DEFAULT 0` 过不了迁移校验）。
- 「退款」= 在同一个事务里把金额原路回补账户余额（支出加回、收入扣回）+ 给这条流水打标记。
- 打了标记之后，这条流水**不再计入**任何统计聚合：月概览、按分类饼图、账户详情月度流入流出、记账页每日小计（`AND refunded = 0`）。因为钱已经回到余额里，再算一次支出就会和余额对不上。
- 已退款的流水在列表里淡化显示并标「已退款」，详情页金额变灰 + 红色「已退款」胶囊，退款按钮置灰防重复。
- `applyBalance()` 对 `refunded` 的记录直接跳过：修改或删除一条已退款的流水都不再动余额，否则会把退款的那笔钱重复算一遍。

## 构建运行

1. Android Studio（Iguana 以上）打开本目录，Sync 后运行到真机/模拟器（minSdk 26）。
2. 首次打开若提示缺少 Gradle Wrapper，可执行 `gradle wrapper --gradle-version 8.9` 生成。
3. 命令行构建：`./gradlew :app:assembleDebug`。

## 设计说明

- 记账时选择「支出账户」，保存后该账户余额实时扣减；删除记录自动回退余额。
- 信用账户（花呗/白条/信用卡）余额为负表示欠款，列表显示「可用额度 = 总额度 + 余额」。
- 记账页**点击**某笔流水进入账单详情页（收据样式），在详情页里退款 / 修改 / 删除；**长按**流水行仍然直接弹删除确认，方便快速删。
- 详情页的数据流直接观察这一条流水（`observeById`），所以退款、修改会即时刷新；被删除时流出发 `null`，页面自动关闭。
- 「修改」复用记一笔面板：`AddRecordSheet(editing = …)` 回填全部字段，保存走 `saveRecordEdit()`（先撤销旧流水的余额影响，再按新值重放）。
- 资产页点击账户进入账单详情页，长按账户弹出删除确认（级联删除其流水）。
- 新增账户分两步：先弹「选择账户类型」（资金/信用/充值/投资/债务，各带说明），再进入开户表单；图标池按类型严格区分（微信支付宝→资金，花呗白条→信用，交通卡/饭卡/会员卡→充值，理财/金币→投资，借贷→债务）。
- 开户表单里**选图标 = 自动填名称 + 自动定底色**：选中「工商银行」就把账户名写成「工商银行」，底色取该图标的品牌色（`IconCatalog.colorOf`）。名称只要被手动改过就不再覆盖；把输入框清空会回到"未编辑"状态，再选图标会重新自动填。选「其他」时自动清空名称，由用户自己填，图标渲染成底色块 + 名称首字。已去掉原来的「自定义底色」九色板（`IconCatalog.palette` 已删除，`accounts.colorArgb` 字段保留，所以不需要数据库迁移）。
- 净资产 = 资产总额（资金+充值+投资）− 负债总额（信用+债务）。
- 统计页饼图数据来自 SQL `GROUP BY categoryId`，类别色与记账分类一致。
