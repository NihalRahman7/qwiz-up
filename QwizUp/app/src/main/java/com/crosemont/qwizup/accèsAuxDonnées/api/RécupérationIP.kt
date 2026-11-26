package com.crosemont.qwizup.accèsAuxDonnées.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RécupérationIP: IOException() {
    private val client = OkHttpClient()
    fun getRéponse(apiUrl: HttpUrl): String?{
        val requête = Request.Builder()
            .url(apiUrl)
            .build()

        return try {
            client.newCall(requête).execute().use { réponse ->
                réponse.body.string()
            }
        } catch (e: IOException){
            println("La requête réseau a échoué ${e.message}")
            throw e
        }
    }
}