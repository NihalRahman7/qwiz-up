package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.domaine.entites.Quiz
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class SourceBiddon: SourceDeDonnees {
    private val quiz = listOf(
        Quiz(1, "bidon-1", "Capitales du monde", "Géographie", 10),
        Quiz(2, "bidon-2", "Bases de Kotlin", "Programmation", 10),
        Quiz(3, "bidon-3", "Système solaire", "Science", 10),
        Quiz(4, "bidon-4", "Histoire du Québec", "Histoire", 10),
        Quiz(5, "bidon-5", "Cuisine du monde", "Culture", 10),
    )

    override suspend fun obtenirQuiz(limite: Int, decalage: Int): List<Quiz> {
        delay(300.milliseconds) // Simule le temps d'une requête réseau
        return quiz.drop(decalage).take(limite)
    }
}