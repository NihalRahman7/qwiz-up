package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.accèsAuxDonnées.SourceBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceDeDonnées
import com.crosemont.qwizup.domaine.exceptions.SourceDeDonnéesException

class ObtenirQuiz(private val source: SourceDeDonnées = SourceBidon()) {

    fun exécuter(limite: Int = 10, décalage: Int = 0): List<Quiz> {
        if (limite <= 0) throw ServiceException("La limite doit être supérieure à 0")
        if (décalage < 0) throw ServiceException("Le décalage ne peut pas être négatif")

        return try {
            source.obtenirQuiz(limite, décalage)
                .filter { it.titre.isNotBlank() && it.nbQuestions > 0 }
        } catch (e: SourceDeDonnéesException) {
            throw ServiceException("Impossible d'obtenir les quiz", e)
        }
    }
}