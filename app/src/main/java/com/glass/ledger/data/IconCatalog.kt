package com.glass.ledger.data

import com.glass.ledger.R

/** 账户图标目录：iconId -> 内置矢量资源。支持自定义选择，按账户类型过滤。 */
object IconCatalog {

    /** 特殊图标 id：无内置图时使用，渲染为默认底色块 + 账户名首字 */
    const val OTHER_ID = "other"

    /** 「其他」图标的兜底底色（品牌蓝紫），也是目录查不到 id 时的回退值 */
    const val DEFAULT_COLOR: Int = 0xFF5B6EF5.toInt()

    data class AccountIcon(
        val id: String,
        val label: String,
        val resId: Int,
        val defaultColor: Int,
        /** 该图标适用的账户类型 */
        val forTypes: Set<AccountType>
    )

    private val FUND = setOf(AccountType.FUND)
    private val CREDIT = setOf(AccountType.CREDIT)
    private val RECHARGE = setOf(AccountType.RECHARGE)
    private val INVEST = setOf(AccountType.INVEST)
    private val DEBT = setOf(AccountType.DEBT)
    private val FUND_CREDIT = setOf(AccountType.FUND, AccountType.CREDIT)
    private val FUND_INVEST = setOf(AccountType.FUND, AccountType.INVEST)
    private val ALL_TYPES = AccountType.entries.toSet()

    /** 内置主流银行 / 支付 APP 图标 + 通用图标。
     *  图标池按业务类型严格区分：
     *  支付类只能做资金账户；花呗/白条只能做信用账户；
     *  交通卡/饭卡/会员卡属于充值账户；基金股票属于投资账户；
     *  银行与通用卡多类型通用。 */
    val all: List<AccountIcon> = listOf(
        AccountIcon("wechat", "微信", R.drawable.ic_wechat, 0xFF2AAE67.toInt(), FUND),
        AccountIcon("alipay", "支付宝", R.drawable.ic_alipay, 0xFF1677FF.toInt(), FUND),
        AccountIcon("wallet", "钱包", R.drawable.ic_wallet, 0xFFF5A623.toInt(), FUND + RECHARGE),
        AccountIcon("cash", "现金", R.drawable.ic_cash, 0xFF34A853.toInt(), FUND + DEBT),
        AccountIcon("phone", "手机支付", R.drawable.ic_phone, 0xFF00B4D8.toInt(), FUND),
        AccountIcon("icbc", "工商银行", R.drawable.ic_icbc, 0xFFC01E1E.toInt(), FUND_CREDIT),
        AccountIcon("ccb", "建设银行", R.drawable.ic_ccb, 0xFF0A5296.toInt(), FUND_CREDIT),
        AccountIcon("abc", "农业银行", R.drawable.ic_abc, 0xFF33A85C.toInt(), FUND_CREDIT),
        AccountIcon("boc", "中国银行", R.drawable.ic_boc, 0xFFD32626.toInt(), FUND_CREDIT),
        AccountIcon("bank", "农商行/银行", R.drawable.ic_bank, 0xFF2E7D32.toInt(), FUND_CREDIT + DEBT),
        AccountIcon("card", "银行卡", R.drawable.ic_card, 0xFF5B6EF5.toInt(), ALL_TYPES),
        AccountIcon("huabei", "花呗", R.drawable.ic_huabei, 0xFF2B7FF3.toInt(), CREDIT),
        AccountIcon("baitiao", "白条", R.drawable.ic_baitiao, 0xFFE4393C.toInt(), CREDIT),
        AccountIcon("buscard", "交通卡", R.drawable.ic_buscard, 0xFFF0862B.toInt(), RECHARGE),
        AccountIcon("meal", "饭卡", R.drawable.ic_meal, 0xFFE85D75.toInt(), RECHARGE),
        AccountIcon("member", "会员卡", R.drawable.ic_member, 0xFF3A3F5C.toInt(), RECHARGE),
        AccountIcon("fund", "理财", R.drawable.ic_fund, 0xFF7B61FF.toInt(), FUND_INVEST),
        AccountIcon("coin", "金币", R.drawable.ic_coin, 0xFFE8B33A.toInt(), FUND_INVEST),
        AccountIcon("debt", "借贷", R.drawable.ic_debt, 0xFF6B7A99.toInt(), DEBT)
    )

    /** 按账户类型返回可选图标 */
    fun forType(type: AccountType): List<AccountIcon> = all.filter { type in it.forTypes }

    private val byId = all.associateBy { it.id }

    fun resOf(id: String): Int = (byId[id] ?: byId["card"])!!.resId

    fun labelOf(id: String): String = (byId[id] ?: byId["card"])!!.label

    /**
     * 图标自带的品牌色：账户底色不再让用户手选，一律跟随图标
     * （工商银行=红、微信=绿…）；「其他」或未知 id 回落到 [DEFAULT_COLOR]。
     */
    fun colorOf(id: String): Int = byId[id]?.defaultColor ?: DEFAULT_COLOR

    /** 该图标对应的默认账户名（选中图标时自动填进「账户名称」） */
    fun nameOf(id: String): String? = byId[id]?.label
}
