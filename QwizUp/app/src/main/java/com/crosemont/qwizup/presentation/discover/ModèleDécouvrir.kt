package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon

/**
 * Modèle de l'écran Découvrir.
 * Les quiz viennent du catalogue (ModèleQuiz), les favoris de ServiceFavoris
 * et les meilleurs scores de ServiceStatistiques (les parties jouées dans l'écran de jeu).
 */
class ModèleDécouvrir(
    private val serviceFavoris: ServiceFavoris,
    private val catalogue: ModèleQuiz = ModèleQuiz(),
    private val serviceStatistiques: ServiceStatistiques = ServiceStatistiques(SourceRésultatsBidon(emptyList())),
) : ContratDécouvrir.IModèleDécouvrir {

    @Volatile
    private var catalogueChargé = false

    override suspend fun obtenirQuiz(catégorie: CatégorieQuiz?, recherche: String): List<CarteDiscover> {
        // Les quiz viennent du service, chargés une seule fois
        if (!catalogueChargé) {
            catalogue.chargerQuizDiscover()
            catalogueChargé = true
        }
        val favoris = serviceFavoris.obtenirFavoris()
        return catalogue.obtenirQuizDiscover(catégorie, recherche)
            .map { it.copy(estFavori = it.titre in favoris) }
    }

    override suspend fun obtenirMeilleursScores(): Map<Int, MeilleurScore> =
        serviceStatistiques.obtenirMeilleursScores()
            .mapValues { (_, partie) -> MeilleurScore(partie.score, partie.total) }

    override suspend fun basculerFavori(titre: String) {
        serviceFavoris.basculerFavori(titre)
    }

    override suspend fun estJouable(quizId: Int): Boolean =
        catalogue.obtenirQuizÀJouer(quizId) != null
}
