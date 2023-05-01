package com.example.dodgebit_

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLiteHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "Juego"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "DodgeBit"
        const val COLUMNA_PUNTOS = "puntos"
        const val COLUMNA_ID = "id"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val Tabla1 = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMNA_ID INTEGER,"
                + "$COLUMNA_PUNTOS INTEGER" + ")")
        db?.execSQL(Tabla1)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }
}