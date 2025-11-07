package com.mad.myfitnesstrackingapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.mad.myfitnesstrackingapp.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

// Reusable types
enum class TopToastType { SUCCESS, ERROR, INFO }

data class TopToastState(
    val visible: Boolean = false,
    val text: String = "",
    val type: TopToastType = TopToastType.INFO,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
    val autoDismissMs: Long = 2500L
)

/**
 * Singleton controller: call TopToastController.show(...) from any composable
 */
object TopToastController {
    private val _state = mutableStateOf(TopToastState())
    val state: State<TopToastState> get() = _state

    fun show(
        text: String,
        type: TopToastType = TopToastType.INFO,
        actionLabel: String? = null,
        action: (() -> Unit)? = null,
        autoDismissMs: Long = 2500L
    ) {
        _state.value = TopToastState(
            visible = true,
            text = text,
            type = type,
            actionLabel = actionLabel,
            action = action,
            autoDismissMs = autoDismissMs
        )
    }

    fun dismiss() {
        _state.value = _state.value.copy(visible = false)
    }
}

/**
 * TopToastHost:
 * - topOffset: how far below the top of the window (e.g., status bar padding + extra)
 * - usePopup: when true the toast is rendered inside a Popup window above everything (recommended)
 *
 * You can place TopToastHost() anywhere (e.g., at top of a screen). When usePopup = true it
 * will still appear on top of all content.
 */
@Composable
fun TopToastHost(
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    topOffset: Dp = 6.dp,
    usePopup: Boolean = true
) {
    val state by TopToastController.state
    val density = LocalDensity.current

    // choose visuals per type (no type annotation on destructuring)
    val pair = when (state.type) {
        TopToastType.SUCCESS -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFF16A34A), Color(0xFF059669))),
            painterResource(id = R.drawable.ic_checked_done24px)
        )
        TopToastType.ERROR -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
            painterResource(id = R.drawable.ic_error_24px)
        )
        TopToastType.INFO -> Pair(
            Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0891B2))),
            painterResource(id = R.drawable.ic_info_24px)
        )
    }
    val bgBrush = pair.first
    val iconPainter: Painter = pair.second

    // the actual composable content of the toast (kept as a lambda for reuse)
    val toastContent: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = state.visible,
            enter = slideInVertically(
                initialOffsetY = { -it },
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp))
                        .background(brush = bgBrush, shape = RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = iconPainter,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = state.text,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    state.actionLabel?.let { label ->
                        TextButton(onClick = {
                            state.action?.invoke()
                            TopToastController.dismiss()
                            onDismiss?.invoke()
                        }) {
                            Text(label.uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                LaunchedEffect(state.visible) {
                    if (state.visible) {
                        val ms = state.autoDismissMs
                        if (ms > 0) {
                            delay(ms)
                            TopToastController.dismiss()
                            onDismiss?.invoke()
                        }
                    }
                }
            }
        }
    }

    if (usePopup) {
        // Convert topOffset to px for popup offset
        val yOffsetPx = with(density) { topOffset.toPx().toInt() }
        // Popup places content in a separate window above app UI
        Popup(
            alignment = Alignment.TopCenter,
            offset = IntOffset(0, yOffsetPx),
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            // Constrain width of the popup content to the window width using a Box
            Box(modifier = Modifier
                .fillMaxWidth()
                .then(modifier)
            ) {
                toastContent()
            }
        }
    } else {
        // Fallback (in-app overlay using zIndex)
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = topOffset)
        ) {
            toastContent()
        }
    }
}
