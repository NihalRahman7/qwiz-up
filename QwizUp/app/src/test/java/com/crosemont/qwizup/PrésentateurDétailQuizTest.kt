package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IVueDétailQuiz
import com.crosemont.qwizup.presentation.discover.PrésentateurDétailQuiz
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Écran Détail du quiz : affiche le quiz choisi dans Découvrir,
 * bascule le favori et ouvre le jeu (ou dit que le quiz arrive bientôt).
 */
class PrésentateurDétailQuizTest {

    // Faux modèle de Découvrir : deux quiz, le 1 est jouable et déjà joué
    private class FauxModèle(private val enPanne: Boolean = false) : ContratDécouvrir.IModèleDécouvrir {
        private val quiz = listOf(
            CarteDiscover(1, CatégorieQuiz.SCIENCES, "Le système solaire", "Planètes et lunes", 5, 3),
            CarteDiscover(2, CatégorieQuiz.HISTOIRE, "La Révolution tranquille", "Le Québec des années 1960", 4, 3)
        )
        val favoris = mutableSetOf<String>()

        override suspend fun obtenirQuiz(catégorie: CatégorieQuiz?, recherche: String): List<CarteDiscover> {
            if (enPanne) throw IllegalStateException("Le modèle est en panne")
            return quiz.map { it.copy(estFavori = it.titre in favoris) }
        }

        override suspend fun obtenirMeilleursScores() = mapOf(1 to MeilleurScore(4, 5))

        override suspend fun basculerFavori(titre: String) {
            if (titre in favoris) favoris.remove(titre) else favoris.add(titre)
        }

        override suspend fun estJouable(quizId: Int) = quizId == 1
    }

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : IVueDétailQuiz {
        var titre: String? = null
        var catégorie: CatégorieQuiz? = null
        var estFavori: Boolean? = null
        var meilleurScore: MeilleurScore? = null
        var jeuOuvert: Int? = null
        var bientôt: String? = null
        var fermée = false
        var erreur = false

        override fun afficherQuiz(
            catégorie: CatégorieQuiz, titre: String, description: String, nbQuestions: Int, duréeMinutes: Int
        ) {
            this.catégorie = catégorie
            this.titre = titre
        }
        override fun afficherFavori(estFavori: Boolean) { this.estFavori = estFavori }
        override fun afficherMeilleurScore(meilleurScore: MeilleurScore?) { this.meilleurScore = meilleurScore }
        override fun ouvrirJeu(quizId: Int) { jeuOuvert = quizId }
        override fun afficherQuizBientôt(titre: String) { bientôt = titre }
        override fun fermer() { fermée = true }
        override fun afficherErreur() { erreur = true }
    }

    private lateinit var vue: FausseVue
    private lateinit var modèle: FauxModèle
    private lateinit var présentateur: PrésentateurDétailQuiz

    @Before
    fun préparer() {
        vue = FausseVue()
        modèle = FauxModèle()
        présentateur = PrésentateurDétailQuiz(modèle, Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)
    }

    @Test
    fun `Étant donné le quiz 1 lorsqu'on ouvre son détail, on obtient son titre et sa catégorie`() {
        présentateur.chargerQuiz(1)

        assertEquals("Le système solaire", vue.titre)
        assertEquals(CatégorieQuiz.SCIENCES, vue.catégorie)
    }

    @Test
    fun `Étant donné le quiz 1 déjà joué lorsqu'on ouvre son détail, on obtient son meilleur score`() {
        présentateur.chargerQuiz(1)

        assertEquals(MeilleurScore(4, 5), vue.meilleurScore)
    }

    @Test
    fun `Étant donné le quiz 2 jamais joué lorsqu'on ouvre son détail, on obtient aucun meilleur score`() {
        présentateur.chargerQuiz(2)

        assertNull(vue.meilleurScore)
    }

    @Test
    fun `Étant donné un quiz pas favori lorsqu'on touche l'étoile, on obtient le quiz en favori`() {
        présentateur.chargerQuiz(1)

        présentateur.surFavoriClique()

        assertEquals(setOf("Le système solaire"), modèle.favoris)
        assertEquals(true, vue.estFavori)
    }

    @Test
    fun `Étant donné un quiz jouable lorsqu'on touche Jouer, on obtient l'écran de jeu`() {
        présentateur.chargerQuiz(1)

        présentateur.surJouerClique()

        assertEquals(1, vue.jeuOuvert)
    }

    @Test
    fun `Étant donné un quiz sans questions lorsqu'on touche Jouer, on obtient le message bientôt`() {
        présentateur.chargerQuiz(2)

        présentateur.surJouerClique()

        assertEquals("La Révolution tranquille", vue.bientôt)
        assertNull(vue.jeuOuvert)
    }

    @Test
    fun `Étant donné un quiz introuvable lorsqu'on ouvre son détail, on obtient une erreur`() {
        présentateur.chargerQuiz(99)

        assertTrue(vue.erreur)
    }

    @Test
    fun `Étant donné un modèle en panne lorsqu'on ouvre un détail, on obtient une erreur`() {
        val vueEnPanne = FausseVue()
        val présentateurEnPanne =
            PrésentateurDétailQuiz(FauxModèle(enPanne = true), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateurEnPanne.attacherVue(vueEnPanne)

        présentateurEnPanne.chargerQuiz(1)

        assertTrue(vueEnPanne.erreur)
    }

    @Test
    fun `Étant donné le détail d'un quiz lorsqu'on touche la flèche, on obtient l'écran fermé`() {
        présentateur.surRetourClique()

        assertTrue(vue.fermée)
    }
}