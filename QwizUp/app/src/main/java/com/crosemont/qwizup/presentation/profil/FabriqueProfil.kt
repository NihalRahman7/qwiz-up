package com.crosemont.qwizup.presentation.profil

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilLocale
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsLocale
import com.crosemont.qwizup.domaine.service.ServiceProfil
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import java.io.File

/**
 * Assemble les morceaux du profil : sources → services → modèle → présentateur.
 * Les statistiques viennent de la même source que l'Accueil.
 */
object FabriqueProfil {

    fun créerPrésentateur(dossierDeLApp: File): ContratProfil.IPrésentateurProfil {
        val sourceProfil = SourceProfilLocale(
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PROFIL),
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PHOTO)
        )
        val sourceRésultats = SourceRésultatsLocale(File(dossierDeLApp, SourceRésultatsLocale.NOM_FICHIER))
        val modèle = ModèleProfil(ServiceProfil(sourceProfil), ServiceStatistiques(sourceRésultats))
        return PrésentateurProfil(modèle)
    }
}