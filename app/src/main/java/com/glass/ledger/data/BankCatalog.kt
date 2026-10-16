package com.glass.ledger.data

import com.glass.ledger.R

/**
 * 银行目录 —— 「银行卡类」图标的唯一数据源。
 *
 * 开户表单里选「储蓄卡 / 信用卡」后进入的选银行列表、资产页的账户行、账单里的账户标签，
 * 显示的都是这里登记的银行；[IconCatalog] 会把 [all] 整个注册成账户图标池，
 * 所以已保存的账户（iconId = "cmb" 之类）在任意页面都能取到名称与品牌色，不需要动数据库。
 *
 * 一、图标长什么样
 * 每家银行的官方 logo 都转成了 res/drawable/ic_bank_<id>.xml，风格与集内其它图标一致：
 * 24dp 方图 = 立体渐变圆角底板（品牌色 浅→原色→深 斜向渐变 + 顶部白色高光）+ 白色 logo 主体。
 *
 * 二、这些 ic_bank_*.xml 是怎么来的（批量转换规则，改图前先读这段）
 * 源文件是 iconfont 银行卡图标集里的官方 SVG（viewBox 1325x1024，单色/双色 path 为主），
 * 转换时只做了三件事，几何一律原样保留：
 * 1. 负号前补空格。Android 的 PathParser 把 '-' 也算作数字字符，`l9-3-2` 这种粘连写法会被
 *    解析成错误的数字，所以 pathData 里每个紧跟在数字或 '.' 后面的 '-' 之前都必须有空格。
 * 2. 配色统一。非白色的 path 一律描成白色（多色 logo 的浅色层也是白色，靠镂空区分层次）；
 *    原图里本来就是白色的 path 是「打在品牌色上的镂空」，改描成底板色才看得出形状。
 *    例外：两层都是「墨」的双色图形（宁波银行那朵花是深橙瓣 + 浅金瓣拼的）全描白会糊成白饼，
 *    深色那层改用 40% 白（#66FFFFFF），把原图的双色层次留住。
 * 3. 摆放。取全部 path 的包围盒，等比缩放居中到 100x100 视口里 74% 的安全框，
 *    缩放平移写在 <group> 上，path 本身不重算，方便日后回源比对。
 * 另外两类特例：原图自带一块占满包围盒的品牌色方块（大连、厦门）时，这块直接丢掉，由玻璃底板代替；
 * 官方 logo 本身就是「实心方块 + 镂空字」（盛京、天津、青岛、华润）时没法两层反色，
 * 整体描白并把安全框缩到 62%，让它读起来是底板里的一枚徽标而不是一块白瓷砖。
 *
 * 三、以后怎么加一家银行
 * 1. 把官方 SVG 转成 ic_bank_<新id>.xml 放进 res/drawable（照上面三条规则）；
 * 2. 在 [all] 里加一行，colorArgb 直接用该 SVG 里 path 的 fill 色，letter 填拼音首字母；
 * 3. 不用改任何 UI 代码。拿不到官方 SVG 的银行就填 resId = 0，
 *    会自动渲染成「品牌色底板 + 名称首字」，同样能选、能存、能显示。
 */
data class Bank(
    val id: String,
    /** 银行全称，选中后直接作为默认账户名 */
    val name: String,
    /** 拼音首字母，用于列表右侧 A–Z 索引分组 */
    val letter: String,
    /** 品牌色（ARGB），作为账户底色；有官方图标时与 ic_bank_$id 底板的中段色一致 */
    val colorArgb: Int,
    /** 银行卡矢量图标资源（ic_bank_*.xml），0 = 用底板 + 首字占位 */
    val resId: Int = 0,
    /** 归入「常用」分组（同时不再出现在 A–Z 段里） */
    val common: Boolean = false
)

object BankCatalog {

