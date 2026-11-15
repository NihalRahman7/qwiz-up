package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue

// Contrat de l'écran de jeu : affichage des questions, rétroaction et résultat final
interface ContratQuestion {

    // Niveau affiché à la fin selon le pourcentage de bonnes réponses
    enum class NiveauResultat { EXCELLENT, BIEN, A_PRATIQUER }

    interface IVueQuestion : Vue {
        // Affiche une question et ses réponses (réponses vides pour vrai ou faux et réponse écrite)
        fun afficherQuestion(
            titreQuiz: String,
            numero: Int,
            total: Int,
            enonce: String,
            type: TypeQuestion,
            reponses: List<String>,
            plusieursReponses: Boolean     // vrai : le joueur coche ses réponses puis touche « Valider »
        )

        // Met à jour la barre de progression (0 à 100)
        fun afficherProgression(pourcentage: Int)

        // Montre les réponses cochées par le joueur (question à plusieurs réponses)
        fun afficherSelection(indices: Set<Int>)

        // Active le bouton « Valider » quand au moins une réponse est cochée
        fun activerValider(actif: Boolean)

        // Colore les bonnes réponses en vert et les mauvaises réponses choisies en orange
        fun afficherCorrection(choisies: Set<Int>, bonnes: Set<Int>, estBonne: Boolean)

        // Active le bouton Suivant ; à la dernière question, il devient « Voir mon résultat »
        fun activerSuivant(estDerniere: Boolean)

        // Affiche l'écran de fin avec le score
        fun afficherResultat(titreQuiz: String, score: Int, total: Int, niveau: NiveauResultat)

        // Demande à l'utilisateur s'il veut vraiment quitter une partie en cours
        fun demanderConfirmationQuitter()

        // Retourne à l'écran précédent
        fun fermer()
    }

    interface IPresentateurQuestion : Presentateur<IVueQuestion> {
        fun surDemarrer(quizId: Int)
        fun surReponseChoisie(indice: Int)   // 0 à 3 ; plusieurs réponses : coche ou décoche la réponse
        fun surSuivantClique()
        fun surRejouerClique()
        fun surQuitterClique()
        fun surQuitterConfirme()
    }
}