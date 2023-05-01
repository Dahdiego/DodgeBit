package com.example.dodgebit_

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.dodgebit_.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sonidoFondo: MediaPlayer
    private lateinit var myReceiver: MyReceiver
    private lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Configuracion ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootview = binding.root
        setContentView(rootview)
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val btmenu = findViewById<Button>(R.id.btmenu)
        btmenu.setOnClickListener { view ->
            abrirMenu(view)
        }

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        sonidoFondo = MediaPlayer.create(this, R.raw.musica_fondo)
        // Crea el objeto MyReceiver y lo registra
        myReceiver = MyReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(myReceiver, intentFilter)

        sonidoFondo.isLooping = true // Reproduce la música de forma continua
        sonidoFondo.start() // Inicia la reproducción
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        binding.btDodgeBit.setOnClickListener(){
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(50)
            }
            val intent = Intent (this, DodgeBit::class.java)
            startActivity(intent)
        }
    } // Fin onCreate

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_lateral, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.imusica -> {
                // Acción al seleccionar la opción 1
                // Cambiar el icono del elemento de menú según su estado actual
                if (sonidoFondo.isPlaying) {
                    sonidoFondo.pause()
                    item.setIcon(R.drawable.musicaoff)
                } else {
                    sonidoFondo.start()
                    item.setIcon(R.drawable.musicaon1)
                }
            }
            R.id.icerrar -> {
                // Acción al seleccionar la opción 2
                finish()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun abrirMenu(view: View) {
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.openDrawer(GravityCompat.START)
    }
    override fun onDestroy() {
        super.onDestroy()
        sonidoFondo.stop()
        sonidoFondo.release()

        // Desregistra el objeto MyReceiver
        unregisterReceiver(myReceiver)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_SCREEN_OFF) {
                // Detiene la música
                sonidoFondo.stop()
            }
        }
    }
}