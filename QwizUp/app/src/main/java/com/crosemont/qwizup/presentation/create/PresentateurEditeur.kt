package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Difficulte
import com.crosemont.qwizup.presentation.ContratVuePresentateur.ErreurEditeur

class PresentateurEditeur(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurEditeur {

    private var vue: ContratVuePresentateur.IVueEditeur? = null

    // Le quiz en cours de modification (pas encore sauvegardé dans le modèle)
    private var quiz = nouveauQuiz()

    override fun attacherVue(vue: ContratVuePresentateur.IVueEditeur) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    override fun chargerQuiz(quizId: Int?) {
        quiz = quizId?.let { modeleQuiz.obtenirQuizCreer(it) } ?: nouveauQuiz()

        vue?.afficherEnTete(nouveauQuiz = quiz.id == 0, estPublie = quiz.estPublie)
        vue?.afficherInfosQuiz(quiz.titre, quiz.description)
        vue?.afficherCategorieChoisie(quiz.categorie)
        vue?.afficherCouverture(quiz.categorie)
        vue?.afficherDifficulteChoisie(quiz.difficulte)
        afficherQuestions()
    }

    override fun surTitreModifie(titre: String) {
        quiz = quiz.copy(titre = titre)
        if (titre.isNotBlank()) vue?.effacerErreurs()
    }

    override fun surDescriptionModifiee(description: String) {
        quiz = quiz.copy(description = description)
    }

    override fun surCategorieCliquee(categorie: CategorieQuiz) {
        quiz = quiz.copy(categorie = categorie)
        vue?.afficherCategorieChoisie(categorie)
        vue?.afficherCouverture(categorie)
    }

    override fun surDifficulteCliquee(difficulte: Difficulte) {
        quiz = quiz.copy(difficulte = difficulte)
        vue?.afficherDifficulteChoisie(difficulte)
    }

    override fun surPrendrePhotoClique() {
        vue?.ouvrirAppareilPhoto()
    }

    override fun surGalerieClique() {
        vue?.ouvrirGalerie()
    }

    override fun surAjouterQuestionClique() {
        vue?.ouvrirEditeurQuestion(null)
    }

    override fun surModifierQuestionClique(numero: Int) {
        vue?.ouvrirEditeurQuestion(numero)
    }

    override fun surSauvegarderClique() {
        if (quiz.titre.isBlank()) {
            vue?.afficherErreur(ErreurEditeur.TITRE_VIDE)
            return
        }
        val id = modeleQuiz.sauvegarderQuizCreer(quiz.copy(titre = quiz.titre.trim()))
        quiz = quiz.copy(id = id)
        vue?.confirmerSauvegarde()
    }

    override fun surPublierClique() {
        when {
            quiz.titre.isBlank() -> vue?.afficherErreur(ErreurEditeur.TITRE_VIDE)
            quiz.questions.isEmpty() -> vue?.afficherErreur(ErreurEditeur.AUCUNE_QUESTION)
            else -> {
                val publie = quiz.copy(titre = quiz.titre.trim(), estPublie = true)
                quiz = publie.copy(id = modeleQuiz.sauvegarderQuizCreer(publie))
                vue?.confirmerPublication(quiz.titre)
            }
        }
    }

    private fun afficherQuestions() {
        vue?.viderQuestions()
        if (quiz.questions.isEmpty()) {
            vue?.afficherAucuneQuestion()
        } else {
            quiz.questions.forEachIndexed { index, question ->
                vue?.ajouterQuestion(
                    index + 1, question.texte, question.estVraiFaux,
                    question.secondes, question.doublePoints
                )
            }
        }
        vue?.afficherNombreQuestions(quiz.questions.size)
    }

    private fun nouveauQuiz() = ModeleQuiz.QuizCreer(
        id = 0,
        titre = "",
        description = "",
        categorie = CategorieQuiz.SCIENCES,
        difficulte = Difficulte.MOYEN,
        questions = emptyList(),
        estPublie = false
    )
}
