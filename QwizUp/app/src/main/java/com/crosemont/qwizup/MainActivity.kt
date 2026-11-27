package com.crosemont.qwizup

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.setupWithNavController
import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilLocale
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceProfil
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

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

        // Cache la barre du bas dans les écrans qui ont leurs propres boutons
        // (éditeur, jeu, détail) et dans les écrans de compte et de profil
        navController.addOnDestinationChangedListener { _, destination, _ ->
            barreDuBas.isVisible = destination.id != R.id.editeurQuizFragment &&
                    destination.id != R.id.questionFragment &&
                    destination.id != R.id.bienvenueFragment &&
                    destination.id != R.id.connexionFragment &&
                    destination.id != R.id.inscriptionFragment &&
                    destination.id != R.id.profilFragment &&
                    destination.id != R.id.detailQuizFragment
        }

        // Pas encore de profil sur l'appareil : on commence par Bienvenue.
        // L'Accueil est retiré de la pile, donc Retour ferme l'app au lieu d'y revenir.
        if (savedInstanceState == null && !profilExiste()) {
            navController.navigate(
                R.id.bienvenueFragment,
                null,
                navOptions { popUpTo(R.id.homeFragment) { inclusive = true } }
            )
        }
    }

    // Vrai si un profil est sauvegardé sur l'appareil (inscription ou connexion déjà faite)
    private fun profilExiste(): Boolean {
        val source = SourceProfilLocale(
            File(filesDir, SourceProfilLocale.NOM_FICHIER_PROFIL),
            File(filesDir, SourceProfilLocale.NOM_FICHIER_PHOTO)
        )
        return try {
            ServiceProfil(source).obtenirProfil() != null
        } catch (e: ServiceException) {
            false
        }
    }
}