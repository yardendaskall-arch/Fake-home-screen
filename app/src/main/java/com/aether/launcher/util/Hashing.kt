package com.aether.launcher.util

import java.security.MessageDigest

/** One-way hash for the app-lock PIN; only the hash is ever persisted (DataStore), never the raw PIN. */
fun sha256(value: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
    return digest.joinToString("") { "%02x".format(it) }
}
