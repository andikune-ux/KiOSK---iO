package dev.andikune.kioskio
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.net.wifi.WifiManager
import android.text.format.Formatter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import dev.andikune.kioskio.crash.CrashReporter
import dev.andikune.kioskio.remote.RemoteServerService
import dev.andikune.kioskio.update.ReleaseUpdate
import dev.andikune.kioskio.update.UpdateChecker
import java.util.concurrent.Executors
class MainActivity:ComponentActivity(){
 private val executor=Executors.newSingleThreadExecutor()
 override fun onCreate(savedInstanceState:Bundle?){
  super.onCreate(savedInstanceState)
  val crash=CrashReporter(this).latestReport()
  if(Build.VERSION.SDK_INT>=26)ContextCompat.startForegroundService(this,Intent(this,RemoteServerService::class.java)) else startService(Intent(this,RemoteServerService::class.java))
  setContent{MaterialTheme{KioskioScreen(crash)}}
  executor.execute{runCatching{UpdateChecker(this).check()}.getOrNull()?.let{runOnUiThread{showUpdate(it)}}}
 }
 private fun showUpdate(update:ReleaseUpdate){
  android.app.AlertDialog.Builder(this).setTitle("Pembaruan Kioskio ${update.tag}")
   .setMessage(update.notes.ifBlank{"Versi baru tersedia."}.take(3000))
   .setNeutralButton("Skip"){_,_->UpdateChecker(this).skip(update.tag)}
   .setNegativeButton("Nanti",null)
   .setPositiveButton("Update"){_,_->val progress=android.app.ProgressDialog(this).apply{setMessage("Mengunduh dan memverifikasi APK…");setCancelable(false);show()};executor.execute{val result=runCatching{UpdateChecker(this).download(update)};runOnUiThread{progress.dismiss();result.exceptionOrNull()?.let{android.widget.Toast.makeText(this,it.message?: "Update gagal",android.widget.Toast.LENGTH_LONG).show()}}}}
   .show()
 }
 override fun onDestroy(){executor.shutdown();super.onDestroy()}
 @Composable private fun KioskioScreen(crash:String?){
  var motor by remember{mutableStateOf("08")};var showCrash by remember{mutableStateOf(crash!=null)}
  var hideIcon by remember{mutableStateOf(packageManager.getComponentEnabledSetting(ComponentName(this@MainActivity,packageName+".LauncherAlias"))==PackageManager.COMPONENT_ENABLED_STATE_DISABLED)}
  Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Text("Kioskio",fontSize=28.sp,fontWeight=FontWeight.Bold)
   Text("Panel pemeliharaan mesin • 45 motor",color=Color.Gray)
   Card{Column(Modifier.padding(14.dp)){Text("Controller: belum teridentifikasi",fontWeight=FontWeight.SemiBold);Text("Kontrol fisik motor terkunci sampai antarmuka controller diverifikasi.")}}
   OutlinedTextField(
    value=motor,
    onValueChange={value->motor=value.filter(Char::isDigit).take(2)},
    label={Text("Nomor motor 01–45")},
    placeholder={Text("Contoh: 08")},
    keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),
    singleLine=true,
    modifier=Modifier.fillMaxWidth().onFocusChanged { focus ->
     if (!focus.isFocused) motor = motor.toIntOrNull()?.takeIf { it in 1..45 }?.toString()?.padStart(2, '0') ?: motor
    }
   )
   Text(
    if(motor.toIntOrNull() in 1..45) "Motor dipilih: ${motor.toInt().toString().padStart(2,'0')}" else "Masukkan nomor motor 01 sampai 45",
    color=if(motor.toIntOrNull() in 1..45) MaterialTheme.colorScheme.primary else Color.Gray
   )
   Text("Perintah rencana: 432° (360° + 20%).")
   Button(onClick={},enabled=false,modifier=Modifier.fillMaxWidth()){Text("Uji motor — controller belum tersedia")}
   Card{Column(Modifier.padding(14.dp)){Text("Dashboard LAN",fontWeight=FontWeight.SemiBold);Text("Server otomatis aktif • Port 14039");Text("http://"+(applicationContext.getSystemService(WIFI_SERVICE) as WifiManager).connectionInfo.ipAddress.let{Formatter.formatIpAddress(it)}+":14039")}}
   Row(verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){Text("Sembunyikan ikon launcher",Modifier.weight(1f));Switch(checked=hideIcon,onCheckedChange={hide->
    hideIcon=hide;val c=ComponentName(this@MainActivity,packageName+".LauncherAlias")
    packageManager.setComponentEnabledSetting(c,if(hide)PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP)
   })}
  }
  if(showCrash&&crash!=null)AlertDialog(onDismissRequest={showCrash=false},title={Text("Laporan crash sebelumnya")},text={Text(crash.take(5000))},confirmButton={TextButton(onClick={val cm=getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager;cm.setPrimaryClip(android.content.ClipData.newPlainText("Kioskio crash log",crash))}){Text("Salin Log")}},dismissButton={TextButton(onClick={CrashReporter(this@MainActivity).markViewed();showCrash=false}){Text("Kembali")}})
 }
}
