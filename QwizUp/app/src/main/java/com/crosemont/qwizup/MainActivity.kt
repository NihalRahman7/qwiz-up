package com.crosemont.qwizup

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Récupère le contrôleur de navigation du NavGraph
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connecte la barre du bas au NavGraph : chaque onglet ouvre la destination qui a le même id
        val barreDuBas = findViewById<BottomNavigationView>(R.id.bottom_nav)
        barreDuBas.setupWithNavController(navController)

        // Cache la barre du bas dans l'éditeur de quiz, qui a ses propres boutons en bas
        navController.addOnDestinationChangedListener { _, destination, _ ->
            barreDuBas.isVisible = destination.id != R.id.editeurQuizFragment &&
                    destination.id != R.id.questionFragment &&
                    destination.id != R.id.rejoindreFragment &&
                    destination.id != R.id.bienvenueFragment &&
                    destination.id != R.id.connexionFragment
        }
    }
}