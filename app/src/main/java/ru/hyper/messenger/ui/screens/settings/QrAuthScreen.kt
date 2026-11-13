package ru.hyper.messenger.ui.screens.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.hyper.messenger.R
import ru.hyper.messenger.ui.components.DemoBadge
import ru.hyper.messenger.ui.components.HyperBackTopBar
import ru.hyper.messenger.ui.components.SystemBarsForTheme
import ru.hyper.messenger.ui.theme.HyperCyan
import ru.hyper.messenger.ui.theme.HyperNavy

@Composable
fun QrAuthScreen(
    onBack: () -> Unit,
    onOpenLoginPage: () -> Unit,
) {
    SystemBarsForTheme()

    var confirmed by rememberSaveable { mutableStateOf(false) }
    var isSimulating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            HyperBackTopBar(
                title = stringResource(R.string.qr_title),
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        if (confirmed) {
            QrSuccessContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                onDone = onBack,
                onAgain = { confirmed = false },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.qr_headline),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.qr_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(22.dp))

                QrViewfinder(
                    isSimulating = isSimulating,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(260.dp),
                )

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.qr_scan_hint),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(6.dp))
                    DemoBadge(text = stringResource(R.string.qr_demo_step))
                }

                Spacer(Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        StepRow(1, stringResource(R.string.qr_step_1))
                        Spacer(Modifier.height(14.dp))
                        StepRow(2, stringResource(R.string.qr_step_2))
                        Spacer(Modifier.height(14.dp))
                        StepRow(3, stringResource(R.string.qr_step_3))
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (!isSimulating) {
                            scope.launch {
                                isSimulating = true
                                delay(1400)
                                isSimulating = false
                                confirmed = true
                            }
                        }
                    },
                    enabled = !isSimulating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    if (isSimulating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.qr_scanning))
                    } else {
                        Text(
                            text = stringResource(R.string.qr_simulate),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onOpenLoginPage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.qr_open_page))
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = stringResource(R.string.qr_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                )

                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun QrViewfinder(
    isSimulating: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "qr-scan")
    val lineFraction by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan-line",
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(34.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D3156),
                        HyperNavy,
                        Color(0xFF020A12),
                    ),
                ),
            ),
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
        ) {
            val bracket = 28.dp.toPx()
            val stroke = 3.dp.toPx()
            val color = HyperCyan.copy(alpha = 0.9f)

            // Top-left
            drawLine(color, Offset(0f, 0f), Offset(bracket, 0f), stroke, StrokeCap.Round)
            drawLine(color, Offset(0f, 0f), Offset(0f, bracket), stroke, StrokeCap.Round)
            // Top-right
            drawLine(color, Offset(size.width - bracket, 0f), Offset(size.width, 0f), stroke, StrokeCap.Round)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, bracket), stroke, StrokeCap.Round)
            // Bottom-left
            drawLine(color, Offset(0f, size.height - bracket), Offset(0f, size.height), stroke, StrokeCap.Round)
            drawLine(color, Offset(0f, size.height), Offset(bracket, size.height), stroke, StrokeCap.Round)
            // Bottom-right
            drawLine(
                color,
                Offset(size.width - bracket, size.height),
                Offset(size.width, size.height),
                stroke,
                StrokeCap.Round,
            )
            drawLine(
                color,
                Offset(size.width, size.height - bracket),
                Offset(size.width, size.height),
                stroke,
                StrokeCap.Round,
            )

            val scanY = 12.dp.toPx() + (size.height - 24.dp.toPx()) * lineFraction
            drawLine(
                color = HyperCyan,
                start = Offset(8.dp.toPx(), scanY),
                end = Offset(size.width - 8.dp.toPx(), scanY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawLine(
                color = HyperCyan.copy(alpha = 0.16f),
                start = Offset(8.dp.toPx(), scanY - 12.dp.toPx()),
                end = Offset(size.width - 8.dp.toPx(), scanY + 12.dp.toPx()),
                strokeWidth = 24.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(88.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.QrCode2,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.54f),
                modifier = Modifier.size(54.dp),
            )
        }

        if (isSimulating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(34.dp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.qr_scanning),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun StepRow(
    number: Int,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun QrSuccessContent(
    modifier: Modifier = Modifier,
    onDone: () -> Unit,
    onAgain: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(62.dp),
            )
        }
        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.qr_success_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.qr_success_desc),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(
                text = stringResource(R.string.qr_done),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onAgain) {
            Text(stringResource(R.string.qr_again))
        }
    }
}
