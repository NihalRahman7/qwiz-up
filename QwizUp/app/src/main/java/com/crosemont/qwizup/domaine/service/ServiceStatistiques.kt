package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.domaine.entites.RésultatPartie
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultats
import java.time.LocalDate

/**
 * Calcule les statistiques du joueur et enregistre ses parties.
 * La source est fournie de l'extérieur, par son interface.
 * La date du jour aussi, pour pouvoir la fixer dans les tests.
 */
class ServiceStatistiques(
    private val source: SourceRésultats,
    private val aujourdhui: () -> LocalDate = { LocalDate.now() }
) {

    fun obtenirStatistiques(): Statistiques {
        val résultats = lireRésultats()
        return Statistiques(
            série = calculerSérie(résultats),
            nbQuizComplètes = résultats.map { it.quizId }.distinct().size,
            tauxRéussite = calculerTauxRéussite(résultats)
        )
    }

    /**
     * Les quiz complétés : ceux réussis à 100 % (toutes les bonnes réponses) au moins une fois.
     * Le plus récent en premier, chaque quiz une seule fois.
     */
    fun obtenirQuizComplétés(): List<Int> =
        lireRésultats()
            .reversed()                          // la dernière partie ajoutée en premier
            .filter { it.score == it.total }     // toutes les bonnes réponses
            .sortedByDescending { it.date }      // le jour le plus récent en premier
            .map { it.quizId }
            .distinct()                          // un quiz réussi deux fois compte une fois

    /** Enregistre une partie terminée, datée d'aujourd'hui. */
    fun enregistrerPartie(quizId: Int, score: Int, total: Int) {
        if (total <= 0) throw ServiceException("Le quiz n'a aucune question")
        if (score !in 0..total) throw ServiceException("Le score doit être entre 0 et $total")

        try {
            source.ajouterRésultat(RésultatPartie(quizId, score, total, aujourdhui()))
        } catch (e: Exception) {
            throw ServiceException("Impossible de sauvegarder le résultat")
        }
    }

    /** Le meilleur résultat obtenu pour chaque quiz déjà joué (la clé est le numéro du quiz). */
    fun obtenirMeilleursScores(): Map<Int, RésultatPartie> =
        lireRésultats()
            .groupBy { it.quizId }
            .mapValues { (_, parties) -> parties.maxBy { it.score.toDouble() / it.total } }

    private fun lireRésultats(): List<RésultatPartie> =
        try {
            source.lireRésultats()
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire les résultats")
        }

    // Compte les jours consécutifs joués, en reculant à partir d'aujourd'hui
    private fun calculerSérie(résultats: List<RésultatPartie>): Int {
        val joursJoues = résultats.map { it.date }.toSet()

        var jour = aujourdhui()
        // Pas encore joué aujourd'hui : la série d'hier compte encore
        if (jour !in joursJoues) jour = jour.minusDays(1)

        var série = 0
        while (jour in joursJoues) {
            série++
            jour = jour.minusDays(1)
        }
        return série
    }

    // Bonnes réponses sur le total des questions, en pourcentage
    private fun calculerTauxRéussite(resultats: List<RésultatPartie>): Int {
        val totalQuestions = resultats.sumOf { it.total }
        if (totalQuestions == 0) return 0
        return resultats.sumOf { it.score } * 100 / totalQuestions
    }
}