package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.sourceDeDonnees.SourceFavorisLocale
import java.io.File

/**
 * Assemble les morceaux de l'écran Découvrir : source → service → modèle → présentateur.
 * Chaque morceau reçoit le suivant de l'extérieur (injection de dépendance).
 */
object FabriqueDécouvrir {

    fun créerPrésentateur(dossierDeLApp: File): ContratDécouvrir.IPrésentateurDécouvrir {
        val source = SourceFavorisLocale(File(dossierDeLApp, "favoris.txt"))
        val service = ServiceFavoris(source)
        val modèle = ModèleDécouvrir(service)
        return PrésentateurDécouvrir(modèle)
    }
}