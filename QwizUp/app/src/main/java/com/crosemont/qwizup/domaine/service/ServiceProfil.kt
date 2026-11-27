package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfil
import com.crosemont.qwizup.domaine.entites.Profil

class ServiceProfil(private val source: SourceProfil) {

    fun obtenirProfil(): Profil? = TODO()

    fun créerProfil(nomUtilisateur: String, courriel: String): Unit = TODO()

    fun changerNomAffiché(nom: String): Unit = TODO()

    fun obtenirPhoto(): ByteArray? = TODO()

    fun changerPhoto(photo: ByteArray?): Unit = TODO()

    fun déconnecter(): Unit = TODO()
}