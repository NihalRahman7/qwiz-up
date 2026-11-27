package com.crosemont.qwizup.presentation.profil

import com.crosemont.qwizup.domaine.entites.Profil
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceProfil
import com.crosemont.qwizup.domaine.service.ServiceStatistiques

class ModèleProfil(
    private val serviceProfil: ServiceProfil,
    private val serviceStatistiques: ServiceStatistiques   // le même que l'Accueil
) : ContratProfil.IModèleProfil {

    override suspend fun obtenirProfil(): Profil? = serviceProfil.obtenirProfil()

    override suspend fun obtenirPhoto(): ByteArray? = serviceProfil.obtenirPhoto()

    override suspend fun obtenirStatistiques(): Statistiques = serviceStatistiques.obtenirStatistiques()

    override suspend fun changerNomAffiché(nom: String) = serviceProfil.changerNomAffiché(nom)

    override suspend fun changerPhoto(photo: ByteArray?) = serviceProfil.changerPhoto(photo)

    override suspend fun déconnecter() = serviceProfil.déconnecter()
}