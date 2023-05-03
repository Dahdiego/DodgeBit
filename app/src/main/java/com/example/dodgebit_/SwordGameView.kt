package com.example.dodgebit_

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.util.*

class SwordGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private lateinit var dbHelper: SQLiteHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var tvRecord: TextView
    private lateinit var tvPuntos: TextView
    private lateinit var tvVelocidad: TextView
    private lateinit var alertDialog: AlertDialog
    private var isPaused = false

    fun setDbHelper(dbHelper: SQLiteHelper) { // Aquí se inicializa dbHelper
        this.dbHelper = dbHelper
    }

    private val backgroundImage =
        BitmapFactory.decodeResource(resources, R.drawable.fondo).let {
            if (it.width <= 0 || it.height <= 0) {
                throw IllegalArgumentException("Dimensiones Fondo invalidas")
            }
            it
        }

    private val swordBitmap = BitmapFactory.decodeResource(resources, R.drawable.espada).let {
        if (it.width <= 0 || it.height <= 0) {
            throw IllegalArgumentException("Dimensiones espada invalidas")
        }
        it
    }
    private val playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.jugador).let {
        if (it.width <= 0 || it.height <= 0) {
            throw IllegalArgumentException("Dimensiones player invalidas")
        }
        it
    }

    private val swordWidth = swordBitmap.width
    private val swordHeight = swordBitmap.height
    private val playerWidth = playerBitmap.width
    private val playerHeight = playerBitmap.height
    private val swordList = mutableListOf<Sword>()
    private var playerX = 0f
    private var puntos = 0
    private var maxScore = 0
    private var GameOver = false


    fun pauseGame() {
        isPaused = true
        handler.removeCallbacksAndMessages(null) // Detener el bucle de actualización
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Juego en pausa")
            .setMessage("¿Quieres continuar o salir?")
            .setCancelable(false)
            .setPositiveButton("Continuar") { _, _ ->
                isPaused = false
                handler.postDelayed(object : Runnable { // Reanudar el bucle de actualización
                    override fun run() {
                        velocidad += 2f
                        handler.postDelayed(this, 5000)
                    }
                }, 5000)
            }
            .setNegativeButton("Salir") { _, _ ->
                (context as Activity).finish()
            }
            .create()
        alertDialog.show()
    }


    private val paint = Paint().apply {
        isAntiAlias = true
        textSize = 50f
    }

    //private var swordCount = 0
    private var velocidadOriginal = 12f
    private val handler = Handler(Looper.getMainLooper())

    private var velocidad = velocidadOriginal

    init {
        handler.postDelayed(object : Runnable {
            override fun run() {
                velocidad += 2f
                tvVelocidad.text = "Velocidad: $velocidad"// Aumentar la velocidad actual en 2 cada 5 segundos
                handler.postDelayed(this, 5000) // Ejecutar el Runnable cada 5 segundos
            }
        }, 5000) // Empezar a ejecutar el Runnable después de 5 segundos
    }

    private fun resetSpeed() {
        maxScore = getRecord()!!
        tvRecord.text = "Record: $maxScore"
        velocidad = velocidadOriginal
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Pintar fondo
        canvas.drawBitmap(backgroundImage, -500f, 0f, paint)

        if (!GameOver) {
            // Pintar espadas
            val iterator = swordList.iterator()
            while (iterator.hasNext()) {
                val sword = iterator.next()
                canvas.drawBitmap(swordBitmap, sword.x, sword.y, paint)
                if (!isPaused) {
                    sword.y += velocidad
                }
                if (sword.y > height) {
                    // Cuando llegan al final desaparecen
                    iterator.remove()
                    //swordCount++
                    addPoint() // Suma 1 al contador
                } else if (sword.x <= playerX + playerWidth && sword.x + swordWidth >= playerX &&
                    sword.y + swordHeight >= height - playerHeight
                ) {
                    // If the sword hits the player, end the game
                    GameOver = true
                    showGameOverDialog()
                }
            }

            // Add new sword every 1.5 seconds
            if (!isPaused && Random().nextInt(45) == 0) {
                swordList.add(
                    Sword(
                        Random().nextInt(width - swordWidth).toFloat(),
                        -swordHeight.toFloat()
                    )
                )
            }

            // Draw Jugador
            canvas.drawBitmap(
                playerBitmap,
                playerX,
                height - playerHeight.toFloat() - 160f,
                paint
            )

            // Pintar puntos
            // canvas.drawText("Puntos: $puntos", 60f, 150f, paint)

            //Pintar velocidad
            //canvas.drawText("Velocidad: $currentSpeed", 700f, 150f, paint)

            // Actualizar puntos
            //puntos += swordCount
            //swordCount = 0


            // addPoint()
            invalidate()
        } else {
            checkRecord()
            resetSpeed()
        }
    }
    private fun addPoint() {
        puntos++
        tvPuntos.text = puntos.toString()
    }

    private fun showGameOverDialog() {
        alertDialog = AlertDialog.Builder(context)
            .setTitle("Game Over")
            .setMessage("Puntuación : $puntos")
            .setCancelable(false)
            .setPositiveButton("Reiniciar") { _, _ ->
                puntos = 0
                //swordCount = 0
                swordList.clear()
                GameOver = false
                invalidate()
            }
            .setNegativeButton("Salir") { _, _ ->
                (context as Activity).finish()
            }
            .create()

        alertDialog.show()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                playerX = event.x - (playerWidth / 2)
                invalidate()
            }
        }
        return true
    }

    fun checkRecord() {
        db = dbHelper.readableDatabase
        val record = getRecord()
        if (record == null || puntos > record) {
            updateRecord()
            tvRecord.text = "Nuevo record: $puntos"
        }
    }

    fun getRecord(): Int? {
        db = dbHelper.readableDatabase
        val cursor = db.query(
            SQLiteHelper.TABLE_NAME,
            arrayOf(DodgeBit.COL_POINTS),
            null,
            null,
            null,
            null,
            "${DodgeBit.COL_POINTS} DESC",
            "1"
        )
        return if (cursor.moveToFirst()) cursor.getInt(0) else null
    }

    fun updateRecord() {
        db = dbHelper.writableDatabase
        db = dbHelper.readableDatabase
        val values = ContentValues().apply {
            put(DodgeBit.COL_POINTS, puntos)
        }
        if(db.update(SQLiteHelper.TABLE_NAME, values, null, null) == 0) {
            db.insert(SQLiteHelper.TABLE_NAME, null, values)
        }
    }

    inner class Sword(var x: Float, var y: Float) {

    }
}