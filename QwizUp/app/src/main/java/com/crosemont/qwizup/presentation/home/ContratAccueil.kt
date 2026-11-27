package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

// Contrat de l'écran d'accueil : ce que la vue sait faire et ce que le présentateur reçoit
interface ContratAccueil {

    interface IVueAccueil : Vue {
        // Ouvre l'écran de jeu pour le quiz choisi
        fun ouvrirQuiz(quizId: Int)

        // Ouvre l'écran pour entrer un code de quiz
        fun ouvrirRejoindre()

        // Ouvre l'écran Profil
        fun ouvrirProfil()

        // Affiche la série, le nombre de quiz complétés et le taux de réussite
        fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int)

        // Montre seulement les cartes « Continuer » de ces quiz (liste vide : la section disparaît)
        fun afficherQuizEnCours(quizIds: List<Int>)

        // Le modèle n'a pas pu répondre
        fun afficherErreur()
    }

    interface IPrésentateurAccueil : Présentateur<IVueAccueil> {
        // L'utilisateur a cliqué sur une carte de quiz
        fun surQuizClique(quizId: Int)

        // L'utilisateur a cliqué sur le bouton « Code »
        fun surCodeClique()

        // L'utilisateur a touché son avatar
        fun surProfilClique()
    }

    interface IModèleAccueil {
        suspend fun obtenirStatistiques(): Statistiques

        // Les quiz à continuer : un quiz réussi à 100 % n'en fait plus partie
        suspend fun obtenirQuizEnCours(): List<Int>
    }
}