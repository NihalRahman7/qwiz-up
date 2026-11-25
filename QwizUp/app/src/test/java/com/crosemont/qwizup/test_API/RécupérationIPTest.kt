package com.crosemont.qwizup.test_API

import com.crosemont.qwizup.accèsAuxDonnées.api.RécupérationIP
import okhttp3.HttpUrl
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test de récupération de l'adresse IP quand on fait une requêtte vers l'API
 */
class RécupérationIPTest {
    @Test
    fun `Étant donné un serveur mock configuré lorsqu'on récupère le lien URL, on obtient la bonne adresse IP`() {
        val serveur = MockWebServer()
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
}