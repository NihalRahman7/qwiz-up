package com.crosemont.qwizup

import com.crosemont.qwizup.accèsAuxDonnées.SourceBidon
import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ObtenirQuiz
import com.crosemont.qwizup.accèsAuxDonnées.SourceDeDonnées
import com.crosemont.qwizup.domaine.exceptions.SourceDeDonnéesException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Duration

class ObtenirQuizTest {
    private class SourceFixe(private val quiz: List<Quiz>) : SourceDeDonnées {
        override fun obtenirQuiz(limite: Int, decalage: Int) = quiz
    }

    private class SourceEnPanne : SourceDeDonnées {
        override fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> =
            throw SourceDeDonnéesException("Pas de réseau")
    }

    // Source bidon sans délai, pour que les tests soient rapides
    private fun sourceBidon() = SourceBidon(Duration.ZERO)

    @Test
    fun `Étant donné une source avec un quiz lorsqu'on demande les quiz, on obtient ce quiz`() =
        runTest {
            val quiz = listOf(Quiz("Kotlin", "Programmation", 10))
            val service = ObtenirQuiz(SourceFixe(quiz))

            val résultat = service.exécuter()

            assertEquals(quiz, résultat)
        }

    @Test
    fun `Étant donné aucune source fournie lorsque on demande les quiz, on obtient ceux de la source bidon`() =
        runTest {
            val service = ObtenirQuiz()
            val résultat = service.exécuter()

            assertTrue(résultat.isNotEmpty())
        }

    @Test
    fun `Étant donné une limite de 2 et un décalage de 2 lorsque on demande les quiz, on obtient les quiz 3 et 4`() =
        runTest {
            val service = ObtenirQuiz(SourceBidon())
            val résultat = service.exécuter(limite = 2, décalage = 2)

            assertEquals(
                listOf("La Révolution tranquille", "Verbes irréguliers anglais"),
                résultat.map { it.titre }
            )
        }

    @Test
    fun `Étant donné des quiz invalides lorsque on demande les quiz, on obtient seulement les quiz valides`() =
        runTest {
            val source = SourceFixe(
                listOf(
                    Quiz("", "Cat", 5),               // Titre vide
                    Quiz("Sans question", "Cat", 0),  // Aucune question
                    Quiz("Valide", "Cat", 4)
                )
            )
            val service = ObtenirQuiz(source)
            val résultat = service.exécuter()

            assertEquals(listOf("Valide"), résultat.map { it.titre })
        }

    @Test
    fun `Étant donné une source en panne lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ObtenirQuiz(SourceEnPanne())

            assertThrows(ServiceException::class.java) { service.exécuter() }
        }

    @Test
    fun `Étant donné une limite de 0 lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ObtenirQuiz(SourceBidon())

            assertThrows(ServiceException::class.java) { service.exécuter(limite = 0) }
        }

    @Test
    fun `Étant donné un décalager negatif lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ObtenirQuiz(SourceBidon())

            assertThrows(ServiceException::class.java) { service.exécuter(décalage = -1) }
        }
}