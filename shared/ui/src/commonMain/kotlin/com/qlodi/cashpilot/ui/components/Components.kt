package com.qlodi.cashpilot.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.qlodi.cashpilot.ui.theme.CashpilotColors
import com.qlodi.cashpilot.ui.theme.NumberFontFamily
import com.qlodi.cashpilot.ui.theme.Radii
import com.qlodi.cashpilot.ui.theme.Spacing

/* ───────────── surfaces ───────────── */

@Composable
fun QCard(modifier: Modifier = Modifier, padding: Int = 18, content: @Composable () -> Unit) {
    val c = CashpilotColors
    Box(
        modifier
            .clip(RoundedCornerShape(Radii.md))
            .background(c.surfaceElevated)
            .border(BorderStroke(1.dp, c.border), RoundedCornerShape(Radii.md))
            .padding(padding.dp),
    ) { content() }
}

/* ───────────── text ───────────── */

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

@Composable
fun SectionTitle(title: String, subtitle: String? = null) {
    val c = CashpilotColors
    Column {
        Text(title, color = c.textPrimary, style = MaterialTheme.typography.headlineSmall)
        if (subtitle != null) {
            Spacer(Modifier.height(Spacing.xs))
            Text(subtitle, color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun QBadge(text: String, color: Color = CashpilotColors.heroCyan) {
    Box(
        Modifier
            .clip(RoundedCornerShape(Radii.pill))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) { Text(text, color = color, style = MaterialTheme.typography.labelMedium) }
}

/* ───────────── buttons (M3, ripple + states + ≥48dp) ───────────── */

@Composable
fun QPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, leading: ImageVector? = null) {
    val c = CashpilotColors
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(Radii.sm),
        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
    ) {
        if (leading != null) { Icon(leading, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun QTonalButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, leading: ImageVector? = null) {
    val c = CashpilotColors
    FilledTonalButton(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(Radii.sm),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = c.surfaceHigh, contentColor = c.textPrimary),
    ) {
        if (leading != null) { Icon(leading, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.sm)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun QDangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = CashpilotColors
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(Radii.sm),
        colors = ButtonDefaults.buttonColors(containerColor = c.danger, contentColor = c.onDanger),
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun QTextLinkButton(text: String, onClick: () -> Unit, color: Color = CashpilotColors.heroCyan, leading: ImageVector? = null) {
    TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        if (leading != null) { Icon(leading, null, Modifier.size(18.dp)); Spacer(Modifier.width(Spacing.xs)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/* ───────────── input ───────────── */

@Composable
fun QTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    isError: Boolean = false,
    password: Boolean = false,
    supportingText: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val c = CashpilotColors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = c.textMuted) } },
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        trailingIcon = trailingIcon,
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(Radii.sm),
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.heroCyan,
            unfocusedBorderColor = c.border,
            cursorColor = c.heroCyan,
            focusedLabelColor = c.heroCyan,
            unfocusedLabelColor = c.textMuted,
            focusedTextColor = c.textPrimary,
            unfocusedTextColor = c.textPrimary,
            focusedContainerColor = c.surfaceHigh,
            unfocusedContainerColor = c.surfaceHigh,
            errorBorderColor = c.danger,
        ),
    )
}

/* ───────────── states ───────────── */

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(Spacing.huge), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CashpilotColors.heroCyan, strokeWidth = 2.5.dp)
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, subtitle: String? = null) {
    val c = CashpilotColors
    QCard(Modifier.fillMaxWidth(), padding = 28) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                Modifier.size(52.dp).clip(RoundedCornerShape(Radii.md)).background(c.heroCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) { Icon(icon, null, tint = c.heroCyan, modifier = Modifier.size(26.dp)) }
            Spacer(Modifier.height(Spacing.md))
            Text(title, color = c.textPrimary, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            if (subtitle != null) {
                Spacer(Modifier.height(Spacing.xs))
                Text(subtitle, color = c.textMuted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            }
        }
    }
}

/* ───────────── confirm dialog ───────────── */

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
) {
    val c = CashpilotColors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = c.surfaceElevated,
        titleContentColor = c.textPrimary,
        textContentColor = c.textSecondary,
        shape = RoundedCornerShape(Radii.lg),
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = if (destructive) c.danger else c.heroCyan)) {
                Text(confirmLabel, style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = c.textMuted)) {
                Text(com.qlodi.cashpilot.ui.i18n.LocalStrings.current.cancel, style = MaterialTheme.typography.labelLarge)
            }
        },
    )
}

/* ───────────── settings rows (стиль frc-personal) ───────────── */

@Composable
fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = CashpilotColors.textMuted,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.08.em),
        modifier = Modifier.padding(start = Spacing.xs, top = Spacing.sm, bottom = Spacing.xs),
    )
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    showChevron: Boolean = false,
    tint: Color = CashpilotColors.textSecondary,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val c = CashpilotColors
    Row(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .heightIn(min = 56.dp)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(Radii.sm)).background(c.surfaceHigh),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(Spacing.md))
        Text(label, color = if (tint == c.danger) c.danger else c.textPrimary, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (trailing != null) trailing()
        if (trailingText != null) Text(trailingText, color = c.textMuted, style = MaterialTheme.typography.bodyMedium)
        if (showChevron) {
            Spacer(Modifier.width(Spacing.sm))
            Icon(Icons.Filled.ChevronRight, null, tint = c.textMuted, modifier = Modifier.size(20.dp))
        }
    }
}

/** Роздільник між рядками в картці. */
@Composable
fun RowDivider() {
    Box(Modifier.fillMaxWidth().padding(start = 62.dp).height(1.dp).background(CashpilotColors.border))
}

/* ───────────── floating bottom nav (1:1 frc-personal docked pill) ───────────── */

@Composable
fun PillNavBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val c = CashpilotColors
    val shape = RoundedCornerShape(Radii.pill)
    Box(
        modifier.clip(shape).background(c.tabBg).border(BorderStroke(1.dp, c.tabBorder), shape).height(66.dp),
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
    val shape = RoundedCornerShape(Radii.pill)
    val pillBg = if (active) c.heroCyan.copy(alpha = 0.16f) else Color.Transparent
    val tint = if (active) c.heroCyan else c.textMuted
    Column(
        Modifier.weight(1f).clip(shape).background(pillBg).clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(2.dp))
        Text(label, color = tint, style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.02.em), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
