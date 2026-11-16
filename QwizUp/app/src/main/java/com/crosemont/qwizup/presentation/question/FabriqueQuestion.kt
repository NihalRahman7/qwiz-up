package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.sourceDeDonnees.SourceResultatsLocale
import java.io.File

/**
 * Assemble les morceaux de l'écran de jeu : source → service → modèle → présentateur.
 * Même fichier que l'accueil : ce que le jeu écrit, l'accueil le lit.
 */
object FabriqueQuestion {

    fun creerPresentateur(dossierDeLApp: File): ContratQuestion.IPresentateurQuestion {
        val source = SourceResultatsLocale(File(dossierDeLApp, SourceResultatsLocale.NOM_FICHIER))
        val service = ServiceStatistiques(source)
        val modele = ModeleQuestion(service)
        return PresentateurQuestion(modele)
    }
}