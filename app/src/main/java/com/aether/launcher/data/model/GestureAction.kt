package com.aether.launcher.data.model

/** Every gesture slot the launcher recognizes and lets the user remap in Settings. */
enum class GestureSlot { SWIPE_UP, SWIPE_DOWN, DOUBLE_TAP, PINCH_IN, EDGE_SWIPE_LEFT, EDGE_SWIPE_RIGHT }

enum class GestureAction(val label: String) {
    OPEN_APP_DRAWER("Open app drawer"),
    OPEN_NOTIFICATIONS("Open notification shade"),
    OPEN_QUICK_SETTINGS("Open quick settings"),
    LOCK_SCREEN("Lock screen"),
    OPEN_WIDGET_STACK("Open widget stack"),
    OPEN_SEARCH("Open search"),
    PREVIOUS_APP("Switch to previous app"),
    NONE("Do nothing");
}

/** Default gesture map new installs start with; fully overridable per-slot in Settings. */
val DEFAULT_GESTURE_MAP: Map<GestureSlot, GestureAction> = mapOf(
    GestureSlot.SWIPE_UP to GestureAction.OPEN_APP_DRAWER,
    GestureSlot.SWIPE_DOWN to GestureAction.OPEN_NOTIFICATIONS,
    GestureSlot.DOUBLE_TAP to GestureAction.LOCK_SCREEN,
    GestureSlot.PINCH_IN to GestureAction.OPEN_WIDGET_STACK,
    GestureSlot.EDGE_SWIPE_LEFT to GestureAction.PREVIOUS_APP,
    GestureSlot.EDGE_SWIPE_RIGHT to GestureAction.OPEN_QUICK_SETTINGS,
)
