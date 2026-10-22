package com.kafecito.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Muestra un monto como dinero, por ejemplo 2.5 -> "$2.50". */
fun Double.comoDinero(): String = "$" + "%.2f".format(this)

/** Muestra una fecha en milisegundos como "20/10/2026 14:30". */
fun Long.comoFecha(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "SV")).format(Date(this))
