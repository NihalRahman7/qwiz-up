package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

// Contrat de l'écran d'accueil : ce que la vue sait faire et ce que le présentateur reçoit
interface ContratAccueil {

    interface IVueAccueil : Vue {
        // Ouvre l'écran de jeu pour le quiz choisi (carte ou code)
        fun ouvrirQuiz(quizId: Int)

        // Ouvre l'écran Profil
        fun ouvrirProfil()

        // Affiche la série, le nombre de quiz complétés et le taux de réussite
        fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int)

        // Montre seulement les cartes « Continuer » de ces quiz (liste vide : la section disparaît)
        fun afficherQuizEnCours(quizIds: List<Int>)

        // Active ou désactive le bouton « Jouer » à côté du champ du code
        fun activerJouerCode(actif: Boolean)

        // Le code ne correspond à aucun quiz
        fun afficherCodeInvalide()

        // Retire le message « code invalide »
        fun effacerErreurCode()

        // Le modèle n'a pas pu répondre
        fun afficherErreur()
    }

    interface IPrésentateurAccueil : Présentateur<IVueAccueil> {
        // L'utilisateur a cliqué sur une carte de quiz
        fun surQuizClique(quizId: Int)

        // Le texte du champ du code a changé
        fun surCodeModifié(code: String)

        // L'utilisateur a touché « Jouer » à côté du champ du code
        fun surJouerCodeClique(code: String)

        // L'utilisateur a touché son avatar
        fun surProfilClique()
    }

    interface IModèleAccueil {
        suspend fun obtenirStatistiques(): Statistiques

        // Les quiz à continuer : un quiz réussi à 100 % n'en fait plus partie
        suspend fun obtenirQuizEnCours(): List<Int>

        // L'id du quiz publié qui a ce code, ou null s'il n'existe pas
        suspend fun trouverQuizParCode(code: String): Int?
    }
}