package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Quiz
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class SourceBidon : SourceDeDonnées {
    private val quiz = listOf(
        Quiz(
            1, "bidon-1", "Le système solaire", "Sciences", 10,
            "Planètes, lunes et distances : teste tes connaissances."
        ),
        Quiz(
            3, "bidon-3", "Capitales d’Europe", "Géographie", 12,
            "Associe chaque pays à sa capitale, de Lisbonne à Helsinki."
        ),
        Quiz(
            4, "bidon-4", "La Révolution tranquille", "Histoire", 8,
            "Les grands changements du Québec des années 1960."
        ),
        Quiz(
            5, "bidon-5", "Verbes irréguliers anglais", "Langues", 15,
            "Be, go, take… retrouve le passé des verbes les plus utilisés."
        )
    )

    override suspend fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> {
        delay(300.milliseconds) // Simule le temps d'une requête réseau
        return quiz.drop(decalage).take(limite)
    }
}