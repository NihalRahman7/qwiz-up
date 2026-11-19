package com.crosemont.qwizup.accèsAuxDonnées

import java.io.File

/**
 * Source « locale » : les favoris sont sauvegardés dans un fichier sur l'appareil,
 * un titre de quiz par ligne. Ils restent donc là quand on ferme l'application.
 */
class SourceFavorisLocale(private val fichier: File) : SourceFavoris {

    override fun lireFavoris(): Set<String> {
        if (!fichier.exists()) return emptySet()   // Aucun favori n'a encore été sauvegardé
        return fichier.readLines().filter { it.isNotBlank() }.toSet()
    }

    override fun écrireFavoris(titres: Set<String>) {
        fichier.writeText(titres.joinToString("\n"))
    }
}