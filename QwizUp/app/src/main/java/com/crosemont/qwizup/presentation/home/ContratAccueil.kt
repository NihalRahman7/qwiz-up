package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue

interface ContratAccueil {

    interface IVueAccueil : Vue {
        // Ouvre l'écran de jeu pour le quiz choisi
        fun ouvrirQuiz(quizId: Int)
        // Ouvre l'écran pour entrer un code de quiz
        fun ouvrirRejoindre()
    }

    interface IPresentateurAccueil : Presentateur<IVueAccueil> {
        // L'utilisateur a cliqué sur une carte de quiz
        fun surQuizClique(quizId: Int)
        // L'utilisateur a cliqué sur le bouton « Code »
        fun surCodeClique()
    }
}