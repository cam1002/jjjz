# 开发笔记

从 README 拆出来的详细部分：功能对照表、工程结构、Base 设计规范、统计口径、逐条设计说明与踩坑记录。
README 只保留一眼能看懂的部分，**动手改代码前先在这里查对应模块**。

## 功能对照
| 需求 | 实现 |
|---|---|
| 液态玻璃风格 | `GlassBackdrop` 彩色光斑背景 + `Modifier.liquidGlass` 半透明渐变/高光描边/柔和投影 |
| 底部悬浮 Tab | `GlassBottomBar` 悬浮玻璃胶囊：记账 / 资产 / 统计 / 设置 |
| 记账（账户胶囊 + 选择账户抽屉） | `LedgerScreen` + `AddRecordSheet`：支出/收入切换 → **当前账户一个胶囊**（默认上次用的那个）→ 金额 → 分类 → 日期 → 备注 → 保存；点胶囊弹 `AccountPickerSheet` 底部抽屉选账户 |
| 资产（银行卡/微信/支付宝等） | `AssetsScreen` + `AddAccountSheet`：资金/信用/充值/投资/债务五类账户；开户表单为全屏页 —— 「图标」区块 + 名称/余额/备注（值右对齐）+ 底部固定「保存」；底色不再手选，跟随图标品牌色，选中图标自动填写账户名称；账户行左滑出红色「删除」 |
| 资金账户固定五格 | 储蓄卡 / 微信 / 支付宝 / 现金 / 其他（顺序固定，其它类型仍按 `IconCatalog.forType` 过滤）；点「储蓄卡」进入选银行列表 |
| 储蓄卡选银行 | `BankPickerList` + `BankCatalog`（43 家主流银行，15 家「常用」+ 右侧 A–Z 索引条跳转，32 家已带官方 logo）。选中后储蓄卡那格的图标、名称、底色都换成这家银行，账户名同步自动填写 |
| 信用账户图标四格 | 信用卡 / 花呗 / 白条 / 其他（同样写死）。点「信用卡」进入同一个 `BankPickerList`（43 家，和储蓄卡共用），选中后这一格换成该银行 logo/名称，账户名自动填「××银行信用卡」 |
| 债务 / 投资账户图标格 | 债务 = **借入 / 借出 / 其他**三格（新图标 `ic_borrow_in` 红·向下入盘、`ic_lend_out` 绿·向上出盘，刻意画成镜像一对），投资 = **股票 / 基金**两格（`ic_stock` 蜡烛图 + 已有的 `ic_fund`）。债务那格的图标同时决定正负号，见下一行 |
| 债务账户正负号 | 「借入」= 欠别人的 → 存成负数、进负债、把净资产拉低；「借出」= 别人欠我的 → 正数、算进资产总额；「其他」按欠债处理。输入框永远填正数，标签跟着图标变成 借入金额 / 借出金额 / 欠债金额，符号在 `signedBalanceOf()` 一处换算 |
| 不计入账户 | 「选择账户」抽屉第一行（灰色铅笔 `ic_no_account`）。选它这笔照常记账、照常进统计，只是不动任何账户余额：`accountId = NO_ACCOUNT_ID = -1`，`adjustBalance` 拿 -1 去 UPDATE 命中不到行，天然空操作。用 -1 而不是 0 是因为 0 已被"账户删除后解绑的流水"占用，两者显示要分开（「不计入账户」vs「已删除账户」） |
| 账户详情账单统计 | `AccountDetailScreen`：点击账户打开全屏详情页，含余额主卡、年份切换、流入/流出年度合计、月度余额趋势图（`BalanceLineChart` 自研玻璃曲线风）、月度账单表格、账户备注、修改账户、「记一笔」预选该账户 |
| 账单详情页（收据样式） | `RecordDetailScreen` + `ReceiptCard`：点击记账页某笔流水进入。分类色通栏（含状态栏）+ 扇贝撕边白纸 + 压边分类徽标 + 点线明细（账户/日期/账本/备注）+ 底部三个圆钮（退款 / 修改 / 删除） |
| 退款 | 详情页点「退款」→ 该笔标记 `refunded` 并把金额原路回补账户余额（事务内完成）；标记后不再计入月概览、饼图、账户统计，列表中淡化显示「已退款」，退款按钮置灰 |
| 总资产展示 | 净资产 = 资产总额 − 负债总额，且恒等于所有账户余额之和。资产总额 = 资金+充值+投资 三类账户 + 任何正余额（借出、信用卡溢缴款）；负债总额 = 信用+债务 两类里的负余额。两条 SQL 的谓词互斥且互补，所以拆完符号后总数仍然对得上。组头金额：信用账户取相反数（欠多少显示多少），债务账户按净值原样相加（借出+借入可能互相抵消） |
| 统计（当周/当月、按类别） | `StatsScreen`：环形比例图（Canvas 自绘 `PieChart`）+ 类别占比明细条 |
| 设置 | `SettingsScreen`（第 4 个 Tab）：顶部一张数据卡「总账单数 / 记账天数」+「其他」组一行意见反馈（调起系统邮件）。按参考图去掉 VIP；账单管理、主题模式两组已注释（深色模式决定不做），代码原样留在文件里，放开注释即可回来 |
| 数据全存本地 | Room 单例数据库 `glass_ledger.db`（当前 version = 4），首次启动自动写入默认分类与账户，加字段靠手写 `MIGRATION_x_y`，老数据不丢 |

