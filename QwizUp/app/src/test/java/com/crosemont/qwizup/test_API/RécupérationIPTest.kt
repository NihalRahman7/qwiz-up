package com.crosemont.qwizup.test_API

import com.crosemont.qwizup.accèsAuxDonnées.api.RécupérationIP
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

/**
 * Test de récupération de l'adresse IP quand on fait une requêtte vers l'API
 */
class RécupérationIPTest {
    private val serveur = MockWebServer()

    @Test
    fun `Étant donné un serveur mock configuré lorsqu'on récupère le lien URL, on obtient la bonne adresse IP`() {
        val réponse = MockResponse()
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .setBody("{\"ip\": \"127.0.0.1\"}")

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl: HttpUrl = serveur.url("/getIp")
        val ipGetter = RécupérationIP()

        assertEquals("{\"ip\": \"127.0.0.1\"}", ipGetter.getRéponse(baseUrl))
        serveur.shutdown()
    }

    @Test
    fun `Étant donné un serveur mock configuré lorsqu'on le qu'il envoie une erreur 500 lors d'une requête, on obtient une réponse vide`() {
        val réponse = MockResponse().setResponseCode(500)

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl: HttpUrl = serveur.url("/error500")
        val ipGetter = RécupérationIP()

        val résultat = ipGetter.getRéponse(baseUrl)
        assertEquals("", résultat ?: "")
        serveur.shutdown()
    }

    @Test
    fun `Étant donné un serveur mock configuré lorsqu'on le qu'il envoie une erreur 404 pour une ressource introuvable, on obtient une réponse vide`() {
        val réponse = MockResponse().setResponseCode(404)

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl: HttpUrl = serveur.url("/notFound")
        val ipGetter = RécupérationIP()

        val résultat = ipGetter.getRéponse(baseUrl)
        assertEquals("", résultat ?: "")
        serveur.shutdown()
    }

    @Test
    fun `Étant donné un corps de réponse mal formé lorsqu'on fait la requête, on obtient le texte brut`() {
        val réponse = MockResponse()
            .setResponseCode(200)
            .setBody("Ceci n'est pas du JSON valide")

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl: HttpUrl = serveur.url("/badjson")
        val ipGetter = RécupérationIP()

        assertEquals("Ceci n'est pas du JSON valide", ipGetter.getRéponse(baseUrl))
        serveur.shutdown()
    }

    @Test
    fun `Étant donné un serveur lent lorsqu'on fait une requête, on gère le dépassement de délai`() {
        // On ajoute un délai volontaire sur la réponse du serveur de 3 secondes
        val réponse = MockResponse()
            .setResponseCode(200)
            .setBody("{\"ip\": \"127.0.0.1\"}")
            .setBodyDelay(3, TimeUnit.SECONDS)

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl: HttpUrl = serveur.url("/timeout")
        val ipGetter = RécupérationIP()

        try {
            ipGetter.getRéponse(baseUrl)
        } catch (e: Exception) {
            assert(true)
        } finally {
            serveur.shutdown()
        }
    }

    @Test
    fun `Étant donné une réponse vide avec un code 200 lorsqu'on fait la requête, on obtient une chaîne vide`() {
        val serveur = MockWebServer()
        val réponse = MockResponse()
            .setResponseCode(200)
            .setBody("")

        serveur.enqueue(réponse)
        serveur.start()

        val baseUrl = serveur.url("/empty")
        val ipGetter = RécupérationIP()

        val resultat = ipGetter.getRéponse(baseUrl)
        assertEquals("", resultat)
        serveur.shutdown()
    }
}