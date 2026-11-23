package dev.andikune.kioskio.remote
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
class RemoteHttpServer(private val port:Int=14039,private val statusProvider:()->String) {
 private val running=AtomicBoolean(false)
 private val executor=Executors.newCachedThreadPool()
 @Volatile private var socket:ServerSocket?=null
 fun start(){
  if(!running.compareAndSet(false,true))return
  executor.execute {
   try { ServerSocket(port).use { server -> socket=server; while(running.get()){val c=server.accept();executor.execute{handle(c)}} } }
   catch(_:Exception){} finally {socket=null;running.set(false)}
  }
 }
 private fun handle(client:Socket){
  client.use { c ->
   c.soTimeout=3000
   val reader=BufferedReader(InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))
   val path=reader.readLine().orEmpty().split(" ").getOrNull(1).orEmpty()
   while(reader.readLine()?.isNotEmpty()==true){}
   val (type,body,status)=when(path){
    "/","/index.html"->Triple("text/html; charset=utf-8",PAGE,"200 OK")
    "/api/status"->Triple("application/json",statusProvider(),"200 OK")
    "/api/motors/test"->Triple("application/json","""{"error":"MOTOR_CONTROL_LOCKED","message":"Controller belum diverifikasi"}""","423 Locked")
    else->Triple("application/json","""{"error":"NOT_FOUND"}""","404 Not Found")
   }
   val bytes=body.toByteArray()
   val out=c.getOutputStream()
   out.write("HTTP/1.1 $status\r\nContent-Type: $type\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n".toByteArray())
   out.write(bytes);out.flush()
  }
 }
 fun stop(){running.set(false);runCatching{socket?.close()}}
 companion object {
  private const val PAGE="""<!doctype html><html lang="id"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Kioskio</title><style>body{font:16px system-ui;background:#f3f5f8;color:#17202a;padding:24px}.card{background:white;border-radius:18px;padding:18px;margin:12px 0}small{color:#667085}</style><h1>Kioskio</h1><small>Dashboard pemeliharaan lokal</small><section class="card"><b>Status controller</b><p id="s">Memuat…</p></section><section class="card">Motor 01–45<p>Kontrol fisik terkunci sampai controller diverifikasi.</p></section><script>fetch('/api/status').then(r=>r.json()).then(x=>s.textContent=x.controller).catch(()=>s.textContent='Offline')</script></html>"""
 }
}
