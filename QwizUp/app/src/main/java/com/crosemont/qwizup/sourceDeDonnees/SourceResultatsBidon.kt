package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.domaine.entites.ResultatPartie
import java.time.LocalDate

/**
 * Source « bidon » : des résultats codés en dur, gardés en mémoire seulement.
 * Sert aux tests et à simuler des données sans fichier.
 */
class SourceResultatsBidon(
    resultatsDeDepart: List<ResultatPartie> = listOf(
        ResultatPartie(1, 4, 5, LocalDate.now()),
        ResultatPartie(2, 3, 5, LocalDate.now().minusDays(1)),
        ResultatPartie(3, 5, 5, LocalDate.now().minusDays(2))
    )
) : SourceResultats {

    private val resultats = resultatsDeDepart.toMutableList()

    override fun lireResultats(): List<ResultatPartie> = resultats.toList()

    override fun ajouterResultat(resultat: ResultatPartie) {
        resultats.add(resultat)
    }
}