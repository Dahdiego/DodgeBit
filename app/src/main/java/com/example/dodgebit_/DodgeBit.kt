package com.example.dodgebit_

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class DodgeBit : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var gameView: SwordGameView
    private lateinit var tvRecord: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var tvVelocidad: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        tvRecord = findViewById(R.id.tvRecord)
        tvPuntos = findViewById(R.id.tvPuntos)
        tvVelocidad = findViewById(R.id.tvVelocidad)
        dbHelper = SQLiteHelper(this)

        val pauseButton = findViewById<Button>(R.id.pause_button)
        gameView = findViewById(R.id.swordGameView)
        gameView.initViews(tvRecord, tvPuntos, tvVelocidad)
        gameView.setDbHelper(dbHelper) // Aquí se pasa dbHelper a la clase SwordGameView

        pauseButton.setOnClickListener {
            gameView.pauseGame()
        }
        tvRecord.post {
            tvRecord.text = "Record: ${gameView.getRecord()}"
        }
    }
    companion object {
        const val COL_POINTS = "puntos"
    }
}

