package dev.andikune.kioskio.remote
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket
import java.net.Socket
class RemoteHttpServerTest{
 @Test fun exposesStatusButRejectsPhysicalMotorControl(){
  val probe=ServerSocket(0);val port=probe.localPort;probe.close()
  val server=RemoteHttpServer(port){"""{"controller":"not-configured"}"""}
  server.start()
  try{
   val status=request(port,"GET /api/status HTTP/1.1")
   assertTrue(status.contains("200 OK"));assertTrue(status.contains("not-configured"))
   val motor=request(port,"POST /api/motors/test HTTP/1.1")
   assertTrue(motor.contains("423 Locked"));assertTrue(motor.contains("MOTOR_CONTROL_LOCKED"))
  }finally{server.stop()}
 }
 private fun request(port:Int,line:String):String{
  var last:Exception?=null
  repeat(30){
   try{
    Socket("127.0.0.1",port).use{socket->
     socket.soTimeout=1000
     socket.getOutputStream().write((line+"\r\nHost: localhost\r\nConnection: close\r\n\r\n").toByteArray())
     return socket.getInputStream().bufferedReader().readText()
    }
   }catch(e:Exception){last=e;Thread.sleep(50)}
  }
  throw last?:IllegalStateException("Server did not start")
 }
}
