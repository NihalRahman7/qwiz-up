package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue
import com.crosemont.qwizup.presentation.ModèleQuiz.CarteDiscover

interface ContratDécouvrir {

    // Le meilleur résultat obtenu à un quiz, par exemple 4 bonnes réponses sur 5
    data class MeilleurScore(val score: Int, val total: Int)

    interface IVueDécouvrir : Vue {
        fun viderQuiz()
        fun ajouterQuiz(
            id: Int,
            catégorie: CatégorieQuiz,
            titre: String,
            description: String,
            nbQuestions: Int,
            duréeMinutes: Int,
            estFavori: Boolean,
            meilleurScore: MeilleurScore?   // null = pas encore joué
        )
        fun afficherAucunRésultat()
        fun afficherAucunFavori()
        fun afficherCatégorieChoisie(catégorie: CatégorieQuiz?)   // null = « Tous »
        fun afficherFavorisChoisis()
        fun ouvrirQuiz(quizId: Int, titre: String)
        fun ouvrirDétail(quizId: Int)   // ouvre l'écran Détail du quiz
        fun afficherQuizBientôt(titre: String)   // le quiz n'a pas encore de questions
        fun afficherErreur()   // le modèle n'a pas pu répondre
        fun afficherChargement(visible: Boolean)

    }

    interface IPrésentateurDécouvrir : Présentateur<IVueDécouvrir> {
        fun rechercher(texte: String)
        fun choisirCatégorie(catégorie: CatégorieQuiz?)   // null = « Tous »
        fun choisirFavoris()   // seulement les quiz avec l'étoile
        fun basculerFavori(quizId: Int)
        fun jouer(quizId: Int)
        fun ouvrirDétail(quizId: Int)   // une carte a été touchée
    }

    // Les fonctions sont « suspend » : elles peuvent prendre du temps (fichier, réseau),
    // donc le présentateur les appelle dans une coroutine, sur un fil d'arrière-plan.
    interface IModèleDécouvrir {
        suspend fun obtenirQuiz(catégorie: CatégorieQuiz?, recherche: String): List<CarteDiscover>
        suspend fun obtenirMeilleursScores(): Map<Int, MeilleurScore>   // la clé est le numéro du quiz
        suspend fun basculerFavori(titre: String)
        suspend fun estJouable(quizId: Int): Boolean
    }
}
