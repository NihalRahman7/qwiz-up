package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteDiscover

/**
 * Modèle de l'écran Découvrir.
 * Le catalogue des quiz vient de ModeleQuiz et les favoris viennent de ServiceFavoris.
 */
class ModèleDécouvrir(
    private val serviceFavoris: ServiceFavoris,
    private val catalogue: ModeleQuiz = ModeleQuiz(),
) : ContratDécouvrir.IModèleDécouvrir {

    @Volatile
    private var catalogueChargé = false

    override suspend fun obtenirQuiz(
        catégorie: CategorieQuiz?,
        recherche: String
    ): List<CarteDiscover> {
        // Les quiz viennent du service, chargés une seule fois
        if (!catalogueChargé) {
            catalogue.chargerQuizDiscover()
            catalogueChargé = true
        }
        val favoris = serviceFavoris.obtenirFavoris()
        return catalogue.obtenirQuizDiscover(catégorie, recherche)
            .map { it.copy(estFavori = it.titre in favoris) }
    }

    override suspend fun basculerFavori(titre: String) {
        serviceFavoris.basculerFavori(titre)
    }

    override suspend fun estJouable(quizId: Int): Boolean =
        catalogue.obtenirQuizAJouer(quizId) != null
}