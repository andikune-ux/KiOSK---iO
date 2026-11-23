package dev.andikune.kioskio
import android.app.Application
import dev.andikune.kioskio.crash.CrashReporter
class KioskioApplication:Application(){override fun onCreate(){super.onCreate();CrashReporter(this).install()}}
