package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ObtenirQuiz
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.accèsAuxDonnées.SourceBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisLocale
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsLocale
import java.io.File

/**
 * Assemble les morceaux de l'écran Découvrir : sources → services → modèle → présentateur.
 * Chaque morceau reçoit le suivant de l'extérieur (injection de dépendance).
 */
object FabriqueDécouvrir {
    fun créerPrésentateur(dossierDeLApp: File): ContratDécouvrir.IPrésentateurDécouvrir {
        // Quiz: source bidon pour l'instant, SourceHttp() plus tard
        val obtenirQuiz = ObtenirQuiz(SourceBidon())
        val catalogue = ModèleQuiz(obtenirQuiz)

        // Favoris
        val source = SourceFavorisLocale(File(dossierDeLApp, "favoris.txt"))
        val service = ServiceFavoris(source)

        // Meilleurs scores : le même fichier que l'écran de jeu, qui y écrit les parties terminées
        val sourceResultats = SourceRésultatsLocale(File(dossierDeLApp, SourceRésultatsLocale.NOM_FICHIER))
        val serviceStatistiques = ServiceStatistiques(sourceResultats)

        val modèle = ModèleDécouvrir(service, catalogue, serviceStatistiques)
        return PrésentateurDécouvrir(modèle)
    }
}
