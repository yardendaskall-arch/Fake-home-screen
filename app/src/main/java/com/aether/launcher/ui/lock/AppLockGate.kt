package com.aether.launcher.ui.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aether.launcher.util.sha256

/** Full-screen gate shown before opening a hidden/locked app: tries biometrics first, PIN as fallback. */
@Composable
fun AppLockGate(storedPinHash: String?, onUnlocked: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var pinInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val activity = context as? FragmentActivity ?: return@LaunchedEffect
        val biometricManager = BiometricManager.from(context)
        val canUseBiometrics = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
        if (canUseBiometrics) {
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onUnlocked()
                    }
                },
            )
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock app")
                    .setSubtitle("Confirm it's you")
                    .setNegativeButtonText("Use PIN instead")
                    .build()
            )
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text("Enter PIN", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pinInput,
                onValueChange = {
                    if (it.length <= 8) {
                        pinInput = it.filter(Char::isDigit)
                        error = false
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = error,
            )
            if (error) Text("Wrong PIN", color = Color.Red, modifier = Modifier.padding(top = 4.dp))
            Spacer(Modifier.height(16.dp))
            Row {
                TextButton(onClick = onCancel) { Text("Cancel") }
                Spacer(Modifier.width(12.dp))
                Button(onClick = {
                    if (storedPinHash == null || sha256(pinInput) == storedPinHash) onUnlocked() else error = true
                }) { Text("Unlock") }
            }
        }
    }
}
