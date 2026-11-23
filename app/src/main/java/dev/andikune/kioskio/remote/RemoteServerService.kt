package dev.andikune.kioskio.remote
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
class RemoteServerService:Service(){
 private var server:RemoteHttpServer?=null
 override fun onCreate(){
  super.onCreate()
  if(Build.VERSION.SDK_INT>=26)getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(CHANNEL,"Server Kioskio",NotificationManager.IMPORTANCE_LOW))
  val n=NotificationCompat.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.stat_sys_upload_done).setContentTitle("Kioskio aktif").setContentText("Dashboard lokal port 14039").setOngoing(true).build()
  startForeground(14039,n)
  server=RemoteHttpServer(statusProvider={"""{"app":"Kioskio","controller":"Belum teridentifikasi","motorCount":45,"motorControlEnabled":false}"""}).also{it.start()}
 }
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int)=START_STICKY
 override fun onDestroy(){server?.stop();server=null;super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
 companion object{private const val CHANNEL="kioskio_server"}
}
