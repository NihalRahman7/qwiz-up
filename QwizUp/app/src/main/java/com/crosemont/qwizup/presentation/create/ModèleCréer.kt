package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.domaine.service.ServiceCréation
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.ModeleQuiz.QuizCreer

/**
 * Modèle de l'écran Créer et de l'éditeur.
 * Il passe par ServiceCréation et transforme les quiz en cartes faciles à afficher.
 */
class ModèleCréer(private val service: ServiceCréation) : ContratCréer.IModèleCréer {

    override suspend fun obtenirBrouillons(): List<CarteCreate> = service.obtenirBrouillons().map { it.enCarte() }

    override suspend fun obtenirPubliés(): List<CarteCreate> = service.obtenirPubliés().map { it.enCarte() }

    override suspend fun obtenirQuiz(quizId: Int): QuizCreer = service.obtenirQuiz(quizId)

    override suspend fun sauvegarderQuiz(quiz: QuizCreer): Int = service.sauvegarder(quiz).id

    private fun QuizCreer.enCarte() = CarteCreate(id, titre, estPublie, questions.size)
}
