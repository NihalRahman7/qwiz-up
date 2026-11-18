package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.domaine.entites.Quiz

interface SourceDeDonnees {
    suspend fun obtenirQuiz(
        limite: Int = 10,
        decalage: Int = 0
    ): List<Quiz> // Decalage indique combien de quiz sauter au début de la liste
}