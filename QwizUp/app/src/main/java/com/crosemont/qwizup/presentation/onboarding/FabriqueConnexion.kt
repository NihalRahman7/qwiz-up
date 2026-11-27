package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilLocale
import com.crosemont.qwizup.domaine.service.ServiceProfil
import java.io.File

/**
 * Assemble les morceaux de la connexion : source → service → modèle → présentateur.
 * C'est la même source que le Profil et l'Inscription.
 */
object FabriqueConnexion {

    fun créerPrésentateur(dossierDeLApp: File): ContratConnexion.IPrésentateurConnexion {
        val source = SourceProfilLocale(
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PROFIL),
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PHOTO)
        )
        val modèle = ModèleConnexion(ServiceProfil(source))
        return PrésentateurConnexion(modèle)
    }
}