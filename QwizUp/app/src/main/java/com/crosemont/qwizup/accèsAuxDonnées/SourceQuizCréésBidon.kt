package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.presentation.ContratVuePrésentateur
import com.crosemont.qwizup.presentation.ModèleQuiz.QuestionCréer
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Source « bidon » : simule la vraie source avec des quiz codés en dur, gardés en mémoire.
 *
 * Sans quizDeDépart, toutes les instances partagent la même liste (comme une seule base de données
 * pour toute l'app). Avec quizDeDépart (dans les tests), l'instance a sa propre liste.
 */
class SourceQuizCréésBidon(
    quizDeDépart: List<QuizCréer>? = null,
    private val délai: Duration = 300.milliseconds,   // simule le temps d'accès aux données
) : SourceQuizCréés {

    private val mémoire: Mémoire = if (quizDeDépart == null) mémoirePartagée else Mémoire(quizDeDépart)

    override suspend fun lireQuiz(): List<QuizCréer> {
        delay(délai)
        return mémoire.quiz
    }

    override suspend fun écrireQuiz(quiz: List<QuizCréer>) {
        delay(délai)
        mémoire.quiz = quiz
    }

    private class Mémoire(var quiz: List<QuizCréer>)

    companion object {
        private fun choix(texte: String, réponses: List<String>, vararg bonnes: Int) =
            QuestionCréer(texte, ContratVuePrésentateur.TypeQuestion.CHOIX_MULTIPLE, réponses, bonnes.toSet())
        private fun vraiFaux(texte: String, vrai: Boolean) =
            QuestionCréer(texte, ContratVuePrésentateur.TypeQuestion.VRAI_FAUX, bonnesRéponses = setOf(if (vrai) 0 else 1))
        private fun ecrite(texte: String, attendue: String) =
            QuestionCréer(texte, ContratVuePrésentateur.TypeQuestion.REPONSE_ECRITE, réponseAttendue = attendue)

        // Données codées en dur, partagées par toute l'app
        private val mémoirePartagée = Mémoire(
            listOf(
                QuizCréer(
                    1, "Le système solaire", "Planètes, lunes et distances.",
                    ContratVuePrésentateur.CatégorieQuiz.SCIENCES, ContratVuePrésentateur.Difficulté.MOYEN,
                    ContratVuePrésentateur.AccèsQuiz.AVEC_CODE,
                    listOf(
                        choix("Quelle est la plus grande planète du système solaire?",
                            listOf("Mars", "Jupiter", "Saturne", "Neptune"), 1),
                        vraiFaux("La Lune produit sa propre lumière.", vrai = false),
                        choix("Quelle planète est la plus proche du Soleil?", listOf("Vénus", "Mercure", "Mars", "Terre"), 1),
                        choix("Combien de planètes compte le système solaire?", listOf("7", "8", "9", "10"), 1),
                        vraiFaux("Mars est surnommée la planète rouge.", vrai = true)
                    ),
                    estPublié = false
                ),
                QuizCréer(
                    2, "Bases de Kotlin", "Les notions de base du langage Kotlin.",
                    ContratVuePrésentateur.CatégorieQuiz.INFORMATIQUE, ContratVuePrésentateur.Difficulté.FACILE,
                    ContratVuePrésentateur.AccèsQuiz.AVEC_CODE,
                    listOf(
                        choix("Quel mot-clé déclare une variable qu'on ne peut pas réassigner?",
                            listOf("var", "val", "let", "const"), 1),
                        vraiFaux("Kotlin peut utiliser les bibliothèques Java.", vrai = true),
                        choix("Quel type représente du texte?", listOf("Int", "Char", "String", "Text"), 2),
                        choix("Quel mot-clé déclare une fonction?", listOf("function", "def", "fun", "fn"), 2),
                        choix("Lesquels sont des types de base en Kotlin?", listOf("Int", "String", "Integer", "Boolean"), 0, 1, 3),
                        vraiFaux("Une variable String peut être null par défaut.", vrai = false)
                    ),
                    estPublié = false
                ),
                QuizCréer(
                    3, "Capitales d'Europe", "Associe chaque pays à sa capitale.",
                    ContratVuePrésentateur.CatégorieQuiz.GÉOGRAPHIE, ContratVuePrésentateur.Difficulté.FACILE,
                    ContratVuePrésentateur.AccèsQuiz.TOUT_LE_MONDE,
                    listOf(
                        choix("Quelle est la capitale du Portugal?", listOf("Porto", "Lisbonne", "Madrid", "Faro"), 1),
                        choix("Quelle est la capitale de la Finlande?", listOf("Oslo", "Stockholm", "Helsinki", "Tallinn"), 2),
                        choix("Lesquelles de ces villes sont des capitales?", listOf("Paris", "Barcelone", "Rome", "Berlin"), 0, 2, 3),
                        vraiFaux("Genève est la capitale de la Suisse.", vrai = false)
                    ),
                    estPublié = true
                )
            )
        )

        /**
         * Lecture immédiate de la liste partagée.
         * Pont temporaire pour l'écran de jeu, qui ne passe pas encore par le service (voir ModèleQuiz).
         */
        fun lireMaintenant(): List<QuizCréer> = mémoirePartagée.quiz
    }
}