## 工程结构
```
GlassLedger/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/libs.versions.toml            # 版本目录（AGP 8.7.3 / Kotlin 2.0.21 / Compose BOM 2024.12）
└── app/src/main/
    ├── AndroidManifest.xml
    ├── res/
    │   ├── drawable/                    # 内置立体渐变矢量图标：21 个通用（储蓄卡、信用卡、微信、支付宝、现金、
    │   │                                # 花呗/白条/公交卡/饭卡/会员卡/金币/借贷/股票/基金/钱包/手机/农商行/通用卡/不计入账户）
    │   │                                # + 32 个银行卡 logo（ic_bank_*.xml，官方 SVG 批量转换，见 BankCatalog 顶部注释）
    │   │                                # 支付宝、微信取自官方 SVG，路径原样保留，只按集内惯例加了圆角底板渐变 + 高光
    │   │                                # 未收录官方 SVG 的银行（BankCatalog 里 resId = 0）自动渲染成品牌色底板 + 名称首字
    │   ├── values/                      # strings / themes / colors
    │   └── mipmap-anydpi-v26/           # 自适应启动图标
    └── java/com/glass/ledger/
        ├── GlassLedgerApp.kt            # Application，持有 Repository
        ├── MainActivity.kt              # 入口：背景 + 四页切换 + 悬浮 + 按钮 + 底栏
        ├── data/
        │   ├── Models.kt                # Account/Category/Record 实体与视图模型
        │   ├── IconCatalog.kt           # 账户图标目录（图标 id → 矢量资源 + 品牌色 + 适用类型），银行图标由 BankCatalog 注册进来
        │   ├── BankCatalog.kt           # 银行卡类图标 list：银行 id/全称/拼音首字母/品牌色/ic_bank_* 资源/是否常用，选银行列表与索引条的唯一数据源，顶部注释含 SVG→VectorDrawable 转换规则
        │   ├── LedgerRepository.kt      # 唯一读写出口（记账同步余额、退款回补、改账重放）
        │   └── local/                   # Daos.kt / AppDatabase.kt（首次种子数据 + v1→v2→v3→v4 迁移）
        └── ui/
            ├── base/                    # ★ 设计基座：GlassTokens / GlassWindow /
            │                            #   GlassPage / GlassDialog（新界面一律从这里起步）
            ├── MainViewModel.kt         # 月份流水、月概览、周/月统计聚合、单笔流水详情流、设置页（天数/笔数/主题模式）
            ├── UiUtils.kt               # 金额/日期格式化、周月区间、颜色混白
            ├── theme/                   # 液态玻璃配色与主题
            ├── components/              # LiquidGlass / GlassBackdrop / GlassBottomBar / GlassWidgets（含共享的
            │                            #   BrandTile / GlyphTile 立体渐变底板）/
            │                            # PieChart / CategoryIcons / ReceiptCard（收据纸面·点线·徽标）/
            │                            # SwipeDeleteRow（左滑露出红色删除钮的行容器）
            └── screens/                 # Ledger / Assets / Stats / Settings / AccountDetail / RecordDetail
                                         #   + 记一笔、开户（AddAccountSheet）、选银行（BankPicker）、
                                         #     选择账户抽屉（AccountPickerSheet）、账户类型选择、
                                         #     日期滚轮等面板
```

