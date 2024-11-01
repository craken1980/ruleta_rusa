package com.example.ruletarusa;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class MonedasDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "MonedasDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MONEDAS = "monedas";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CANTIDAD = "cantidad";

    public MonedasDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_MONEDAS_TABLE = "CREATE TABLE " + TABLE_MONEDAS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CANTIDAD + " INTEGER" + ")";
        db.execSQL(CREATE_MONEDAS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MONEDAS);
        onCreate(db);
    }

    // Guardar monedas usando RxJava
    public Observable<Void> getGuardarMonedasObservable(final int cantidad) {
        return Observable.create(emitter -> {
            SQLiteDatabase db = this.getWritableDatabase();
            try {
                ContentValues values = new ContentValues();
                values.put(COLUMN_CANTIDAD, cantidad);

                db.delete(TABLE_MONEDAS, null, null); // Elimina registros existentes
                db.insert(TABLE_MONEDAS, null, values);
                emitter.onComplete(); // Indica que la operación fue exitosa
            } catch (Exception e) {
                emitter.onError(e); // Maneja errores
            } finally {
                db.close(); // Cierra la base de datos
            }
        }).subscribeOn(Schedulers.io()).map(v -> null); // Ejecuta en un hilo de IO
    }

    // Insertar o actualizar monedas
    public void guardarMonedas(int cantidad) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CANTIDAD, cantidad);

        // Inserta o actualiza el valor de monedas (solo guarda un registro)
        db.delete(TABLE_MONEDAS, null, null); // Borra el registro anterior si existe
        db.insert(TABLE_MONEDAS, null, values);
        db.close();
    }

    // Obtener la cantidad de monedas guardadas
    public int obtenerMonedas() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MONEDAS, new String[]{COLUMN_CANTIDAD}, null, null, null, null, null);

        int cantidad = 0;
        if (cursor.moveToFirst()) {
            cantidad = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return cantidad;
    }

    // Obtener monedas usando RxJava
    public Observable<Integer> getMonedasObservable() {
        return Observable.create(emitter -> {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = null;
            try {
                cursor = db.query(TABLE_MONEDAS, new String[]{COLUMN_CANTIDAD}, null, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int cantidad = cursor.getInt(0);
                    emitter.onNext(cantidad); // Emitir la cantidad de monedas
                } else {
                    emitter.onNext(0); // Si no hay registros, emitir 0
                }
                emitter.onComplete(); // Completar la emisión
            } catch (Exception e) {
                emitter.onError(e); // Maneja errores
            } finally {
                if (cursor != null) {
                    cursor.close(); // Cerrar el cursor
                }
                db.close(); // Cerrar la base de datos
            }
        }).subscribeOn(Schedulers.io()).map(v -> null); // Ejecuta en un hilo de IO
    }
}
