package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Quiz

interface SourceDeDonnées {
    fun obtenirQuiz(
        limite: Int = 10,
        décalage: Int = 0
    ): List<Quiz> // Decalage indique combien de quiz sauter au début de la liste
}