## 设计规范（Base 层，`ui/base/`）
所有界面的状态栏、玻璃质感、圆角、间距、字号都收敛到这一层，**新增界面必须复用，不允许再写死数值**。

| 文件 | 提供什么 | 用法 |
|---|---|---|
| `GlassBottomSheet.kt` | `GlassBottomSheet`（底部抽屉骨架：贴底、只圆上面两角、标题行 + 圆形关闭钮 + 细线、内容超高自己滚、首帧从下面滑进来）、`GlassSheetRow`（抽屉里的图标行：左图标 + 标题 + 右对齐的值 + 行尾细线，选中态 `Surface.Selected` 玻璃底） | 短选择列表用抽屉，别再见就开一个全屏页：`GlassBottomSheet("选择账户", onDismiss) { GlassSheetRow(...) }` |
| `GlassTokens.kt` | 唯一的设计数值来源：`CLARITY` 全局清晰度、`Corner/Elevation/Surface/Space/Type` 档位、`PageBackground/PanelBackground/BrandBrush/AccentText/BrandSoft` 颜色 | `GlassTokens.Corner.Field`、`GlassTokens.Surface.Card`、`GlassTokens.Elevation.Chip` |
| `GlassWindow.kt` | 系统栏唯一实现（沉浸式、透明状态栏/导航栏、浅色图标、禁用对比度遮罩）；`GlassDialogWindow(dimAmount, fillScreen)` 给 Dialog 窗口套用同一套，`fillScreen = true` 额外关掉 Android 15+ 的窗口避让 | Activity 在 `GlassLedgerTheme` 里调用；全屏页弹窗走 `GlassPageDialog` 自动带上，居中面板**不要**传 `fillScreen` |
| `GlassPage.kt` | `GlassPage`（页面骨架：底色渐变 + 光斑 + 状态栏留白 + 统一顶栏 + 左右留白 + 滚动/键盘避让，`topBand/topBandHeight` 可铺一条含状态栏的分类色带）、`GlassPageDialog` 的全屏版、`GlassTopBar`、`GlassIconButton`（圆钮，默认 40dp，详情页底部操作钮传 56dp）、`GlassBrandButton`（品牌渐变实心钮：FAB、「+」） | 新页面：`GlassPage(title, onBack/onClose) { ... }`；内容自己放 LazyColumn 时传 `scroll = false` |
| `GlassDialog.kt` | `GlassDialogPanel`（居中玻璃弹窗骨架：圆角卡 + 标题行 + 可选关闭 + 分隔线）、`GlassPageDialog`（全屏表单弹窗，`scroll = false` 时内容自己滚、底部按钮可以贴屏固定）、`GlassDialogOption`（弹窗选项行）、`GlassPrimaryButton`（主按钮：保存/确定，禁用态自动降级为玻璃）、`GlassFieldDivider` | 新弹窗：`GlassDialogPanel(title, onDismiss) { … }`；表单页：`GlassPageDialog(title, onClose/onBack, scroll = false) { … }` |

配套约定：

- `Modifier.liquidGlass(shape, alpha, elevation)` 是唯一玻璃实现，`alpha` 只传 `GlassTokens.Surface.*` 档位，最终透明度由 `surfaceAlpha()` + `CLARITY` 统一映射；想整体调浓/调淡只改 `GlassTokens.CLARITY`。
- 旧的 `GlassCornerLarge/Medium/Small/Pill` 只是 `GlassTokens.Corner.*` 的别名，新代码直接用 token。
- 主按钮的品牌渐变只能来自 `GlassTokens.BrandBrush`（`GlassPrimaryButton` / `GlassBrandButton` 内部使用），别再手写 `listOf(0xFF6C7CFF, 0xFF5B6EF5)`。
- 一级 Tab 页（记账/资产/统计/设置）背景由 `MainActivity` 统一提供，因此它们只加 `contentPadding`；二级页、覆盖层、全屏弹窗一律走 `GlassPage` / `GlassPageDialog`，状态栏天然一致。
- **全屏弹窗必须用 `GlassPageDialog`，不要自己开 `Dialog`。** Compose 的 Dialog 会把内容按「整屏高度」测量，再把窗口设为这个尺寸；Android 15+（本项目在 SDK 37 模拟器上复现）还会额外把窗口顶到状态栏下面，于是可见区域正好比测量高度少一个状态栏 —— 贴在底部的「保存」按钮会被顶出屏幕。修法是 `WindowManager.LayoutParams.setFitInsetsTypes(0) + setFitInsetsSides(0)`（API 35+），已封装在 `GlassDialogWindow(fillScreen = true)` 里；窗口覆盖到系统栏之后，弹窗内的 `statusBarsPadding()/navigationBarsPadding()` 也会正常生效。`setDecorFitsSystemWindows`、`FLAG_LAYOUT_IN_SCREEN`、`layoutInDisplayCutoutMode = ALWAYS` 都解决不了这个问题。

