package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Presentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Vue
import com.crosemont.qwizup.presentation.ModeleQuiz.CarteDiscover

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
        fun afficherErreur()   // le modèle n'a pas pu répondre
        fun afficherChargement(visible: Boolean)
    }

    interface IPrésentateurDécouvrir : Presentateur<IVueDécouvrir> {
        fun rechercher(texte: String)
        fun choisirCatégorie(catégorie: CategorieQuiz?)   // null = « Tous »
        fun basculerFavori(quizId: Int)
        fun jouer(quizId: Int)
    }

    // Les fonctions sont « suspend » : elles peuvent prendre du temps (fichier, réseau),
    // donc le présentateur les appelle dans une coroutine, sur un fil d'arrière-plan.
    interface IModèleDécouvrir {
        suspend fun obtenirQuiz(catégorie: CategorieQuiz?, recherche: String): List<CarteDiscover>
        suspend fun basculerFavori(titre: String)
        suspend fun estJouable(quizId: Int): Boolean
    }
}