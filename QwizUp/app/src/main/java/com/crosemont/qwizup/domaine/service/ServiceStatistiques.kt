package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.domaine.entites.ResultatPartie
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.sourceDeDonnees.SourceResultats
import java.time.LocalDate

/**
 * Calcule les statistiques du joueur et enregistre ses parties.
 * La source est fournie de l'extérieur, par son interface.
 * La date du jour aussi, pour pouvoir la fixer dans les tests.
 */
class ServiceStatistiques(
    private val source: SourceResultats,
    private val aujourdhui: () -> LocalDate = { LocalDate.now() }
) {

    fun obtenirStatistiques(): Statistiques {
        val resultats = lireResultats()
        return Statistiques(
            serie = calculerSerie(resultats),
            nbQuizCompletes = resultats.map { it.quizId }.distinct().size,
            tauxReussite = calculerTauxReussite(resultats)
        )
    }

    /** Enregistre une partie terminée, datée d'aujourd'hui. */
    fun enregistrerPartie(quizId: Int, score: Int, total: Int) {
        if (total <= 0) throw ServiceException("Le quiz n'a aucune question")
        if (score !in 0..total) throw ServiceException("Le score doit être entre 0 et $total")

        try {
            source.ajouterResultat(ResultatPartie(quizId, score, total, aujourdhui()))
        } catch (e: Exception) {
            throw ServiceException("Impossible de sauvegarder le résultat")
        }
    }

    private fun lireResultats(): List<ResultatPartie> =
        try {
            source.lireResultats()
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire les résultats")
        }

    // Compte les jours consécutifs joués, en reculant à partir d'aujourd'hui
    private fun calculerSerie(resultats: List<ResultatPartie>): Int {
        val joursJoues = resultats.map { it.date }.toSet()

        var jour = aujourdhui()
        // Pas encore joué aujourd'hui : la série d'hier compte encore
        if (jour !in joursJoues) jour = jour.minusDays(1)

        var serie = 0
        while (jour in joursJoues) {
            serie++
            jour = jour.minusDays(1)
        }
        return serie
    }

    // Bonnes réponses sur le total des questions, en pourcentage
    private fun calculerTauxReussite(resultats: List<ResultatPartie>): Int {
        val totalQuestions = resultats.sumOf { it.total }
        if (totalQuestions == 0) return 0
        return resultats.sumOf { it.score } * 100 / totalQuestions
    }
}