已接入 Base 层的界面：`LedgerScreen`、`AssetsScreen`、`StatsScreen`、`SettingsScreen`、`AccountDetailScreen`、`RecordDetailScreen`、`AddRecordSheet`、`AddAccountSheet`、`BankPickerList`、`AccountTypePickerDialog`、`DateTimeWheelDialog`、`GlassConfirmDialog`、`GlassBottomBar`、`GlassCard/GlassSegmented`、`MainActivity` FAB。

收据专用件（`ui/components/ReceiptCard.kt`）：`ReceiptSurface`（扇贝撕边白纸 + 压边徽标）、`ReceiptCategoryBadge`、`DottedLeader`（点线/分隔线）。它们只负责"纸"的形状（Canvas 现算路径），按钮、弹窗、页面骨架、状态栏仍然全部走 Base 层，不另立一套风格。

滚轮专用件（`ui/components/NumberWheel.kt`）：`NumberWheel(values, selectedIndex, showTwoDigits, onSelected)` 是**受控组件**，`selectedIndex` 就是外部那个值（`DateTimeWheelDialog` 传年/月/日/时/分五个状态进来），外部改了它滚轮自己滚过去 —— 点「当前日期」一次性回填后，五个滚轮会一起跳到现在。两条不能写回去的死路：滚动位置由 `rememberLazyListState` 持有，它只认首次组合的初始索引，所以程序化赋值必须显式 `animateScrollToItem`，而且**不能拿 `listState.isScrollInProgress` 当闸门**（手势和吸附动画期间它一直是 true，会变成"状态已经改了、滚轮不动"）；组件内部表示"当前居中"的派生值不能叫 `selectedIndex`，否则遮蔽同名参数，effect 变成自己和自己判等。程序化滚动与手势上报靠 `programmaticScroll` + `lastEmitted` 两个标记互斥，保证不会来回打转。

设置页（`ui/screens/SettingsScreen.kt`，第 4 个 Tab）按参考图裁过，VIP 相关内容一律没有；现在实际在屏幕上的是「数据卡 + 其他（意见反馈）」，「账务（账单管理）」和「通用（主题模式）」两组已注释掉、代码仍留在原文件里。两个数字的口径要记住：

- **记账天数**来自系统 `PackageInfo.firstInstallTime`（`MainViewModel.installEpochDay`），`今天 − 安装日 + 1`，安装当天算第 1 天。覆盖安装（`adb install -r`、Android Studio 重跑）不会重置，只有卸载重装才归零，所以它统计的是"这台设备装了这个应用多久"，不是"记了几天账"。
- **总账单数**是 `SELECT COUNT(*) FROM records`（`RecordDao.observeTotalCount`，走 Flow，记一笔/删一笔都会即时刷新）。这里**故意不排除 `refunded`** —— 这一栏读的是"一共记了多少笔"，和统计页/月概览那些 `AND refunded = 0` 的金额口径是两回事，不要把它们对齐。

