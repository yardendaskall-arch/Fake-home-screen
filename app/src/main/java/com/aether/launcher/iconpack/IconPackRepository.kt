package com.aether.launcher.iconpack

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import com.aether.launcher.data.model.IconShape
import org.xmlpull.v1.XmlPullParser

data class InstalledIconPack(val packageName: String, val label: String)

/**
 * Resolves the drawable for a launchable activity, applying (in priority order):
 *  1. A user-selected third-party icon pack's themed icon (parsed straight from its appfilter.xml)
 *  2. A forced icon shape mask (circle / squircle / rounded square / teardrop) over the app's own icon
 *  3. The app's own adaptive/legacy icon, untouched
 */
class IconPackRepository(private val context: Context) {

    private var activePackPackage: String? = null
    private var appFilterMap: Map<String, String>? = null // componentName -> drawable resource name
    private var iconMaskShape: IconShape = IconShape.SYSTEM

    fun setActiveIconPack(packageName: String?) {
        activePackPackage = packageName
        appFilterMap = packageName?.let { loadAppFilter(it) }
    }

    fun setIconShape(shape: IconShape) {
        iconMaskShape = shape
    }

    fun findInstalledIconPacks(): List<InstalledIconPack> {
        val pm = context.packageManager
        val actions = listOf("com.aether.launcher.ICON_PACK", "com.novalauncher.THEME", "com.anddoes.launcher.THEME")
        return actions.flatMap { action ->
            pm.queryIntentActivities(Intent(action), PackageManager.MATCH_DEFAULT_ONLY)
        }.distinctBy { it.activityInfo.packageName }.map {
            InstalledIconPack(it.activityInfo.packageName, it.loadLabel(pm).toString())
        }
    }

    fun resolveIcon(activity: LauncherActivityInfo, context: Context): Drawable {
        val componentKey = "${activity.applicationInfo.packageName}/${activity.componentName.className}"
        val themed = activePackPackage?.let { pack -> loadThemedIcon(pack, componentKey) }
        if (themed != null) return themed

        val original = activity.getIcon(0)
        return if (iconMaskShape == IconShape.SYSTEM) original else maskIcon(original, iconMaskShape)
    }

    private fun loadThemedIcon(packPackage: String, componentKey: String): Drawable? {
        val drawableName = appFilterMap?.get(componentKey) ?: return null
        return runCatching {
            val packResources = context.packageManager.getResourcesForApplication(packPackage)
            val resId = packResources.getIdentifier(drawableName, "drawable", packPackage)
            if (resId == 0) null else packResources.getDrawable(resId, context.theme)
        }.getOrNull()
    }

    /** Parses <item component="ComponentInfo{pkg/activity}" drawable="name" /> entries out of appfilter.xml. */
    private fun loadAppFilter(packPackage: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        runCatching {
            val res = context.packageManager.getResourcesForApplication(packPackage)
            val xmlId = res.getIdentifier("appfilter", "xml", packPackage)
            if (xmlId == 0) return@runCatching
            val parser = res.getXml(xmlId)
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                if (parser.eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    val component = parser.getAttributeValue(null, "component")
                    val drawable = parser.getAttributeValue(null, "drawable")
                    if (component != null && drawable != null) {
                        // component looks like: ComponentInfo{com.pkg/com.pkg.Activity}
                        val key = component.removePrefix("ComponentInfo{").removeSuffix("}")
                        result[key] = drawable
                    }
                }
                parser.next()
            }
        }
        return result
    }

    private fun maskIcon(source: Drawable, shape: IconShape): Drawable {
        val size = 108
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val path = shapePath(shape, size.toFloat())
        canvas.clipPath(path)
        source.setBounds(0, 0, size, size)
        source.draw(canvas)
        return BitmapDrawable(context.resources, bitmap)
    }

    private fun shapePath(shape: IconShape, size: Float): Path {
        val path = Path()
        when (shape) {
            IconShape.CIRCLE -> path.addCircle(size / 2, size / 2, size / 2, Path.Direction.CW)
            IconShape.SQUIRCLE -> path.addRoundRect(0f, 0f, size, size, size * 0.33f, size * 0.33f, Path.Direction.CW)
            IconShape.ROUNDED_SQUARE -> path.addRoundRect(0f, 0f, size, size, size * 0.16f, size * 0.16f, Path.Direction.CW)
            IconShape.TEARDROP -> {
                val r = size * 0.16f
                path.addRoundRect(0f, 0f, size, size, floatArrayOf(r, r, r, r, r, r, size * 0.45f, size * 0.45f), Path.Direction.CW)
            }
            IconShape.SYSTEM -> path.addRect(0f, 0f, size, size, Path.Direction.CW)
        }
        return path
    }
}
