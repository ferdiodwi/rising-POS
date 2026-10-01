# DESIGN.md: Rising POS

> Arah desain resmi untuk aplikasi **Rising POS** (Android / Jetpack Compose, Material 3).
> Dokumen ini adalah "soul" desain: identitas, palet, tipografi, spacing, komponen.
> `antislop` adalah filternya; file ini yang memberi arah. Setiap keputusan punya alasan tertulis (R-31).

---

## 1. Design Read (dials)

> **Reading this as:** aplikasi kasir/POS untuk pelaku UMKM Indonesia, dipakai cepat di meja kasir yang terang, sering satu tangan; bahasa visual **tenang, tegas, fungsional**; dial **ENERGY 1 / RHYTHM 1 / MOTION 1**.

| Dial | Nilai | Alasan |
|---|---|---|
| **ENERGY** | **1 (Calm)** | Ini alat kerja, bukan landing page. Kasir butuh tenang & fokus; layar tidak boleh "berteriak". |
| **RHYTHM** | **1 (Uniform)** | Layar POS adalah alat. Keseragaman (chip kategori, grid kartu, baris total) membangun memori otot: kasir tahu letak semuanya tanpa berpikir. Keseragaman di sini **pilihan sadar**, bukan kelalaian (R-05). |
| **MOTION** | **1 (Minimal)** | Gerak = gangguan di alat kerja. Hanya ada feedback: ripple saat tekan, transisi bottom sheet standar. Tidak ada animasi dekoratif, tidak ada loop. |

**Alasan tiga dial rendah:** semua tetangga dial tinggi (ENERGY 3, RHYTHM 3, MOTION 3) mengasumsikan pengguna menikmati antarmuka. Kasir tidak menikmati; dia melayani antrean. Desain yang benar adalah desain yang menghilang.

---

## 2. Identitas & Motif

**Prinsip inti:** *kasir harus bisa membaca angka uang dari jauh, dengan cepat, tanpa salah.* Semua keputusan turun dari sini.

**Motif identitas: bahasa "nota" (struk).**
Rising POS pada akhirnya mencetak struk. Motifnya adalah bahasa struk itu sendiri:
- **Angka uang selalu pakai figur tabular** (lebar digit sama) supaya nominal sejajar vertikal di daftar dan ringkasan, memudahkan membandingkan harga.
- **Nomor struk ditulis monospace** (mis. `TRX-A01-20260930-00042`) karena itu identitas transaksi yang perlu dipindai karakter-per-karakter.
- **Pemisah tipis bergaya perforasi** (divider halus) dipakai untuk memisah blok ringkasan, seperti garis sobek di struk.

*Alasan:* motif ini lahir dari produknya sendiri (alat yang menghasilkan nota), bukan dari template. Kalau logo ditukar, desainnya tetap terasa milik Rising POS karena bahasanya dari struk, bukan dari "SaaS dashboard".

**Aksen tunggal (one deliberate accent):** **navy brand** dipakai HANYA di (a) tombol aksi utama, (b) state terpilih (chip/segment), (c) fokus. Warna status (hijau/amber/merah) adalah *informasi*, bukan aksen. Di luar tiga titik itu, navy tidak muncul. Nol aksen = steril; aksen di mana-mana = slop.

---

## 3. Palet Warna

Semua pasangan teks/latar sudah **dihitung** dengan rumus WCAG 2.x (bukan dikira-kira). Angka di kolom rasio = hasil hitung.

### Core (netral + brand), hanya 2 warna inti + 1 aksen

| Token | Hex | Peran |
|---|---|---|
| `ink` | `#0F172A` | Teks utama, judul, angka uang |
| `ink2` | `#475569` | Teks sekunder, label |
| `muted` | `#5B6879` | Teks tersier, caption (digelapkan agar lolos AA di background) |
| `bg` | `#F6F7F9` | Latar layar |
| `surface` | `#FFFFFF` | Latar kartu/sheet |
| `line` | `#E4E7EC` | Garis dekoratif halus (card border, divider) |
| `lineStrong` | `#828C99` | Garis informatif (outline input, fokus), wajib 3:1 |

