
package gt.edu.umg.misgastos.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movimientos")
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tipo: String,
    val descripcion: String,
    val monto: Double,
    val categoria: String,
    val fecha: String
)

