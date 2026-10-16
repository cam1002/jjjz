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
     *  储蓄卡/微信/支付宝/手机支付/钱包只能做资金账户；信用卡、花呗/白条只能做信用账户；
     *  饭卡/交通卡/会员卡属于充值账户；股票/基金属于投资账户；借入/借出属于债务账户；
     *  银行类图标不在这里列，统一由 [BankCatalog] 提供（见文件末尾）。
     *  注：五种类型在新增账户页都是写死的格子（`AddAccountSheet.iconSlotsOf`，顺序和数量都不随数据变化），
     *  [forType] 只剩兜底作用；但这里的 forTypes 标签仍要和写死的格子保持一致，别留过期信息，
     *  已经不下格子但历史账户在用的图标（金币、借贷）照样要留在目录里，否则取不到名称和底色。 */
    private val genericIcons = listOf(
        AccountIcon("debitcard", "储蓄卡", R.drawable.ic_debit_card, 0xFFF59B29.toInt(), FUND),
        AccountIcon("creditcard", "信用卡", R.drawable.ic_credit_card, 0xFFA82350.toInt(), CREDIT),
        AccountIcon("wechat", "微信", R.drawable.ic_wechat, 0xFF2AAE67.toInt(), FUND),
        AccountIcon("alipay", "支付宝", R.drawable.ic_alipay, 0xFF1677FF.toInt(), FUND),
        AccountIcon("wallet", "钱包", R.drawable.ic_wallet, 0xFFF5A623.toInt(), FUND),
        AccountIcon("cash", "现金", R.drawable.ic_cash, 0xFF34A853.toInt(), FUND + DEBT),
        AccountIcon("phone", "手机支付", R.drawable.ic_phone, 0xFF00B4D8.toInt(), FUND),
        AccountIcon("bank", "农商行/银行", R.drawable.ic_bank, 0xFF2E7D32.toInt(), FUND_CREDIT + DEBT),
        AccountIcon("card", "银行卡", R.drawable.ic_card, 0xFF5B6EF5.toInt(), ALL_TYPES),
        AccountIcon("huabei", "花呗", R.drawable.ic_huabei, 0xFF2B7FF3.toInt(), CREDIT),
        AccountIcon("baitiao", "白条", R.drawable.ic_baitiao, 0xFFE4393C.toInt(), CREDIT),
        AccountIcon("buscard", "交通卡", R.drawable.ic_buscard, 0xFFF0862B.toInt(), RECHARGE),
        AccountIcon("meal", "饭卡", R.drawable.ic_meal, 0xFFE85D75.toInt(), RECHARGE),
        AccountIcon("member", "会员卡", R.drawable.ic_member, 0xFF3A3F5C.toInt(), RECHARGE),
        AccountIcon("stock", "股票", R.drawable.ic_stock, 0xFFD6334B.toInt(), INVEST),
        AccountIcon("fund", "基金", R.drawable.ic_fund, 0xFF7B61FF.toInt(), INVEST),
        AccountIcon("coin", "金币", R.drawable.ic_coin, 0xFFE8B33A.toInt(), FUND_INVEST),
        AccountIcon("borrow_in", "借入", R.drawable.ic_borrow_in, 0xFFE2483D.toInt(), DEBT),
        AccountIcon("lend_out", "借出", R.drawable.ic_lend_out, 0xFF2BA864.toInt(), DEBT),
        AccountIcon("debt", "借贷", R.drawable.ic_debt, 0xFF6B7A99.toInt(), DEBT),
        /**
         * 「不计入账户」：记一笔时可以选它，表示这笔流水不挂任何账户、不动任何余额。
         * 它不是账户类型，所以不进任何图标池（forTypes 空），只给流水列表/详情页显示账户那一栏用；
         * 老的「借贷」图标（debt）同样留在目录里，历史账户按 id 还要取名称和底色。
         */
        AccountIcon("no_account", "不计入账户", R.drawable.ic_no_account, 0xFFAEB7C8.toInt(), emptySet())
    )

    /** 银行图标不进任何类型的图标池：43 家银行铺成格子太多，也分不清是储蓄卡还是信用卡。
     *  它们由「选择银行」全屏列表提供（资金账户点「储蓄卡」、信用账户点「信用卡」都会进去），
     *  选中后存的是银行 id，所以这里照样要注册，历史账户才能取到名称/品牌色。 */
    private val bankIcons = BankCatalog.all.map {
        AccountIcon(
            id = it.id,
            label = it.name,
            resId = it.resId,
            defaultColor = it.colorArgb,
            forTypes = emptySet()
        )
    }

    val all: List<AccountIcon> = genericIcons + bankIcons

    /** 按账户类型返回可选图标 */
    fun forType(type: AccountType): List<AccountIcon> = all.filter { type in it.forTypes }

    private val byId = all.associateBy { it.id }

    /** 矢量资源；返回 0 表示这个图标没有内置 logo（渲染成品牌色底板 + 名称首字） */
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
