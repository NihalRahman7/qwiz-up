package com.crosemont.qwizup.presentation.library

import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon
import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteLibrary
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie


class ModèleLibrairie(
    private val serviceFavoris: ServiceFavoris,
    private val catalogue: ModèleQuiz = ModèleQuiz(),
    // Les parties jouées : sert à l'onglet Complétés (aucune partie par défaut)
    private val serviceStatistiques: ServiceStatistiques = ServiceStatistiques(SourceRésultatsBidon(emptyList())),
) : ContratLibrairie.IModèleLibrairie {

    override suspend fun obtenirQuiz(onglet: OngletLibrairie): List<CarteLibrary> =
        when (onglet) {
            OngletLibrairie.FAVORIS -> obtenirFavoris()
            OngletLibrairie.COMPLÉTÉS -> obtenirComplétés()
            OngletLibrairie.CRÉES -> catalogue.obternirCrees()
        }

    /** Les quiz réussis à 100 %. Un résultat dont le quiz n'existe plus est ignoré. */
    private fun obtenirComplétés(): List<CarteLibrary> =
        serviceStatistiques.obtenirQuizComplétés().mapNotNull { quizId ->
            catalogue.obtenirQuizÀJouer(quizId)?.let { quiz ->   // mêmes questions que dans le jeu
                val nb = quiz.questions.size
                CarteLibrary(
                    quiz.id,
                    quiz.titre,
                    "${if (nb == 1) "1 question" else "$nb questions"} · toutes bonnes",
                    badge = "100 %",
                    typeBadge = ContratLibrairie.TypeBadge.PROGRESSION   // badge vert
                )
            }
        }

    private suspend fun obtenirFavoris(): List<CarteLibrary> {
        val favoris = serviceFavoris.obtenirFavoris()
        catalogue.chargerQuizDiscover()   // les quiz viennent de ObtenirQuiz de SourceHttp.kt
        return catalogue.obtenirQuizDiscover(null, "")
            .filter { it.titre in favoris }
            .map { quiz ->
                CarteLibrary(
                    quiz.id,
                    quiz.titre,
                    "${nomCatégorie(quiz.catégorie)} · ${quiz.nbQuestions} questions",
                    badge = null,   // pas de badge pour un favori
                    typeBadge = ContratLibrairie.TypeBadge.NOUVEAU
                )
            }
    }

    private fun nomCatégorie(catégorie: CatégorieQuiz): String = when (catégorie) {
        CatégorieQuiz.SCIENCES -> "Sciences"
        CatégorieQuiz.HISTOIRE -> "Histoire"
        CatégorieQuiz.LANGUES -> "Langues"
        CatégorieQuiz.GÉOGRAPHIE -> "Géographie"
        CatégorieQuiz.INFORMATIQUE -> "Informatique"
        CatégorieQuiz.AUTRE -> "Autre"
    }
}