入口的取舍（2026-10-17）：意见反馈是唯一真正能用的，`ACTION_SENDTO + mailto:` 调起系统邮件，正文自动带应用版本、机型和 Android 版本，本机没有邮件应用时 `catch (ActivityNotFoundException)` 退化成 Toast，收件人是 `SettingsScreen.kt` 顶部的 `FEEDBACK_MAIL`。账单管理（点了只弹 Toast）和主题模式都被注释掉了 —— **深色模式决定不做**：整套玻璃配色（`GlassTokens`、`theme/Color.kt` 的底色与光斑、白玻璃 + 深墨字）按浅色写死，真要做要么把 `InkPrimary/InkSecondary` 等 token 改成可变值 + `key(主题档)` 整树重建（省，但切换会闪断），要么上 `CompositionLocal` 两套配色（正规，但要机械替换 150 处引用并手工修 `PieChart`/`BalanceLineChart`/`ReceiptCard` 这几个在 `Canvas {}` 绘制域里取颜色的文件）。这两条路都记在这儿，以后要捡起来直接用。注销的代码用块注释原样留在 `SettingsScreen.kt` / `MainViewModel.kt` 里（Kotlin 块注释可嵌套，所以 KDoc 也能整段包住），放开注释 + 恢复那批 import 就能回来，`themeMode` 在 `glass_ledger_prefs` 里的旧值无害。

立体渐变底板从 `AccountIcon` 里抽成了 `GlassWidgets.kt` 的 `BrandTile` / `GlyphTile`（账户图标和设置页每一行的彩色图标共用同一套画法），再要一个"品牌色底板 + 白色图形"直接调它们，别复制粘贴那段渐变。

## 退款与统计口径
- `records.refunded`（Room v3 新增列，`@ColumnInfo(defaultValue = "0")` 必须写，否则 `ALTER TABLE ... DEFAULT 0` 过不了迁移校验）。
- 「退款」= 在同一个事务里把金额原路回补账户余额（支出加回、收入扣回）+ 给这条流水打标记。
- 打了标记之后，这条流水**不再计入**任何统计聚合：月概览、按分类饼图、账户详情月度流入流出、记账页每日小计（`AND refunded = 0`）。因为钱已经回到余额里，再算一次支出就会和余额对不上。
- 已退款的流水在列表里淡化显示并标「已退款」，详情页金额变灰 + 红色「已退款」胶囊，退款按钮置灰防重复。
- `applyBalance()` 对 `refunded` 的记录直接跳过：修改或删除一条已退款的流水都不再动余额，否则会把退款的那笔钱重复算一遍。

