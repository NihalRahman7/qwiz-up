package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.accèsAuxDonnées.SourceBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisLocale
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsLocale
import java.io.File


object FabriqueLibrairie {

    fun creerPresentateur(dossierDeLApp: File): ContratLibrairie.IPrésentateurLibrary {
        val catalogue = ModèleQuiz(ServiceQuiz(SourceBidon()))
        val serviceFavoris = ServiceFavoris(SourceFavorisLocale(File(dossierDeLApp, "favoris.txt")))
        // Même fichier que le jeu : une partie finie à 100 % apparaît dans Complétés
        val serviceStatistiques = ServiceStatistiques(
            SourceRésultatsLocale(File(dossierDeLApp, SourceRésultatsLocale.NOM_FICHIER))
        )
        val modèle = ModèleLibrairie(serviceFavoris, catalogue, serviceStatistiques)
        return PrésentateurLibrairie(modèle)
    }
}