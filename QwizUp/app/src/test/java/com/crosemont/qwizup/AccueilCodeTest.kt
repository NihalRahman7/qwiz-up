package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.presentation.home.ContratAccueil
import com.crosemont.qwizup.presentation.home.PrésentateurAccueil
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Accueil : on tape le code d'un quiz (5 lettres ou chiffres) et « Jouer » ouvre ce quiz.
 */
class AccueilCodeTest {

    // Faux modèle : un seul quiz publié, le 3, avec le code K7M4Q
    private class FauxModèle : ContratAccueil.IModèleAccueil {
        override suspend fun obtenirStatistiques() = Statistiques(0, 0, 0)
        override suspend fun obtenirQuizEnCours() = emptyList<Int>()
        override suspend fun trouverQuizParCode(code: String): Int? = if (code == "K7M4Q") 3 else null
    }

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : ContratAccueil.IVueAccueil {
        var boutonActif: Boolean? = null
        var codeInvalide = false
        var quizOuvert: Int? = null

        override fun ouvrirQuiz(quizId: Int) { quizOuvert = quizId }
        override fun ouvrirProfil() {}
        override fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int) {}
        override fun afficherQuizEnCours(quizIds: List<Int>) {}
        override fun activerJouerCode(actif: Boolean) { boutonActif = actif }
        override fun afficherCodeInvalide() { codeInvalide = true }
        override fun effacerErreurCode() { codeInvalide = false }
        override fun afficherErreur() {}
    }

    private lateinit var vue: FausseVue
    private lateinit var présentateur: PrésentateurAccueil

    @Before
    fun préparer() {
        vue = FausseVue()
        présentateur = PrésentateurAccueil(FauxModèle(), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)
    }

    @Test
    fun `Étant donné l'accueil lorsqu'on l'ouvre, on obtient le bouton Jouer du code désactivé`() {
        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné 5 lettres et chiffres lorsqu'on tape, on obtient le bouton Jouer activé`() {
        présentateur.surCodeModifié("K7M4Q")

        assertEquals(true, vue.boutonActif)
    }

    @Test
    fun `Étant donné un vieux code de 6 chiffres lorsqu'on tape, on obtient le bouton Jouer désactivé`() {
        présentateur.surCodeModifié("111111")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné un code avec un symbole lorsqu'on tape, on obtient le bouton Jouer désactivé`() {
        présentateur.surCodeModifié("K7M-Q")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné le code d'un quiz publié lorsqu'on touche Jouer, on obtient ce quiz ouvert`() {
        présentateur.surJouerCodeClique("K7M4Q")

        assertEquals(3, vue.quizOuvert)
    }

    @Test
    fun `Étant donné un code tapé en minuscules lorsqu'on touche Jouer, on obtient le même quiz ouvert`() {
        présentateur.surJouerCodeClique("k7m4q")

        assertEquals(3, vue.quizOuvert)
    }

    @Test
    fun `Étant donné un code qui n'existe pas lorsqu'on touche Jouer, on obtient code invalide`() {
        présentateur.surJouerCodeClique("ZZZZZ")

        assertTrue(vue.codeInvalide)
        assertNull(vue.quizOuvert)
    }

    @Test
    fun `Étant donné un code invalide affiché lorsqu'on tape de nouveau, on obtient le message effacé`() {
        présentateur.surJouerCodeClique("ZZZZZ")

        présentateur.surCodeModifié("K7M4")

        assertEquals(false, vue.codeInvalide)
    }
}