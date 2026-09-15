package com.aether.launcher.gesture

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.aether.launcher.data.model.GestureAction
import com.aether.launcher.data.model.GestureSlot
import kotlin.math.abs

/**
 * Attaches Aether's full home-screen gesture surface to a composable: vertical swipes, edge
 * swipes, a two-finger pinch, and a double-tap, each dispatched through the user's configurable
 * [GestureSlot] -> [GestureAction] map (see Settings > Gestures).
 */
fun Modifier.aetherGestures(
    gestureMap: Map<GestureSlot, GestureAction>,
    onAction: (GestureAction) -> Unit,
): Modifier = this
    .pointerInput(gestureMap) {
        awaitEachGesture {
            var event = awaitPointerEvent()
            val downId = event.changes.first().id
            var totalDrag = Offset.Zero
            var maxPointerCount = 1
            while (event.changes.any { it.pressed }) {
                maxPointerCount = maxOf(maxPointerCount, event.changes.size)
                val change = event.changes.firstOrNull { it.id == downId } ?: event.changes.first()
                totalDrag += change.positionChange()
                event = awaitPointerEvent()
            }

            val isMostlyVertical = abs(totalDrag.y) > abs(totalDrag.x) * 1.5f
            val slot = when {
                maxPointerCount >= 2 -> GestureSlot.PINCH_IN
                isMostlyVertical && totalDrag.y < -80 -> GestureSlot.SWIPE_UP
                isMostlyVertical && totalDrag.y > 80 -> GestureSlot.SWIPE_DOWN
                !isMostlyVertical && totalDrag.x < -80 -> GestureSlot.EDGE_SWIPE_LEFT
                !isMostlyVertical && totalDrag.x > 80 -> GestureSlot.EDGE_SWIPE_RIGHT
                else -> null
            }
            slot?.let { gestureMap[it] }?.let(onAction)
        }
    }
    .pointerInput(gestureMap) {
        detectTapGestures(
            onDoubleTap = { gestureMap[GestureSlot.DOUBLE_TAP]?.let(onAction) },
        )
    }
