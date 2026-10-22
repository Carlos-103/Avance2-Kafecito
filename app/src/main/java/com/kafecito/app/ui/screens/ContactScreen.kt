package com.kafecito.app.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.kafecito.app.R
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kafecito.app.ui.theme.*

/** Contacto y soporte técnico. Pantalla informativa */
@Composable
fun ContactScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KafeWhite)
            .padding(20.dp)
    ) {
        Text(text = "CONTACTO", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "En Kafecito valoramos tu experiencia. Si tienes comentarios, sugerencias o requieres asistencia técnica, no dudes en contactarnos a través de los siguientes medios.",
            style = MaterialTheme.typography.bodyMedium,
            color = KafeGray
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Soporte técnico", style = MaterialTheme.typography.bodyLarge)
        Text(text = "+(503) 7654 2289", style = MaterialTheme.typography.bodyMedium, color = KafeGray)

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Correo electrónico", style = MaterialTheme.typography.bodyLarge)
        Text(text = "kafecito@correo.com", style = MaterialTheme.typography.bodyMedium, color = KafeGray)

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(KafeChip),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.contacto),
                    contentDescription = "Logo Kafecito",
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}
