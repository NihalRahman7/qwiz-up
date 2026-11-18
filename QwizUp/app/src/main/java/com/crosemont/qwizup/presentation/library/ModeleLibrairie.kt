package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteLibrary
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie


class ModeleLibrairie(
    private val serviceFavoris: ServiceFavoris,
    private val catalogue: ModeleQuiz = ModeleQuiz(),
) : ContratLibrairie.IModeleLibrairie {

    override suspend fun obtenirQuiz(onglet: OngletLibrairie): List<CarteLibrary> =
        when (onglet) {
            OngletLibrairie.FAVORIS -> obtenirFavoris()
            OngletLibrairie.COMPLETES -> catalogue.obternirCompletes()
            OngletLibrairie.CREES -> catalogue.obternirCrees()
        }

    private suspend fun obtenirFavoris(): List<CarteLibrary> {
        val favoris = serviceFavoris.obtenirFavoris()
        catalogue.chargerQuizDiscover()   // les quiz viennent de ServiceQuiz
        return catalogue.obtenirQuizDiscover(null, "")
            .filter { it.titre in favoris }
            .map { quiz ->
                CarteLibrary(
                    quiz.id,
                    quiz.titre,
                    "${nomCatégorie(quiz.categorie)} · ${quiz.nbQuestions} questions",
                    badge = null,   // pas de badge pour un favori
                    typeBadge = ContratLibrairie.TypeBadge.NOUVEAU
                )
            }
    }

    private fun nomCatégorie(catégorie: CategorieQuiz): String = when (catégorie) {
        CategorieQuiz.SCIENCES -> "Sciences"
        CategorieQuiz.HISTOIRE -> "Histoire"
        CategorieQuiz.LANGUES -> "Langues"
        CategorieQuiz.GEOGRAPHIE -> "Géographie"
        CategorieQuiz.INFORMATIQUE -> "Informatique"
        CategorieQuiz.AUTRE -> "Autre"
    }
}