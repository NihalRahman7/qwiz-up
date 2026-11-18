package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue
import com.crosemont.qwizup.presentation.ModèleQuiz

// Contrat de l'écran de jeu : affichage des questions, rétroaction et résultat final
interface ContratQuestion {

    // Niveau affiché à la fin selon le pourcentage de bonnes réponses
    enum class NiveauRésultat { EXCELLENT, BIEN, A_PRATIQUER }

    interface IVueQuestion : Vue {
        // Affiche une question et ses réponses (réponses vides pour vrai ou faux et réponse écrite)
        fun afficherQuestion(
            titreQuiz: String,
            numero: Int,
            total: Int,
            énoncé: String,
            type: TypeQuestion,
            réponses: List<String>,
            plusieursRéponses: Boolean     // vrai : le joueur coche ses réponses puis touche « Valider »
        )

        // Met à jour la barre de progression (0 à 100)
        fun afficherProgression(pourcentage: Int)

        // Montre les réponses cochées par le joueur (question à plusieurs réponses)
        fun afficherSélection(indices: Set<Int>)

        // Active le bouton « Valider » quand au moins une réponse est cochée
        fun activerValider(actif: Boolean)

        // Colore les bonnes réponses en vert et les mauvaises réponses choisies en orange
        fun afficherCorrection(choisies: Set<Int>, bonnes: Set<Int>, estBonne: Boolean)

        // Active le bouton Suivant ; à la dernière question, il devient « Voir mon résultat »
        fun activerSuivant(estDerniere: Boolean)

        // Affiche l'écran de fin avec le score
        fun afficherRésultat(titreQuiz: String, score: Int, total: Int, niveau: NiveauRésultat)

        // Demande à l'utilisateur s'il veut vraiment quitter une partie en cours
        fun demanderConfirmationQuitter()

        // Le modèle n'a pas pu charger le quiz ou sauvegarder le résultat
        fun afficherErreur()

        // Retourne à l'écran précédent
        fun fermer()
    }

    interface IPrésentateurQuestion : Présentateur<IVueQuestion> {
        fun surDémarrer(quizId: Int)
        fun surRéponseChoisie(indice: Int)   // 0 à 3 ; plusieurs réponses : coche ou décoche la réponse
        fun surSuivantClique()
        fun surRejouerClique()
        fun surQuitterClique()
        fun surQuitterConfirme()
    }

    // Les fonctions sont « suspend » : elles peuvent lire ou écrire un fichier,
    // donc le présentateur les appelle sur un fil d'arrière-plan.
    interface IModèleQuestion {
        suspend fun obtenirQuizAJouer(quizId: Int): ModèleQuiz.QuizCréer?
        suspend fun enregistrerPartie(quizId: Int, score: Int, total: Int)
    }
}