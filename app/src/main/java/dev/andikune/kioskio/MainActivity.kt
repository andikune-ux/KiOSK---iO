package dev.andikune.kioskio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF4F6F8)
                ) {
                    KioskioDashboard()
                }
            }
        }
    }
}

@Composable
private fun KioskioDashboard() {
    var motorInput by remember { mutableStateOf("") }
    var selectedMotor by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column {
            Text("Kioskio", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Panel pemeliharaan mesin", color = Color(0xFF667085))
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Status koneksi", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Controller belum teridentifikasi", color = Color(0xFFB54708))
                Text(
                    "Pengujian fisik motor dinonaktifkan sampai antarmuka controller terverifikasi.",
                    color = Color(0xFF667085),
                    fontSize = 13.sp
                )
            }
        }

        Text("Pilih motor", fontWeight = FontWeight.SemiBold)
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items((1..45).toList()) { number ->
                OutlinedButton(
                    onClick = {
                        selectedMotor = number
                        motorInput = number.toString().padStart(2, '0')
                    },
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(number.toString().padStart(2, '0'))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Nomor motor", fontWeight = FontWeight.SemiBold)
                BasicTextField(
                    value = motorInput,
                    onValueChange = { value ->
                        motorInput = value.filter(Char::isDigit).take(2)
                        selectedMotor = motorInput.toIntOrNull()?.takeIf { it in 1..45 }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF2F4F7), RoundedCornerShape(10.dp))
                        .padding(14.dp)
                )
                Text("Perintah awal yang direncanakan: 432° (360° + 20%).")
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Uji motor — menunggu identifikasi controller")
                }
            }
        }
    }
}
