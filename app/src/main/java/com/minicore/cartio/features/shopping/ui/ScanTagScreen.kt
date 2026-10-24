package com.minicore.cartio.features.shopping.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minicore.cartio.R
import com.minicore.cartio.core.scanner.ScanFrame
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanTagScreen(
    onNavigateUp: () -> Unit,
    onTextSelected: (String) -> Unit,
    viewModel: ScanTagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ScanTagEvent.TextSelected -> {
                    onTextSelected(event.text)
                    onNavigateUp()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scan_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                !hasCameraPermission -> PermissionRationale(
                    onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    onOpenSettings = {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                        )
                    },
                    onEnterManually = onNavigateUp
                )
                uiState.status == ScanStatus.Result -> ScanResultList(
                    blocks = uiState.blocks,
                    onBlockSelected = viewModel::onBlockSelected
                )
                uiState.status == ScanStatus.Empty -> ScanEmptyState(
                    onRetry = viewModel::retry,
                    onEnterManually = onNavigateUp
                )
                uiState.status == ScanStatus.Error -> ScanErrorState(
                    onRetry = viewModel::retry,
                    onEnterManually = onNavigateUp
                )
                else -> CameraCaptureView(
                    isProcessing = uiState.status == ScanStatus.Processing,
                    onFrameCaptured = viewModel::onImageCaptured,
                    onEnterManually = onNavigateUp
                )
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    onGrant: () -> Unit,
    onOpenSettings: () -> Unit,
    onEnterManually: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.scan_permission_rationale_title), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.scan_permission_rationale_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(onClick = onGrant) { Text(stringResource(R.string.scan_grant_permission)) }
        TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.scan_open_settings)) }
        TextButton(onClick = onEnterManually) { Text(stringResource(R.string.scan_enter_manually)) }
    }
}

@Composable
private fun ScanEmptyState(onRetry: () -> Unit, onEnterManually: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.scan_no_text_found), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.scan_no_text_found_body),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.scan_retry)) }
        TextButton(onClick = onEnterManually) { Text(stringResource(R.string.scan_enter_manually)) }
    }
}

@Composable
private fun ScanErrorState(onRetry: () -> Unit, onEnterManually: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.scan_camera_unavailable), style = MaterialTheme.typography.titleMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) { Text(stringResource(R.string.scan_retry)) }
        TextButton(onClick = onEnterManually) { Text(stringResource(R.string.scan_enter_manually)) }
    }
}

private const val MAX_RESULT_BLOCKS = 20

@Composable
private fun ScanResultList(blocks: List<String>, onBlockSelected: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            stringResource(R.string.scan_pick_text),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn {
            items(blocks.take(MAX_RESULT_BLOCKS)) { block ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    onClick = { onBlockSelected(block) }
                ) {
                    Text(block, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CameraCaptureView(
    isProcessing: Boolean,
    onFrameCaptured: (ScanFrame) -> Unit,
    onEnterManually: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var bindError by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { cameraProvider?.unbindAll() }
    }

    if (bindError) {
        ScanErrorState(onRetry = { bindError = false }, onEnterManually = onEnterManually)
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val provider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                        val capture = ImageCapture.Builder().build()
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            capture
                        )
                        imageCapture = capture
                        cameraProvider = provider
                    } catch (e: Exception) {
                        bindError = true
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isProcessing) {
                CircularProgressIndicator()
            } else {
                IconButton(
                    onClick = {
                        val capture = imageCapture ?: return@IconButton
                        capture.takePicture(
                            ContextCompat.getMainExecutor(context),
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: ImageProxy) {
                                    scope.launch {
                                        val bytes = image.toJpegByteArray()
                                        val rotation = image.imageInfo.rotationDegrees
                                        image.close()
                                        onFrameCaptured(ScanFrame(bytes, rotation))
                                    }
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    bindError = true
                                }
                            }
                        )
                    }
                ) {
                    Icon(
                        Icons.Rounded.CameraAlt,
                        contentDescription = stringResource(R.string.scan_capture),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

private fun ImageProxy.toJpegByteArray(): ByteArray {
    val buffer = planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}
