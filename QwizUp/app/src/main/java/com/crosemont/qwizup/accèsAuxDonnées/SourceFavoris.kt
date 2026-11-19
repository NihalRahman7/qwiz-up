package com.crosemont.qwizup.accèsAuxDonnées

/**
 * Source de données des quiz favoris de l'utilisateur.
 * Un favori est retenu par le titre du quiz.
 */

interface SourceFavoris {
    fun lireFavoris(): Set<String>
    fun écrireFavoris(titres: Set<String>)
}