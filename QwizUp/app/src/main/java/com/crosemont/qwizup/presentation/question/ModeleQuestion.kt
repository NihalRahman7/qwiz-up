package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ModeleQuiz

/**
 * Modèle de l'écran de jeu.
 * Les quiz viennent de ModeleQuiz et les résultats sont enregistrés par ServiceStatistiques.
 */
class ModeleQuestion(
    private val serviceStatistiques: ServiceStatistiques,
    private val catalogue: ModeleQuiz = ModeleQuiz(),
) : ContratQuestion.IModeleQuestion {

    override suspend fun obtenirQuizAJouer(quizId: Int): ModeleQuiz.QuizCreer? =
        catalogue.obtenirQuizAJouer(quizId)

    override suspend fun enregistrerPartie(quizId: Int, score: Int, total: Int) {
        serviceStatistiques.enregistrerPartie(quizId, score, total)
    }
}