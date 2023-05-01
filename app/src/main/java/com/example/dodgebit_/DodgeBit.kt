package com.example.dodgebit_

import android.app.Activity
import android.content.Context
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class DodgeBit : AppCompatActivity() {
    private lateinit var gameView: SwordGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)


        val pauseButton = findViewById<Button>(R.id.pause_button)

        gameView = findViewById(R.id.swordGameView)

        pauseButton.setOnClickListener {
            gameView.pauseGame()
        }
    }
}
class SwordGameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private lateinit var alertDialog: AlertDialog
    private var isPaused = false

    private val backgroundImage = BitmapFactory.decodeResource(resources, R.drawable.fondo1).let {
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
    private var score = 0
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
            canvas.drawBitmap(playerBitmap, playerX, height - playerHeight.toFloat() - 160f, paint)

            // Draw score
            canvas.drawText("Score: $score", 60f, 150f, paint)

            //Pintar velocidad
            canvas.drawText("Velocidad: $currentSpeed", 700f, 150f, paint)

            // Update score and reset sword count
            score += swordCount
            swordCount = 0

            // Invalidate to update the view
            invalidate()
        } else {
            resetSpeed()
        }
    }

    private fun showGameOverDialog() {
        alertDialog = AlertDialog.Builder(context)
            .setTitle("Game Over")
            .setMessage("Cigarros sin Fumar: $score")
            .setCancelable(false)
            .setPositiveButton("Reiniciar") { _, _ ->
                score = 0
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

    inner class Sword(var x: Float, var y: Float) {

    }
}