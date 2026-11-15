package com.crosemont.qwizup.sourceDeDonnees

/** Source « bidon » : simule la vraie source en gardant les favoris en mémoire. */

class SourceFavorisBidon(favorisDeDépart: Set<String> = emptySet()) : SourceFavoris {

    private var favoris = favorisDeDépart

    override fun lireFavoris(): Set<String> = favoris

    override fun écrireFavoris(titres: Set<String>) {
        favoris = titres
    }
}