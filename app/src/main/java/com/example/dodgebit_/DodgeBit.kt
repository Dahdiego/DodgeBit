package com.example.dodgebit_

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dodgebit_.SQLiteHelper.Companion.TABLE_NAME
import java.util.*

class DodgeBit : AppCompatActivity() {
    private lateinit var dbHelper: SQLiteHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var gameView: SwordGameView
    private lateinit var tvRecord: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        tvRecord = findViewById(R.id.tvRecord)
        dbHelper = SQLiteHelper(this)

        val pauseButton = findViewById<Button>(R.id.pause_button)
        gameView = findViewById(R.id.swordGameView)

        gameView.setDbHelper(dbHelper) // Aquí se pasa dbHelper a la clase SwordGameView



        pauseButton.setOnClickListener {
            gameView.pauseGame()
        }
        tvRecord.text = "Record: ${gameView.getRecord()}"
    }

    class SwordGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

        private lateinit var dbHelper: SQLiteHelper
        private lateinit var db: SQLiteDatabase
        private lateinit var tvRecord: TextView
        private lateinit var alertDialog: AlertDialog
        private var isPaused = false
        fun setDbHelper(dbHelper: SQLiteHelper) { // Aquí se inicializa dbHelper
            this.dbHelper = dbHelper
        }

        private val backgroundImage =
            BitmapFactory.decodeResource(resources, R.drawable.fondo1).let {
                if (it.width <= 0 || it.height <= 0) {
                    throw IllegalArgumentException("Dimensiones Fondo invalidas")
                }
                it
            }

        private val swordBitmap = BitmapFactory.decodeResource(resources, R.drawable.espada2).let {
            if (it.width <= 0 || it.height <= 0) {
                throw IllegalArgumentException("Dimensiones espada invalidas")
            }
            it
        }
        private val playerBitmap = BitmapFactory.decodeResource(resources, R.drawable.player3).let {
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
        private var isGameOver = false

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
                            currentSpeed += 2f
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

        private var swordCount = 0
        private var originalSpeed = 12f
        private val handler = Handler(Looper.getMainLooper())

        private var currentSpeed = originalSpeed

        init {
            handler.postDelayed(object : Runnable {
                override fun run() {
                    currentSpeed += 2f // Aumentar la velocidad actual en 2 cada 5 segundos
                    handler.postDelayed(this, 5000) // Ejecutar el Runnable cada 5 segundos
                }
            }, 5000) // Empezar a ejecutar el Runnable después de 5 segundos
        }

        private fun resetSpeed() {
            maxScore = getRecord()!!
            tvRecord.text = "Record: $maxScore"
            currentSpeed = originalSpeed
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Draw background image
            canvas.drawBitmap(backgroundImage, 0f, 0f, paint)

            if (!isGameOver) {
                // Draw swords
                val iterator = swordList.iterator()
                while (iterator.hasNext()) {
                    val sword = iterator.next()
                    canvas.drawBitmap(swordBitmap, sword.x, sword.y, paint)
                    if (!isPaused) {
                        sword.y += currentSpeed
                    }
                    if (sword.y > height) {
                        // If the sword reaches the bottom of the screen, remove it using the iterator
                        iterator.remove()
                        swordCount++ // Add 1 to sword count
                    } else if (sword.x <= playerX + playerWidth && sword.x + swordWidth >= playerX &&
                        sword.y + swordHeight >= height - playerHeight
                    ) {
                        // If the sword hits the player, end the game
                        isGameOver = true
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

                // Draw player
                canvas.drawBitmap(
                    playerBitmap,
                    playerX,
                    height - playerHeight.toFloat() - 160f,
                    paint
                )

                // Draw score
                canvas.drawText("Puntos: $puntos", 60f, 150f, paint)

                //Pintar velocidad
                canvas.drawText("Velocidad: $currentSpeed", 700f, 150f, paint)

                // Update score and reset sword count
                puntos += swordCount
                swordCount = 0

                // Invalidate to update the view
                invalidate()
            } else {
                checkRecord()
                resetSpeed()
            }
        }

        private fun showGameOverDialog() {
            alertDialog = AlertDialog.Builder(context)
                .setTitle("Game Over")
                .setMessage("Cigarros sin Fumar: $puntos")
                .setCancelable(false)
                .setPositiveButton("Reiniciar") { _, _ ->
                    puntos = 0
                    swordCount = 0
                    swordList.clear()
                    isGameOver = false
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
                TABLE_NAME,
                arrayOf(COL_POINTS),
                null,
                null,
                null,
                null,
                "$COL_POINTS DESC",
                "1"
            )
            return if (cursor.moveToFirst()) cursor.getInt(0) else null
        }

        fun updateRecord() {
            db = dbHelper.writableDatabase
            db = dbHelper.readableDatabase
            val values = ContentValues().apply {
                put(COL_POINTS, puntos)
            }
            if(db.update(TABLE_NAME, values, null, null) == 0) {
                db.insert(TABLE_NAME, null, values)
            }
        }

        inner class Sword(var x: Float, var y: Float) {

        }
    }
    companion object {
        const val COL_POINTS = "puntos"
    }
}