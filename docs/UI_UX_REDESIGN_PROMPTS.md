# 🎨 RISING POS — Prompt Redesign UI/UX

> Panduan lengkap untuk merombak **seluruh** UI/UX aplikasi Rising POS (Android / Jetpack Compose).
> Setiap prompt **standalone** — bisa di-copy paste satu per satu ke AI (Claude, Cursor, ChatGPT, dsb).

---

## 📋 Cara Pakai

1. **Baca dulu Bagian 0 (Design System)** — ini fondasi semua halaman.
2. Kalau AI-nya belum tahu design system, **paste Bagian 0 dulu**, baru prompt halaman.
3. Tiap prompt halaman sudah menyertakan ringkasan design token singkat, jadi tetap bisa dipakai sendiri.
4. Urutan pengerjaan yang disarankan: **Bagian 0 → 1 → 2 → 3 → ... → 9 → 10 (dialog)**.

---

# BAGIAN 0 — DESIGN SYSTEM & FONDASI

## 🎯 Prompt 0: Design System & Tema

```
Kamu adalah senior product designer + Android developer yang spesialis UI/UX aplikasi POS (Point of Sale) mobile.

Tolong buatkan DESIGN SYSTEM lengkap untuk aplikasi Android "Rising POS" — aplikasi kasir/POS untuk UMKM Indonesia (kedai kopi, restoran, toko retail, warung). Dipakai kasir saat melayani pelanggan, sering dalam kondisi ramai/cepat, sering pakai satu tangan.

KONTEKS TEKNIS:
- Android, Jetpack Compose, Material 3
- Bahasa UI: Indonesia
- Target device: HP (utama), tablet (bonus)
- Tema default: LIGHT (paling terbaca di toko terang), tapi dark mode tetap dipoles rapi
- Gaya visual: FLAT MINIMALIS — border halus, minim shadow, fokus ke kejelasan & keterbacaan. BUKAN gaya "AI slop" (jangan pakai gradient berlebihan, glow, shadow tebal, emoji bertebaran, atau warna-warni norak).
- Warna brand: BIRU PROFESIONAL (navy biru bersih, kesan terpercaya & rapi)

YANG SAYA MAU (output):
1. **Color tokens** — palet warna lengkap (hex) untuk light & dark:
   - Brand/primary (navy biru), hover/pressed state
   - Surface (background layar, card, elevated)
   - Text (primary, secondary, muted, disabled)
   - Border/outline (halus)
   - Semantic: success (hijau, untuk uang masuk/selesai), warning (amber, stok menipis), danger (merah, hapus/void), info
   - Warna khusus POS: "harga/omzet" (harus menonjol), "stok menipis"
   - Semua HARUS lolos kontras WCAG AA (teks ≥ 4.5:1)
2. **Typography scale** — pakai font default Android (Roboto/SansSerif) atau font modern gratis (mis. Inter, Plus Jakarta Sans). Definisikan:
   - Display (angka besar — omzet/total, buat dibaca dari jauh)
   - Heading (judul halaman, judul section)
   - Body (teks umum)
   - Label (label tombol, caption, chip)
   - Khusus: style untuk ANGKA UANG (tabular/monospace-ish biar rapi sejajar) & style untuk ANGKA BESAR di checkout
   - Sertakan fontSize, lineHeight, fontWeight, letterSpacing
3. **Spacing scale** — berbasis 4dp/8dp (4, 8, 12, 16, 20, 24, 32, 40, 48). Jelaskan kapan pakai yang mana.
4. **Radius scale** — sudut: kecil (6-8dp), sedang (12dp), besar (16dp), pill (full). Rekomendasi untuk card, tombol, chip, input, dialog.
5. **Elevation & border** — karena flat minimalis: kapan pakai border halus (1dp) vs shadow tipis. Rekomendasi default.
6. **Touch targets** — minimum 48x48dp (Material). Ukuran tombol aksi utama di POS harus besar (min 56dp tinggi) karena dipakai cepat & satu tangan.
7. **Komponen dasar** — spesifikasi visual untuk:
   - Primary button, secondary button, ghost/text button, danger button
   - Card (default, list item, stat card)
   - Text field (default, error, focused)
   - Chip / filter chip (selected & unselected)
   - Bottom sheet, dialog, snackbar
   - Bottom navigation bar
   - Empty state, loading state, error state
   - Badge (notifikasi, jumlah item)
8. **Iconografi** — pakai Material Symbols/Icons outlined, ukuran standar, ketebalan stroke konsisten.
9. **Prinsip UX** untuk POS: hierarki jelas (aksi utama paling menonjol), minim tap, jangan salah-tap (aksi destruktif dibedakan jelas), angka uang selalu terbaca, feedback instan.

FORMAT OUTPUT:
- Tabel token (nama token → nilai → kegunaan)
- Sertakan kode Kotlin Compose untuk `Color.kt` dan `Theme.kt` (Material3 ColorScheme + Typography + Shapes) yang SIAP PAKAI.
- Bahasa penjelasan: Indonesia santai-profesional.

Jangan tanya balik, langsung berikan hasil terbaik. Buat rapi, konsisten, dan benar-benar bisa diimplementasikan.
```

