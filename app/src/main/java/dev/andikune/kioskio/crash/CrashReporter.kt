package dev.andikune.kioskio.crash
import android.content.Context
import android.os.Build
import dev.andikune.kioskio.storage.AppStorage
import java.io.File
import java.util.Date
class CrashReporter(context:Context) {
 private val app=context.applicationContext
 private val storage=AppStorage(app)
 private val previous=Thread.getDefaultUncaughtExceptionHandler()
 fun install(){
  Thread.setDefaultUncaughtExceptionHandler { thread,error ->
   runCatching {
    val stamp=System.currentTimeMillis()
    val report=buildString {
     appendLine("Kioskio Crash Report"); appendLine("Time: "+Date(stamp))
     appendLine("Version: "+app.packageManager.getPackageInfo(app.packageName,0).versionName)
     appendLine("Android: "+Build.VERSION.RELEASE+" API "+Build.VERSION.SDK_INT)
     appendLine("Device: "+Build.MANUFACTURER+" "+Build.MODEL)
     appendLine("Thread: "+thread.name); appendLine("Exception: "+error.javaClass.name)
     appendLine("Message: "+error.message.orEmpty().take(2000)); appendLine()
     error.stackTrace.take(100).forEach{appendLine(it.toString())}
    }
    storage.atomicWrite(File(storage.crashes,"crash-"+stamp+".txt"),report.toByteArray())
    storage.trimCrashLogs()
   }
   previous?.uncaughtException(thread,error)
  }
 }
 fun latestReport():String?{
  val prefs=app.getSharedPreferences("crash_state",Context.MODE_PRIVATE)
  val latest=storage.crashes.listFiles()?.filter{it.isFile&&it.name.endsWith(".txt")}?.maxByOrNull{it.lastModified()}?:return null
  if(latest.lastModified()<=prefs.getLong("last_seen",0L))return null
  return latest.readText()
 }
 fun markViewed(){val latest=storage.crashes.listFiles()?.filter{it.isFile&&it.name.endsWith(".txt")}?.maxByOrNull{it.lastModified()};app.getSharedPreferences("crash_state",Context.MODE_PRIVATE).edit().putLong("last_seen",latest?.lastModified()?:System.currentTimeMillis()).apply()}
}
