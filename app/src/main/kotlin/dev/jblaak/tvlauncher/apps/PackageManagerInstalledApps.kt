package dev.jblaak.tvlauncher.apps

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import dev.jblaak.tvlauncher.home.AppBanner
import dev.jblaak.tvlauncher.home.InstalledApps

internal class PackageManagerInstalledApps(
    private val packageManager: PackageManager,
) : InstalledApps {

    override fun isInstalled(packageName: String): Boolean = launchIntent(packageName) != null

    override fun banner(packageName: String): AppBanner? {
        val drawable = launchIntent(packageName)?.let { packageManager.getActivityBanner(it) }
            ?: applicationBanner(packageName)
            ?: return null
        return drawable.toAppBanner()
    }

    fun launchIntent(packageName: String): Intent? = packageManager.getLeanbackLaunchIntentForPackage(packageName)
        ?: packageManager.getLaunchIntentForPackage(packageName)

    private fun applicationBanner(packageName: String): Drawable? = try {
        packageManager.getApplicationBanner(packageName)
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private fun Drawable.toAppBanner(): AppBanner {
        val bitmap = toBitmap(width = 320, height = 180)
        val sample = bitmap.scale(32, 18)
        val pixels = IntArray(sample.width * sample.height)
        sample.getPixels(pixels, 0, sample.width, 0, 0, sample.width, sample.height)
        return AppBanner(
            painter = BitmapPainter(bitmap.asImageBitmap()),
            colors = bannerColors(pixels.map { Color(it) }),
        )
    }
}
