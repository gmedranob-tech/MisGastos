
package gt.edu.umg.misgastos.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gt.edu.umg.misgastos.ControlGastos
import gt.edu.umg.misgastos.Movimiento
import gt.edu.umg.misgastos.TipoMovimiento
import gt.edu.umg.misgastos.android.data.MisGastosDatabase
import gt.edu.umg.misgastos.android.data.MovimientoDao
import gt.edu.umg.misgastos.android.data.MovimientoEntity
import gt.edu.umg.misgastos.android.ui.theme.MisGastosAndroidTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random
import kotlinx.coroutines.launch

private val categoriasIngresos = listOf(
    "Salario",
    "Ventas",
    "Negocio",
    "Otros ingresos"
)

private val categoriasGastos = listOf(
    "Alimentación",
    "Transporte",
    "Vivienda",
    "Salud",
    "Educación",
    "Servicios",
    "Entretenimiento",
    "Otros gastos"
)

private data class Versiculo(
    val texto: String,
    val referencia: String
)

private val versiculos = listOf(
    Versiculo(
        "Todo lo que hagáis, hacedlo de corazón, como para el Señor y no para los hombres.",
        "Colosenses 3:23"
    ),
    Versiculo(
        "Encomienda al Señor tus obras, y tus pensamientos serán afirmados.",
        "Proverbios 16:3"
    ),
    Versiculo(
        "Todo lo puedo en Cristo que me fortalece.",
        "Filipenses 4:13"
    ),
    Versiculo(
        "El Señor es mi pastor; nada me faltará.",
        "Salmos 23:1"
    ),
    Versiculo(
        "Fíate del Señor de todo tu corazón.",
        "Proverbios 3:5"
    ),
    Versiculo(
        "Este es el día que hizo el Señor; nos gozaremos y alegraremos en él.",
        "Salmos 118:24"
    ),
    Versiculo(
        "La bendición del Señor es la que enriquece.",
        "Proverbios 10:22"
    ),
    Versiculo(
        "Y todo lo que hacéis, hacedlo todo para la gloria de Dios.",
        "1 Corintios 10:31"
    )
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MisGastosAndroidTheme {
                val baseDatos = remember {
                    MisGastosDatabase.obtenerBaseDatos(applicationContext)
                }

                PantallaInicio(
                    dao = baseDatos.movimientoDao(),
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun PantallaInicio(
    dao: MovimientoDao,
    modifier: Modifier = Modifier
) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    var versionMovimientos by remember { mutableIntStateOf(0) }
    var movimientoAEliminar by remember {
        mutableStateOf<MovimientoEntity?>(null)
    }
    var movimientoAEditar by remember {
        mutableStateOf<MovimientoEntity?>(null)
    }
    var movimientos by remember {
        mutableStateOf<List<MovimientoEntity>>(emptyList())
    }
    var pantallaActual by remember { mutableStateOf("Inicio") }

    val context = LocalContext.current
    val preferencias = remember {
        context.getSharedPreferences("mis_gastos_preferencias", Context.MODE_PRIVATE)
    }
    var nombreUsuario by remember {
        mutableStateOf(preferencias.getString("nombre_usuario", "") ?: "")
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(versionMovimientos) {
        movimientos = dao.obtenerTodos()
    }

    val totalIngresos = movimientos
        .filter { it.tipo == TipoMovimiento.INGRESO.name }
        .sumOf { it.monto }

    val totalGastos = movimientos
        .filter { it.tipo == TipoMovimiento.GASTO.name }
        .sumOf { it.monto }

    val balance = totalIngresos - totalGastos

    if (nombreUsuario.isBlank()) {
        PantallaBienvenida(
            onContinuar = { nombre ->
                preferencias.edit()
                    .putString("nombre_usuario", nombre)
                    .apply()
                nombreUsuario = nombre
            }
        )
    } else if (mostrarFormulario) {
        FormularioMovimiento(
            dao = dao,
            movimientoEditar = movimientoAEditar,
            balanceActual = balance,
            onCancelar = {
                mostrarFormulario = false
                movimientoAEditar = null
            },
            onGuardar = {
                versionMovimientos++
                mostrarFormulario = false
                movimientoAEditar = null
            }
        )
    } else {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = pantallaActual == "Inicio",
                        onClick = { pantallaActual = "Inicio" },
                        icon = { Text("⌂", fontSize = 20.sp) },
                        label = { Text("Inicio") }
                    )

                    NavigationBarItem(
                        selected = pantallaActual == "Historial",
                        onClick = { pantallaActual = "Historial" },
                        icon = { Text("≡", fontSize = 20.sp) },
                        label = { Text("Historial") }
                    )
                }
            }
        ) { innerPadding ->
            if (pantallaActual == "Inicio") {
                ContenidoInicio(
                    movimientos = movimientos,
                    totalIngresos = totalIngresos,
                    totalGastos = totalGastos,
                    balance = balance,
                    nombreUsuario = nombreUsuario,
                    onRegistrar = {
                        movimientoAEditar = null
                        mostrarFormulario = true
                    },
                    onEditar = { movimiento ->
                        movimientoAEditar = movimiento
                        mostrarFormulario = true
                    },
                    onEliminar = { movimiento ->
                        movimientoAEliminar = movimiento
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                ContenidoHistorial(
                    movimientos = movimientos,
                    onEditar = { movimiento ->
                        movimientoAEditar = movimiento
                        mostrarFormulario = true
                    },
                    onEliminar = { movimiento ->
                        movimientoAEliminar = movimiento
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    movimientoAEliminar?.let { movimiento ->
        ConfirmarEliminacion(
            descripcion = movimiento.descripcion,
            onConfirmar = {
                coroutineScope.launch {
                    try {
                        dao.eliminar(movimiento)
                        versionMovimientos++
                        movimientoAEliminar = null
                    } catch (e: Exception) {
                        movimientoAEliminar = null
                    }
                }
            },
            onCancelar = {
                movimientoAEliminar = null
            }
        )
    }
}

@Composable
private fun ContenidoInicio(
    movimientos: List<MovimientoEntity>,
    totalIngresos: Double,
    totalGastos: Double,
    balance: Double,
    nombreUsuario: String,
    onRegistrar: () -> Unit,
    onEditar: (MovimientoEntity) -> Unit,
    onEliminar: (MovimientoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val versiculoDelDia = remember(context) {
        obtenerVersiculoDelDia(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "¡Hola, $nombreUsuario!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Este es el resumen de tus finanzas",
            style = MaterialTheme.typography.bodyLarge
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (balance < 0) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(text = "Balance disponible")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Q ${"%.2f".format(Locale.getDefault(), balance)}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (balance < 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }

        if (balance < 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "¡Atención! Balance negativo",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )

                    Text(
                        text = "Tus gastos superan tus ingresos. " +
                                "Revisa tus movimientos y procura " +
                                "equilibrar tus finanzas.",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaResumen(
                titulo = "Ingresos",
                monto = "Q ${"%.2f".format(Locale.getDefault(), totalIngresos)}",
                modifier = Modifier.weight(1f)
            )

            TarjetaResumen(
                titulo = "Gastos",
                monto = "Q ${"%.2f".format(Locale.getDefault(), totalGastos)}",
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onRegistrar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "+ Registrar movimiento")
        }

        Text(
            text = "Movimientos recientes",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (movimientos.isEmpty()) {
            Text(
                text = "Todavía no hay movimientos registrados.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            movimientos.take(5).forEach { movimiento ->
                MovimientoReciente(
                    movimiento = movimiento,
                    onEditar = { onEditar(movimiento) },
                    onEliminar = { onEliminar(movimiento) }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Un mensaje para tu día",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "“${versiculoDelDia.texto}”",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = versiculoDelDia.referencia,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContenidoHistorial(
    movimientos: List<MovimientoEntity>,
    onEditar: (MovimientoEntity) -> Unit,
    onEliminar: (MovimientoEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var filtroTipo by remember { mutableStateOf("Todos") }
    var filtroCategoria by remember { mutableStateOf("Todas") }
    var fechaDesde by remember { mutableStateOf("") }
    var fechaHasta by remember { mutableStateOf("") }

    var mostrarCalendarioDesde by remember { mutableStateOf(false) }
    var mostrarCalendarioHasta by remember { mutableStateOf(false) }

    val categoriasDisponibles = remember(movimientos) {
        (categoriasIngresos + categoriasGastos + "General" +
                movimientos.map { it.categoria })
            .distinct()
            .sorted()
    }

    val movimientosFiltrados = movimientos.filter { movimiento ->
        val coincideTipo = when (filtroTipo) {
            "Ingreso" -> movimiento.tipo == TipoMovimiento.INGRESO.name
            "Gasto" -> movimiento.tipo == TipoMovimiento.GASTO.name
            else -> true
        }

        val coincideCategoria =
            filtroCategoria == "Todas" ||
                    movimiento.categoria == filtroCategoria

        val coincideDesde =
            fechaDesde.isBlank() || movimiento.fecha >= fechaDesde

        val coincideHasta =
            fechaHasta.isBlank() || movimiento.fecha <= fechaHasta

        coincideTipo && coincideCategoria &&
                coincideDesde && coincideHasta
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Historial de movimientos",
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Consulta y filtra todos tus ingresos y gastos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "Tipo de movimiento",
            fontWeight = FontWeight.SemiBold
        )

        SelectorDesplegable(
            valor = filtroTipo,
            opciones = listOf("Todos", "Ingreso", "Gasto"),
            alSeleccionar = { filtroTipo = it },
            etiqueta = "Tipo"
        )

        Text(
            text = "Categoría",
            fontWeight = FontWeight.SemiBold
        )

        SelectorDesplegable(
            valor = filtroCategoria,
            opciones = listOf("Todas") + categoriasDisponibles,
            alSeleccionar = { filtroCategoria = it },
            etiqueta = "Categoría"
        )

        Text(
            text = "Filtrar por fecha",
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = "Selecciona el período que deseas consultar.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )

        OutlinedButton(
            onClick = { mostrarCalendarioDesde = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (fechaDesde.isBlank()) {
                    "Fecha inicial: Seleccionar fecha"
                } else {
                    "Fecha inicial: ${formatearFechaVisible(fechaDesde)}"
                }
            )
        }

        OutlinedButton(
            onClick = { mostrarCalendarioHasta = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (fechaHasta.isBlank()) {
                    "Fecha final: Seleccionar fecha"
                } else {
                    "Fecha final: ${formatearFechaVisible(fechaHasta)}"
                }
            )
        }

        if (
            fechaDesde.isNotBlank() &&
            fechaHasta.isNotBlank() &&
            fechaDesde > fechaHasta
        ) {
            Text(
                text = "La fecha inicial no puede ser posterior a la fecha final.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        OutlinedButton(
            onClick = {
                filtroTipo = "Todos"
                filtroCategoria = "Todas"
                fechaDesde = ""
                fechaHasta = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar filtros")
        }

        Text(
            text = "Resultados: ${movimientosFiltrados.size}",
            fontWeight = FontWeight.Bold
        )

        if (
            fechaDesde.isNotBlank() &&
            fechaHasta.isNotBlank() &&
            fechaDesde > fechaHasta
        ) {
            Text(
                text = "Revisa el período seleccionado para mostrar los resultados.",
                color = MaterialTheme.colorScheme.error
            )
        } else if (movimientosFiltrados.isEmpty()) {
            Text(
                text = "No hay movimientos que coincidan con los filtros.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            movimientosFiltrados.forEach { movimiento ->
                MovimientoReciente(
                    movimiento = movimiento,
                    onEditar = { onEditar(movimiento) },
                    onEliminar = { onEliminar(movimiento) }
                )
            }
        }
    }

    if (mostrarCalendarioDesde) {
        SelectorFechaDialog(
            fechaActual = fechaDesde,
            alCancelar = { mostrarCalendarioDesde = false },
            alConfirmar = { fecha ->
                fechaDesde = fecha
                mostrarCalendarioDesde = false
            }
        )
    }

    if (mostrarCalendarioHasta) {
        SelectorFechaDialog(
            fechaActual = fechaHasta,
            alCancelar = { mostrarCalendarioHasta = false },
            alConfirmar = { fecha ->
                fechaHasta = fecha
                mostrarCalendarioHasta = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectorFechaDialog(
    fechaActual: String,
    alCancelar: () -> Unit,
    alConfirmar: (String) -> Unit
) {
    val fechaInicialMillis = remember(fechaActual) {
        convertirFechaAMillis(fechaActual)
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = fechaInicialMillis
    )

    DatePickerDialog(
        onDismissRequest = alCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis

                    if (millis != null) {
                        alConfirmar(convertirMillisAFecha(millis))
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCancelar) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Seleccionar fecha",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 16.dp
                    )
                )
            }
        )
    }
}

private fun convertirFechaAMillis(fecha: String): Long? {
    if (fecha.isBlank()) return null

    return try {
        val formato = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
            isLenient = false
        }

        formato.parse(fecha)?.time
    } catch (e: Exception) {
        null
    }
}

private fun convertirMillisAFecha(millis: Long): String {
    val formato = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    return formato.format(Date(millis))
}

private fun formatearFechaVisible(fecha: String): String {
    return try {
        val formatoEntrada = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        val formatoSalida = SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

        val fechaConvertida = formatoEntrada.parse(fecha)

        if (fechaConvertida != null) {
            formatoSalida.format(fechaConvertida)
        } else {
            fecha
        }
    } catch (e: Exception) {
        fecha
    }
}

@Composable
fun TarjetaResumen(
    titulo: String,
    monto: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = titulo)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = monto,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun MovimientoReciente(
    movimiento: MovimientoEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val esIngreso = movimiento.tipo == TipoMovimiento.INGRESO.name
    val signo = if (esIngreso) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = movimiento.descripcion,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (esIngreso) "Ingreso" else "Gasto",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Categoría: ${movimiento.categoria}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "Fecha: ${movimiento.fecha}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "$signo Q ${"%.2f".format(Locale.getDefault(), movimiento.monto)}",
                    fontWeight = FontWeight.Bold,
                    color = if (esIngreso) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onEditar) {
                    Text("Editar")
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(onClick = onEliminar) {
                    Text(
                        "Eliminar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun FormularioMovimiento(
    dao: MovimientoDao,
    movimientoEditar: MovimientoEntity?,
    balanceActual: Double,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit
) {
    var tipo by remember(movimientoEditar) {
        mutableStateOf(
            if (movimientoEditar?.tipo == TipoMovimiento.INGRESO.name) {
                "Ingreso"
            } else {
                "Gasto"
            }
        )
    }

    var descripcion by remember(movimientoEditar) {
        mutableStateOf(movimientoEditar?.descripcion ?: "")
    }

    var monto by remember(movimientoEditar) {
        mutableStateOf(movimientoEditar?.monto?.toString() ?: "")
    }

    var categoria by remember(movimientoEditar) {
        mutableStateOf(movimientoEditar?.categoria ?: categoriasGastos.first())
    }

    var mensajeError by remember { mutableStateOf("") }
    var mostrarAlertaBalance by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val controlGastos = remember { ControlGastos() }

    val categoriasActuales =
        if (tipo == "Ingreso") categoriasIngresos else categoriasGastos

    LaunchedEffect(tipo) {
        if (categoria !in categoriasActuales) {
            categoria = categoriasActuales.first()
        }
    }

    fun guardarMovimiento(montoNumerico: Double) {
        try {
            val nuevoMovimiento = Movimiento(
                tipo = if (tipo == "Ingreso") {
                    TipoMovimiento.INGRESO
                } else {
                    TipoMovimiento.GASTO
                },
                descripcion = descripcion.trim(),
                monto = montoNumerico,
                categoria = categoria,
                fecha = movimientoEditar?.fecha ?: SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).format(Date())
            )

            controlGastos.registrarMovimiento(nuevoMovimiento)

            val movimientoEntity = MovimientoEntity(
                id = movimientoEditar?.id ?: 0,
                tipo = nuevoMovimiento.tipo.name,
                descripcion = nuevoMovimiento.descripcion,
                monto = nuevoMovimiento.monto,
                categoria = nuevoMovimiento.categoria,
                fecha = nuevoMovimiento.fecha
            )

            coroutineScope.launch {
                try {
                    if (movimientoEditar == null) {
                        dao.insertar(movimientoEntity)
                    } else {
                        dao.actualizar(movimientoEntity)
                    }

                    onGuardar()
                } catch (e: Exception) {
                    mensajeError = "No se pudo guardar el movimiento."
                }
            }
        } catch (e: IllegalArgumentException) {
            mensajeError =
                e.message ?: "No se pudo registrar el movimiento."
        } catch (e: Exception) {
            mensajeError =
                "Ocurrió un error al guardar el movimiento."
        }
    }

    fun validarYGuardar() {
        val montoNumerico = monto.toDoubleOrNull()

        mensajeError = when {
            descripcion.isBlank() ->
                "Escribe una descripción."

            montoNumerico == null ||
                    !montoNumerico.isFinite() ||
                    montoNumerico <= 0 ->
                "Ingresa un monto válido mayor que cero."

            categoria.isBlank() ->
                "Selecciona una categoría."

            else -> ""
        }

        if (mensajeError.isNotEmpty()) return

        val cantidad = montoNumerico!!

        // Calcula el efecto real del movimiento.
        // Si se está editando, primero se resta el efecto anterior.
        val impactoAnterior = movimientoEditar?.let {
            if (it.tipo == TipoMovimiento.INGRESO.name) {
                it.monto
            } else {
                -it.monto
            }
        } ?: 0.0

        val impactoNuevo = if (tipo == "Ingreso") {
            cantidad
        } else {
            -cantidad
        }

        val balanceDespues =
            balanceActual - impactoAnterior + impactoNuevo

        if (tipo == "Gasto" && balanceDespues < 0) {
            mostrarAlertaBalance = true
        } else {
            guardarMovimiento(cantidad)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (movimientoEditar == null) {
                "Nuevo movimiento"
            } else {
                "Editar movimiento"
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tipo de movimiento",
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tipo == "Ingreso") {
                Button(
                    onClick = { tipo = "Ingreso" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ingreso")
                }
            } else {
                OutlinedButton(
                    onClick = { tipo = "Ingreso" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Ingreso")
                }
            }

            if (tipo == "Gasto") {
                Button(
                    onClick = { tipo = "Gasto" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gasto")
                }
            } else {
                OutlinedButton(
                    onClick = { tipo = "Gasto" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Gasto")
                }
            }
        }

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            placeholder = { Text("Ej. Almuerzo o salario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = monto,
            onValueChange = { nuevoValor ->
                if (nuevoValor.matches(Regex("^\\d*\\.?\\d*$"))) {
                    monto = nuevoValor
                }
            },
            label = { Text("Monto en quetzales (Q)") },
            placeholder = { Text("Ej. 25.00") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )

        Text(
            text = "Categoría",
            fontWeight = FontWeight.Bold
        )

        SelectorDesplegable(
            valor = categoria,
            opciones = categoriasActuales,
            alSeleccionar = { categoria = it },
            etiqueta = "Selecciona una categoría"
        )

        Button(
            onClick = { validarYGuardar() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (movimientoEditar == null) {
                    "Guardar movimiento"
                } else {
                    "Actualizar movimiento"
                }
            )
        }

        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error
            )
        }

        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar y volver")
        }
    }

    if (mostrarAlertaBalance) {
        AlertDialog(
            onDismissRequest = {
                mostrarAlertaBalance = false
            },
            title = {
                Text(
                    text = "¡Atención! Balance negativo",
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    "Este gasto hará que tus gastos superen tus ingresos " +
                            "y tu balance quedará negativo.\n\n" +
                            "¿Deseas registrar?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarAlertaBalance = false
                        guardarMovimiento(monto.toDouble())
                    }
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mostrarAlertaBalance = false
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun SelectorDesplegable(
    valor: String,
    opciones: List<String>,
    alSeleccionar: (String) -> Unit,
    etiqueta: String
) {
    var expandido by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { expandido = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(valor.ifBlank { etiqueta })
                Text("▾")
            }
        }

        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text = { Text(opcion) },
                    onClick = {
                        alSeleccionar(opcion)
                        expandido = false
                    }
                )
            }
        }
    }
}

private fun obtenerVersiculoDelDia(context: Context): Versiculo {
    val preferencias = context.getSharedPreferences(
        "mis_gastos_preferencias",
        Context.MODE_PRIVATE
    )

    val fechaHoy = SimpleDateFormat(
        "yyyy-MM-dd",
        Locale.getDefault()
    ).format(Date())

    val fechaGuardada = preferencias.getString("fecha_versiculo", null)
    val indiceGuardado = preferencias.getInt("indice_versiculo", -1)

    if (fechaGuardada == fechaHoy && indiceGuardado in versiculos.indices) {
        return versiculos[indiceGuardado]
    }

    var nuevoIndice = Random.nextInt(versiculos.size)

    if (versiculos.size > 1 && nuevoIndice == indiceGuardado) {
        nuevoIndice = (nuevoIndice + 1) % versiculos.size
    }

    preferencias.edit()
        .putString("fecha_versiculo", fechaHoy)
        .putInt("indice_versiculo", nuevoIndice)
        .apply()

    return versiculos[nuevoIndice]
}

@Composable
private fun PantallaBienvenida(
    onContinuar: (String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "¡Bienvenido a Mis Gastos!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Organiza tus ingresos y gastos de una manera sencilla.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                mensajeError = ""
            },
            label = { Text("¿Cómo te llamas?") },
            placeholder = { Text("Escribe tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (mensajeError.isNotBlank()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val nombreLimpio = nombre.trim()
                if (nombreLimpio.isBlank()) {
                    mensajeError = "Por favor, escribe tu nombre para continuar."
                } else {
                    onContinuar(nombreLimpio)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Comenzar")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "No necesitas crear una cuenta ni proporcionar una contraseña.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ConfirmarEliminacion(
    descripcion: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text("Eliminar movimiento")
        },
        text = {
            Text(
                "¿Estás segura de que deseas eliminar \"$descripcion\"? " +
                        "Esta acción no se puede deshacer."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(
                    "Eliminar",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text("Cancelar")
            }
        }
    )
}