---

# BAGIAN 1 — LAYAR ONBOARDING

## 🎯 Prompt 1: Halaman Onboarding / Setup Awal

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, light theme default, gaya FLAT MINIMALIS, brand navy biru profesional).

DESIGN SYSTEM SINGKAT (patuhi):
- Primary: navy biru (#1E40AF–#1E3A8A range), on-primary putih
- Surface: background #F7F8FA, card putih, border halus #E2E8F0 (1dp), shadow minimal
- Text: utama #0F172A, sekunder #64748B
- Success #059669, Danger #DC2626, Warning #D97706
- Spacing basis 4/8dp. Radius: card 16dp, tombol 12dp, chip pill
- Touch target min 48dp, tombol utama min 56dp
- Tipografi sans-serif modern (Inter/Plus Jakarta Sans), heading semi-bold, body regular

SEKARANG REDESIGN HALAMAN: **ONBOARDING / SETUP AWAL**
Ini halaman pertama kali user buka app — mereka mengisi identitas usaha. Harus terasa menyambut, cepat, tidak bikin malas. Dipakai sekali, jadi boleh agak "hero" tapi tetap profesional.

DATA / FIELD YANG ADA (jangan hilangkan, boleh ditata ulang):
- Judul & subjudul sambutan (mis. "Selamat datang di Rising POS" + ajakan mengisi data usaha)
- Nama Toko / Usaha * (wajib) — placeholder "Contoh: Kedai Kopi Rising"
- Nomor Telepon / WhatsApp (opsional)
- Alamat Usaha (opsional)
- Kode perangkat (wajib) — placeholder "Contoh: A01 untuk Kasir Utama, B01 untuk Kasir 2"
- Pilihan Jenis Usaha (business type) — pakai chip/pilihan visual (mis. Kedai Kopi, Restoran, Retail/Toko, Warung) — ikon + label, single-select
- Tombol utama: "Mulai" / "Selesai Setup" (ikon arrow)
- Info kecil: kode perangkat dipakai di nomor struk

YANG SAYA MAU:
1. Layout 1 halaman yang rapi, scrollable, dengan HIRARKI JELAS: bagian sambutan → jenis usaha → data usaha → kode perangkat → CTA.
2. Bagian atas boleh ada elemen brand ringan (ikon toko/logo Rising POS, BUKAN ilustrasi 3D berlebihan).
3. Pilihan jenis usaha pakai grid/kartu kecil dengan ikon outline + label, ada state selected (border primary + bg tint tipis) — jelas mana yang terpilih.
4. Field wajib ditandai jelas (*), validasi inline (error di bawah field, warna danger, pesan singkat bahasa Indonesia).
5. Tombol utama besar, full-width, sticky di bawah (selalu terlihat), disabled kalau field wajib belum valid.
6. Keyboard-aware: saat field difokus & keyboard muncul, tombol tetap accessible.
7. Sebutkan semua STATE: default, field kosong, field error, loading (saat submit), sukses.

OUTPUT:
- Struktur layout (top→bottom) dalam bentuk list komponen
- Spesifikasi tiap komponen (ukuran, warna, spacing, tipografi)
- Kode Kotlin Compose lengkap untuk `OnboardingScreen()` (Material 3), modular & rapi, pakai design token di atas.
- Catatan singkat alasan UX tiap keputusan.

Jangan tanya balik, langsung hasilkan yang terbaik.
```

---

# BAGIAN 2 — LAYAR KASIR (POS) — PALING PENTING

## 🎯 Prompt 2: Halaman Kasir (POS / Transaksi)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme default, gaya FLAT MINIMALIS, brand navy biru profesional, target HP utama).

DESIGN SYSTEM SINGKAT (patuhi):
- Primary navy biru #1E40AF, on-primary putih; primary container biru muda tipis
- Background #F7F8FA, card putih, border halus #E2E8F0, shadow minimal
- Text utama #0F172A, sekunder #64748B, muted #94A3B8
- Success #059669 (uang/selesai), Danger #DC2626 (void/hapus), Warning #D97706 (stok menipis)
- Spacing 4/8dp basis; radius card 16dp, tombol 12dp, chip pill
- Touch target min 48dp; tombol checkout min 56dp tinggi
- Angka uang: font tabular biar rapi sejajar

SEKARANG REDESIGN HALAMAN: **KASIR (POS)** — ini layar paling sering dipakai & paling penting.
Tujuan: kasir bisa cari produk, masukkan ke keranjang, dan bayar SANGAT cepat, satu tangan, tanpa salah tap.

FITUR / DATA YANG ADA (jangan hilangkan):
- Header: nama toko, kode device, tombol scan barcode, tombol pesanan ditahan (held orders) + badge jumlah
- Search bar: "Cari nama, SKU, atau barcode..."
- Baris kategori (horizontal scroll): "Semua", kategori lain, tombol tambah kategori
- Grid produk (2 kolom di HP, 3 di tablet): kartu produk = nama, harga, stok/unit, indikator stok menipis/habis, badge varian/modifier kalau ada
- Produk bisa punya VARIAN (mis. ukuran) & MODIFIER (mis. extra shot) → buka dialog saat di-tap
- Keranjang: daftar item (nama, varian, qty +/- , harga, subtotal per item, hapus), pilih pelanggan, tombol diskon
- Ringkasan keranjang: subtotal, diskon, pajak/PPN, service charge, TOTAL (besar), tombol checkout
- Tombol "Tahan" (hold) keranjang
- Phone layout: keranjang = bottom sheet / sticky bar di bawah + tombol "Lihat Keranjang (n item) • Rp xxx"
- Tablet layout: 2 pane (katalog kiri, keranjang kanan)

YANG SAYA MAU:
1. **Header ringkas**: nama toko + device di kiri, ikon scan + ikon pesanan ditahan (dengan badge) di kanan. Bersih, tidak tinggi berlebihan.
2. **Search bar** prominent, sudut membulat, ikon search, ada tombol clear saat ada teks. Bisa juga jadi pintu masuk scan.
3. **Kategori** sebagai chip horizontal — chip selected jelas (bg primary, teks putih), unselected (border halus, teks sekunder).
4. **Grid produk** — kartu bersih: nama (max 2 baris), harga tebal, info stok kecil. State: normal, stok menipis (aksen warning), stok habis (redup + label "Habis", tidak bisa di-tap). Saat di-tap: feedback tekan (ripple/scale) + masuk keranjang / buka dialog varian.
5. **Keranjang HP**: sticky bottom bar yang menampilkan total + jumlah item + tombol utama "Bayar". Tap → bottom sheet keranjang penuh (daftar item, edit qty, hapus, diskon, pelanggan, tombol Bayar besar).
6. **Keranjang Tablet**: panel kanan tetap, lebar ~360dp, item list + ringkasan + tombol Bayar.
7. **Ringkasan uang**: TOTAL paling menonjol (angka besar, tebal). Subtotal/diskon/pajak/service kecil di atasnya. Format "Rp 25.000".
8. Tombol Bayar: full-width, tinggi ≥56dp, warna primary, teks jelas ("Bayar • Rp 25.000").
9. Empty state: kalau belum ada produk / hasil pencarian kosong / keranjang kosong — pesan ramah + ikon outline.
10. Sebutkan STATE: loading produk, produk kosong, hasil cari kosong, keranjang kosong, keranjang terisi, produk habis.

OUTPUT:
- Struktur layout HP (top→bottom) & tablet (2 pane) dalam list komponen
- Spesifikasi tiap komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `PosScreen()`, `ProductGrid()`, `ProductCard()`, `CategoriesRow()`, `PosHeader()`, dan `CartView()` — modular, pakai design token.
- Fokus pada kecepatan & pencegahan salah tap.
- Catatan UX singkat.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 3 — LAYAR PRODUK

## 🎯 Prompt 3: Halaman Produk (Manajemen Produk)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669, danger #DC2626, warning #D97706. Radius card 16dp, tombol 12dp, chip pill. Spacing 4/8dp. Touch min 48dp.

REDESIGN HALAMAN: **PRODUK**
Tujuan: pemilik toko mengelola daftar produk — cari, tambah, edit, hapus, atur kategori & varian. Halaman manajemen, jadi fokus ke kejelasan data & kemudahan edit.

DATA / FITUR (jangan hilangkan):
- Header: judul "Produk" + tombol "Tambah produk"
- Search: "Cari nama, SKU, atau barcode..."
- Filter kategori (chip horizontal): "Semua", kategori lain, tombol "Tambah Kategori Baru"
- Daftar produk: nama, harga jual, harga modal (opsional), stok + satuan, kategori, status aktif/nonaktif, badge varian
- Aksi per produk: Edit, Hapus (dengan konfirmasi "Apakah Anda yakin ingin menghapus '<nama>'?")
- Form tambah/edit produk (bottom sheet): Nama Produk*, Kategori (Tanpa Kategori), Harga Jual*, Harga Modal, toggle "Kelola Stok Otomatis" (sub: "Stok berkurang otomatis saat checkout"), Jumlah Stok, Satuan (pcs/porsi/botol), Batas Minimum Stok Menipis, daftar Varian (Nama Varian*, harga), pilihan modifier/topping
- Dialog tambah kategori: Nama Kategori (placeholder "Contoh: Minuman Dingin"), Simpan/Batal

YANG SAYA MAU:
1. Header ringkas: judul halaman + tombol primary "Tambah produk" (ikon + teks). Di HP boleh tombol FAB atau tombol di header.
2. Search + filter kategori di bawah header, sticky saat scroll (biar gampang filter saat list panjang).
3. **List produk**: item row bersih dengan: nama (tebal), baris info kecil (kategori • stok • satuan), harga jual (tebal, warna menonjol), harga modal kecil/muted. Aksi (edit/hapus) lewat ikon menu (⋮) atau swipe — bukan tombol besar yang bikin ramai. Badge "Nonaktif" (abu) & "Stok habis" (danger) jelas.
4. Boleh opsi tampilan list (bukan grid) — karena data manajemen lebih enak dibaca list.
5. **Form tambah/edit** pakai bottom sheet full-height dengan section jelas: Info Dasar, Harga, Stok, Varian, Modifier. Field wajib bertanda *. Validasi inline. Tombol Simpan sticky di bawah.
6. Toggle "Kelola Stok Otomatis" dengan penjelasan singkat di bawahnya.
7. Bagian varian: list dinamis (tambah/hapus baris varian), tiap varian: nama + harga.
8. Konfirmasi hapus: dialog jelas, tombol danger "Hapus" + "Batal", sebutkan nama produk.
9. Empty state (belum ada produk) & hasil pencarian kosong.
10. Sebutkan STATE: loading, kosong, ada data, form default, form error, konfirmasi hapus.

OUTPUT:
- Struktur layout list & form (list komponen)
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `ProductScreen()`, `ProductItemRow()`, `ProductFormBottomSheet()`, dialog kategori — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 4 — LAYAR STOK / INVENTORY

## 🎯 Prompt 4: Halaman Stok (Inventory & Mutasi Stok)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669 (stok masuk), danger #DC2626 (stok keluar/rusak), warning #D97706 (stok menipis). Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp.

REDESIGN HALAMAN: **STOK (Inventory)**
Tujuan: pemilik toko memantau stok, mencatat stok masuk/keluar, dan melihat riwayat mutasi stok.

DATA / FITUR (jangan hilangkan):
- Ringkasan/daftar produk dengan pelacakan stok: nama, stok saat ini + satuan, batas minimum, status (aman/menipis/habis)
- Aksi cepat per produk: "+ Masuk" (stok masuk), stok keluar/kurang
- Bottom sheet aksi stok: Produk* (picker, tampil "nama (Stok: X unit)"), jumlah, tipe (masuk/keluar), "Keterangan / Alasan (Opsional)" placeholder "Contoh: Kulakan supplier A / Basi / Salah hitung", tombol "Simpan Perubahan Stok"
- Tab/section riwayat mutasi stok: tipe (masuk/keluar), qty change, qty sebelum → sesudah, alasan, waktu
- Empty state: "Tidak ada produk dengan pelacakan stok." / "Belum ada pergerakan stok tercatat."

YANG SAYA MAU:
1. Header judul "Stok" + mungkin ringkasan kecil (mis. "3 produk perlu restok") dengan warna warning kalau ada.
2. Dua bagian utama: **Daftar Stok Produk** & **Riwayat Mutasi**. Pakai segmented control / tab (Material 3 SegmentedButton) biar rapi, BUKAN dua list panjang bertumpuk.
3. **Item stok**: nama produk, angka stok besar & jelas, satuan, indikator status (chip/badge warna: Aman=hijau, Menipis=amber, Habis=merah), progress bar tipis ke batas minimum (opsional), tombol "+ Masuk" kecil di kanan.
4. **Riwayat mutasi**: timeline/list item dengan ikon arah (↑ masuk hijau / ↓ keluar merah), qty change (+5 / -3), "sebelum → sesudah", alasan, waktu relatif (mis. "2 jam lalu").
5. **Bottom sheet aksi stok**: jelas & aman — pilih produk, pilih tipe (masuk/keluar) sebagai toggle besar, input jumlah dengan +/- stepper, keterangan, tombol simpan besar. Preview "Stok setelah: 15 unit" biar user yakin.
6. Aksi destruktif (stok keluar) dibedakan warna.
7. Empty states ramah untuk kedua bagian.
8. Sebutkan STATE: loading, ada data, stok menipis, stok habis, riwayat kosong, form aksi stok.

OUTPUT:
- Struktur layout (tab/segmented + list) dalam list komponen
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `InventoryScreen()`, `ProductStockRow()`, `MovementItemRow()`, `StockActionBottomSheet()` — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 5 — LAYAR RIWAYAT TRANSAKSI

## 🎯 Prompt 5: Halaman Riwayat Transaksi

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B. Status: Selesai=success #059669, Ditahan=warning #D97706, Void/Batal=danger #DC2626, Refund=info. Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp. Angka uang tabular.

REDESIGN HALAMAN: **RIWAYAT TRANSAKSI**
Tujuan: kasir/pemilik melihat daftar transaksi, mencari, melihat detail, cetak ulang struk, void/refund.

DATA / FITUR (jangan hilangkan):
- Header judul "Riwayat"
- Search: "Cari no struk, produk, atau catatan..."
- Filter status (chip): "Semua", "Selesai", "Void / Batal", "Refund", "Ditahan"
- Filter periode (hari ini / minggu / bulan / custom) — kalau ada
- Daftar transaksi: nomor struk, waktu, total, metode bayar (Tunai/QRIS/Kartu), status, jumlah item, nama pelanggan (kalau ada)
- Detail transaksi: daftar item, subtotal, diskon, pajak, service, total, metode bayar, bayar & kembalian, kasir, device, catatan
- Aksi: Cetak ulang struk, Bagikan, Void (dengan otorisasi PIN owner + alasan "Tuliskan alasan..."), Refund
- Dialog otorisasi void: judul "Otorisasi Void", input alasan

YANG SAYA MAU:
1. Header judul + (opsional) ringkasan kecil total transaksi periode terpilih.
2. Search + filter status chip horizontal, sticky saat scroll.
3. **Item transaksi** sebagai card/row bersih: nomor struk (mono/tebal), waktu (relatif + absolut kecil), total (tebal, menonjol), chip status warna, metode bayar dengan ikon kecil, jumlah item. 
4. Status jelas lewat chip/badge warna (Selesai hijau, Ditahan amber, Void merah, Refund biru).
5. Tap item → **detail transaksi** (bottom sheet / halaman) dengan struktur seperti struk: daftar item, ringkasan uang, info pembayaran, aksi (Cetak, Bagikan, Void, Refund). Total paling menonjol.
6. Void pakai konfirmasi kuat: judul "Otorisasi Void", field alasan wajib, tombol danger "Ya, Batalkan Transaksi" + "Batal". Void/Refund harus jelas aksi destruktif.
7. Grouping per tanggal (header tanggal) biar enak dibaca kalau banyak.
8. Empty state: belum ada transaksi / hasil filter kosong.
9. Sebutkan STATE: loading, kosong, ada data, hasil filter kosong, detail transaksi, konfirmasi void.

OUTPUT:
- Struktur layout list & detail (list komponen)
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `TransactionScreen()`, `TransactionItemCard()`, `TransactionDetailDialog()` — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 6 — LAYAR PENGELUARAN

## 🎯 Prompt 6: Halaman Pengeluaran (Expenses)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, danger/outflow #DC2626 (pengeluaran), success #059669. Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp. Angka uang tabular.

REDESIGN HALAMAN: **PENGELUARAN**
Tujuan: pemilik toko mencatat biaya operasional (beli bahan, gaji, listrik, dll) dan melihat riwayatnya.

DATA / FITUR (jangan hilangkan):
- Header judul "Pengeluaran" + tombol "Catat pengeluaran"
- Ringkasan total pengeluaran periode (mis. bulan ini)
- Filter periode (hari/minggu/bulan)
- Daftar pengeluaran: kategori, nominal, tanggal, catatan
- Kategori pengeluaran (mis. Bahan Baku, Gaji, Operasional, Lain-lain)
- Form: "Nominal Pengeluaran *" (dengan prefix "Rp"), kategori, tanggal, "Catatan / Keterangan (Opsional)" placeholder "Contoh: Beli beras 25kg & minyak goreng"

YANG SAYA MAU:
1. Header judul + tombol primary "Catat pengeluaran".
2. **Kartu ringkasan** total pengeluaran periode terpilih (angka besar, warna netral/danger tipis) + filter periode sebagai segmented control.
3. **Item pengeluaran**: ikon kategori, nama kategori (tebal), catatan kecil (muted), nominal (tebal, warna outflow/merah lembut), tanggal. Bisa grouping per tanggal.
4. Kategori dengan ikon outline yang konsisten (bahan baku, gaji, operasional, lain-lain).
5. **Form bottom sheet**: Nominal besar & prominent (input angka, prefix "Rp", keyboard angka), kategori sebagai chip/grid pilihan, tanggal (date picker), catatan. Tombol Simpan besar.
6. Empty state: belum ada pengeluaran.
7. Sebutkan STATE: loading, kosong, ada data, form default, form error, hasil filter kosong.

OUTPUT:
- Struktur layout (list komponen)
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `ExpenseScreen()`, `ExpenseItemRow()`, `ExpenseFormBottomSheet()` — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 7 — LAYAR PELANGGAN

## 🎯 Prompt 7: Halaman Pelanggan (Customers)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669, danger #DC2626. Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp. Angka uang tabular.

REDESIGN HALAMAN: **PELANGGAN**
Tujuan: mengelola data pelanggan (nama, telepon, email, alamat, catatan) + melihat riwayat belanja & total belanja.

DATA / FITUR (jangan hilangkan):
- Header judul "Pelanggan" + tombol "Tambah pelanggan"
- Search: "Cari nama atau no. telepon pelanggan..."
- Daftar pelanggan: nama, no telepon, total belanja, jumlah transaksi, terakhir belanja
- Detail pelanggan: info kontak (telepon/email/alamat/catatan), statistik (total belanja, jumlah transaksi, terakhir), riwayat pembelian
- Aksi: Edit, Hapus (konfirmasi "Apakah Anda yakin ingin menghapus pelanggan '...'?")
- Customer picker (saat checkout POS): cari & pilih pelanggan

YANG SAYA MAU:
1. Header judul + tombol primary "Tambah pelanggan".
2. Search prominent, sticky.
3. **Item pelanggan**: avatar inisial (lingkaran warna primer/muted), nama (tebal), no telepon (sekunder), dan di kanan: total belanja (tebal) + "X transaksi" kecil. Kalau ada, "Terakhir belanja: <tanggal>" kecil muted.
4. Tap → **detail pelanggan** (bottom sheet/halaman): kartu kontak (dengan aksi telepon/WhatsApp/email kalau ada), statistik belanja dalam kartu kecil, riwayat pembelian (list transaksi singkat).
5. **Form pelanggan**: Nama*, Telepon, Email, Alamat, Catatan. Validasi inline (mis. telepon format angka). Tombol Simpan besar.
6. Konfirmasi hapus jelas (danger + nama pelanggan).
7. **Customer picker** (untuk POS): bottom sheet dengan search + list, tombol "Pelanggan baru" di atas, tap untuk pilih. Ringkas & cepat.
8. Empty state: belum ada pelanggan / hasil cari kosong.
9. Sebutkan STATE: loading, kosong, ada data, detail, form default/error, konfirmasi hapus, picker.

OUTPUT:
- Struktur layout list, detail, form, picker (list komponen)
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `CustomerScreen()`, `CustomerCard()`, `CustomerDetailDialog()`, `CustomerFormDialog()`, `CustomerPickerDialog()` — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 8 — LAYAR LAPORAN / DASHBOARD

## 🎯 Prompt 8: Halaman Laporan (Dashboard)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669, warning #D97706, danger #DC2626. Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp. Angka uang tabular (biar rapi).

REDESIGN HALAMAN: **LAPORAN (Dashboard)**
Tujuan: pemilik toko memantau performa usaha secara cepat — omzet, jumlah transaksi, pengeluaran, selisih, produk terlaris, kondisi stok.

DATA / FITUR (jangan hilangkan):
- Header: "Laporan usaha" + subjudul "Pantau penjualan dan kebutuhan toko."
- Filter periode (hari/minggu/bulan/tahun) — segmented control
- **Kartu metrik**:
  - Omzet (mis. "Omzet · Hari ini") + "X transaksi selesai"
  - Rata-rata transaksi
  - Pengeluaran
  - Selisih pemasukan (net)
- Section "Kelola usaha": shortcut ke Stok produk ("X produk aktif"), Pengeluaran ("Catat biaya operasional toko"), Pelanggan ("Kontak dan riwayat pembelian")
- Section "Produk terlaris": ranking (nomor, nama, "X item terjual")
- Section "Kondisi stok": peringatan "X produk perlu restok" + list produk stok menipis/habis (nama, "Sisa X unit", "Minimum X unit")

YANG SAYA MAU:
1. Header + subjudul ringkas. Filter periode (SegmentedButton) tepat di bawah header.
2. **Kartu omzet utama** paling menonjol: label kecil ("Omzet · Hari ini"), angka besar tebal, sub-info "X transaksi selesai". Ini hero metric.
3. **Grid kartu metrik sekunder** (2 kolom): Rata-rata transaksi, Pengeluaran, Selisih pemasukan — tiap kartu: ikon kecil, label, angka. Warna angka bisa beda (net positif hijau, negatif merah).
4. **Section "Kelola usaha"**: list shortcut dengan ikon + judul + subjudul + chevron, tap → navigasi.
5. **Produk terlaris**: list ranking dengan nomor bulat (1,2,3...), nama produk, "X item terjual". Tiga teratas boleh aksen.
6. **Kondisi stok**: kalau ada peringatan, tampilkan banner/kartu warning di atas + list produk bermasalah (nama, sisa, minimum) dengan badge warna (menipis amber, habis merah). Kalau aman: empty state ramah "Tidak ada peringatan stok menipis."
7. Layout rapi, banyak whitespace, tidak padat. Angka uang tabular & sejajar.
8. Sebutkan STATE: loading, ada data, tidak ada penjualan (empty), ada peringatan stok, tidak ada peringatan.

OUTPUT:
- Struktur layout (top→bottom) dalam list komponen
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `DashboardScreen()`, `ReportValue()`, `OperationalRow()`, kartu metrik — modular + design token.
- Catatan UX.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 9 — LAYAR PENGATURAN

## 🎯 Prompt 9: Halaman Pengaturan (Settings)

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669, danger #DC2626, warning #D97706. Radius 16dp/12dp/pill. Spacing 4/8dp. Touch min 48dp.

REDESIGN HALAMAN: **PENGATURAN**
Tujuan: pemilik toko mengatur profil usaha, tampilan, fitur operasional, printer, keamanan, ekspor/cadangan. Halaman ini banyak isi → harus sangat terorganisir, jangan terasa panjang & menakutkan.

DATA / FITUR (jangan hilangkan, kelompokkan jadi SECTION yang rapi):
- **Profil Usaha**: Nama Toko, No. Telepon/WhatsApp, Alamat, Pesan di bagian bawah struk, Kode perangkat (placeholder "Contoh: A01 untuk kasir 1, B01 untuk kasir 2"), tombol "Simpan Profil"
- **Tampilan**: pilihan tema (Terang / Gelap / Sistem)
- **Fitur Operasional** (toggle):
  - Pencatatan Stok — "Otomatis catat stok masuk, keluar, dan sisa produk"
  - Nomor Meja — "Cocok untuk kedai, restoran, atau kafe dine-in"
  - Topping & Modifier — "Pilihan opsi tambahan seperti extra shot, gula, topping"
  - Barcode Scanner — "Scan barcode fisik atau kamera untuk mencari produk"
  - Aktifkan Pajak (PPN / PB1)
  - Biaya Layanan (Service Charge) — persentase
  - Cetak Struk Otomatis — "Langsung cetak struk sesaat setelah transaksi kasir berhasil"
- **Printer**: Pilih Printer, ukuran kertas (58 mm / 80 mm), "Cetak percobaan"
- **Keamanan**: PIN owner (aktif/nonaktif), ganti PIN — dengan "Otorisasi Ubah PIN" (verifikasi PIN lama)
- **Data**: Ekspor Transaksi, Ekspor Produk, Ekspor Biaya, Ekspor Pelanggan, Cadangkan data, Pulihkan data (dengan "Otorisasi Pulihkan Database" + pilih file cadangan)
- **Tentang**: versi app, update

YANG SAYA MAU:
1. Header judul "Pengaturan".
2. Konten dibagi **SECTION dengan header kecil** (uppercase/muted, mis. "PROFIL USAHA", "TAMPILAN", "FITUR", "PRINTER", "KEAMANAN", "DATA", "TENTANG") — tiap section dalam kartu/grup dengan divider halus antar item.
3. **Item pengaturan** konsisten: ikon outline kiri, judul + subjudul (muted), kontrol kanan (chevron untuk navigasi, Switch untuk toggle, nilai untuk pilihan seperti "58 mm"). Tinggi item ≥56dp biar gampang di-tap.
4. Toggle switch: jelas on/off, warna primary saat on. Item dengan subjudul selalu tampilkan penjelasan singkat.
5. Pilihan tema: SegmentedButton atau 3 opsi kartu (Terang/Gelap/Sistem) dengan ikon.
6. Aksi berisiko (Pulihkan data, Reset) dibedakan warna danger & ada konfirmasi + otorisasi PIN.
7. Ekspor: list item dengan ikon tipe (transaksi/produk/biaya/pelanggan), tap → proses dengan feedback (loading → sukses).
8. Cadangan: tombol jelas, jelaskan singkat fungsinya.
9. Sebutkan STATE: default, toggle on/off, proses ekspor (loading/sukses/gagal), dialog otorisasi PIN, konfirmasi pulihkan.

OUTPUT:
- Struktur layout lengkap per section (list komponen)
- Spesifikasi komponen (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose untuk `SettingsScreen()`, `SettingToggleRow()`, `SettingNavRow()`, `SettingSectionHeader()` — modular + design token.
- Catatan UX soal pengelompokan & pengurangan beban kognitif.

Jangan tanya balik, langsung hasil terbaik.
```

---

# BAGIAN 10 — DIALOG & BOTTOM SHEET PENTING

## 🎯 Prompt 10: Checkout, Diskusi, PIN, & Dialog Lainnya

```
Lanjutkan redesign "Rising POS" (Android Jetpack Compose, Material 3, bahasa Indonesia, LIGHT theme, FLAT MINIMALIS, brand navy biru, HP utama).

DESIGN SYSTEM SINGKAT: primary navy #1E40AF, bg #F7F8FA, card putih border #E2E8F0, text #0F172A/#64748B, success #059669, danger #DC2626, warning #D97706. Radius dialog 20-24dp, sheet 20dp atas, tombol 12dp. Spacing 4/8dp. Touch min 48dp. Angka uang tabular.

REDESIGN KUMPULAN DIALOG & BOTTOM SHEET berikut. Untuk masing-masing, berikan struktur, spesifikasi, dan kode Compose.

1. **CHECKOUT DIALOG / SHEET** (pembayaran — paling penting):
   - Ringkasan total besar & menonjol
   - Pilih metode bayar (Tunai / QRIS / Kartu / Transfer) — pilihan besar, jelas terpilih
   - Kalau Tunai: input "Nominal Uang Tunai" dengan tombol cepat ("Uang Pas", "+5k", "+10k", "+20k", "+50k"), tampil KEMBALIAN (change) real-time, warna success kalau cukup
   - Mode "Bagi pembayaran" (split): 2 metode, input "Nominal Bagian 1", tombol cepat "50:50"
   - Opsi: Nomor Meja ("Contoh: Meja 03"), Catatan Pesanan ("Contoh: Less ice, jangan pedas")
   - Tombol "Bayar" besar full-width, disabled sampai valid
   - Sebutkan state: default, uang kurang (warning), uang cukup (kembalian hijau), split aktif

2. **DISCOUNT DIALOG**:
   - Input diskon (nominal atau persen — toggle), alasan diskon (opsional)
   - Preview total setelah diskon
   - Tombol Terapkan / Batal

3. **HOLD CART DIALOG** (tahan pesanan):
   - Konfirmasi menahan keranjang saat ini, opsi beri nama/label pesanan
   - Tombol Tahan / Batal

4. **HELD ORDERS DIALOG** (daftar pesanan ditahan):
   - List pesanan ditahan: label, waktu, jumlah item, total
   - Tap → lanjutkan pesanan (load ke keranjang), aksi hapus
   - Empty state kalau tidak ada

5. **VARIANT PICKER DIALOG** (pilih varian produk):
   - Nama produk + harga dasar
   - Pilihan varian (mis. ukuran) sebagai kartu/chip besar, tampil harga tiap varian
   - Pilihan modifier/topping (kalau ada) dengan checkbox + harga
   - Preview harga total, tombol "Tambah ke Keranjang"

6. **RECEIPT SUCCESS DIALOG** (setelah bayar sukses):
   - Ikon sukses (centang hijau), "Pembayaran Berhasil"
   - Ringkasan: total, bayar, kembalian
   - Tombol: "Cetak Struk", "Bagikan", "Transaksi Baru"
   - Auto-close / tombol selesai jelas

7. **SECURITY PIN DIALOG** (otorisasi):
   - Judul sesuai konteks (mis. "Otorisasi Ubah PIN", "Otorisasi Void")
   - Input PIN (bulat/dots, keyboard angka), max 6 digit
   - Debounce / anti brute-force (loading saat verifikasi, error kalau salah)
   - Tombol konfirmasi / batal

8. **PIN SETUP DIALOG** (buat/ganti PIN):
   - Input PIN baru + konfirmasi PIN
   - Aturan PIN (min digit), validasi cocok
   - Tombol Simpan

OUTPUT untuk tiap dialog/sheet:
- Struktur (list komponen) + spesifikasi (ukuran, warna, spacing, tipografi, state)
- Kode Kotlin Compose (Material 3 ModalBottomSheet / AlertDialog) modular + design token
- Catatan UX singkat (fokus: kejelasan, pencegahan salah input uang, feedback instan)

Jangan tanya balik, langsung hasil terbaik.
```

---

# 📌 CATATAN AKHIR

- **Konsistensi**: setelah semua halaman, pastikan spacing, radius, warna, dan tipografi identik antar halaman — inilah yang bikin terasa "profesional" bukan "AI slop".
- **Anti "AI slop"**: hindari gradient norak, shadow tebal, emoji bertebaran, warna >4 jenis per layar, animasi berlebihan. Flat + border halus + hierarki jelas = kunci.
- **Aksesibilitas**: kontras teks ≥ 4.5:1, touch target ≥ 48dp, jangan hanya andalkan warna (pakai ikon/teks juga).
- **Real device test**: cek di HP layar kecil (5") dan tablet, serta mode gelap.
- **Prioritas pengerjaan**: 0 (design system) → 2 (Kasir) → 10 (Checkout) → 8 (Laporan) → sisanya.

> Setelah dapat hasil dari AI, jangan langsung copy semua. Iterasi: minta revisi bagian tertentu ("buat kartu produk lebih ringkas", "angka total kurang menonjol") sampai benar-benar cocok.
