package com.qlodi.cashpilot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.NumberFontFamily

/** Картка з рамкою (radius 14, як у референсі). */
@Composable
fun QCard(modifier: Modifier = Modifier, padding: Int = 18, content: @Composable () -> Unit) {
    val c = CashpilotColors
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(c.surfaceElevated)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(14.dp))
            .padding(padding.dp),
    ) { content() }
}

/** Фінансове число — моноширинне, вирівнювання по розряду. */
@Composable
fun NumberText(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = CashpilotColors.textPrimary,
    size: Int = 22,
    weight: FontWeight = FontWeight.SemiBold,
) {
    Text(
        value,
        modifier = modifier,
        color = color,
        style = TextStyle(fontFamily = NumberFontFamily, fontSize = size.sp, fontWeight = weight),
    )
}

/** Заголовок секції екрана. */
@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    val c = CashpilotColors
    Column {
        Text(title, color = c.textPrimary, style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold))
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = c.textMuted, fontSize = 14.sp)
        }
    }
}

/** Бейдж юрисдикції / статусу. */
@Composable
fun QBadge(text: String, color: Color = CashpilotColors.heroCyan) {
    Box(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) { Text(text, color = color, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) }
}

// ── Floating bottom pill nav (mobile) — дзеркалить QPillTabBar/QPillTab з frc-personal ──

@Composable
fun PillNavBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val c = CashpilotColors
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier
            .clip(shape)
            .background(c.tabBg)
            .border(BorderStroke(1.dp, c.tabBorder), shape)
            .height(66.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().fillMaxHeight().padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
fun RowScope.PillNavItem(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val c = CashpilotColors
    val shape = RoundedCornerShape(999.dp)
    val pillBg = if (active) c.heroCyan.copy(alpha = 0.16f) else Color.Transparent
    val tint = if (active) c.heroCyan else c.textMuted
    Column(
        Modifier
            .weight(1f)
            .clip(shape)
            .background(pillBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(
            label, color = tint,
            style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.02.em),
            maxLines = 1, overflow = TextOverflow.Ellipsis,
        )
    }
}
