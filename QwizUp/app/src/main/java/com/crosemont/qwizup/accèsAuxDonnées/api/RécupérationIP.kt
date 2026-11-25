package com.crosemont.qwizup.accèsAuxDonnées.api

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class RécupérationIP(apiUrl: HttpUrl): IOException() {
    val client = OkHttpClient()
    val requette = Request.Builder()
        .url(apiUrl)
        .build()

    fun getRéponse(){
        try {
            val réponse: Response = client.newCall(requette).execute()
        } catch (e: IOException){
            println("La requête réseau a échoué ${e.message}")
        }
    }
}