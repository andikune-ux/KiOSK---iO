package dev.andikune.kioskio.update
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import dev.andikune.kioskio.BuildConfig
import dev.andikune.kioskio.storage.AppStorage
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
data class ReleaseUpdate(val tag:String,val notes:String,val apkUrl:String,val sha256:String?)
class UpdateChecker(private val context:Context){
 private val prefs=context.getSharedPreferences("update_state",Context.MODE_PRIVATE)
 private val storage=AppStorage(context)
 fun check():ReleaseUpdate?{
  val c=URL("https://api.github.com/repos/andikune-ux/KiOSK---iO/releases/latest").openConnection() as HttpURLConnection
  c.connectTimeout=5000;c.readTimeout=5000;c.setRequestProperty("Accept","application/vnd.github+json");c.setRequestProperty("User-Agent","Kioskio-Android")
  try{
   if(c.responseCode!=200)return null
   val release=JSONObject(c.inputStream.bufferedReader().use{it.readText()})
   if(release.optBoolean("draft")||release.optBoolean("prerelease"))return null
   val tag=release.optString("tag_name")
   if(compare(tag,BuildConfig.VERSION_NAME)<=0||prefs.getString("skip",null)==tag)return null
   val assets=release.optJSONArray("assets")?:return null
   for(i in 0 until assets.length()){
    val a=assets.getJSONObject(i)
    if(a.optString("name").endsWith(".apk",true)){
     val digest=a.optString("digest").removePrefix("sha256:").takeIf{it.length==64}
     return ReleaseUpdate(tag,release.optString("body").take(10000),a.optString("browser_download_url"),digest)
    }
   }
   return null
  }finally{c.disconnect()}
 }
 private fun compare(a:String,b:String):Int{
  val x=a.removePrefix("v").split(".").map{it.toIntOrNull()?:0};val y=b.removePrefix("v").split(".").map{it.toIntOrNull()?:0}
  for(i in 0 until maxOf(x.size,y.size)){val d=x.getOrElse(i){0}.compareTo(y.getOrElse(i){0});if(d!=0)return d};return 0
 }
 fun skip(tag:String){prefs.edit().putString("skip",tag).apply()}
 fun download(update:ReleaseUpdate):File{
  val c=URL(update.apkUrl).openConnection() as HttpURLConnection;c.connectTimeout=10000;c.readTimeout=20000
  try{
   check(c.responseCode==200){"HTTP ${c.responseCode}"}
   val dest=File(storage.updates,"Kioskio-${update.tag}.apk");val temp=File(storage.updates,dest.name+".part")
   c.inputStream.use{input->temp.outputStream().use{input.copyTo(it)}}
   check(temp.length()>0){"APK kosong"}
   update.sha256?.let{expected->
    val md=MessageDigest.getInstance("SHA-256");temp.inputStream().use{input->val b=ByteArray(8192);while(true){val n=input.read(b);if(n<0)break;md.update(b,0,n)}}
    check(md.digest().joinToString(""){"%02x".format(it)}.equals(expected,true)){"Checksum APK tidak cocok"}
   }
   check(temp.renameTo(dest)){"Gagal menyimpan APK"}
   val uri=FileProvider.getUriForFile(context,context.packageName+".files",dest)
   val intent=Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
   if(Build.VERSION.SDK_INT>=26&&!context.packageManager.canRequestPackageInstalls())context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) else context.startActivity(intent)
   return dest
  }finally{c.disconnect()}
 }
}
