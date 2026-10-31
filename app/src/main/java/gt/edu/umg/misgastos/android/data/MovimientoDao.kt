
package gt.edu.umg.misgastos.android.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MovimientoDao {

    @Insert
    suspend fun insertar(movimiento: MovimientoEntity)

    @Query("SELECT * FROM movimientos ORDER BY id DESC")
    suspend fun obtenerTodos(): List<MovimientoEntity>

    @Update
    suspend fun actualizar(movimiento: MovimientoEntity)

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)

    @Query("DELETE FROM movimientos")
    suspend fun eliminarTodos()
}
