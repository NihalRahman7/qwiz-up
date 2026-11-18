package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsLocale
import java.io.File

/**
 * Assemble les morceaux de l'écran de jeu : source → service → modèle → présentateur.
 * Même fichier que l'accueil : ce que le jeu écrit, l'accueil le lit.
 */
object FabriqueQuestion {

    fun créerPresentateur(dossierDeLApp: File): ContratQuestion.IPrésentateurQuestion {
        val source = SourceRésultatsLocale(File(dossierDeLApp, SourceRésultatsLocale.NOM_FICHIER))
        val service = ServiceStatistiques(source)
        val modèle = ModèleQuestion(service)
        return PrésentateurQuestion(modèle)
    }
}