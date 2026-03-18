package com.thepriceisright.ui.screens.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.thepriceisright.domain.model.*
import com.thepriceisright.scanner.CameraManager
import com.thepriceisright.ui.components.*
import com.thepriceisright.ui.theme.*

@Composable
fun ScannerScreen(
    onBarcodeScanned: (String) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.CAMERA
        val granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !uiState.permissionGranted -> {
                // Camera permission denied
                EmptyState(
                    icon = Icons.Outlined.CameraAlt,
                    title = "Camera permission needed",
                    message = "Grant camera access to scan product barcodes and compare prices instantly",
                    actionLabel = "Grant Permission",
                    onAction = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }

            uiState.isCameraActive -> {
                // Camera preview with overlay
                CameraPreview(
                    onBarcodeDetected = viewModel::onBarcodeDetected
                )

                // Scanning overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Darkened overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )

                    // Scan window
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Spacing.xl)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(280.dp, 160.dp)
                                .clip(RoundedCornerShape(Radius.lg))
                                .background(Color.Transparent)
                        ) {
                            // Corner brackets visual
                            // Top-left, top-right, bottom-left, bottom-right would be drawn here
                        }
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(Radius.full)
                        ) {
                            Text(
                                text = "Point at a barcode to scan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = Spacing.base, vertical = Spacing.sm)
                            )
                        }
                    }
                }
            }

            else -> {
                // Result view
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.base),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Scanned barcode info
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(Radius.md)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.base)
                            ) {
                                Icon(
                                    Icons.Outlined.QrCodeScanner,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Column {
                                    Text(
                                        "Barcode Scanned",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        uiState.lastScannedBarcode ?: "",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Product info
                    item {
                        when (val product = uiState.product) {
                            is Resource.Loading -> {
                                PriceCardSkeleton()
                            }
                            is Resource.Success -> {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(Spacing.base)) {
                                        Text(
                                            product.data.name,
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${product.data.brand} · ${product.data.weight}${product.data.weightUnit.abbreviation}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(Spacing.base),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Outlined.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.md))
                                        Text(
                                            product.message,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                            null -> {}
                        }
                    }

                    // Price comparison
                    when (val prices = uiState.prices) {
                        is Resource.Loading -> {
                            items(3) { PriceCardSkeleton() }
                        }
                        is Resource.Success -> {
                            val sorted = prices.data.sortedBy { it.price }
                            items(sorted) { quote ->
                                PriceComparisonCard(
                                    quote = quote,
                                    isCheapest = quote == sorted.firstOrNull()
                                )
                            }
                        }
                        is Resource.Error -> {
                            item {
                                ErrorState(
                                    message = prices.message,
                                    onRetry = viewModel::onRescanClicked
                                )
                            }
                        }
                        null -> {}
                    }

                    // Rescan button
                    item {
                        Button(
                            onClick = viewModel::onRescanClicked,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text("Scan Another Product")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onBarcodeDetected: (BarcodeResult) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).also { previewView ->
                val cameraManager = CameraManager(
                    context = ctx,
                    lifecycleOwner = lifecycleOwner,
                    previewView = previewView,
                    onBarcodeDetected = onBarcodeDetected
                )
                cameraManager.startCamera()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
