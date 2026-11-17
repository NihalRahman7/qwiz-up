package com.crosemont.qwizup.domaine.entites

import java.time.LocalDate

data class ResultatPartie(
    val quizId: Int,
    val score: Int, // nombre de bonnes réponses
    val total: Int, // nombre de questions
    val date: LocalDate // jour où la partie a été jouée
)

data class Statistiques(
    val serie: Int, // jours consécutifs avec au moins une partie
    val nbQuizCompletes: Int, // nombre de quiz différents terminés
    val tauxReussite: Int // pourcentage de bonnes réponses (0 à 100)
)

