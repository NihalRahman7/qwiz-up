package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.domaine.service.ServiceProfil

class ModèleConnexion(
    private val serviceProfil: ServiceProfil
) : ContratConnexion.IModèleConnexion {

    override suspend fun connecter(nomUtilisateur: String) =
        serviceProfil.connecter(nomUtilisateur)
}