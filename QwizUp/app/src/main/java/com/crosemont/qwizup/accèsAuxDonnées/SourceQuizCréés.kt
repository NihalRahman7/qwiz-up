package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer

/**
 * Source de données des quiz créés par l'utilisateur (brouillons et quiz publiés).
 * Les fonctions sont « suspend » : une vraie source (fichier, réseau) peut prendre du temps.
 */
interface SourceQuizCréés {
    suspend fun lireQuiz(): List<QuizCréer>
    suspend fun écrireQuiz(quiz: List<QuizCréer>)
}
