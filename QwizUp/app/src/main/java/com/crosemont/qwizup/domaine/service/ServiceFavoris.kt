package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.sourceDeDonnees.SourceFavoris

/**
 * Gère les quiz favoris de l'utilisateur.
 * La source de données est fournie de l'extérieur, par son interface.
 */
class ServiceFavoris(private val source: SourceFavoris) {

    fun obtenirFavoris(): Set<String> =
        try {
            source.lireFavoris()
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire les favoris")
        }

    /** Ajoute le quiz aux favoris s'il n'y est pas, sinon le retire. */
    fun basculerFavori(titre: String) {
        if (titre.isBlank()) throw ServiceException("Le titre du quiz est vide")

        val favoris = obtenirFavoris()
        val nouveauxFavoris = if (titre in favoris) favoris - titre else favoris + titre

        try {
            source.écrireFavoris(nouveauxFavoris)
        } catch (e: Exception) {
            throw ServiceException("Impossible de sauvegarder les favoris")
        }
    }
}