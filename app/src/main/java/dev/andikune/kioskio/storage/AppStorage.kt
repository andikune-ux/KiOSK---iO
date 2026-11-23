package dev.andikune.kioskio.storage
import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
class AppStorage(context: Context) {
 private val root=File(context.filesDir,"Kioskio")
 val updates=File(root,"Pembaruan Aplikasi")
 val crashes=File(root,"Log Crash")
 init { check(updates.mkdirs()||updates.isDirectory); check(crashes.mkdirs()||crashes.isDirectory) }
 fun atomicWrite(target:File,bytes:ByteArray) {
  require(target.canonicalPath.startsWith(root.canonicalPath+File.separator))
  val temp=File(target.parentFile,target.name+".tmp")
  FileOutputStream(temp).use { it.write(bytes); it.fd.sync() }
  if(!temp.renameTo(target)){temp.delete();error("Gagal mengganti file")}
 }
 fun trimCrashLogs(maxFiles:Int=12) {
  crashes.listFiles()?.filter{it.isFile}?.sortedByDescending{it.lastModified()}?.drop(maxFiles)?.forEach{it.delete()}
 }
}
