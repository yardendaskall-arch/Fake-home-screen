package com.aether.launcher

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

/**
 * Deliberately plain android.widget UI (no Compose) so it still renders even if the crash
 * originated inside Compose/Compiler runtime itself. Shown by the uncaught-exception handler
 * installed in AetherApplication so a crash is readable on-device without ADB.
 */
class CrashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val trace = intent.getStringExtra(EXTRA_STACK_TRACE) ?: "No stack trace captured."

        val textView = TextView(this).apply {
            text = "Aether crashed. Copy this and send it back:\n\n$trace"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            textSize = 12f
            setPadding(32, 80, 32, 32)
            setTextIsSelectable(true)
        }
        setContentView(ScrollView(this).apply { addView(textView) })
    }

    companion object {
        const val EXTRA_STACK_TRACE = "stack_trace"
    }
}
