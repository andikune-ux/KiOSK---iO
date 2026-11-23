package dev.andikune.kioskio.motor
data class MotorCommand(val motorNumber:Int,val degrees:Int)
sealed interface MotorResult {
 data class Rejected(val reason:String):MotorResult
 data object ControllerNotConfigured:MotorResult
}
class MotorController {
 fun validate(number:Int,degrees:Int):MotorResult {
  if(number !in 1..45)return MotorResult.Rejected("Nomor motor harus 01–45")
  if(degrees !in 1..720)return MotorResult.Rejected("Sudut harus 1–720 derajat")
  return MotorResult.ControllerNotConfigured
 }
 fun execute(command:MotorCommand):MotorResult=validate(command.motorNumber,command.degrees)
}