| Token | Hex | Peran |
|---|---|---|
| `navy` | `#1E40AF` | **Aksen**: CTA utama, state terpilih, fokus |
| `navyDeep` | `#16307A` | Navy pressed/dark |
| `navySubtle` | `#EEF3FF` | Latar tint navy (chip terpilih, badge info) |

### Status (informasi, bukan aksen)

| Token | Hex | Latar tint | Peran |
|---|---|---|---|
| `success` | `#047857` | `#E7F5EF` | Uang cukup, transaksi selesai, stok aman |
| `warning` | `#B45309` | `#FDF1E3` | Stok menipis, uang kurang, pesanan ditahan |
| `danger` | `#B91C1C` | `#FDECEC` | Hapus, void, stok habis, error |
| `info` | `#0369A1` | `#EAF4FC` | Netral informatif (refund) |

### Kontras terverifikasi (rumus WCAG 2.x)

| Pasangan | Rasio | Normal (4.5) | Large (3.0) | Non-text (3.0) |
|---|---|---|---|---|
| ink on surface | 17.85 | PASS | PASS | PASS |
| ink2 on surface | 7.58 | PASS | PASS | PASS |
| muted on surface | 5.93 | PASS | PASS | PASS |
| muted on bg | 5.53 | PASS | PASS | PASS |
| white on navy | 8.72 | PASS | PASS | PASS |
| navy on navySubtle | 7.85 | PASS | PASS | PASS |
| success on surface | 5.48 | PASS | PASS | PASS |
| success on tint | 4.88 | PASS | PASS | PASS |
| warning on surface | 5.02 | PASS | PASS | PASS |
| danger on surface | 6.47 | PASS | PASS | PASS |
| lineStrong vs surface | 3.41 | n/a | n/a | PASS |

Catatan: `line` (`#E4E7EC`, rasio 1.24) dipakai **hanya untuk garis dekoratif** (batas kartu, divider), bukan untuk menyampaikan informasi kontrol, jadi tidak tunduk pada 3:1. Outline input/fokus pakai `lineStrong`.

---

## 4. Tipografi

**Font:** sistem sans-serif Android (**Roboto**), alasan: kecepatan render, konsistensi dengan OS, dan angka tabular tersedia native. Bukan dipilih karena tren, tapi karena nol biaya muat dan paling terbaca di HP murah.

**Angka:** semua nominal uang memakai `FontFeatureSetting("tnum")` (tabular figures) supaya digit sejajar.

| Token | Size / Line | Weight | Pakai untuk |
|---|---|---|---|
| `displayMoney` | 30 / 36 | Bold | Total bayar besar, omzet hero |
| `titlePage` | 22 / 28 | SemiBold | Judul halaman ("Kasir", "Produk") |
| `titleSection` | 16 / 22 | SemiBold | Judul section, nama di kartu |
| `priceCard` | 16 / 22 | Bold | Harga di kartu produk (tabular) |
| `body` | 14 / 20 | Regular | Teks umum |
| `label` | 13 / 18 | Medium | Label tombol, chip |
| `caption` | 12 / 16 | Regular | Caption, info kecil |
| `money` | 14 / 20 | Medium | Nominal di baris ringkasan (tabular) |
| `receiptNo` | 12 / 16 | Medium | Nomor struk (monospace) |

*Alasan hierarki:* ukuran dijaga berjarak (12/13/14/16/22/30) supaya perbedaan level jelas, tidak ada 15 vs 16 yang membingungkan.

---

## 5. Spacing & Radius

**Spacing (basis 4dp):** `xs 4` · `sm 8` · `md 12` · `lg 16` · `xl 24` · `2xl 32`.
- Padding tepi layar: **16dp**.
- Padding dalam kartu: **14dp**.
- Jarak antar kartu grid: **10dp**.
- Jarak antar section: **24dp**.
- Padding bawah konten: **+88dp** (ruang bar keranjang sticky agar item terakhir tidak tertutup, R-03).

**Radius:** `sm 8` · `md 12` · `lg 16` · `pill 999`.
- Kartu & sheet: **16dp** (dipilih sengaja, konsisten).
- Tombol: **12dp** (bukan pill; pill untuk semua elemen = slop, R-11).
- Chip: **pill** (chip memang secara konvensi pill, jadi ini pengecualian beralasan).
- Input: **12dp**.

