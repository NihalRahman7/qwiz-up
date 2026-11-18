package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.domaine.entites.RésultatPartie
import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.domaine.service.ServiceStatistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import com.crosemont.qwizup.presentation.discover.ModèleDécouvrir
import com.crosemont.qwizup.accèsAuxDonnées.SourceDeDonnées
import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisBidon
import com.crosemont.qwizup.accèsAuxDonnées.SourceRésultatsBidon
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ModèleDécouvrirTest {

    private fun créerModèle(vararg favoris: String) =
        ModèleDécouvrir(ServiceFavoris(SourceFavorisBidon(favoris.toSet())))

    private fun quizDe(modèle: ModèleDécouvrir, catégorie: CatégorieQuiz? = null, recherche: String = "") =
        runBlocking { modèle.obtenirQuiz(catégorie, recherche) }

    private fun basculer(modèle: ModèleDécouvrir, titre: String) =
        runBlocking { modèle.basculerFavori(titre) }

    private fun estJouable(modèle: ModèleDécouvrir, quizId: Int) =
        runBlocking { modèle.estJouable(quizId) }

    @Test
    fun `Étant donné qu'aucun filtre n'est appliqué lorsqu'on demande les quiz, on obtient les quatre quiz`() {
        val modèle = créerModèle()

        assertEquals(4, quizDe(modèle).size)
    }

    @Test
    fun `Étant donné que la catégorie Histoire est sélectionnée lorsqu'on demande les quiz, on obtient La Révolution tranquille`() {
        val modèle = créerModèle()

        val quiz = quizDe(modèle, catégorie = CatégorieQuiz.HISTOIRE)

        assertEquals(listOf("La Révolution tranquille"), quiz.map { it.titre })
    }

    @Test
    fun `Étant donné qu'une recherche est effectuée sans accent lorsqu'on demande les quiz, on obtient le quiz avec accent`() {
        val modèle = créerModèle()

        val quiz = quizDe(modèle, recherche = "revolution")

        assertEquals(listOf("La Révolution tranquille"), quiz.map { it.titre })
    }

    @Test
    fun `Étant donné qu'aucun favori n'est sauvegardé lorsqu'on demande les quiz, on obtient aucun quiz favori`() {
        val modèle = créerModèle()

        assertTrue(quizDe(modèle).none { it.estFavori })
    }

    @Test
    fun `Étant donné qu'un favori est sauvegardé lorsqu'on demande les quiz, on obtient seulement ce quiz en favori`() {
        val modèle = créerModèle("La Révolution tranquille")

        val favoris = quizDe(modèle).filter { it.estFavori }

        assertEquals(listOf("La Révolution tranquille"), favoris.map { it.titre })
    }

    @Test
    fun `Étant donné qu'un quiz n'est pas favori lorsqu'on bascule son favori, on obtient ce quiz en favori`() {
        val modèle = créerModèle()

        basculer(modèle, "Le système solaire")

        val favoris = quizDe(modèle).filter { it.estFavori }
        assertEquals(listOf("Le système solaire"), favoris.map { it.titre })
    }

    @Test
    fun `Étant donné qu'un quiz contient des questions lorsqu'on demande s'il est jouable, on obtient vrai`() {
        val modèle = créerModèle()

        assertTrue(estJouable(modèle, 1))   // 1 = Le système solaire
    }

    @Test
    fun `Étant donné que les quiz d'histoire et de langues ont des questions lorsqu'on demande s'ils sont jouables, on obtient vrai`() {
        val modèle = créerModèle()

        assertTrue(estJouable(modèle, 4))   // 4 = La Révolution tranquille
        assertTrue(estJouable(modèle, 5))   // 5 = Verbes irréguliers anglais
    }

    @Test
    fun `Étant donné qu'un quiz est inconnu lorsqu'on demande s'il est jouable, on obtient faux`() {
        val modèle = créerModèle()

        assertFalse(estJouable(modèle, 99))
    }

    @Test
    fun `Étant donné qu'un catalogue est fourni au modèle lorsqu'on demande les quiz, on obtient les quiz de ce catalogue`() {
        val source = object : SourceDeDonnées {
            override suspend fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> =
                listOf(Quiz(9, "test-9", "Quiz de test", "Sciences", 3))
        }
        val modèle = ModèleDécouvrir(
            ServiceFavoris(SourceFavorisBidon()),
            ModèleQuiz(ServiceQuiz(source))
        )

        assertEquals(listOf("Quiz de test"), quizDe(modèle).map { it.titre })
    }

    private fun modèleAvecParties(vararg parties: RésultatPartie) =
        ModèleDécouvrir(
            ServiceFavoris(SourceFavorisBidon()),
            serviceStatistiques = ServiceStatistiques(SourceRésultatsBidon(parties.toList()))
        )

    private fun scoresDe(modèle: ModèleDécouvrir) = runBlocking { modèle.obtenirMeilleursScores() }

    @Test
    fun `Étant donné qu'un quiz a été joué deux fois lorsqu'on demande les meilleurs scores, on obtient le meilleur des deux`() {
        val modèle = modèleAvecParties(
            RésultatPartie(1, 2, 5, LocalDate.of(2026, 11, 15)),
            RésultatPartie(1, 4, 5, LocalDate.of(2026, 11, 16))
        )

        assertEquals(MeilleurScore(4, 5), scoresDe(modèle)[1])
    }

    @Test
    fun `Étant donné qu'aucune partie n'a été jouée lorsqu'on demande les meilleurs scores, on obtient aucun score`() {
        val modèle = modèleAvecParties()

        assertTrue(scoresDe(modèle).isEmpty())
    }
}
