package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.accèsAuxDonnées.SourceProfil
import com.crosemont.qwizup.domaine.entites.Profil

/**
 * Gère le profil de l'utilisateur : nom affiché et photo.
 * La source de données est fournie de l'extérieur, par son interface.
 */
class ServiceProfil(private val source: SourceProfil) {

    fun obtenirProfil(): Profil? =
        try {
            source.lireProfil()
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire le profil")
        }

    /** À l'inscription, le nom affiché est le nom d'utilisateur (on peut le changer ensuite). */
    fun créerProfil(nomUtilisateur: String, courriel: String) {
        if (nomUtilisateur.isBlank()) throw ServiceException("Le nom d'utilisateur est vide")
        val profil = Profil(nomUtilisateur.trim(), nomUtilisateur.trim(), courriel.trim())
        enregistrer(profil)
    }

    fun changerNomAffiché(nom: String) {
        val nomPropre = nom.trim()
        if (nomPropre.isEmpty()) throw ServiceException("Le nom affiché est vide")
        if (nomPropre.length > LONGUEUR_MAX_NOM) throw ServiceException("Le nom affiché est trop long")

        val profil = obtenirProfil() ?: throw ServiceException("Aucun profil")
        enregistrer(profil.copy(nomAffiché = nomPropre))
    }

    fun obtenirPhoto(): ByteArray? =
        try {
            source.lirePhoto()
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire la photo")
        }

    /** null : retire la photo. */
    fun changerPhoto(photo: ByteArray?) {
        try {
            source.écrirePhoto(photo)
        } catch (e: Exception) {
            throw ServiceException("Impossible de sauvegarder la photo")
        }
    }

    fun déconnecter() {
        try {
            source.effacer()
        } catch (e: Exception) {
            throw ServiceException("Impossible d'effacer le profil")
        }
    }

    private fun enregistrer(profil: Profil) {
        try {
            source.écrireProfil(profil)
        } catch (e: Exception) {
            throw ServiceException("Impossible de sauvegarder le profil")
        }
    }

    companion object {
        const val LONGUEUR_MAX_NOM = 30
    }
}