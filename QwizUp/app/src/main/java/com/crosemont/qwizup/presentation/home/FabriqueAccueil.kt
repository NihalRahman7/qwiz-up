package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsLocale
import java.io.File

/**
 * Assemble les morceaux de l'accueil : source → service → modèle → présentateur.
 * Chaque morceau reçoit le suivant de l'extérieur (injection de dépendance).
 */
object FabriqueAccueil {

    fun créerPrésentateur(dossierDeLApp: File): ContratAccueil.IPrésentateurAccueil {
        val source = SourceRésultatsLocale(File(dossierDeLApp, SourceRésultatsLocale.NOM_FICHIER))
        val service = ServiceStatistiques(source)
        val modèle = ModèleAccueil(service)
        return PrésentateurAccueil(modèle)
    }
}