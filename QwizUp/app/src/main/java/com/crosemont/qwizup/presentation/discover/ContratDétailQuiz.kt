package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore

// Contrat de l'écran Détail du quiz (le modèle est celui de Découvrir)
interface ContratDétailQuiz {

    interface IVueDétailQuiz : Vue {
        fun afficherQuiz(catégorie: CatégorieQuiz, titre: String, description: String, nbQuestions: Int, duréeMinutes: Int)

        // Étoile pleine ou vide
        fun afficherFavori(estFavori: Boolean)

        // null : le quiz n'a pas encore été joué, le bandeau est caché
        fun afficherMeilleurScore(meilleurScore: MeilleurScore?)

        // Ouvre l'écran de jeu
        fun ouvrirJeu(quizId: Int)

        // Le quiz n'a pas encore de questions
        fun afficherQuizBientôt(titre: String)

        fun fermer()

        // Le quiz est introuvable, ou le modèle n'a pas pu répondre
        fun afficherErreur()
    }

    interface IPrésentateurDétailQuiz : Présentateur<IVueDétailQuiz> {
        fun chargerQuiz(quizId: Int)
        fun surFavoriClique()
        fun surJouerClique()
        fun surRetourClique()
    }
}