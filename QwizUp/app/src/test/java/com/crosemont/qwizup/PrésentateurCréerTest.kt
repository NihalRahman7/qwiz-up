package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.ModeleQuiz.CarteCreate
import com.crosemont.qwizup.presentation.ModeleQuiz.QuizCreer
import com.crosemont.qwizup.presentation.create.ContratCréer
import com.crosemont.qwizup.presentation.create.ContratCréer.OngletCreer
import com.crosemont.qwizup.presentation.create.PrésentateurCréer
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PrésentateurCréerTest {

    private lateinit var vue: FausseVue
    private lateinit var modèle: FauxModèle
    private lateinit var présentateur: PrésentateurCréer

    @Before
    fun préparer() {
        vue = FausseVue()
        modèle = FauxModèle()
        // Unconfined : les coroutines s'exécutent tout de suite, sans vrai fil d'arrière-plan
        présentateur = PrésentateurCréer(modèle, Dispatchers.Unconfined, Dispatchers.Unconfined)
    }

    @Test
    fun `Étant donné deux brouillons et un quiz publié lorsqu'on attache la vue, on obtient les compteurs et les brouillons`() {
        présentateur.attacherVue(vue)

        assertEquals(2 to 1, vue.compteurs)
        assertEquals(OngletCreer.BROUILLONS, vue.ongletChoisi)
        assertEquals(listOf("Brouillon A", "Brouillon B"), vue.titresAffichés)
    }

    @Test
    fun `Étant donné un quiz publié lorsqu'on choisit l'onglet Publiés, on obtient seulement le quiz publié`() {
        présentateur.attacherVue(vue)

        présentateur.surOngletClique(OngletCreer.PUBLIES)

        assertEquals(OngletCreer.PUBLIES, vue.ongletChoisi)
        assertEquals(listOf("Quiz publié"), vue.titresAffichés)
    }

    @Test
    fun `Étant donné aucun brouillon lorsqu'on attache la vue, on obtient le message de liste vide`() {
        modèle.brouillons = emptyList()

        présentateur.attacherVue(vue)

        assertEquals(OngletCreer.BROUILLONS, vue.listeVide)
        assertTrue(vue.titresAffichés.isEmpty())
    }

    @Test
    fun `Étant donné un modèle en panne lorsqu'on attache la vue, on obtient un message d'erreur`() {
        modèle.enPanne = true

        présentateur.attacherVue(vue)

        assertTrue(vue.erreurChargement)
    }

    @Test
    fun `Étant donné la vue attachée lorsqu'on touche Nouveau quiz, on obtient l'éditeur ouvert sans id`() {
        présentateur.attacherVue(vue)

        présentateur.surNouveauQuizClique()

        assertTrue(vue.éditeurOuvert)
        assertNull(vue.idOuvert)
    }

    @Test
    fun `Étant donné la vue détachée lorsqu'on touche un onglet, on obtient aucun changement dans la vue`() {
        présentateur.attacherVue(vue)
        présentateur.detacherVue()
        vue.titresAffichés.clear()

        présentateur.surOngletClique(OngletCreer.PUBLIES)

        assertFalse(vue.titresAffichés.isNotEmpty())
    }

    private class FauxModèle : ContratCréer.IModèleCréer {
        var brouillons = listOf(CarteCreate(1, "Brouillon A", false, 3), CarteCreate(2, "Brouillon B", false, 1))
        var publiés = listOf(CarteCreate(3, "Quiz publié", true, 5))
        var enPanne = false

        override suspend fun obtenirBrouillons(): List<CarteCreate> {
            if (enPanne) throw IllegalStateException("panne")
            return brouillons
        }
        override suspend fun obtenirPubliés(): List<CarteCreate> = publiés
        override suspend fun obtenirQuiz(quizId: Int): QuizCreer = throw UnsupportedOperationException()
        override suspend fun sauvegarderQuiz(quiz: QuizCreer): Int = throw UnsupportedOperationException()
    }

    private class FausseVue : ContratCréer.IVueCréer {
        var compteurs: Pair<Int, Int>? = null
        var ongletChoisi: OngletCreer? = null
        val titresAffichés = mutableListOf<String>()
        var listeVide: OngletCreer? = null
        var éditeurOuvert = false
        var idOuvert: Int? = null
        var erreurChargement = false

        override fun afficherCompteurs(nbBrouillons: Int, nbPublies: Int) { compteurs = nbBrouillons to nbPublies }
        override fun afficherOngletChoisi(onglet: OngletCreer) { ongletChoisi = onglet }
        override fun viderCartes() { titresAffichés.clear(); listeVide = null }
        override fun ajouterCarteQuiz(id: Int, titre: String, nbQuestions: Int, estPublie: Boolean) { titresAffichés += titre }
        override fun afficherListeVide(onglet: OngletCreer) { listeVide = onglet }
        override fun ouvrirEditeur(quizId: Int?) { éditeurOuvert = true; idOuvert = quizId }
        override fun afficherErreurChargement() { erreurChargement = true }
    }
}
