package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.domaine.service.ServiceProfil

class ModèleInscription(
    private val serviceProfil: ServiceProfil
) : ContratInscription.IModèleInscription {

    override suspend fun créerProfil(nomUtilisateur: String, courriel: String) =
        serviceProfil.créerProfil(nomUtilisateur, courriel)
}