## 设计说明
- 记账时选定账户，保存后该账户余额实时扣减/入账；删除记录自动回退余额。选「不计入账户」的流水照常记账、照常进统计，只是不碰任何账户余额（`accountId = -1`，`adjustBalance` 命中不到行 = 空操作）。
- 信用账户（花呗/白条/信用卡）余额为负表示欠款，列表显示「可用额度 = 总额度 + 余额」。
- 记账页**点击**某笔流水进入账单详情页（收据样式），在详情页里退款 / 修改 / 删除；**长按**流水行仍然直接弹删除确认，方便快速删。
- 详情页的数据流直接观察这一条流水（`observeById`），所以退款、修改会即时刷新；被删除时流出发 `null`，页面自动关闭。
- 「修改」复用记一笔面板：`AddRecordSheet(editing = …)` 回填全部字段，保存走 `saveRecordEdit()`（先撤销旧流水的余额影响，再按新值重放）。
- 资产页点击账户进入账单详情页；**左滑**（或长按）账户行露出红色「删除」钮，点击弹确认框，文案就是「确定删除当前账户吗？删除后不可恢复。相关账单不再与其关联。」。删除账户**不连带删流水**：只把这些流水的 `accountId` 置 0（`records` 表本来就没有指向 `accounts` 的外键，0 是合法值），账单和统计照旧，账户一栏沿用原来的「已删除账户」兜底名。两步放在同一个 `withTransaction` 里。
- `SwipeDeleteRow` 的删除钮**高度用 `onSizeChanged` 实测行高**（不写死），两端收成半圆（`CircleShape` 作用在非正方形上就是胶囊），红渐变 + 白色垃圾桶 + 「删除」。**只在行真的拉开后才组装这一层**：玻璃行是半透明的，底下常驻一个红色块会直接透出来，每行右边都会浮出一团淡红。
- 新增账户分两步：先弹「选择账户类型」（资金/信用/充值/投资/债务，各带说明），再进入开户表单。
- **五种账户类型的图标格全部写死在 `iconSlotsOf`，不再走 `IconCatalog.forType` 过滤**：走过滤会把不相干的通用图标混进池子（充值账户里冒出「钱包」「银行卡」），投资账户里混进「金币」。现在的池子是 —— 资金 = 储蓄卡/微信/支付宝/现金/其他，信用 = 信用卡/花呗/白条/其他，充值 = 饭卡/交通卡/会员卡/其他，债务 = 借入/借出/其他，投资 = 股票/基金（就两格，想换名字直接改名称输入框）。为此 `wallet` 的 `forTypes` 去掉了 RECHARGE；`coin`（金币）、`debt`（借贷）不再出现在任何池子里，但条目保留，历史账户还要按 id 取名称/品牌色。`forType` 只剩 `iconSlotsOf` 末尾的兜底分支在用。
- **资金和信用账户的图标格都是写死的，且第一格都能点进「选择银行」**：资金 = 储蓄卡 / 微信 / 支付宝 / 现金 / 其他，信用 = 信用卡 / 花呗 / 白条 / 其他（顺序和数量都不随数据变化，截图要求）。两类共用同一份 `BankCatalog` 和同一个 `BankPickerList`：15 家常用银行在最上面，其余按拼音首字母分组，右侧 A–Z 索引条点击即滚动到对应段；43 家全都在，不区分储蓄卡/信用卡（城商行两边都能办）。选中银行后：这一格的图标换成银行 logo、底色取品牌色、名称自动填银行全称；**信用账户额外在名字后面补「信用卡」三个字**（工商银行信用卡）——只写「工商银行」在资产列表里分不清是储蓄卡还是信用卡。银行图标本身不进任何类型的图标池（`forTypes = emptySet()`），43 家铺成格子太多，但它们照样注册在 `IconCatalog` 里，历史账户按 id 取名称/品牌色。
- **32 家银行有官方 logo**（`res/drawable/ic_bank_<id>.xml`，从 iconfont 下载的官方 SVG 批量转换：工商/建设/农业/中国/交通/招商/邮储/中信/兴业/浦发/民生/平安/光大/广发/华夏/汇丰/北京/上海/盛京/江苏/宁波/青岛/天津/厦门/浙商/广州/大连/华润/德州/东营/上海农商/广州农商）。统一风格 = 品牌色立体渐变圆角底板 + 白色 logo，和集内其它图标一致。转换规则（负号补空格、配色统一描白、包围盒缩放居中、自带方块与镂空方块两类特例）写在 `BankCatalog` 顶部注释里，改图前先读那段。批量转换脚本是一次性工具，没有入库，以后拿到新 SVG 照着那三条规则手工处理即可。
- **微信图标 `ic_wechat.xml` 同样取自官方 SVG（weixin.svg）**，和银行卡走的是同一套处理：官方气泡路径原样保留（只补负号空格），外面套品牌色立体渐变底板 `#FF6ED897 → #FF3BCA72 → #FF298D50`。注意官方字形是「单气泡 + 对勾撇」，不是常见的双气泡标志。`IconCatalog` 里 wechat 的 `defaultColor` 仍保留原来的 0xFF2AAE67 作为界面强调色（和支付宝同一套做法：底板用官方色、目录色只做 UI 强调，老账户存的底色不受影响）。
- **信用卡图标 `ic_credit_card.xml`**：酒红立体渐变底板 + 白色卡正面（芯片 + 三组卡号点 + 姓名线）。和 `ic_debit_card`（橙·卡背面磁条 + ¥）刻意一个画正面、一个画背面，储蓄卡/信用卡两格并排时一眼分得清；底板用酒红（#8E1B3F）而不是正红，是因为信用那四格里白条已经是正红（#E4393C），同屏撞色会读成同一个东西。
- **图标格下方的名字不会出现省略号**：一格只有 58dp，`IconTile` 的文案规则是——带斜杠的取斜杠前那段（「农商行/银行」→「农商行」）；六字以上且以「银行」结尾的去掉「银行」二字（选中的「广州农商银行」显示成「广州农商」，目录条目 rcb 本身也已改名为「农商银行」）；剩下的才允许换两行。
- 剩下 10 家城市银行（成都/长沙/重庆/东莞/杭州/恒丰/哈尔滨/南京/苏州/中原）在 `BankCatalog` 里 `resId = 0`，`AccountIcon` 自动降级为「品牌色圆角底板 + 名称首字」，视觉上和其它立体图标同风格。以后拿到官方 SVG：放进 `res/drawable`，把那一行的 `resId` 改成 `R.drawable.ic_bank_xxx` 即可，其它代码不用动 —— 储蓄卡和信用卡两个入口都会立刻看到这家银行。
- 开户表单字段按截图只有 **名称 / 余额 / 备注**（值右对齐、同一张玻璃卡），已去掉「关联账本」和「计入总资产」——所有账户一律计入净资产。`accounts.note` 是 Room v4 新增列（`@ColumnInfo(defaultValue = "''")`，值写的是 SQL 表达式文本，漏写会过不了迁移校验），账户详情页会显示备注，详情页右上角的「修改账户」弹窗同样能改备注（名称 / 余额 / 额度 / 备注，走 `updateAccount`，不需要迁移）。
- 信用卡比资金账户多「已用额度 / 总额度」两栏，债务账户那一栏跟着图标改名（借入金额 / 借出金额 / 欠债金额），投资账户仍是「余额」，其余字段一致。
- 开户表单里**选图标 = 自动填名称 + 自动定底色**：选中「工商银行」就把账户名写成「工商银行」，底色取该图标的品牌色（`IconCatalog.colorOf`）。名称只要被手动改过就不再覆盖；把输入框清空会回到"未编辑"状态，再选图标会重新自动填。选「其他」时自动清空名称，由用户自己填，图标渲染成底色块 + 名称首字。已去掉原来的「自定义底色」九色板（`IconCatalog.palette` 已删除，`accounts.colorArgb` 字段保留，所以不需要数据库迁移）。
- 净资产 = 资产总额 − 负债总额 = 所有账户余额之和。资产总额 SQL：`type IN ('FUND','RECHARGE','INVEST') OR balance > 0`；负债总额 SQL：`type IN ('CREDIT','DEBT') AND balance < 0`。两条谓词互斥且互补（一个账户只会命中一条，且必命中一条），所以「净资产 = 资产 − 负债」这个不变式在借出记正、借入记负之后照样成立，不需要改表结构。
- 符号规则只写在一处：`signedBalanceOf(type, iconId, input)`（`Models.kt`），开户、修改账户弹窗都调它；`isPayableAccount(type, iconId)` 决定"这是欠别人的钱吗"，同时被输入框标签、组头汇总方式（`AssetsScreen` 里信用取相反数、债务按净值原样相加）复用。判断依据是**图标 id**，所以老数据不动表也能自动归位。唯一例外是**溢缴款**：欠债类账户余额为正（花呗还多了）时它是资产，「修改账户」弹窗回填原值、保存也保持正数，否则"打开就点保存"会把 +66 悄悄翻成 -66。
- **记一笔的账户一栏按截图改成"一个胶囊 + 一个抽屉"**：支出/收入切换按钮下面只显示当前账户（图标 + 名称 + 小箭头），点它弹 `AccountPickerSheet`（标题「选择账户」、右上角圆形关闭钮、第一行「不计入账户」、每个账户一行 图标+名称+右对齐余额，负数红色，行间细线，当前项加 `Surface.Selected` 玻璃底）。原来的"所有账户铺成一片胶囊"没了，日期旁边那个挤着的备注输入框也挪到日期下面单独一行（占位文案「添加备注」，仍限 30 字）。
- 默认账户 = 上次选的那个，存在 SharedPreferences（`glass_ledger_prefs` / `lastAccountId`），杀进程也记得；`MainViewModel` 因此多收一个 `prefs` 参数，Factory 用 Application 的 context 拿。上次那个账户被删了就退到列表第一个，一个账户都没有就落到「不计入账户」。因为 Room 的 `accounts` 流首帧一定是空列表，光看"列表为空"分不清"还没加载"和"真没有账户"，所以加了 `accountsLoaded` 标志，默认值只在读到过一次之后定下来。
- 底部抽屉骨架 `GlassBottomSheet` + 行组件 `GlassSheetRow` 进了 Base 层（`ui/base/GlassBottomSheet.kt`），配 `GlassTokens.Corner.BottomSheet`（只圆上面两角）。它同样要 `fillScreen = true`：让弹窗窗口铺满整屏，再在满屏 Box 里 `Alignment.BottomCenter` 把面板压到底部；窗口自己 wrap_content 会被系统垂直居中，贴不到底。高度上限用 `heightIn(max = 屏高 × 0.74)` + 内容自己滚动，`weight` 在 wrap_content 的 Column 里会塌成 0。
- 统计页饼图数据来自 SQL `GROUP BY categoryId`，类别色与记账分类一致。
