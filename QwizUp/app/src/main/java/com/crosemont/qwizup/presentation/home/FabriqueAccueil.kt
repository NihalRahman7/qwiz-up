package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.sourceDeDonnees.SourceResultatsLocale
import java.io.File

/**
 * Assemble les morceaux de l'accueil : source → service → modèle → présentateur.
 * Chaque morceau reçoit le suivant de l'extérieur (injection de dépendance).
 */
object FabriqueAccueil {

    fun creerPresentateur(dossierDeLApp: File): ContratAccueil.IPresentateurAccueil {
        val source = SourceResultatsLocale(File(dossierDeLApp, SourceResultatsLocale.NOM_FICHIER))
        val service = ServiceStatistiques(source)
        val modele = ModeleAccueil(service)
        return PresentateurAccueil(modele)
    }
}