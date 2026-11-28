package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.Quiz
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SourceBidon(private val délai: Duration = 300.milliseconds) : SourceDeDonnées {
    private val quiz = listOf(
        Quiz(
            "Le système solaire", "Sciences", 10,
            "Planètes, lunes et distances : teste tes connaissances."
        ),
        Quiz(
            "Capitales d’Europe", "Géographie", 12,
            "Associe chaque pays à sa capitale, de Lisbonne à Helsinki."
        ),
        Quiz(
            "La Révolution tranquille", "Histoire", 8,
            "Les grands changements du Québec des années 1960."
        ),
        Quiz(
            "Verbes irréguliers anglais", "Langues", 15,
            "Be, go, take… retrouve le passé des verbes les plus utilisés."
        )
    )

    override fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> {
        Thread.sleep(délai.inWholeMilliseconds) // Simule le temps d'une requête réseau
        return quiz.drop(decalage).take(limite)
    }
}