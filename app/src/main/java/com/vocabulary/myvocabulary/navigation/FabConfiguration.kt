package com.vocabulary.myvocabulary.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

sealed interface FabConfiguration{
    val isVisible: Boolean
    data class Hidden(
        override val isVisible: Boolean = false
    ): FabConfiguration

    data class FabButton(
        override val isVisible: Boolean = true,
        val icon: ImageVector,
        val iconLabelId: Int,
        val onClick: () -> Unit = {},
        val extendedLabelId: Int? = null,
        val containerColor: Color? = null
    ): FabConfiguration

    data class FabMenu(
        override val isVisible: Boolean = true,
        val expanded: Boolean = false,
        val onExpandedChange: (Boolean) -> Unit,
        val icon: ImageVector,
        val labelId: Int,
        val items: List<FabButton> = emptyList(),
    ): FabConfiguration
}