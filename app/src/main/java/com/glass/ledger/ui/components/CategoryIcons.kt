package com.glass.ledger.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

/** 分类图标目录：iconKey -> Material 图标 */
object CategoryIcons {
    fun of(key: String): ImageVector = when (key) {
        "restaurant" -> Icons.Filled.Restaurant
        "bus" -> Icons.Filled.DirectionsBus
        "shopping" -> Icons.Filled.ShoppingBag
        "theater" -> Icons.Filled.Theaters
        "home" -> Icons.Filled.Home
        "medical" -> Icons.Filled.LocalHospital
        "education" -> Icons.Filled.School
        "phone" -> Icons.Filled.Phone
        "gift" -> Icons.Filled.CardGiftcard
        "salary" -> Icons.Filled.Payments
        "bonus" -> Icons.Filled.WorkspacePremium
        "invest" -> Icons.Filled.TrendingUp
        else -> Icons.Filled.MoreHoriz
    }
}
