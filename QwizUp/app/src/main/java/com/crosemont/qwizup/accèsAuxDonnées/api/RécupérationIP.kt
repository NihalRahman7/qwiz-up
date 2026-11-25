package com.crosemont.qwizup.accèsAuxDonnées.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RécupérationIP: IOException() {
    val client = OkHttpClient()
    fun getRéponse(apiUrl: HttpUrl): String?{
        val requêtte = Request.Builder()
            .url(apiUrl)
            .build()

        return try {
            val réponse: Response = client.newCall(requêtte).execute()
            réponse.body.string()
        } catch (e: IOException){
            println("La requête réseau a échoué ${e.message}")
            null
        }
    }
}