package com.glass.ledger.data

import com.glass.ledger.R

/** 账户图标目录：iconId -> 内置矢量资源。支持自定义选择。 */
object IconCatalog {

    data class AccountIcon(
        val id: String,
        val label: String,
        val resId: Int,
        val defaultColor: Int
    )

    /** 内置主流银行 / 支付 APP 图标 + 通用图标 */
    val all: List<AccountIcon> = listOf(
        AccountIcon("wechat", "微信", R.drawable.ic_wechat, 0xFF2AAE67.toInt()),
        AccountIcon("alipay", "支付宝", R.drawable.ic_alipay, 0xFF1677FF.toInt()),
        AccountIcon("icbc", "工商银行", R.drawable.ic_icbc, 0xFFC01E1E.toInt()),
        AccountIcon("ccb", "建设银行", R.drawable.ic_ccb, 0xFF0A5296.toInt()),
        AccountIcon("abc", "农业银行", R.drawable.ic_abc, 0xFF33A85C.toInt()),
        AccountIcon("boc", "中国银行", R.drawable.ic_boc, 0xFFD32626.toInt()),
        AccountIcon("bank", "农商行/银行", R.drawable.ic_bank, 0xFF2E7D32.toInt()),
        AccountIcon("card", "银行卡", R.drawable.ic_card, 0xFF5B6EF5.toInt()),
        AccountIcon("wallet", "钱包", R.drawable.ic_wallet, 0xFFF5A623.toInt()),
        AccountIcon("cash", "现金", R.drawable.ic_cash, 0xFF34A853.toInt()),
        AccountIcon("coin", "金币", R.drawable.ic_coin, 0xFFE8B33A.toInt()),
        AccountIcon("huabei", "花呗", R.drawable.ic_huabei, 0xFF2B7FF3.toInt()),
        AccountIcon("baitiao", "白条", R.drawable.ic_baitiao, 0xFFE4393C.toInt()),
        AccountIcon("fund", "理财", R.drawable.ic_fund, 0xFF7B61FF.toInt()),
        AccountIcon("phone", "手机支付", R.drawable.ic_phone, 0xFF00B4D8.toInt())
    )

    private val byId = all.associateBy { it.id }

    fun resOf(id: String): Int = (byId[id] ?: byId["card"])!!.resId

    fun labelOf(id: String): String = (byId[id] ?: byId["card"])!!.label

    /** 自定义账户可选底色 */
    val palette: List<Int> = listOf(
        0xFF5B6EF5.toInt(), 0xFF2AAE67.toInt(), 0xFF1677FF.toInt(),
        0xFFC01E1E.toInt(), 0xFFF5A623.toInt(), 0xFF7B61FF.toInt(),
        0xFFEC407A.toInt(), 0xFF00B4D8.toInt(), 0xFF78909C.toInt()
    )
}
