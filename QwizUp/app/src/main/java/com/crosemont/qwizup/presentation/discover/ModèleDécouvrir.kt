package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover

/**
 * Modèle de l'écran Découvrir.
 * Le catalogue des quiz vient de ModeleQuiz et les favoris viennent de ServiceFavoris.
 */
class ModèleDécouvrir(
    private val serviceFavoris: ServiceFavoris,
    private val catalogue: ModèleQuiz = ModèleQuiz(),
) : ContratDécouvrir.IModèleDécouvrir {

    @Volatile
    private var catalogueChargé = false

    override suspend fun obtenirQuiz(
        catégorie: CatégorieQuiz?,
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
        catalogue.obtenirQuizÀJouer(quizId) != null
}