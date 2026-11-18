package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.RésultatPartie
import java.time.LocalDate

/**
 * Source « bidon » : des résultats codés en dur, gardés en mémoire seulement.
 * Sert aux tests et à simuler des données sans fichier.
 */
class SourceRésultatsBidon(
    résultatsDeDepart: List<RésultatPartie> = listOf(
        RésultatPartie(1, 4, 5, LocalDate.now()),
        RésultatPartie(2, 3, 5, LocalDate.now().minusDays(1)),
        RésultatPartie(3, 5, 5, LocalDate.now().minusDays(2))
    )
) : SourceRésultats {

    private val résultats = résultatsDeDepart.toMutableList()

    override fun lireRésultats(): List<RésultatPartie> = résultats.toList()

    override fun ajouterRésultat(resultat: RésultatPartie) {
        résultats.add(resultat)
    }
}