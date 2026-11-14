package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue

interface ContratDécouvrir {

    interface IVueDécouvrir : Vue {
        fun viderQuiz()
        fun ajouterQuiz(
            id: Int,
            catégorie: CategorieQuiz,
            titre: String,
            description: String,
            nbQuestions: Int,
            duréeMinutes: Int,
            estFavori: Boolean
        )
        fun afficherAucunRésultat()
        fun afficherCatégorieChoisie(catégorie: CategorieQuiz?)   // null = « Tous »
        fun ouvrirQuiz(quizId: Int, titre: String)
        fun afficherQuizBientôt(titre: String)   // le quiz n'a pas encore de questions
    }

    interface IPrésentateurDécouvrir : Presentateur<IVueDécouvrir> {
        fun rechercher(texte: String)
        fun choisirCatégorie(catégorie: CategorieQuiz?)   // null = « Tous »
        fun basculerFavori(quizId: Int)
        fun jouer(quizId: Int)
    }
}
