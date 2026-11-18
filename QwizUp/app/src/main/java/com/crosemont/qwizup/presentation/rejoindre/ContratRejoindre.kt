package com.crosemont.qwizup.presentation.rejoindre

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

interface ContratRejoindre {
    interface IVueRejoindre : Vue {

        // Active ou désactive le bouton « Rejoindre »
        fun activerRejoindre(actif: Boolean)

        // Affiche le message « code invalide » sous le champ
        fun afficherCodeInvalide()

        // Retire le message d'erreur
        fun effacerErreur()

        // Ouvre l'écran de jeu avec le quiz trouvé
        fun ouvrirQuiz(quizId: Int)

        // Ferme l'écran
        fun fermer()
    }

    interface IPrésentateurRejoindre : Présentateur<IVueRejoindre> {
        // Le texte du champ a changé
        fun surCodeModifie(code: String)

        // L'utilisateur a touché « Rejoindre »
        fun surRejoindreClique(code: String)

        // L'utilisateur a touché le ✕
        fun surRetourClique()
    }

}