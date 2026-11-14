package com.crosemont.qwizup.presentation.discover

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz

class PrésentateurDécouvrir(
    var modèle: ModeleQuiz = ModeleQuiz(),
) : ContratDécouvrir.IPrésentateurDécouvrir {

    private var vue: ContratDécouvrir.IVueDécouvrir? = null

    // Filtres choisis par l'utilisateur
    private var catégorieChoisie: CategorieQuiz? = null   // null = « Tous »
    private var recherche = ""

    override fun attacherVue(vue: ContratDécouvrir.IVueDécouvrir) {
        this.vue = vue
        vue.afficherCatégorieChoisie(catégorieChoisie)
        afficherQuiz()
    }

    override fun detacherVue() {
        vue = null
    }

    override fun rechercher(texte: String) {
        recherche = texte
        afficherQuiz()
    }

    override fun choisirCatégorie(catégorie: CategorieQuiz?) {
        catégorieChoisie = catégorie
        vue?.afficherCatégorieChoisie(catégorie)
        afficherQuiz()
    }

    override fun basculerFavori(quizId: Int) {
        modèle.basculerFavoriDiscover(quizId)
        afficherQuiz()
    }

    override fun jouer(quizId: Int) {
        val quiz = modèle.obtenirQuizDiscoverParId(quizId) ?: return

        // Seuls les quiz qui ont des questions peuvent être joués
        if (modèle.obtenirQuizAJouer(quizId) != null) {
            vue?.ouvrirQuiz(quizId, quiz.titre)
        } else {
            vue?.afficherQuizBientôt(quiz.titre)
        }
    }

    // Demande au modèle les quiz qui correspondent aux filtres, puis les envoie à la vue
    private fun afficherQuiz() {
        val quiz = modèle.obtenirQuizDiscover(catégorieChoisie, recherche)

        vue?.viderQuiz()
        if (quiz.isEmpty()) {
            vue?.afficherAucunRésultat()
        } else {
            quiz.forEach {
                vue?.ajouterQuiz(
                    it.id, it.categorie, it.titre, it.description,
                    it.nbQuestions, it.dureeMinutes, it.estFavori
                )
            }
        }
    }
}