*Alasan radius dipisah:* radius jadi alat hierarki. Kartu besar 16, kontrol 12, chip pill. Kalau semua pill, bahasa visual "ini kartu / ini tombol" hilang.

---

## 6. Elevasi & Border (flat minimalis)

- **Default: flat.** Kartu memakai border `line` 1dp, **tanpa shadow**.
- Shadow HANYA untuk elemen yang benar-benar melayang di atas konten: **bottom sheet** dan **bar keranjang sticky**. Alasannya tertulis: elemen ini menutupi konten, jadi butuh penanda elevasi.
- Tidak ada glow di mana pun. Tidak ada gradient. Tidak ada glassmorphism.

*Alasan:* di alat kerja, permukaan datar lebih cepat dipindai; shadow di mana-mana membuat semua elemen "mengambang" dan menghapus hierarki (R-12).

---

## 7. Touch & Aksesibilitas

- Touch target minimum **48dp** (Material). Tombol aksi utama **56dp** tinggi.
- Jarak antar target interaktif **≥ 8dp**.
- Fokus terlihat: outline `navy` 2dp (bukan `outline: none`), kontras ≥ 3:1.
- Status **tidak pernah warna saja**: selalu warna + ikon + teks (mis. "Stok habis" bukan cuma teks merah).
- Teks harus lolos AA; kalau tidak, warna token yang diganti, bukan "dianggap lolos".

---

## 8. Komponen

| Komponen | Spesifikasi |
|---|---|
| **Primary button** | navy, teks putih, tinggi 56dp, radius 12, ikon opsional. Disabled: `line` + teks `muted`. |
| **Secondary button** | surface, border `lineStrong` 1dp, teks `ink`. Tinggi 56dp. |
| **Danger button** | `danger` bg, teks putih. Hanya untuk aksi merusak (hapus/void). |
| **Card** | surface, border `line` 1dp, radius 16, **tanpa shadow**. Padding 14dp. |
| **Chip (kategori)** | pill. Terpilih: `navySubtle` bg + `navy` teks + border `navy`. Tidak terpilih: surface + border `line` + teks `ink2`. Tinggi ≥ 40dp. |
| **Input** | radius 12, border `line` → `navy` saat fokus. Label di atas. Error: border `danger` + teks `danger` (bukan hanya warna). |
| **Bottom sheet** | radius atas 16, shadow tipis (penanda elevasi). Drag handle 32x4. |
| **Bottom nav** | 5 tujuan (HP): Kasir, Produk, Riwayat, Laporan, Pengaturan. Ikon 24dp, label `caption`. Item aktif: ikon+label `navy`. |
| **Empty state** | ikon outline + judul + **penyebab & aksi berikutnya** (R-27), bukan "No data". |
| **Bar keranjang sticky** | navy, radius 16, tampil hanya saat keranjang terisi: jumlah item + total + tombol "Lihat pesanan". Tinggi ≥ 60dp. |

---

## 9. Aturan Anti-Slop yang Dipegang

- **Tanpa em dash** di semua teks UI (R-02). Pakai koma, titik, atau tanda kurung.
- **Tanpa emoji** di teks UI.
- **Tanpa angka karangan.** Semua nominal berasal dari data nyata (R-17/R-38).
- **Tanpa ikon generik** (sparkle, bintang, kilat, robot). Ikon harus relevan (R-04).
- **Palet ≤ 3 warna inti + 1 aksen** (R-29). Neutral tidak dihitung.
- **Satu focal point per layar.** Di layar Kasir: tombol Bayar / total.
- **Setiap kontrol punya perilaku nyata** atau tidak ada (R-26).
- **Tiap layar data punya empty / loading / error** (R-27).

---

## 10. Catatan Implementasi

- Token warna/tipografi/radius dipetakan ke `ui/theme/Color.kt` + `Theme.kt` (Material 3 `ColorScheme` + `Typography` + `Shapes`).
- `PosColors` (CompositionLocal) menyimpan token teks netral (`ink`, `ink2`, `muted`, `line`, dst) supaya konsisten antar layar.
- Dark mode tetap disiapkan (bukan default), mengikuti dial yang sama; kontras diverifikasi ulang untuk dark.
- Target saat ini: **HP portrait**. Tablet menyusul dengan pola dua panel (katalog + keranjang), dial tidak berubah.
