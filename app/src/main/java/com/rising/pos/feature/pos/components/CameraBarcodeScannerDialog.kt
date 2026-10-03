package com.rising.pos.feature.pos.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Size as AndroidSize
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.rising.pos.domain.model.CartState
import com.rising.pos.feature.pos.ScanFeedback
import com.rising.pos.ui.components.quantityLabel
import com.rising.pos.ui.theme.posMoney
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val PrimaryBlue = Color(0xFF0066FF)
private val ReticleBlue = Color(0xFF007BFF)
private val CyanLaser = Color(0xFF00D2FF)

@Composable
fun CameraBarcodeScannerDialog(
    cart: CartState,
    currencySymbol: String,
    onProcessScan: suspend (String) -> ScanFeedback,
    onUpdateQuantity: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val context = LocalContext.current
        var hasCameraPermission by remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            )
        }

        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasCameraPermission = isGranted
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (!hasCameraPermission) {
                CameraPermissionScreen(
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onDismiss = onDismiss
                )
            } else {
                MinimarketScannerContent(
                    cart = cart,
                    currencySymbol = currencySymbol,
                    onProcessScan = onProcessScan,
                    onUpdateQuantity = onUpdateQuantity,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun MinimarketScannerContent(
    cart: CartState,
    currencySymbol: String,
    onProcessScan: suspend (String) -> ScanFeedback,
    onUpdateQuantity: (String, Double) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraLensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Mekanisme Penguncian Barcode (Opsi A):
    // Barcode yang sama hanya scan 1x selama masih berada di area kamera.
    // Tidak akan auto-increment berulang kali jika barang ditahan di depan kamera.
    var lockedBarcode by remember { mutableStateOf<String?>(null) }
    var lastSeenTimestamp by remember { mutableLongStateOf(0L) }

    // Simpan feedback produk terakhir yang berhasil discan
    var lastScannedSuccess by remember { mutableStateOf<ScanFeedback.Success?>(null) }
    var lastScannedNotFound by remember { mutableStateOf<ScanFeedback.NotFound?>(null) }

    // Audio ToneGenerator
    val toneGen = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        } catch (_: Exception) {
            null
        }
    }

    // Mesin Barcode ML Kit dioptimalkan khusus format ritel (menghilangkan overhead dekoder 2D berat)
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_QR_CODE
            )
            .build()
        BarcodeScanning.getClient(options)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            try { toneGen?.release() } catch (_: Exception) { }
            try { barcodeScanner.close() } catch (_: Exception) { }
            try { cameraExecutor.shutdown() } catch (_: Exception) { }
        }
    }

    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun triggerFocus(xNormalized: Float, yNormalized: Float) {
        try {
            val factory = SurfaceOrientedMeteringPointFactory(1f, 1f)
            val point = factory.createPoint(xNormalized, yNormalized)
            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
                .setAutoCancelDuration(3, TimeUnit.SECONDS)
                .build()
            cameraControl?.startFocusAndMetering(action)
        } catch (_: Exception) { }
    }

    fun triggerBeepFeedback() {
        try {
            toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP, 130)
        } catch (_: Exception) { }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(70, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(70)
            }
        } catch (_: Exception) { }
    }

    fun handleDetectedBarcode(rawCode: String) {
        val now = System.currentTimeMillis()
        val isSameAsLocked = (rawCode == lockedBarcode)
        val timeSinceLastSeen = now - lastSeenTimestamp

        // Selalu catat waktu terakhir barcode terlihat di kamera
        lastSeenTimestamp = now

        // Jika barcode yang sama masih berada di area kamera (belum pernah dijauhkan > 1.8 detik),
        // kunci barcode aktif sehingga tidak terjadi penambahan kuantitas berulang kali.
        if (isSameAsLocked && timeSinceLastSeen < 1800L) {
            return
        }

        lockedBarcode = rawCode

        scope.launch {
            val result = onProcessScan(rawCode)
            when (result) {
                is ScanFeedback.Success -> {
                    triggerBeepFeedback()
                    lastScannedSuccess = result
                    lastScannedNotFound = null
                }
                is ScanFeedback.NotFound -> {
                    lastScannedNotFound = result
                    lastScannedSuccess = null
                }
                is ScanFeedback.NeedsVariant -> {
                    triggerBeepFeedback()
                    onDismiss()
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight
        val cutoutWidth = (screenWidth * 0.86f).coerceIn(260.dp, 380.dp)
        val cutoutHeight = (cutoutWidth * 0.58f).coerceIn(160.dp, 230.dp)

        val topBarHeight = 72.dp
        val bottomSheetEstimatedHeight = 240.dp
        val availableCenterY = topBarHeight + (screenHeight - topBarHeight - bottomSheetEstimatedHeight) / 2
        val boxTopDp = availableCenterY - (cutoutHeight / 2)
        val boxBottomDp = availableCenterY + (cutoutHeight / 2)

        // Resolusi 720p: Garis barcode tetap tajam, CPU 3x lebih ringan dibanding 1080p
        val resolutionSelector = remember {
            ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        AndroidSize(1280, 720),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()
        }

        // Re-bind kamera ketika lens facing berubah (flip kamera) atau saat provider siap
        LaunchedEffect(cameraLensFacing, previewViewRef, cameraProviderRef) {
            val pv = previewViewRef ?: return@LaunchedEffect
            val provider = cameraProviderRef ?: return@LaunchedEffect

            try {
                provider.unbindAll()

                val preview = Preview.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .build()
                    .also {
                        it.setSurfaceProvider(pv.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setResolutionSelector(resolutionSelector)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        val primaryBarcode = barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                                        if (primaryBarcode != null) {
                                            val raw = primaryBarcode.rawValue
                                            if (!raw.isNullOrBlank()) {
                                                handleDetectedBarcode(raw)
                                            }
                                        }
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraLensFacing)
                    .build()

                val camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                cameraControl = camera.cameraControl

                // Trik POS Ritel: Zoom default 1.18x agar tidak perlu menempelkan HP ke barang
                try {
                    val maxZoom = camera.cameraInfo.zoomState.value?.maxZoomRatio ?: 1f
                    camera.cameraControl.setZoomRatio(1.18f.coerceAtMost(maxZoom))
                } catch (_: Exception) { }

                // Kunci fokus otomatis tepat di tengah bingkai
                val centerYNorm = ((boxTopDp + cutoutHeight / 2) / screenHeight).coerceIn(0.1f, 0.9f)
                triggerFocus(0.5f, centerYNorm)

                // Pulihkan status senter jika sebelumnya aktif
                if (isTorchOn) {
                    camera.cameraControl.enableTorch(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 1. CameraX Preview Layer dengan Tap-To-Focus & Mode Performa Hardware
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val normX = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val normY = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                        triggerFocus(normX, normY)
                    }
                },
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    previewViewRef = this
                    ProcessCameraProvider.getInstance(ctx).addListener({
                        cameraProviderRef = ProcessCameraProvider.getInstance(ctx).get()
                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        // 2. Viewfinder Bingkai Persegi Panjang dengan Sudut Biru & Garis Laser Cyan
        val infiniteTransition = rememberInfiniteTransition(label = "laser")
        val laserProgress by infiniteTransition.animateFloat(
            initialValue = 0.08f,
            targetValue = 0.92f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "laserProgress"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val boxW = cutoutWidth.toPx()
            val boxH = cutoutHeight.toPx()
            val boxLeft = (canvasW - boxW) / 2f
            val boxTop = boxTopDp.toPx()

            // Area gelap di luar bingkai
            drawRect(
                color = Color.Black.copy(alpha = 0.50f),
                size = size
            )

            // Lubang transparan kamera
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(boxLeft, boxTop),
                size = Size(boxW, boxH),
                cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                blendMode = BlendMode.Clear
            )

            // 4 Sudut Siku Biru Tebal Melengkung Halus (Sesuai Desain Minimarket)
            val cornerRadiusPx = 16.dp.toPx()
            val cornerLenPx = 36.dp.toPx()
            val cornerColor = ReticleBlue
            val cornerStrokeStyle = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)

            // Kiri-Atas
            val pathTL = Path().apply {
                moveTo(boxLeft, boxTop + cornerLenPx)
                lineTo(boxLeft, boxTop + cornerRadiusPx)
                arcTo(
                    rect = Rect(
                        left = boxLeft,
                        top = boxTop,
                        right = boxLeft + cornerRadiusPx * 2,
                        bottom = boxTop + cornerRadiusPx * 2
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(boxLeft + cornerLenPx, boxTop)
            }
            drawPath(pathTL, cornerColor, style = cornerStrokeStyle)

            // Kanan-Atas
            val pathTR = Path().apply {
                moveTo(boxLeft + boxW - cornerLenPx, boxTop)
                lineTo(boxLeft + boxW - cornerRadiusPx, boxTop)
                arcTo(
                    rect = Rect(
                        left = boxLeft + boxW - cornerRadiusPx * 2,
                        top = boxTop,
                        right = boxLeft + boxW,
                        bottom = boxTop + cornerRadiusPx * 2
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(boxLeft + boxW, boxTop + cornerLenPx)
            }
            drawPath(pathTR, cornerColor, style = cornerStrokeStyle)

            // Kiri-Bawah
            val pathBL = Path().apply {
                moveTo(boxLeft, boxTop + boxH - cornerLenPx)
                lineTo(boxLeft, boxTop + boxH - cornerRadiusPx)
                arcTo(
                    rect = Rect(
                        left = boxLeft,
                        top = boxTop + boxH - cornerRadiusPx * 2,
                        right = boxLeft + cornerRadiusPx * 2,
                        bottom = boxTop + boxH
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(boxLeft + cornerLenPx, boxTop + boxH)
            }
            drawPath(pathBL, cornerColor, style = cornerStrokeStyle)

            // Kanan-Bawah
            val pathBR = Path().apply {
                moveTo(boxLeft + boxW - cornerLenPx, boxTop + boxH)
                lineTo(boxLeft + boxW - cornerRadiusPx, boxTop + boxH)
                arcTo(
                    rect = Rect(
                        left = boxLeft + boxW - cornerRadiusPx * 2,
                        top = boxTop + boxH - cornerRadiusPx * 2,
                        right = boxLeft + boxW,
                        bottom = boxTop + boxH
                    ),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(boxLeft + boxW, boxTop + boxH - cornerLenPx)
            }
            drawPath(pathBR, cornerColor, style = cornerStrokeStyle)

            // Garis Laser Cyan / Biru Muda Khas Scanner Minimarket
            val laserY = boxTop + (boxH * laserProgress)
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        CyanLaser.copy(alpha = 0.05f),
                        CyanLaser.copy(alpha = 0.95f),
                        Color.White,
                        CyanLaser.copy(alpha = 0.95f),
                        CyanLaser.copy(alpha = 0.05f)
                    ),
                    startX = boxLeft,
                    endX = boxLeft + boxW
                ),
                start = Offset(boxLeft + 6.dp.toPx(), laserY),
                end = Offset(boxLeft + boxW - 6.dp.toPx(), laserY),
                strokeWidth = 2.8.dp.toPx()
            )
        }

        // 3. Top Header Bar: Arrow Back, "Pindai barang", Flashlight, Flip Camera
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Pindai barang",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            // Senter / Flashlight
            IconButton(
                onClick = {
                    isTorchOn = !isTorchOn
                    cameraControl?.enableTorch(isTorchOn)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Senter",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Balik Kamera Depan / Belakang
            IconButton(
                onClick = {
                    cameraLensFacing = if (cameraLensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Balik Kamera",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 4. Instruksi di atas bingkai: "Arahkan barcode ke dalam bingkai"
        Text(
            text = "Arahkan barcode ke dalam bingkai",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (boxTopDp - 38.dp).coerceAtLeast(topBarHeight + 6.dp))
        )

        // 5. Badge Kapsul Melayang di bawah bingkai: "Pindai otomatis aktif"
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = boxBottomDp + 18.dp),
            color = Color(0xDD0F172A),
            shape = RoundedCornerShape(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pindai otomatis aktif",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Arahkan barcode • Atur jumlah di bawah",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }
        }

        // 6. Bottom Sheet Putih (Sesuai Screenshot User)
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
            ) {
                // Section 1: Produk Terakhir Discan (Format Indomie Goreng dengan Stepper Langsung)
                if (lastScannedSuccess != null) {
                    val item = lastScannedSuccess!!
                    val liveCartItem = cart.items.find { it.cartItemId == item.cartItemId }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto Produk
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            val img = liveCartItem?.product?.imageUrl ?: item.imageUrl
                            if (!img.isNullOrBlank()) {
                                AsyncImage(
                                    model = img,
                                    contentDescription = item.productName,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Inventory2,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(14.dp))

                        // Nama & Harga Satuan
                        Column(modifier = Modifier.weight(1f)) {
                            val name = liveCartItem?.let {
                                if (it.variant != null) "${it.product.name} - ${it.variant.name}" else it.product.name
                            } ?: item.productName

                            Text(
                                text = name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            val unitPrice = liveCartItem?.unitPrice ?: item.price
                            Text(
                                text = "${posMoney(unitPrice, currencySymbol)} / pcs",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // Stepper Kuantitas [ - ] [ QTY ] [ + ]
                        val currentQty = liveCartItem?.quantity ?: item.quantity
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(3.dp)
                            ) {
                                // Tombol Minus [-] (Lingkaran Putih Rapi dengan Border Halus)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                        .clickable {
                                            val targetId = liveCartItem?.cartItemId ?: item.cartItemId
                                            if (currentQty <= 1.0) {
                                                lastScannedSuccess = null
                                            }
                                            if (targetId.isNotBlank()) {
                                                onUpdateQuantity(targetId, -1.0)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Kurang",
                                        tint = Color(0xFF334155),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Teks Jumlah (Lebar terjamin & centered agar angka tidak terjepit)
                                Box(
                                    modifier = Modifier
                                        .widthIn(min = 36.dp)
                                        .padding(horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = quantityLabel(currentQty),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Tombol Plus [+] (Lingkaran Biru Rapi Berjarak Sempurna)
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue)
                                        .clickable {
                                            val targetId = liveCartItem?.cartItemId ?: item.cartItemId
                                            if (targetId.isNotBlank()) {
                                                onUpdateQuantity(targetId, +1.0)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Tambah",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                } else if (lastScannedNotFound != null) {
                    val notFound = lastScannedNotFound!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFEF2F2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Barcode Belum Terdaftar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                            Text(
                                text = "Kode: ${notFound.barcode}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                } else {
                    // Tampilan awal sebelum ada barang discan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.QrCodeScanner,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Siap memindai barang",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Arahkan kamera ke barcode kemasan produk",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFFF1F5F9),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 14.dp)
                    )
                }

                // Section 2: Ringkasan Keranjang & Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kiri: Ikon Tas Biru + Judul & Jumlah Barang
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingBag,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Keranjang",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "${quantityLabel(cart.totalItemCount)} barang",
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    // Pembatas Vertikal Halus
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(34.dp)
                            .background(Color(0xFFE2E8F0))
                    )

                    Spacer(Modifier.width(16.dp))

                    // Kanan: Total Harga
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = posMoney(cart.subtotal, currencySymbol),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Section 3: Tombol "Selesai pindai ->" (Full width, tanpa tombol input manual)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Selesai pindai",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPermissionScreen(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(PrimaryBlue.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, PrimaryBlue.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Izin Kamera Diperlukan",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Untuk menggunakan fitur scan barcode otomatis seperti kasir minimarket, mohon izinkan akses kamera.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Izinkan Kamera", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(14.dp))

        TextButton(onClick = onDismiss) {
            Text("Kembali ke Kasir", color = Color(0xFF94A3B8))
        }
    }
}
