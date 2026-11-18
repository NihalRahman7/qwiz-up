package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.sourceDeDonnees.SourceBiddon
import com.crosemont.qwizup.sourceDeDonnees.SourceFavorisLocale
import java.io.File


object FabriqueLibrairie {

    fun creerPresentateur(dossierDeLApp: File): ContratLibrairie.IPresentateurLibrary {
        val catalogue = ModeleQuiz(ServiceQuiz(SourceBiddon()))

        val serviceFavoris = ServiceFavoris(SourceFavorisLocale(File(dossierDeLApp, "favoris.txt")))

        val modele = ModeleLibrairie(serviceFavoris, catalogue)
        return PresentateurLibrairie(modele)
    }
}