    /**
     * 主流银行：常用在前，其余按拼音首字母排。
     *
     * 前 21 家 + 汇丰 / 华润 / 德州 / 东营 / 上海农商 / 广州农商 共 32 家已收录官方 logo
     * （[resId] 指向 ic_bank_*.xml）；剩下的城市银行暂时用底板 + 首字占位。
     * 品牌色取自各行 VI 主色，与转换后的底板中段色保持一致，占位渲染时颜色不会跳。
     */
    val all: List<Bank> = listOf(
        // ---- 常用：六大行 + 全国性股份制商业银行 ----
        Bank("icbc", "工商银行", "G", 0xFFC42B25.toInt(), R.drawable.ic_bank_icbc, common = true),
        Bank("ccb", "建设银行", "J", 0xFF004F9C.toInt(), R.drawable.ic_bank_ccb, common = true),
        Bank("abc", "农业银行", "N", 0xFF009C96.toInt(), R.drawable.ic_bank_abc, common = true),
        Bank("boc", "中国银行", "Z", 0xFFAF2434.toInt(), R.drawable.ic_bank_boc, common = true),
        Bank("bocom", "交通银行", "J", 0xFF00367A.toInt(), R.drawable.ic_bank_bocom, common = true),
        Bank("cmb", "招商银行", "Z", 0xFFE50012.toInt(), R.drawable.ic_bank_cmb, common = true),
        Bank("psbc", "邮储银行", "Y", 0xFF007E3E.toInt(), R.drawable.ic_bank_psbc, common = true),
        Bank("citic", "中信银行", "Z", 0xFFD7000F.toInt(), R.drawable.ic_bank_citic, common = true),
        Bank("cib", "兴业银行", "X", 0xFF004187.toInt(), R.drawable.ic_bank_cib, common = true),
        Bank("spdb", "浦发银行", "P", 0xFF074195.toInt(), R.drawable.ic_bank_spdb, common = true),
        Bank("cmbc", "民生银行", "M", 0xFF1D2088.toInt(), R.drawable.ic_bank_cmbc, common = true),
        Bank("pab", "平安银行", "P", 0xFFEC681B.toInt(), R.drawable.ic_bank_pab, common = true),
        Bank("ceb", "光大银行", "G", 0xFF6D1687.toInt(), R.drawable.ic_bank_ceb, common = true),
        Bank("gdb", "广发银行", "G", 0xFFBF0017.toInt(), R.drawable.ic_bank_gdb, common = true),
        Bank("hxb", "华夏银行", "H", 0xFFE2301F.toInt(), R.drawable.ic_bank_hxb, common = true),

        // ---- 城市银行 / 农商行 / 外资行，按拼音首字母 ----
        Bank("bcb", "北京银行", "B", 0xFFE3131B.toInt(), R.drawable.ic_bank_bcb),
        Bank("bocd", "成都银行", "C", 0xFFE4002B.toInt()),
        Bank("cbb", "长沙银行", "C", 0xFF005BAA.toInt()),
        Bank("cqcb", "重庆银行", "C", 0xFF1B4994.toInt()),
        Bank("dlcb", "大连银行", "D", 0xFFC62723.toInt(), R.drawable.ic_bank_dlcb),
        Bank("drcb", "东莞银行", "D", 0xFF005BAA.toInt()),
        Bank("dzcb", "德州银行", "D", 0xFFE50012.toInt(), R.drawable.ic_bank_dzcb),
        Bank("dycb", "东营银行", "D", 0xFFA30000.toInt(), R.drawable.ic_bank_dycb),
        Bank("gzcb", "广州银行", "G", 0xFF008BD5.toInt(), R.drawable.ic_bank_gzcb),
        Bank("gzrcb", "广州农商银行", "G", 0xFFB69600.toInt(), R.drawable.ic_bank_gzrcb),
        Bank("hzcb", "杭州银行", "H", 0xFF00559C.toInt()),
        Bank("hfb", "恒丰银行", "H", 0xFFC8102E.toInt()),
        Bank("hrbb", "哈尔滨银行", "H", 0xFF005BAA.toInt()),
        Bank("hsbc", "汇丰银行", "H", 0xFFE7290F.toInt(), R.drawable.ic_bank_hsbc),
        Bank("crb", "华润银行", "H", 0xFFD68E18.toInt(), R.drawable.ic_bank_crb),
        Bank("jsb", "江苏银行", "J", 0xFF005BAC.toInt(), R.drawable.ic_bank_jsb),
        Bank("njcb", "南京银行", "N", 0xFF004A99.toInt()),
        Bank("nbb", "宁波银行", "N", 0xFFDB8635.toInt(), R.drawable.ic_bank_nbb),
        // 兜底条目：不确定具体是哪家农商/城商行时选它，名字用四字的通用叫法，
        // 选中后落到开户表单那一格（58dp）也不会被截断
        Bank("rcb", "农商银行", "N", 0xFF2E7D32.toInt(), R.drawable.ic_bank),
        Bank("qdcb", "青岛银行", "Q", 0xFFD8353B.toInt(), R.drawable.ic_bank_qdcb),
        Bank("shb", "上海银行", "S", 0xFF293C8F.toInt(), R.drawable.ic_bank_shb),
        Bank("shrcb", "上海农商银行", "S", 0xFF074195.toInt(), R.drawable.ic_bank_shrcb),
        Bank("sib", "盛京银行", "S", 0xFFE51F1F.toInt(), R.drawable.ic_bank_sib),
        Bank("szb", "苏州银行", "S", 0xFF005BAA.toInt()),
        Bank("tcb", "天津银行", "T", 0xFF005BAB.toInt(), R.drawable.ic_bank_tcb),
        Bank("xmb", "厦门银行", "X", 0xFF005C9C.toInt(), R.drawable.ic_bank_xmb),
        Bank("zhb", "浙商银行", "Z", 0xFFBD9B0B.toInt(), R.drawable.ic_bank_zhb),
        Bank("zyb", "中原银行", "Z", 0xFFE60012.toInt())
    )

    /** 「常用」段：按声明顺序展示 */
    val common: List<Bank> = all.filter { it.common }

    /** A–Z 段：字母 -> 该字母下的银行（保持声明顺序） */
    val sections: List<Pair<String, List<Bank>>> = all.filterNot { it.common }
        .groupBy { it.letter }
        .toSortedMap()
        .map { (letter, list) -> letter to list }

    /** 索引条：常用 + 实际出现的字母 */
    val indexes: List<String> = listOf("常") + sections.map { it.first }

    private val byId = all.associateBy { it.id }

    fun of(id: String): Bank? = byId[id]

    fun isBank(id: String): Boolean = byId.containsKey(id)
}
