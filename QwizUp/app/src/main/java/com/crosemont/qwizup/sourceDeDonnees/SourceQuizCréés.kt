package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.presentation.ModeleQuiz.QuizCreer

/**
 * Source de données des quiz créés par l'utilisateur (brouillons et quiz publiés).
 * Les fonctions sont « suspend » : une vraie source (fichier, réseau) peut prendre du temps.
 */
interface SourceQuizCréés {
    suspend fun lireQuiz(): List<QuizCreer>
    suspend fun écrireQuiz(quiz: List<QuizCreer>)
}
