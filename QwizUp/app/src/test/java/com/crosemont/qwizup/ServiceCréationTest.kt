package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.service.ServiceCréation
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.ContratVuePresentateur.AccesQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Difficulte
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ModeleQuiz.QuestionCreer
import com.crosemont.qwizup.presentation.ModeleQuiz.QuizCreer
import com.crosemont.qwizup.sourceDeDonnees.SourceQuizCréés
import com.crosemont.qwizup.sourceDeDonnees.SourceQuizCréésBidon
import java.io.IOException
import kotlin.time.Duration
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class ServiceCréationTest {

    private lateinit var source: SourceQuizCréésBidon
    private lateinit var service: ServiceCréation

    private val questionComplète = QuestionCreer(
        "Quelle est la capitale du Canada?", TypeQuestion.CHOIX_MULTIPLE,
        listOf("Toronto", "Ottawa", "Montréal", "Vancouver"), setOf(1)
    )
    private val questionIncomplète = QuestionCreer("", TypeQuestion.CHOIX_MULTIPLE)

    private fun quiz(id: Int, titre: String, estPublie: Boolean, questions: List<QuestionCreer> = listOf(questionComplète)) =
        QuizCreer(
            id, titre, "", CategorieQuiz.GEOGRAPHIE, Difficulte.FACILE, AccesQuiz.TOUT_LE_MONDE,
            questions, estPublie
        )

    @Before
    fun préparer() {
        // Chaque test a sa propre liste, sans attente
        source = SourceQuizCréésBidon(
            quizDeDépart = listOf(quiz(1, "Brouillon de départ", false), quiz(2, "Quiz publié", true)),
            délai = Duration.ZERO
        )
        service = ServiceCréation(source)
    }

    @Test
    fun `Étant donné un brouillon et un quiz publié lorsqu'on demande les brouillons, on obtient seulement le brouillon`() = runBlocking {
        assertEquals(listOf("Brouillon de départ"), service.obtenirBrouillons().map { it.titre })
    }

    @Test
    fun `Étant donné un brouillon et un quiz publié lorsqu'on demande les publiés, on obtient seulement le quiz publié`() = runBlocking {
        assertEquals(listOf("Quiz publié"), service.obtenirPubliés().map { it.titre })
    }

    @Test
    fun `Étant donné un nouveau quiz lorsqu'on le sauvegarde, on obtient un nouvel id et le quiz est dans la source`() = runBlocking {
        val enregistré = service.sauvegarder(quiz(0, "  Capitales du monde  ", false))

        assertEquals(3, enregistré.id)
        assertEquals("Capitales du monde", enregistré.titre)   // les espaces sont retirés
        assertEquals(3, source.lireQuiz().size)
    }

    @Test
    fun `Étant donné un quiz existant lorsqu'on le sauvegarde, on obtient le quiz modifié sans doublon`() = runBlocking {
        service.sauvegarder(quiz(1, "Nouveau titre", false))

        val tous = source.lireQuiz()
        assertEquals(2, tous.size)
        assertEquals("Nouveau titre", tous.first { it.id == 1 }.titre)
    }

    @Test
    fun `Étant donné un brouillon sans titre lorsqu'on le sauvegarde, on obtient une ServiceException`() = runBlocking {
        try {
            service.sauvegarder(quiz(0, "   ", false))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertEquals(2, source.lireQuiz().size)   // rien n'a été enregistré
        }
    }

    @Test
    fun `Étant donné un brouillon avec une question incomplète lorsqu'on le sauvegarde, on obtient le brouillon enregistré`() = runBlocking {
        val enregistré = service.sauvegarder(quiz(0, "Brouillon", false, listOf(questionIncomplète)))

        assertEquals(3, enregistré.id)
    }

    @Test
    fun `Étant donné un quiz publié avec une question incomplète lorsqu'on le sauvegarde, on obtient une ServiceException`() = runBlocking {
        try {
            service.sauvegarder(quiz(0, "Quiz", true, listOf(questionComplète, questionIncomplète)))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertTrue(e.message!!.contains("2"))   // c'est la question 2 qui n'est pas complète
        }
    }

    @Test
    fun `Étant donné un quiz publié sans question lorsqu'on le sauvegarde, on obtient une ServiceException`() = runBlocking {
        try {
            service.sauvegarder(quiz(0, "Quiz", true, emptyList()))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            // attendu
        }
    }

    @Test
    fun `Étant donné un quiz avec plusieurs bonnes réponses remplies lorsqu'on le publie, on obtient le quiz enregistré`() = runBlocking {
        val plusieurs = QuestionCreer(
            "Lesquelles sont des capitales?", TypeQuestion.CHOIX_MULTIPLE,
            listOf("Paris", "Barcelone", "Rome", "Berlin"), setOf(0, 2, 3)
        )
        val enregistré = service.sauvegarder(quiz(0, "Capitales", true, listOf(plusieurs)))

        assertTrue(enregistré.estPublie)
    }

    @Test
    fun `Étant donné un id inexistant lorsqu'on demande le quiz, on obtient une ServiceException`() = runBlocking {
        try {
            service.obtenirQuiz(99)
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            // attendu
        }
    }

    @Test
    fun `Étant donné une source en panne lorsqu'on demande les brouillons, on obtient une ServiceException`() = runBlocking {
        val serviceEnPanne = ServiceCréation(SourceEnPanne())
        try {
            serviceEnPanne.obtenirBrouillons()
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertTrue(e.cause is IOException)
        }
    }

    @Test
    fun `Étant donné un quiz avec un id qui n'existe pas lorsqu'on le sauvegarde, on obtient une ServiceException`() = runBlocking {
        try {
            service.sauvegarder(quiz(99, "Quiz fantôme", false))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertEquals(2, source.lireQuiz().size)   // rien n'a été ajouté
        }
    }

    @Test
    fun `Étant donné une question à choix multiple sans bonne réponse cochée lorsqu'on publie, on obtient une ServiceException`() = runBlocking {
        val sansBonneRéponse = questionComplète.copy(bonnesReponses = emptySet())
        try {
            service.sauvegarder(quiz(0, "Quiz", true, listOf(sansBonneRéponse)))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            // attendu
        }
    }

    @Test
    fun `Étant donné une bonne réponse cochée mais vide lorsqu'on publie, on obtient une ServiceException`() = runBlocking {
        val réponseVide = questionComplète.copy(reponses = listOf("Toronto", "", "Montréal", "Vancouver"))   // la B est cochée
        try {
            service.sauvegarder(quiz(0, "Quiz", true, listOf(réponseVide)))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            // attendu
        }
    }

    @Test
    fun `Étant donné une question à réponse écrite sans réponse attendue lorsqu'on publie, on obtient une ServiceException`() = runBlocking {
        val écriteVide = QuestionCreer("Explique le cycle de l'eau", TypeQuestion.REPONSE_ECRITE, reponseAttendue = "  ")
        try {
            service.sauvegarder(quiz(0, "Quiz", true, listOf(écriteVide)))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            // attendu
        }
    }

    @Test
    fun `Étant donné une source en panne lorsqu'on sauvegarde, on obtient une ServiceException`() = runBlocking {
        val serviceEnPanne = ServiceCréation(SourceEnPanne())
        try {
            serviceEnPanne.sauvegarder(quiz(0, "Quiz", false))
            fail("Une ServiceException était attendue")
        } catch (e: ServiceException) {
            assertTrue(e.cause is IOException)
        }
    }

    /** Fausse source qui échoue toujours, comme un fichier illisible. */
    private class SourceEnPanne : SourceQuizCréés {
        override suspend fun lireQuiz(): List<QuizCreer> = throw IOException("Lecture impossible")
        override suspend fun écrireQuiz(quiz: List<QuizCreer>) = throw IOException("Écriture impossible")
    }
}
