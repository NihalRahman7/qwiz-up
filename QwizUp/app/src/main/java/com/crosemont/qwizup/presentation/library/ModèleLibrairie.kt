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
            OngletLibrairie.COMPLÉTÉS -> catalogue.obternirCompletes()
            OngletLibrairie.CRÉES -> catalogue.obternirCrees()
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