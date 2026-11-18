package com.crosemont.qwizup

import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon
import com.crosemont.qwizup.domaine.entites.RésultatPartie
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.home.ContratAccueil
import com.crosemont.qwizup.presentation.home.ModèleAccueil
import com.crosemont.qwizup.presentation.home.PrésentateurAccueil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Section « Continuer » de l'accueil : un quiz réussi à 100 % n'est plus en cours,
 * il disparaît de « Continuer » (il est maintenant dans Librairie → Complétés).
 */
class AccueilQuizEnCoursTest {

    private val aujourdhui = LocalDate.of(2026, 11, 18)

    private fun partie(quizId: Int, score: Int, total: Int) =
        RésultatPartie(quizId, score, total, aujourdhui)

    // Quiz en cours simulés : 1 = Le système solaire, 2 = Bases de Kotlin, 101 = La Révolution tranquille
    private fun quizEnCours(vararg parties: RésultatPartie) = runBlocking {
        ModèleAccueil(ServiceStatistiques(SourceRésultatsBidon(parties.toList())) { aujourdhui })
            .obtenirQuizEnCours()
    }

    // ----- Modèle -----

    @Test
    fun `Étant donné qu'aucune partie n'a été jouée lorsqu'on obtient les quiz en cours, on obtient les trois quiz`() {
        assertEquals(listOf(1, 2, 101), quizEnCours())
    }

    @Test
    fun `Étant donné le système solaire réussi à 100 % lorsqu'on obtient les quiz en cours, il n'y est plus`() {
        assertEquals(listOf(2, 101), quizEnCours(partie(1, 5, 5)))
    }

    @Test
    fun `Étant donné le système solaire fini à 4 sur 5 lorsqu'on obtient les quiz en cours, il y est encore`() {
        assertEquals(listOf(1, 2, 101), quizEnCours(partie(1, 4, 5)))
    }

    @Test
    fun `Étant donné les trois quiz réussis à 100 % lorsqu'on obtient les quiz en cours, on obtient une liste vide`() {
        assertTrue(quizEnCours(partie(1, 5, 5), partie(2, 6, 6), partie(101, 4, 4)).isEmpty())
    }

    // ----- Présentateur -----

    private class FausseVue : ContratAccueil.IVueAccueil {
        var quizEnCoursAffichés: List<Int>? = null
        override fun ouvrirQuiz(quizId: Int) {}
        override fun ouvrirRejoindre() {}
        override fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int) {}
        override fun afficherQuizEnCours(quizIds: List<Int>) { quizEnCoursAffichés = quizIds }
        override fun afficherErreur() {}
    }

    private class FauxModèle(private val enCours: List<Int>) : ContratAccueil.IModèleAccueil {
        override suspend fun obtenirStatistiques() = Statistiques(0, 0, 0)
        override suspend fun obtenirQuizEnCours() = enCours
    }

    private fun attacher(enCours: List<Int>): FausseVue {
        val vue = FausseVue()
        PrésentateurAccueil(FauxModèle(enCours), Dispatchers.Unconfined, Dispatchers.Unconfined)
            .attacherVue(vue)
        return vue
    }

    @Test
    fun `Étant donné un seul quiz en cours lorsqu'on ouvre l'accueil, la vue affiche seulement cette carte`() {
        assertEquals(listOf(2), attacher(listOf(2)).quizEnCoursAffichés)
    }

    @Test
    fun `Étant donné aucun quiz en cours lorsqu'on ouvre l'accueil, la vue reçoit une liste vide`() {
        assertEquals(emptyList<Int>(), attacher(emptyList()).quizEnCoursAffichés)
    }
}
