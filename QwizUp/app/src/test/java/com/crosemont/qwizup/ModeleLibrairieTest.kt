package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceFavoris
import com.crosemont.qwizup.domaine.service.ObtenirQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ModèleLibrairie
import com.crosemont.qwizup.accèsAuxDonnées.SourceDeDonnées
import com.crosemont.qwizup.accèsAuxDonnées.SourceFavorisBidon
import com.crosemont.qwizup.domaine.exceptions.SourceDeDonnéesException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeleLibrairieTest {

    private class SourceQuizTest : SourceDeDonnées {
        override  fun obtenirQuiz(limite: Int, décalage: Int): List<Quiz> = listOf(
            Quiz( "Le système solaire", "Sciences", 10),
            Quiz("La Révolution tranquille", "Histoire", 8),
            Quiz("Verbes irréguliers anglais", "Langues", 15)
        )
    }

    private fun catalogue() = ModèleQuiz(ObtenirQuiz(SourceQuizTest()))

    private fun creerModele(vararg favoris: String) =
        ModèleLibrairie(ServiceFavoris(SourceFavorisBidon(favoris.toSet())), catalogue())

    private fun quizDe(modele: ModèleLibrairie, onglet: OngletLibrairie = OngletLibrairie.FAVORIS) =
        runBlocking { modele.obtenirQuiz(onglet) }

    @Test
    fun `Étant donné qu'aucun favori n'existe lorsqu'on demande les favoris, on obtient une liste vide`() {
        val modele = creerModele()

        assertTrue(quizDe(modele).isEmpty())
    }

    @Test
    fun `Étant donné qu'un favori a été mis dans Découvrir lorsqu'on demande les favoris, on obtient ce quiz avec sa catégorie`() {
        val modele = creerModele("La Révolution tranquille")

        val quiz = quizDe(modele).single()

        assertEquals("La Révolution tranquille", quiz.titre)
        assertEquals("Histoire · 8 questions", quiz.sousTitre)
        assertNull(quiz.badge)
    }

    @Test
    fun `Étant donné qu'un favori n'existe plus dans le catalogue lorsqu'on demande les favoris, on obtient ce titre ignoré`() {
        val modele = creerModele("Un quiz supprimé", "Le système solaire")

        assertEquals(listOf("Le système solaire"), quizDe(modele).map { it.titre })
    }

    @Test(expected = ServiceException::class)
    fun `Étant donné que la source des quiz est en panne lorsqu'on demande les favoris, on obtient une ServiceException`() {
        val sourceEnPanne = object : SourceDeDonnées {
            override fun obtenirQuiz(limite: Int, décalage: Int): List<Quiz> =
                throw SourceDeDonnéesException("Pas de réseau")
        }
        val modele = ModèleLibrairie(
            ServiceFavoris(SourceFavorisBidon(setOf("Le système solaire"))),
            ModèleQuiz(ObtenirQuiz(sourceEnPanne))
        )

        quizDe(modele)
    }
}