package com.crosemont.qwizup.accèsAuxDonnées.http

import com.crosemont.qwizup.accèsAuxDonnées.SourceDeDonnées
import com.crosemont.qwizup.domaine.entites.Quiz
import com.crosemont.qwizup.domaine.exceptions.SourceDeDonnéesException
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Source de données HTTP : récupère les quiz depuis QuizAPI.
 * Tout ce qui touche au réseau et au JSON reste dans cette classe : à l'extérieur,
 * on ne voit que des Quiz ou une SourceDeDonneesException
 */
class SourceHttp internal constructor(private val service: QuizApiService): SourceDeDonnées {
    constructor(
        cléApi: String,
        urlDeBase: String = URL_PAR_DEFAUT,
        délaiSecondes: Long = 10
    ): this(créerService(cléApi, urlDeBase, délaiSecondes))

    override fun obtenirQuiz(limite: Int, décalage: Int): List<Quiz> {
        val réponse = try {
            service.obtenirQuestions(limite, décalage).execute()
        } catch (e: IOException){
            throw SourceDeDonnéesException("Impossible de joindre le serveur : ${e.message}")
        } catch (e: JsonParseException){
            throw SourceDeDonnéesException("La réponse du serveur est illisible")
        }

        if (!réponse.isSuccessful){
            throw SourceDeDonnéesException("Le serveur a répondu par une erreur (code ${réponse.code()})")
        }
        return convertir(réponse.body())
    }

    // L'API retourne des questions : on les regroupe par quiz
    private fun convertir(réponse: ReponseQuestions?): List<Quiz> {
        val questions = réponse?.data ?: throw SourceDeDonnéesException("La réponse du serveur est incomplète")
        return questions
            .filter { !it.quizId.isNullOrBlank() }
            .groupBy { it.quizId!! }
            .values
            .map { questionsDuQuiz ->
                val première = questionsDuQuiz.first()
                Quiz(
                    titre = première.quizTitre?.takeIf { it.isNotBlank() } ?: "Quiz sans titre",
                    catégorie = première.catégorie?.takeIf { it.isNotBlank() } ?: "Autre",
                    nbQuestions = questionsDuQuiz.size
                )
            }
    }

    private companion object {
        const val URL_PAR_DEFAUT = "https://quizapi.io/api/v1/"

        fun créerService(cléApi: String, urlDeBase: String, délaiSecondes: Long): QuizApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(délaiSecondes, TimeUnit.SECONDS)
                .readTimeout(délaiSecondes, TimeUnit.SECONDS)
                .addInterceptor { chaîne ->
                    chaîne.proceed(
                        chaîne.request().newBuilder()
                            .header("Authorization", "Bearer $cléApi")
                            .build()
                    )
                }
                .build()

            return Retrofit.Builder()
                .baseUrl(urlDeBase)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(QuizApiService::class.java)
        }
    }
}

// Description de l'API (réservée à ce package)
internal interface QuizApiService {
    @GET("questions")
    fun obtenirQuestions(
        @Query("limit") limite: Int,
        @Query("offset") décalage: Int
    ): Call<ReponseQuestions>
}

// Les champs sont nullables : Gson ignore les types non nuls de Kotlin si le JSON est incomplet
internal data class ReponseQuestions(
    val data: List<QuestionApi>?
)

internal data class QuestionApi(
    val quizId: String?,
    @SerializedName("quizTitle") val quizTitre: String?,
    @SerializedName("category") val catégorie: String?
)