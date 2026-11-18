package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ModèleQuiz.QuestionCréer
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer

/**
 * Quiz « bidon » de plus, jouables depuis l'accueil (Continuer et Suggestions), pour tester.
 * Ce ne sont pas des quiz créés par l'utilisateur : ils n'apparaissent pas dans Créer.
 * Leurs id commencent à 101 pour ne jamais tomber sur l'id d'un quiz créé (1, 2, 3, 4…).
 */
object QuizÀJouerBidon {

    private fun choix(texte: String, réponses: List<String>, vararg bonnes: Int) =
        QuestionCréer(texte, TypeQuestion.CHOIX_MULTIPLE, réponses, bonnes.toSet())

    private fun vraiFaux(texte: String, vrai: Boolean) =
        QuestionCréer(texte, TypeQuestion.VRAI_FAUX, bonnesRéponses = setOf(if (vrai) 0 else 1))

    val quiz: List<QuizCréer> = listOf(
        QuizCréer(
            101, "La Révolution tranquille", "Les grands changements du Québec des années 1960.",
            CatégorieQuiz.HISTOIRE, Difficulté.MOYEN, AccèsQuiz.TOUT_LE_MONDE,
            listOf(
                choix("Qui était premier ministre du Québec au début de la Révolution tranquille?",
                    listOf("Maurice Duplessis", "Jean Lesage", "René Lévesque", "Robert Bourassa"), 1),
                vraiFaux("Le slogan « Maîtres chez nous » date de l'élection de 1962.", vrai = true),
                choix("Quel ministère a été créé en 1964?",
                    listOf("Le ministère de l'Éducation", "Le ministère de l'Environnement",
                        "Le ministère de l'Immigration", "Le ministère du Tourisme"), 0),
                vraiFaux("Les cégeps ont été créés pendant la Révolution tranquille.", vrai = true)
            ),
            estPublié = true
        ),
        QuizCréer(
            102, "Verbes irréguliers anglais", "Retrouve le passé des verbes les plus utilisés.",
            CatégorieQuiz.LANGUES, Difficulté.FACILE, AccèsQuiz.TOUT_LE_MONDE,
            listOf(
                choix("Quel est le passé de « go »?", listOf("goed", "went", "gone", "goes"), 1),
                choix("Quel est le passé de « take »?", listOf("taked", "took", "taken", "tooked"), 1),
                choix("Quel est le passé de « buy »?", listOf("buyed", "brought", "bought", "boughten"), 2),
                vraiFaux("Au passé, « read » s'écrit encore « read ».", vrai = true),
                choix("Quel est le passé de « see »?", listOf("seen", "saw", "seed", "sawed"), 1)
            ),
            estPublié = true
        ),
        QuizCréer(
            103, "Géographie du Québec", "Villes, fleuve et voisins du Québec.",
            CatégorieQuiz.GÉOGRAPHIE, Difficulté.FACILE, AccèsQuiz.TOUT_LE_MONDE,
            listOf(
                choix("Quelle est la capitale du Québec?", listOf("Montréal", "Québec", "Gatineau", "Sherbrooke"), 1),
                choix("Quel fleuve traverse le Québec?",
                    listOf("Le Fraser", "Le Mackenzie", "Le Saint-Laurent", "Le Mississippi"), 2),
                vraiFaux("Montréal est une île.", vrai = true),
                choix("Quelle province est à l'ouest du Québec?",
                    listOf("L'Ontario", "Le Nouveau-Brunswick", "Le Manitoba", "La Nouvelle-Écosse"), 0)
            ),
            estPublié = true
        )
    )

    fun trouver(quizId: Int): QuizCréer? = quiz.find { it.id == quizId }
}
