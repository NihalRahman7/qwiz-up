package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue

// Contrat de l'écran de jeu : affichage des questions, rétroaction et résultat final
interface ContratQuestion {

    // Niveau affiché à la fin selon le pourcentage de bonnes réponses
    enum class NiveauResultat { EXCELLENT, BIEN, A_PRATIQUER }

    interface IVueQuestion : Vue {
        // Affiche la question courante et ses choix de réponse
        fun afficherQuestion(
            titreQuiz: String,
            numero: Int,
            total: Int,
            enonce: String,
            type: TypeQuestion,
            reponses: List<String>
        )

        fun afficherProgression(pourcentage: Int) // Met à jour la barre de progression (0 à 100)

        fun afficherBonneReponse(indice: Int) // Colore la bonne réponse en vert

        fun afficherMauvaiseReponse(indiceChoisi: Int, indiceBonneReponse: Int) // Colore le choix en orange et montre la bonne réponse

        fun activerSuivant(estDerniere: Boolean) // Active le bouton « Suivante » ou « Voir mon résultat »

        fun afficherResultat(titreQuiz: String, score: Int, total: Int, niveau: NiveauResultat) // Affiche l'écran de résultat à la fin du quiz

        fun demanderConfirmationQuitter() // Demande si l'utilisateur veut vraiment quitter une partie en cours

        fun fermer() // Ferme l'écran de jeu
    }

    interface IPresentateurQuestion : Presentateur<IVueQuestion> {
        fun surDemarrer(quizId: Int)
        fun surReponseChoisie(indice: Int)
        fun surSuivantClique()
        fun surRejouerClique()
        fun surQuitterClique()
        // L'utilisateur a confirmé qu'il veut quitter
        fun surQuitterConfirme()
    }
}