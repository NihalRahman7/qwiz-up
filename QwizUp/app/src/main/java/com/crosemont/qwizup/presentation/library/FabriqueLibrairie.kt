package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.accèsAuxDonnées.SourceBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisLocale
import java.io.File


object FabriqueLibrairie {

    fun creerPresentateur(dossierDeLApp: File): ContratLibrairie.IPrésentateurLibrary {
        val catalogue = ModèleQuiz(ServiceQuiz(SourceBidon()))
        val serviceFavoris = ServiceFavoris(SourceFavorisLocale(File(dossierDeLApp, "favoris.txt")))
        val modèle = ModèleLibrairie(serviceFavoris, catalogue)
        return PrésentateurLibrairie(modèle)
    }
}