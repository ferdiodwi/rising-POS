package com.rising.pos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.ui.theme.PrimaryBlue
import com.rising.pos.ui.theme.Slate200
import com.rising.pos.ui.theme.Slate400
import com.rising.pos.ui.theme.Slate50
import com.rising.pos.ui.theme.Slate500
import com.rising.pos.ui.theme.Slate700
import com.rising.pos.ui.theme.Slate900

/**
 * Judul dan Subjudul standar untuk header 5 menu utama Rising POS.
 */
@Composable
fun PosHeaderTitleSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    extraSubtitle: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Slate500
            )
        )
        if (!extraSubtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = extraSubtitle,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate500
                )
            )
        }
    }
}

/**
 * Tombol aksi header di pojok kanan atas dengan kontainer bersudut tumpul (12dp)
 * dan border halus, serasi dengan desain mockup.
 */
@Composable
fun PosHeaderActionButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    badgeColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    size: Dp = 44.dp
) {
    Surface(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Slate200),
        modifier = modifier.size(size)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                BadgedBox(
                    badge = {
                        if (badgeCount > 0) {
                            Badge(
                                containerColor = badgeColor,
                                contentColor = Color.White
                            ) {
                                Text("$badgeCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = Slate900,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Search bar rounded bersudut tumpul (12dp) dengan tinggi 48dp,
 * ikon cari di kiri, placeholder halus, dan opsi tombol barcode di kanan.
 */
@Composable
fun PosSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingBarcodeAction: (() -> Unit)? = null,
    onSearchAction: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Slate50,
        border = BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Slate900
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchAction?.invoke() }),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(fontSize = 14.sp, color = Slate400)
                        )
                    }
                    innerTextField()
                }
            )
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Hapus",
                        tint = Slate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (trailingBarcodeAction != null) {
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate200)
                )
                Spacer(Modifier.width(4.dp))
                IconButton(
                    onClick = trailingBarcodeAction,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = CashierIcons.Barcode,
                        contentDescription = "Scan barcode",
                        tint = Slate700,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

/**
 * Tombol filter tanggal tab chip:
 * - Aktif: Latar biru muda (#EFF6FF), border #BFDBFE, teks & ikon biru (#2563EB)
 * - Tidak aktif: Latar surface/putih, border #E2E8F0, teks & ikon abu (#475569)
 */
@Composable
fun PosDateFilterButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val bgColor = if (selected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
    val borderColor = if (selected) Color(0xFFBFDBFE) else Slate200
    val contentColor = if (selected) PrimaryBlue else Slate700
    val fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium

    Surface(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontWeight = fontWeight,
                    color = contentColor
                ),
                maxLines = 1
            )
        }
    }
}
