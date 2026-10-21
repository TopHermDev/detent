package app.detent.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import kotlin.math.roundToInt

/** Battery, wifi, and mobile signal for the Detent status bar. The values update between [start] and [stop]. */
class DeviceStatus(private val ctx: Context) {
    /** Battery charge from 0 to 100, or -1 before the first reading. */
    var battery by mutableIntStateOf(-1)
        private set
    var charging by mutableStateOf(false)
        private set

    /** Wifi signal from 0 to 2, or -1 when no wifi network is connected. */
    var wifi by mutableIntStateOf(-1)
        private set

    /** Mobile signal from 0 to 4, or -1 on a device without a mobile radio. */
    var cell by mutableIntStateOf(-1)
        private set

    private val cm = ctx.getSystemService(ConnectivityManager::class.java)
    private val tm = ctx.getSystemService(TelephonyManager::class.java)
    private val main = Handler(Looper.getMainLooper())
    private val hasCell = ctx.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    private var on = false

    /** Signal strength in dBm of each connected wifi network. */
    private val nets = HashMap<Network, Int>()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) = readBattery(i)
    }

    private val wifiCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            nets[network] = caps.signalStrength
            wifi = nets.values.maxOf { wifiLevel(it) }
        }

        override fun onLost(network: Network) {
            nets.remove(network)
            wifi = nets.values.maxOfOrNull { wifiLevel(it) } ?: -1
        }
    }

    private var cellListener: Any? = null

    fun start() {
        if (on) return
        on = true
        ContextCompat.registerReceiver(
            ctx, batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED,
        )?.let(::readBattery)
        startWifi()
        startCell()
    }

    fun stop() {
        if (!on) return
        on = false
        runCatching { ctx.unregisterReceiver(batteryReceiver) }
        runCatching { cm?.unregisterNetworkCallback(wifiCallback) }
        stopCell()
    }

    private fun readBattery(i: Intent) {
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        if (level >= 0 && scale > 0) battery = (level * 100f / scale).roundToInt()
        val st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        charging = st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun startWifi() {
        val cm = cm ?: return
        nets.clear()
        // The callback reports only the networks that are connected now. Without one, no callback comes.
        val caps = runCatching { cm.getNetworkCapabilities(cm.activeNetwork) }.getOrNull()
        wifi = if (caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) wifiLevel(caps.signalStrength) else -1
        val req = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        runCatching { cm.registerNetworkCallback(req, wifiCallback, main) }
    }

    private fun startCell() {
        if (!hasCell || tm == null) {
            cell = -1
            return
        }
        cell = runCatching { tm.signalStrength?.level }.getOrNull() ?: 0
        cellListener = runCatching {
            if (Build.VERSION.SDK_INT >= 31) {
                val cb = object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                    override fun onSignalStrengthsChanged(ss: SignalStrength) {
                        cell = ss.level
                    }
                }
                tm.registerTelephonyCallback(ctx.mainExecutor, cb)
                cb
            } else {
                @Suppress("DEPRECATION")
                val l = object : PhoneStateListener() {
                    @Deprecated("Deprecated in Java")
                    override fun onSignalStrengthsChanged(ss: SignalStrength) {
                        cell = ss.level
                    }
                }
                @Suppress("DEPRECATION")
                tm.listen(l, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                l
            }
        }.getOrNull()
    }

    private fun stopCell() {
        val l = cellListener ?: return
        cellListener = null
        runCatching {
            if (Build.VERSION.SDK_INT >= 31 && l is TelephonyCallback) {
                tm?.unregisterTelephonyCallback(l)
            } else {
                @Suppress("DEPRECATION")
                tm?.listen(l as PhoneStateListener, PhoneStateListener.LISTEN_NONE)
            }
        }
    }

    private companion object {
        /** Wifi level from 0 to 2 for a signal strength in dBm. Without a value, the level is full. */
        fun wifiLevel(rssi: Int) = when {
            rssi == NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED -> 2
            rssi >= -67 -> 2
            rssi >= -78 -> 1
            else -> 0
        }
    }
}
