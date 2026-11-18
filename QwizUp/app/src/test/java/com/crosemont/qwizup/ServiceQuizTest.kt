package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.domaine.service.ServiceQuiz
import com.crosemont.qwizup.sourceDeDonnees.SourceDeDonnees
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class ServiceQuizTest {
    private class SourceFixe(private val quiz: List<Quiz>) : SourceDeDonnees {
        override suspend fun obtenirQuiz(limite: Int, decalage: Int) = quiz
    }

    private class SourceEnPanne : SourceDeDonnees {
        override suspend fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> =
            throw IOException("Pas de réseau")
    }

    private suspend inline fun <reified T : Throwable> assertLance(bloc: suspend () -> Unit) {
        try {
            bloc()
        } catch (e: Throwable) {
            if (e is T) return
            throw e
        }
        fail("${T::class.simpleName} attendue")
    }

    @Test
    fun `Étant donné une source avec un quiz lorsqu'on demande les quiz, on obtient ce quiz`() =
        runTest {
            val quiz = listOf(Quiz(1, "a", "Kotlin", "Programmation", 10))
            val service = ServiceQuiz(SourceFixe(quiz))
            val resultat = service.obtenirQuiz()

            assertEquals(quiz, resultat)
        }

    @Test
    fun `Étant donné aucune source fournie lorsque on demande les quiz, on obtient ceux de la source bidon`() =
        runTest {
            val service = ServiceQuiz()
            val resultat = service.obtenirQuiz()

            assertTrue(resultat.isNotEmpty())
        }

    @Test
    fun `Étant donné une limite de 2 et un décalage de 2 lorsque on demande les quiz, on obtient les quiz 3 et 4`() =
        runTest {
            val service = ServiceQuiz()
            val resultat = service.obtenirQuiz(limite = 2, decalage = 2)

            assertEquals(listOf(4, 5), resultat.map { it.id })
        }

    @Test
    fun `Étant donné des quiz invalides lorsque on demande les quiz, on obtient seulement les quiz valides`() =
        runTest {
            val source = SourceFixe(
                listOf(
                    Quiz(1, "a", "", "Cat", 5),               // Titre vide
                    Quiz(2, "b", "Sans question", "Cat", 0),  // Aucune question
                    Quiz(3, "c", "Valide", "Cat", 4)
                )
            )
            val service = ServiceQuiz(source)
            val resultat = service.obtenirQuiz()

            assertEquals(listOf(3), resultat.map { it.id })
        }

    @Test
    fun `Étant donné une source en panne lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ServiceQuiz(SourceEnPanne())

            assertLance<ServiceException> { service.obtenirQuiz() }
        }

    @Test
    fun `Étant donné une limite de 0 lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ServiceQuiz()

            assertLance<ServiceException> { service.obtenirQuiz(limite = 0) }
        }

    @Test
    fun `Étant donné un décalager negatif lorsque on demande les quiz, on obtient une ServiceException`() =
        runTest {
            val service = ServiceQuiz()

            assertLance<ServiceException> { service.obtenirQuiz(decalage = -1) }
        }
}