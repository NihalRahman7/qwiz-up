package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ModèleQuiz

/**
 * Modèle de l'écran de jeu.
 * Les quiz viennent de ModeleQuiz et les résultats sont enregistrés par ServiceStatistiques.
 */
class ModèleQuestion(
    private val serviceStatistiques: ServiceStatistiques,
    private val catalogue: ModèleQuiz = ModèleQuiz(),
) : ContratQuestion.IModèleQuestion {

    override suspend fun obtenirQuizAJouer(quizId: Int): ModèleQuiz.QuizCréer? =
        catalogue.obtenirQuizÀJouer(quizId)

    override suspend fun enregistrerPartie(quizId: Int, score: Int, total: Int) {
        serviceStatistiques.enregistrerPartie(quizId, score, total)
    }
}