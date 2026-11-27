package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfilLocale
import com.crosemont.qwizup.domaine.service.ServiceProfil
import java.io.File

/**
 * Assemble les morceaux de l'inscription : source → service → modèle → présentateur.
 * C'est la même source que le Profil : le compte créé ici s'affiche dans le Profil.
 */
object FabriqueInscription {

    fun créerPrésentateur(dossierDeLApp: File): ContratInscription.IPrésentateurInscription {
        val source = SourceProfilLocale(
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PROFIL),
            File(dossierDeLApp, SourceProfilLocale.NOM_FICHIER_PHOTO)
        )
        val modèle = ModèleInscription(ServiceProfil(source))
        return PrésentateurInscription(modèle)
    }
}