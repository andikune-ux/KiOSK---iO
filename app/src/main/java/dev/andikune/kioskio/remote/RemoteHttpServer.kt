package dev.andikune.kioskio.remote
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class RemoteHttpServer(private val port:Int=14039, private val statusProvider:()->String) {
 private val running=AtomicBoolean(false)
 private val executor=Executors.newCachedThreadPool()
 @Volatile private var socket:ServerSocket?=null

 fun start(){
  if(!running.compareAndSet(false,true)) return
  executor.execute {
   try {
    ServerSocket(port).use { server ->
     socket=server
     while(running.get()) {
      val client=server.accept()
      executor.execute { handle(client) }
     }
    }
   } catch(_:Exception) {
   } finally {
    socket=null
    running.set(false)
   }
  }
 }

 private fun handle(client:Socket){
  client.use { c ->
   c.soTimeout=3000
   val reader=BufferedReader(InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))
   val path=reader.readLine().orEmpty().split(" ").getOrNull(1).orEmpty()
   while(reader.readLine()?.isNotEmpty()==true) {}
   val (type,body,status)=when(path) {
    "/", "/index.html" -> Triple("text/html; charset=utf-8",PAGE,"200 OK")
    "/api/status" -> Triple("application/json; charset=utf-8",statusProvider(),"200 OK")
    "/api/motors/test" -> Triple("application/json; charset=utf-8","""{"error":"MOTOR_CONTROL_LOCKED","message":"Controller belum diverifikasi"}""","423 Locked")
    else -> Triple("application/json; charset=utf-8","""{"error":"NOT_FOUND"}""","404 Not Found")
   }
   val bytes=body.toByteArray(StandardCharsets.UTF_8)
   val out=c.getOutputStream()
   out.write("HTTP/1.1 $status\r\nContent-Type: $type\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\nCache-Control: no-store\r\n\r\n".toByteArray(StandardCharsets.UTF_8))
   out.write(bytes)
   out.flush()
  }
 }

 fun stop(){
  running.set(false)
  runCatching { socket?.close() }
  executor.shutdownNow()
 }

 companion object {
  private const val PAGE = """<!doctype html>
<html lang="id">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1,viewport-fit=cover">
<meta name="theme-color" content="#f3f5f8">
<title>Kioskio Remote</title>
<style>
:root{font-family:system-ui,-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;color:#182230;background:#f3f5f8}
*{box-sizing:border-box}body{margin:0;padding:22px 16px 36px;max-width:760px;margin-inline:auto}
h1{font-size:27px;margin:0 0 4px} .sub{color:#667085;font-size:14px;margin:0 0 20px}
.card{background:#fff;border:1px solid #e7eaf0;border-radius:18px;padding:17px;margin:12px 0;box-shadow:0 4px 18px #1b2a3a0a}
.label{font-size:14px;font-weight:650;margin-bottom:9px;display:block}
.status{font-size:15px;margin:0}.muted{font-size:13px;color:#667085;line-height:1.5}
.field{display:flex;gap:9px;margin:10px 0 14px}
input{min-width:0;flex:1;border:1px solid #cfd6e1;border-radius:12px;padding:13px 14px;font-size:18px;font-weight:650;background:#fff;color:#182230}
button{font:inherit;cursor:pointer}
.confirm{border:0;border-radius:12px;background:#172b4d;color:white;padding:0 18px;font-weight:650}
.grid{display:grid;grid-template-columns:repeat(5,minmax(0,1fr));gap:8px}
.motor{min-height:45px;border:1px solid #d9dee7;background:#fff;border-radius:11px;font-weight:650;color:#344054}
.motor.selected{background:#e8f0ff;border:2px solid #3975d7;color:#174ea6}
.test{width:100%;padding:13px;border:0;border-radius:12px;background:#aab2bf;color:#fff;font-weight:700;margin-top:12px}
.notice{background:#fff7e6;border:1px solid #f5dfad;color:#79530a;padding:12px;border-radius:12px;font-size:13px;line-height:1.5}
@media(min-width:560px){.grid{grid-template-columns:repeat(9,minmax(0,1fr))}}
</style>
</head>
<body>
<h1>Kioskio</h1><p class="sub">Remote maintenance · jaringan Wi-Fi lokal</p>
<section class="card"><span class="label">Status controller</span><p class="status" id="controller">Memuat status…</p><p class="muted" id="serverState">Memeriksa koneksi…</p></section>
<section class="card">
<span class="label">Pilih motor (01–45)</span>
<div class="field">
<input id="motorInput" type="text" inputmode="numeric" pattern="[0-9]*" maxlength="2" autocomplete="off" placeholder="Masukkan 01–45" aria-label="Nomor motor 01 sampai 45">
<button class="confirm" id="selectBtn" type="button">Pilih</button>
</div>
<div class="grid" id="motorGrid" aria-label="Daftar motor"></div>
<p class="muted" id="selection">Belum ada motor dipilih.</p>
<div class="notice">Kontrol fisik masih terkunci sampai papan pengendali dan protokol komunikasinya diverifikasi. Pemilihan nomor di halaman ini belum memutar motor.</div>
<button class="test" type="button" disabled>Uji motor — belum tersedia</button>
</section>
<script>
(function(){
 const input=document.getElementById('motorInput');
 const grid=document.getElementById('motorGrid');
 const selection=document.getElementById('selection');
 let selected=0;
 function selectMotor(n){
  if(!Number.isInteger(n)||n<1||n>45){selection.textContent='Masukkan nomor motor 01 sampai 45.';return;}
  selected=n;input.value=String(n).padStart(2,'0');
  selection.textContent='Motor terpilih: '+input.value;
  grid.querySelectorAll('button').forEach((b,i)=>b.classList.toggle('selected',i+1===n));
 }
 for(let n=1;n<=45;n++){
  const b=document.createElement('button');b.type='button';b.className='motor';b.textContent=String(n).padStart(2,'0');
  b.addEventListener('click',()=>selectMotor(n));grid.appendChild(b);
 }
 document.getElementById('selectBtn').addEventListener('click',()=>selectMotor(Number(input.value)));
 input.addEventListener('input',()=>{input.value=input.value.replace(/[^0-9]/g,'').slice(0,2);if(input.value.length===2)selectMotor(Number(input.value));});
 input.addEventListener('keydown',e=>{if(e.key==='Enter'){e.preventDefault();selectMotor(Number(input.value));}});
 fetch('/api/status',{cache:'no-store'}).then(r=>{if(!r.ok)throw new Error('HTTP '+r.status);return r.json();}).then(data=>{
  document.getElementById('controller').textContent=data.controller||'Status tidak tersedia';
  document.getElementById('serverState').textContent='Terhubung · '+(data.motorCount||45)+' motor terdaftar';
 }).catch(()=>{document.getElementById('controller').textContent='Tidak dapat membaca status';document.getElementById('serverState').textContent='Server dapat dijangkau, tetapi endpoint status gagal.';});
})();
</script>
</body></html>"""
 }
}
