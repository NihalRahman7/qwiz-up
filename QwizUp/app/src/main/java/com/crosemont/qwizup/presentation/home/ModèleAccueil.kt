package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceStatistiques

class ModèleAccueil(
    private val serviceStatistiques: ServiceStatistiques
) : ContratAccueil.IModèleAccueil {

    override suspend fun obtenirStatistiques(): Statistiques =
        serviceStatistiques.obtenirStatistiques()

    override suspend fun obtenirQuizEnCours(): List<Int> {
        val complétés = serviceStatistiques.obtenirQuizComplétés().toSet()
        return QUIZ_EN_COURS_SIMULÉS.filter { it !in complétés }   // réussi à 100 % : plus en cours
    }

    companion object {
        /**
         * Les quiz « en cours » sont encore simulés : la progression d'un quiz pas fini n'est pas sauvegardée.
         * Ce sont les deux cartes « Continuer » du XML : 1 = Le système solaire, 2 = Bases de Kotlin.
         */
        val QUIZ_EN_COURS_SIMULÉS = listOf(1, 2)
    }
}