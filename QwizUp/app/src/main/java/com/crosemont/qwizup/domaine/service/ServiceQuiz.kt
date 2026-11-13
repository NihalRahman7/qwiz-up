package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.sourceDeDonnees.SourceBiddon
import com.crosemont.qwizup.sourceDeDonnees.SourceDeDonnees
import kotlinx.coroutines.CancellationException

class ServiceQuiz(private val source: SourceDeDonnees = SourceBiddon()) {
    suspend fun obtenirQuiz(limite: Int = 10, decalage: Int = 0): List<Quiz>{
        if (limite <= 0) throw ServiceException("La limite doit être supérieure à 0")
        if (decalage < 0) throw ServiceException("Le décalage ne peut pas être négatif")

        return try {
            source.obtenirQuiz(limite, decalage)
                .filter { it.titre.isNotBlank() && it.nbQuestions > 0 }
        } catch (e: CancellationException){
            throw e
        } catch (e: Exception){
            throw ServiceException("Impossible d'obtenir les quiz", e)
        }
    }
}