package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ModèleQuiz

class ModèleAccueil(
    private val serviceStatistiques: ServiceStatistiques,
    // Les quiz publiés et leur code (le même catalogue que Créer)
    private val catalogue: ModèleQuiz = ModèleQuiz()
) : ContratAccueil.IModèleAccueil {

    override suspend fun obtenirStatistiques(): Statistiques =
        serviceStatistiques.obtenirStatistiques()

    override suspend fun obtenirQuizEnCours(): List<Int> {
        val complétés = serviceStatistiques.obtenirQuizComplétés().toSet()
        return QUIZ_EN_COURS_SIMULÉS.filter { it !in complétés }   // réussi à 100 % : plus en cours
    }

    // Le code tapé sur l'accueil : on cherche le quiz publié qui l'a reçu à sa publication
    override suspend fun trouverQuizParCode(code: String): Int? =
        catalogue.trouverQuizParCode(code)

    companion object {
        /**
         * Les quiz « en cours » sont encore simulés : la progression d'un quiz pas fini n'est pas sauvegardée.
         * Ce sont les trois cartes « Continuer » du XML :
         * 1 = Le système solaire, 2 = Bases de Kotlin, 101 = La Révolution tranquille.
         */
        val QUIZ_EN_COURS_SIMULÉS = listOf(1, 2, 101)
    }
}