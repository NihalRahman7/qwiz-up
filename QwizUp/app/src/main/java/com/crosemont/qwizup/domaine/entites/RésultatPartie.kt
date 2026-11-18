package com.crosemont.qwizup.domaine.entites

import java.time.LocalDate

data class RésultatPartie(
    val quizId: Int,
    val score: Int, // nombre de bonnes réponses
    val total: Int, // nombre de questions
    val date: LocalDate // jour où la partie a été jouée
)

data class Statistiques(
    val série: Int, // jours consécutifs avec au moins une partie
    val nbQuizComplètes: Int, // nombre de quiz différents terminés
    val tauxRéussite: Int // pourcentage de bonnes réponses (0 à 100)
)

