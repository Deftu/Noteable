@file:OptIn(ExperimentalOneConfigUiApi::class)

package dev.deftu.noteable.gui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.polyfrost.oneconfig.api.ui.v1.ExperimentalOneConfigUiApi
import org.polyfrost.oneconfig.api.ui.v1.components.Icon
import org.polyfrost.oneconfig.api.ui.v1.components.Text
import org.polyfrost.oneconfig.api.ui.v1.components.onClick
import org.polyfrost.oneconfig.api.ui.v1.components.rememberInteractionSource
import org.polyfrost.oneconfig.api.ui.v1.theme.OneConfigTheme

// The design system defines no destructive token, so this is its documented fallback
val DANGER = Color(0xFFC0392B)

enum class ButtonHierarchy { PRIMARY, SECONDARY }

/** The design system's small (30px) button: primary fills with the accent, secondary sits on the component surface */
@Composable
fun SmallButton(
    label: String,
    icon: String?,
    modifier: Modifier = Modifier,
    hierarchy: ButtonHierarchy = ButtonHierarchy.SECONDARY,
    destructive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val theme = OneConfigTheme.current
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val fillColor = if (destructive) DANGER else OneConfigTheme.accent

    val background by animateColorAsState(
        when (hierarchy) {
            // Hover raises a filled button by lifting it towards its text colour
            ButtonHierarchy.PRIMARY -> if (hovered && enabled) lerp(fillColor, theme.accentTextColor, 0.12f) else fillColor
            ButtonHierarchy.SECONDARY -> theme.componentBackground
        },
    )
    val border by animateColorAsState(
        when {
            hierarchy == ButtonHierarchy.PRIMARY -> Color.Transparent
            hovered && enabled -> theme.textColorSecondary.copy(alpha = 0.6f)
            else -> theme.borderColor
        },
    )
    val foreground = when {
        hierarchy == ButtonHierarchy.PRIMARY -> theme.accentTextColor
        destructive -> DANGER
        else -> theme.textColor
    }.let { if (enabled) it else it.copy(alpha = 0.5f) }

    Row(
        modifier
            .height(30.dp)
            .background(background, theme.buttonShape)
            .border(1.dp, border, theme.buttonShape)
            .hoverable(interaction)
            .let { if (enabled) it.pointerHoverIcon(PointerIcon.Hand) else it }
            .onClick(interaction, enabled, onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) Icon(icon, foreground, Modifier.size(16.dp))
        Text(label, Modifier, foreground, 12.sp, FontWeight.Medium)
    }
}

/** The design system's small icon-only button: a 30px square on the component surface, matching [SmallButton]'s height */
@Composable
fun IconOnlyButton(icon: String, modifier: Modifier = Modifier, selected: Boolean = false, onClick: () -> Unit) {
    val theme = OneConfigTheme.current
    val interaction = rememberInteractionSource()
    val hovered by interaction.collectIsHoveredAsState()
    val background by animateColorAsState(if (selected) OneConfigTheme.accent else theme.componentBackground)
    val border by animateColorAsState(
        when {
            selected -> Color.Transparent
            hovered -> theme.textColorSecondary.copy(alpha = 0.6f)
            else -> theme.borderColor
        },
    )
    val foreground = when {
        selected -> theme.accentTextColor
        hovered -> theme.textColor
        else -> theme.textColor.copy(alpha = 0.7f)
    }
    Box(
        modifier
            .size(30.dp)
            .background(background, theme.buttonShape)
            .border(1.dp, border, theme.buttonShape)
            .hoverable(interaction)
            .pointerHoverIcon(PointerIcon.Hand)
            .onClick(interaction, onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, foreground, Modifier.size(16.dp))
    }
}

/** The design system's Dark Tag: a fixed 18px capsule for status, never an action */
@Composable
fun StatusTag(label: String, modifier: Modifier = Modifier) {
    // The XS radius step, which the Minecraft themes collapse to square corners like every other radius
    val theme = OneConfigTheme.current
    val shape = theme.checkBoxShape
    Box(
        modifier
            .height(18.dp)
            .background(theme.componentBackground, shape)
            .border(1.dp, theme.borderColor, shape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, Modifier, theme.textColorSecondary, 10.sp, FontWeight.Medium)
    }
}

@Composable
fun ClippedText(text: String, modifier: Modifier, color: Color, size: TextUnit, weight: FontWeight, maxLines: Int) {
    Text(text, modifier, color, size, null, BaselineShift.None, weight, TextAlign.Start, maxLines, TextOverflow.Ellipsis)
}
