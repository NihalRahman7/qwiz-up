package com.crosemont.qwizup.test_API

import com.crosemont.qwizup.accèsAuxDonnées.api.SourceQuizApi
import com.crosemont.qwizup.domaine.exceptions.SourceDeDonnéesException
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class SourceQuizApiTest {

    private lateinit var serveur: MockWebServer

    private val jsonDeuxQuiz = """
        {
          "success": true,
          "data": [
            {"id": "q1", "quizId": "quiz-a", "quizTitle": "Linux de base", "category": "Linux"},
            {"id": "q2", "quizId": "quiz-a", "quizTitle": "Linux de base", "category": "Linux"},
            {"id": "q3", "quizId": "quiz-b", "quizTitle": "Bases de Docker", "category": "DevOps"}
          ]
        }
    """.trimIndent()

    @Before
    fun démarrerServeur() {
        serveur = MockWebServer()
        serveur.start()
    }

    @After
    fun arrêterServeur() {
        serveur.shutdown()
    }

    private fun créerSource() = SourceQuizApi(
        cléApi = "cle-test",
        urlDeBase = serveur.url("/").toString(),
        délaiSecondes = 1
    )

    private fun répondre(code: Int = 200, corps: String = "") {
        serveur.enqueue(
            MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .setBody(corps)
        )
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
    fun `Étant donné 3 questions de 2 quiz lorsqu'on obtient les quiz, on obtient 2 quiz avec le bon nombre de questions`() = runBlocking {
        répondre(corps = jsonDeuxQuiz)

        val quiz = créerSource().obtenirQuiz(10, 0)

        assertEquals(listOf("quiz-a", "quiz-b"), quiz.map { it.quizIdApi })
        assertEquals(listOf("Linux de base", "Bases de Docker"), quiz.map { it.titre })
        assertEquals(listOf(2, 1), quiz.map { it.nbQuestions })
    }

    @Test
    fun `Étant donné une clé d'API lorsqu'on obtient les quiz, la requête contient l'autorisation et les paramètres`() = runBlocking {
        répondre(corps = jsonDeuxQuiz)

        créerSource().obtenirQuiz(limite = 5, decalage = 10)

        val requête = serveur.takeRequest(1, TimeUnit.SECONDS)!!
        assertEquals("Bearer cle-test", requête.getHeader("Authorization"))
        assertEquals("/questions?limit=5&offset=10", requête.path)
    }

    @Test
    fun `Étant donné une liste de questions vide lorsqu'on obtient les quiz, on obtient une liste vide`() = runBlocking {
        répondre(corps = """{"success": true, "data": []}""")
        val quiz = créerSource().obtenirQuiz(10, 0)
        assertTrue(quiz.isEmpty())
    }

    @Test
    fun `Étant donné une question sans quizId lorsqu'on obtient les quiz, on l'ignore`() = runBlocking {
        répondre(
            corps = """
                {"data": [
                  {"id": "q1", "quizTitle": "Sans quiz", "category": "Linux"},
                  {"id": "q2", "quizId": "quiz-a", "quizTitle": "Valide", "category": "Linux"}
                ]}
            """.trimIndent()
        )
        val quiz = créerSource().obtenirQuiz(10, 0)
        assertEquals(listOf("quiz-a"), quiz.map { it.quizIdApi })
    }

    @Test
    fun `Étant donné une erreur 401 lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(code = 401)
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné une erreur 404 lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(code = 404)
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné une erreur 500 lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(code = 500)
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné un JSON mal formé lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(corps = "Ceci n'est pas du JSON valide")
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné une réponse vide avec un code 200 lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(corps = "")
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné une réponse sans champ data lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        répondre(corps = """{"success": true}""")
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }

    @Test
    fun `Étant donné un serveur plus lent que le délai permis lorsqu'on obtient les quiz, on obtient une SourceDeDonneesException`() = runBlocking {
        serveur.enqueue(
            MockResponse()
                .setBody(jsonDeuxQuiz)
                .setHeadersDelay(3, TimeUnit.SECONDS)   // le client n'attend que 1 seconde
        )
        assertLance<SourceDeDonnéesException> { créerSource().obtenirQuiz(10, 0) }
    }
}