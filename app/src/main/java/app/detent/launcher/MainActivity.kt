package app.detent.launcher

import android.app.ActivityOptions
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.core.app.NotificationManagerCompat
import app.detent.launcher.ui.DetentRoot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.concurrent.thread

class MainActivity : ComponentActivity(), Host {
    // AndroidUiDispatcher.Main carries the frame clock that the dial loop waits on.
    private val scope = CoroutineScope(SupervisorJob() + AndroidUiDispatcher.Main)
    private lateinit var repo: AppsRepository
    private lateinit var state: LauncherState
    private val launcherApps by lazy { getSystemService(LauncherApps::class.java) }
    private val roleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        state.refreshSystem()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bars = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(bars, bars)
        super.onCreate(savedInstanceState)
        window.isNavigationBarContrastEnforced = false
        repo = AppsRepository(applicationContext)
        state = LauncherState(this, repo, Prefs(applicationContext), Feedback(applicationContext), scope)
        repo.start()
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = state.back()
        })
        setContent { DetentRoot(state) }
    }

    override fun onResume() {
        super.onResume()
        state.onResume()
    }

    override fun onPause() {
        super.onPause()
        state.onPause()
    }

    override fun onStop() {
        super.onStop()
        state.onStop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val home = intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)
        val alreadyHome = hasWindowFocus() && intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == 0
        if (home && alreadyHome) state.onHome()
    }

    override fun onDestroy() {
        repo.stop()
        state.release()
        scope.cancel()
        super.onDestroy()
    }

    private fun reveal(b: Rect?): Bundle? = b?.let {
        ActivityOptions.makeClipRevealAnimation(window.decorView, it.left, it.top, it.width(), it.height()).toBundle()
    }

    override fun launchApp(app: AppEntry, bounds: Rect?) =
        launcherApps.startMainActivity(app.component, app.user, bounds, reveal(bounds))

    override fun launchShortcut(info: ShortcutInfo, bounds: Rect?) =
        launcherApps.startShortcut(info, bounds, reveal(bounds))

    override fun showAppInfo(app: AppEntry, bounds: Rect?) =
        launcherApps.startAppDetailsActivity(app.component, app.user, bounds, reveal(bounds))

    override fun isDefaultHome(): Boolean =
        getSystemService(RoleManager::class.java)?.isRoleHeld(RoleManager.ROLE_HOME) == true

    override fun requestHome() {
        val rm = getSystemService(RoleManager::class.java)
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_HOME) && !rm.isRoleHeld(RoleManager.ROLE_HOME)) {
            roleRequest.launch(rm.createRequestRoleIntent(RoleManager.ROLE_HOME))
        } else {
            open(Intent(Settings.ACTION_HOME_SETTINGS))
        }
    }

    override fun hasNotificationAccess() =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    override fun openNotificationAccess() {
        val cn = ComponentName(this, UnreadService::class.java).flattenToString()
        val detail = if (Build.VERSION.SDK_INT >= 30) {
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS)
                .putExtra(Settings.EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME, cn)
        } else null
        if (detail == null || !open(detail)) open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun setWallpaper(theme: ThemeDef, pal: Palette, left: Boolean, done: (Boolean) -> Unit) {
        val size = if (Build.VERSION.SDK_INT >= 30) {
            windowManager.maximumWindowMetrics.bounds.let { it.width() to it.height() }
        } else {
            @Suppress("DEPRECATION")
            DisplayMetrics().also { windowManager.defaultDisplay.getRealMetrics(it) }.let { it.widthPixels to it.heightPixels }
        }
        val app = applicationContext
        thread(name = "detent-wallpaper") {
            val ok = runCatching { SystemWallpaper.apply(app, theme, pal, left, size.first, size.second) }.isSuccess
            runOnUiThread { done(ok) }
        }
    }

    private fun open(intent: Intent): Boolean = try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
