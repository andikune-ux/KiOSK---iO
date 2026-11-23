package dev.andikune.kioskio.motor
import org.junit.Assert.assertEquals
import org.junit.Test
class MotorControllerTest{
 private val driver=MotorController()
 @Test fun acceptsMotorRange(){assertEquals(MotorResult.ControllerNotConfigured,driver.validate(1,432));assertEquals(MotorResult.ControllerNotConfigured,driver.validate(45,432));assertEquals(MotorResult.Rejected("Nomor motor harus 01–45"),driver.validate(46,432))}
 @Test fun rejectsInvalidDegrees(){assertEquals(MotorResult.Rejected("Sudut harus 1–720 derajat"),driver.validate(8,0));assertEquals(MotorResult.Rejected("Sudut harus 1–720 derajat"),driver.validate(8,721))}
 @Test fun neverClaimsHardwareMovement(){assertEquals(MotorResult.ControllerNotConfigured,driver.execute(MotorCommand(8,432)))}
}
