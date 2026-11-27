package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.presentation.home.ContratAccueil
import com.crosemont.qwizup.presentation.home.PrésentateurAccueil
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Accueil : toucher l'avatar ouvre l'écran Profil.
 */
class AccueilProfilTest {

    private class FausseVue : ContratAccueil.IVueAccueil {
        var profilOuvert = false

        override fun ouvrirQuiz(quizId: Int) {}
        override fun ouvrirProfil() { profilOuvert = true }
        override fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int) {}
        override fun afficherQuizEnCours(quizIds: List<Int>) {}
        override fun activerJouerCode(actif: Boolean) {}
        override fun afficherCodeInvalide() {}
        override fun effacerErreurCode() {}
        override fun afficherErreur() {}
    }

    private class FauxModèle : ContratAccueil.IModèleAccueil {
        override suspend fun obtenirStatistiques() = Statistiques(0, 0, 0)
        override suspend fun obtenirQuizEnCours() = emptyList<Int>()
        override suspend fun trouverQuizParCode(code: String): Int? = null
    }

    @Test
    fun `Étant donné l'accueil lorsqu'on touche l'avatar, on obtient l'écran Profil`() {
        val vue = FausseVue()
        val présentateur = PrésentateurAccueil(FauxModèle(), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)

        présentateur.surProfilClique()

        assertTrue(vue.profilOuvert)
    }
}