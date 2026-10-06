package com.rising.pos.feature.product

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.core.database.entity.*
import com.rising.pos.feature.pos.CategoryFilters
import com.rising.pos.feature.pos.components.ProductPhoto
import com.rising.pos.ui.components.*
import com.rising.pos.ui.theme.TabletCashierTheme
import com.rising.pos.ui.theme.posMoney

@Composable
internal fun ProductTabletMasterDetail(
    displayedProducts: List<ProductWithCategory>, allProducts: List<ProductWithCategory>,
    categories: List<CategoryEntity>, selectedCategoryId: String?, searchQuery: String,
    currencySymbol: String, stockFilter: StockFilterType, onStockFilterChange: (StockFilterType) -> Unit,
    onSearchChange: (String) -> Unit, onSelectCategory: (String?) -> Unit,
    onEditProduct: (ProductWithCategory) -> Unit, onAdjustStock: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductWithCategory) -> Unit, modifier: Modifier = Modifier
) = TabletCashierTheme {
    val colors = MaterialTheme.colorScheme
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var page by rememberSaveable(searchQuery, selectedCategoryId, stockFilter) { mutableStateOf(0) }
    val pages = ((displayedProducts.size + 5) / 6).coerceAtLeast(1)
    val safePage = page.coerceIn(0, pages - 1)
    val visible = displayedProducts.drop(safePage * 6).take(6)
    val selected = visible.firstOrNull { it.product.id == selectedId } ?: visible.firstOrNull()
    val lowCount = allProducts.count { it.product.trackStock && it.product.stock > 0 && it.product.stock <= it.product.minStock }
    Surface(modifier, color = colors.surface, contentColor = colors.onSurface) {
        TabletWorkspaceSplit(master = {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("product_master")) {
                PosSearchBar(searchQuery, onSearchChange, "Cari nama atau kode barang")
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${displayedProducts.size} produk", Modifier.weight(1f), color = colors.onSurfaceVariant, fontSize = 14.sp)
                    if (lowCount > 0) {
                        Surface(onClick = { onStockFilterChange(if (stockFilter == StockFilterType.ALL) StockFilterType.LOW_STOCK else StockFilterType.ALL) },
                            color = com.rising.pos.ui.theme.WarningAmberContainer, shape = RoundedCornerShape(8.dp)) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Warning, null, Modifier.size(18.dp), tint = com.rising.pos.ui.theme.WarningAmber)
                                Spacer(Modifier.width(5.dp)); Text("$lowCount stok menipis", fontSize = 12.sp, color = com.rising.pos.ui.theme.WarningAmber)
                            }
                        }
                    }
                }
                CategoryFilters(categories, selectedCategoryId, onSelectCategory, 0.dp)
                Spacer(Modifier.height(10.dp))
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val tableWidth = maxWidth.coerceAtLeast(540.dp)
                    Column(Modifier.horizontalScroll(rememberScrollState())) {
                        Column(Modifier.width(tableWidth).border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))) {
                            Row(Modifier.fillMaxWidth().background(colors.surfaceContainer).padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("Produk", Modifier.weight(2.6f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Kategori", Modifier.weight(1.3f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Harga jual", Modifier.weight(1.3f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Stok", Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.width(48.dp))
                            }
                            visible.forEach { item ->
                                key(item.product.id) {
                                val product = item.product
                                var menu by remember(product.id) { mutableStateOf(false) }
                                val chosen = selected?.product?.id == product.id
                                Surface(onClick = { selectedId = product.id }, color = if (chosen) colors.primaryContainer else colors.surface,
                                    modifier = Modifier.testTag("product_row_${product.id}")) {
                                    Row(Modifier.fillMaxWidth().drawBehind {
                                        if (chosen) drawRect(colors.primary, size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height))
                                    }.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Row(Modifier.weight(2.6f), verticalAlignment = Alignment.CenterVertically) {
                                            ProductPhoto(product.imageUrl, product.name, Modifier.size(48.dp))
                                            Spacer(Modifier.width(10.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(product.sku?.takeIf { it.isNotBlank() } ?: product.barcode?.takeIf { it.isNotBlank() } ?: "Belum ada kode", fontSize = 12.sp, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                        Text(item.category?.name ?: "Umum", Modifier.weight(1.3f), fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(posMoney(product.sellingPrice, currencySymbol), Modifier.weight(1.3f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Box(Modifier.weight(1f)) {
                                            val stock = if (product.trackStock) "${quantityLabel(product.stock)} ${product.unit}" else "Tanpa batas"
                                            if (product.trackStock && product.stock <= product.minStock) {
                                                WorkspaceBadge(stock, com.rising.pos.ui.theme.WarningAmber, com.rising.pos.ui.theme.WarningAmberContainer)
                                            } else Text(stock, fontSize = 13.sp)
                                        }
                                        Box {
                                            IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, "Opsi ${product.name}") }
                                            DropdownMenu(menu, { menu = false }) {
                                                DropdownMenuItem(text = { Text("Edit produk") }, onClick = { menu = false; onEditProduct(item) })
                                                DropdownMenuItem(text = { Text("Sesuaikan stok") }, enabled = product.trackStock, onClick = { menu = false; onAdjustStock(product) })
                                                DropdownMenuItem(text = { Text("Hapus produk", color = colors.error) }, onClick = { menu = false; onDeleteProduct(item) })
                                            }
                                        }
                                    }
                                }
                                HorizontalDivider(color = colors.outlineVariant)
                                }
                            }
                        }
                    }
                }
                if (displayedProducts.isEmpty()) WorkspaceEmptyState(Icons.Outlined.Inventory2,
                    if (searchQuery.isBlank() && selectedCategoryId == null) "Belum ada produk" else "Produk tidak ditemukan",
                    "Tambah produk atau ubah pencarian dan kategori.")
            }
            WorkspacePagination(displayedProducts.size, safePage, 6, "produk") { page = it }
        }, detail = {
            if (selected != null) {
                val product = selected.product
                key(product.id) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).testTag("product_detail")) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Detail produk", Modifier.weight(1f), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        WorkspaceBadge(if (product.isActive) "Aktif" else "Nonaktif", if (product.isActive) com.rising.pos.ui.theme.SuccessGreen else colors.onSurfaceVariant,
                            if (product.isActive) com.rising.pos.ui.theme.SuccessGreenContainer else colors.surfaceContainer)
                    }
                    Spacer(Modifier.height(16.dp))
                    ProductPhoto(product.imageUrl, product.name, Modifier.fillMaxWidth().aspectRatio(2f))
                    Spacer(Modifier.height(10.dp))
                    Text(product.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(product.sku?.takeIf { it.isNotBlank() } ?: product.barcode?.takeIf { it.isNotBlank() } ?: "Belum ada kode", fontSize = 13.sp, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(14.dp))
                    WorkspaceDetailField("Kategori", selected.category?.name ?: "Umum")
                    WorkspaceDetailField("Harga jual", posMoney(product.sellingPrice, currencySymbol))
                    WorkspaceDetailField("Stok tersedia", if (product.trackStock) "${quantityLabel(product.stock)} ${product.unit}" else "Tanpa batas")
                    WorkspaceDetailField("Stok minimum", if (product.trackStock) "${quantityLabel(product.minStock)} ${product.unit}" else "Tidak dicatat")
                    HorizontalDivider(Modifier.padding(vertical = 12.dp), color = colors.outlineVariant)
                    val low = product.trackStock && product.stock <= product.minStock
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (low) Icons.Outlined.Warning else Icons.Outlined.CheckCircle, null,
                            tint = if (low) com.rising.pos.ui.theme.WarningAmber else com.rising.pos.ui.theme.SuccessGreen)
                        Spacer(Modifier.width(8.dp))
                        Text(if (!product.trackStock) "Pencatatan stok nonaktif." else if (low) "Stok menipis, segera tambah stok." else "Stok masih mencukupi.", fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                }
                }
                WorkspaceAction("Edit produk", Icons.Outlined.Edit, { onEditProduct(selected) })
                Spacer(Modifier.height(8.dp))
                WorkspaceAction("Sesuaikan stok", CashierIcons.Box, { onAdjustStock(product) }, secondary = true, enabled = product.trackStock)
            } else {
                WorkspaceEmptyState(Icons.Outlined.Inventory2, "Detail produk", "Pilih atau tambahkan produk untuk melihat detailnya.")
            }
        })
    }
}
