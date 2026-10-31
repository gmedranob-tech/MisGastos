
package gt.edu.umg.misgastos.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MovimientoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MisGastosDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao

    companion object {
        @Volatile
        private var INSTANCE: MisGastosDatabase? = null

        fun obtenerBaseDatos(context: Context): MisGastosDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    MisGastosDatabase::class.java,
                    "mis_gastos_db"
                ).build()

                INSTANCE = instancia
                instancia
            }
        }
    }
}

