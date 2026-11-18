package com.kafecito.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Convierte un monto a formato de dinero, con dos decimales. */
fun Double.comoDinero(): String = "$" + "%.2f".format(this)

/** Convierte una fecha en milisegundos al formato dd/MM/yyyy HH:mm. */
fun Long.comoFecha(): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "SV")).format(Date(this))
