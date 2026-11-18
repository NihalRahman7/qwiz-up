package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.domaine.entites.ResultatPartie
import java.io.File
import java.time.LocalDate

/**
 * Garde les résultats dans un fichier sur l'appareil.
 * Une ligne par partie : quizId;score;total;date  (ex. : 1;4;5;2026-11-15)
 */
class SourceResultatsLocale(private val fichier: File) : SourceResultats {

    override fun lireResultats(): List<ResultatPartie> {

        if (!fichier.exists()) return emptyList() // Premier lancement de l'app : aucun fichier, donc aucun résultat

        return fichier.readLines()
            .filter { it.isNotBlank() }
            .map { ligne ->
                val (quizId, score, total, date) = ligne.split(";")
                ResultatPartie(quizId.toInt(), score.toInt(), total.toInt(), LocalDate.parse(date))
            }
    }

    override fun ajouterResultat(resultat: ResultatPartie) {

        fichier.appendText("${resultat.quizId};${resultat.score};${resultat.total};${resultat.date}\n") // On ajoute à la fin du fichier sans effacer les anciennes parties
    }

    companion object {

        const val NOM_FICHIER =
            "resultats.txt" // Le même fichier est utilisé par l'accueil (lecture) et le jeu (écriture)
    }
}