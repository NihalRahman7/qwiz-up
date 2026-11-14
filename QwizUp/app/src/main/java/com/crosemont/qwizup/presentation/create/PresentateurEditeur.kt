package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ModeleQuiz.QuestionCreer
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.AccesQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Difficulte
import com.crosemont.qwizup.presentation.ContratVuePresentateur.ErreurEditeur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion

class PresentateurEditeur(
    var modeleQuiz: ModeleQuiz = ModeleQuiz(),
) : ContratVuePresentateur.IPresentateurEditeur {

    private var vue: ContratVuePresentateur.IVueEditeur? = null

    // Le quiz en cours de modification (pas encore sauvegardé dans le modèle)
    private var quiz = nouveauQuiz()

    // Le quiz tel qu'il était à l'ouverture : sert à savoir s'il y a des changements non sauvegardés
    private var quizInitial = nouveauQuiz()

    override fun attacherVue(vue: ContratVuePresentateur.IVueEditeur) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    override fun chargerQuiz(quizId: Int?) {
        quiz = quizId?.let { modeleQuiz.obtenirQuizCreer(it) } ?: nouveauQuiz()
        quizInitial = quiz

        vue?.afficherEnTete(nouveauQuiz = quiz.id == 0)
        vue?.afficherInfosQuiz(quiz.titre, quiz.description)
        vue?.afficherCategorieChoisie(quiz.categorie)
        vue?.afficherCouverture(quiz.categorie)
        vue?.afficherDifficulteChoisie(quiz.difficulte)
        vue?.afficherAccesChoisi(quiz.acces)
        vue?.afficherPublication(quiz.estPublie)
        afficherQuestions()
    }

    // ----- Infos du quiz -----

    override fun surTitreModifie(titre: String) {
        quiz = quiz.copy(titre = titre)
        if (titre.isNotBlank()) vue?.effacerErreurs()
        mettreAJourBouton()
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

    override fun surAccesClique(acces: AccesQuiz) {
        quiz = quiz.copy(acces = acces)
        vue?.afficherAccesChoisi(acces)
    }

    override fun surPrendrePhotoClique() {
        vue?.ouvrirAppareilPhoto()
    }

    override fun surGalerieClique() {
        vue?.ouvrirGalerie()
    }

    // ----- Questions -----

    override fun surAjouterQuestionClique() {
        quiz = quiz.copy(questions = quiz.questions + questionVide())
        afficherQuestions()
    }

    override fun surSupprimerQuestionClique(index: Int) {
        if (index !in quiz.questions.indices) return
        vue?.demanderConfirmationSuppression(index)
    }

    override fun surSuppressionConfirmee(index: Int) {
        quiz = quiz.copy(questions = quiz.questions.filterIndexed { i, _ -> i != index })
        afficherQuestions()
    }

    override fun surTypeQuestionClique(index: Int, type: TypeQuestion) {
        val question = quiz.questions.getOrNull(index) ?: return
        if (question.type == type) return
        modifierQuestion(index) { it.copy(type = type, bonneReponse = 0) }
        afficherQuestions()   // la carte change de forme : on la redessine
    }

    override fun surTexteQuestionModifie(index: Int, texte: String) {
        modifierQuestion(index) { it.copy(texte = texte) }
    }

    override fun surReponseModifiee(index: Int, numeroReponse: Int, texte: String) {
        modifierQuestion(index) { question ->
            question.copy(reponses = question.reponses.mapIndexed { i, r -> if (i == numeroReponse) texte else r })
        }
    }

    override fun surBonneReponseCliquee(index: Int, numeroReponse: Int) {
        modifierQuestion(index) { it.copy(bonneReponse = numeroReponse) }
        vue?.afficherBonneReponse(index, numeroReponse)
    }

    override fun surReponseAttendueModifiee(index: Int, texte: String) {
        modifierQuestion(index) { it.copy(reponseAttendue = texte) }
    }

    // ----- Sauvegarde -----

    override fun surPublicationCochee(cochee: Boolean) {
        quiz = quiz.copy(estPublie = cochee)
        mettreAJourBouton()
    }

    override fun surEnregistrerClique() {
        // Normalement le bouton est déjà gris s'il manque quelque chose, mais on vérifie quand même
        val manque = ceQuiManque()
        if (manque != null) {
            if (manque.erreur == ErreurEditeur.QUESTION_INCOMPLETE) vue?.afficherErreurQuestion(manque.numeroQuestion)
            else vue?.afficherErreur(manque.erreur)
            return
        }
        val aSauvegarder = quiz.copy(titre = quiz.titre.trim())
        quiz = aSauvegarder.copy(id = modeleQuiz.sauvegarderQuizCreer(aSauvegarder))
        quizInitial = quiz
        if (quiz.estPublie) vue?.confirmerPublication(quiz.titre)
        else vue?.confirmerSauvegarde()
    }

    /** Ce qui empêche d'enregistrer le quiz (numéro de question à partir de 1). */
    private data class Manque(val erreur: ErreurEditeur, val numeroQuestion: Int = 0)

    /** Un brouillon a juste besoin d'un titre ; pour publier, il faut aussi des questions complètes. */
    private fun ceQuiManque(): Manque? {
        if (quiz.titre.isBlank()) return Manque(ErreurEditeur.TITRE_VIDE)
        if (!quiz.estPublie) return null
        if (quiz.questions.isEmpty()) return Manque(ErreurEditeur.AUCUNE_QUESTION)
        val incomplete = quiz.questions.indexOfFirst { !estComplete(it) }
        if (incomplete != -1) return Manque(ErreurEditeur.QUESTION_INCOMPLETE, incomplete + 1)
        return null
    }

    /** Met à jour le texte du bouton et le rend gris s'il manque quelque chose. */
    private fun mettreAJourBouton() {
        val manque = ceQuiManque()
        vue?.afficherBoutonEnregistrer(quiz.estPublie, manque?.erreur, manque?.numeroQuestion ?: 0)
    }

    // ----- Fermeture -----

    override fun surFermerClique() {
        if (quiz != quizInitial) vue?.demanderConfirmationQuitter()
        else vue?.fermer()
    }

    override fun surQuitterConfirme() {
        vue?.fermer()
    }

    /** Une question est complète quand elle a un texte et une bonne réponse remplie. */
    private fun estComplete(question: QuestionCreer): Boolean = question.texte.isNotBlank() && when (question.type) {
        TypeQuestion.CHOIX_MULTIPLE -> question.reponses.getOrElse(question.bonneReponse) { "" }.isNotBlank()
        TypeQuestion.VRAI_FAUX -> true
        TypeQuestion.REPONSE_ECRITE -> question.reponseAttendue.isNotBlank()
    }

    private fun modifierQuestion(index: Int, changement: (QuestionCreer) -> QuestionCreer) {
        if (index !in quiz.questions.indices) return
        quiz = quiz.copy(questions = quiz.questions.mapIndexed { i, q -> if (i == index) changement(q) else q })
        mettreAJourBouton()
    }

    private fun afficherQuestions() {
        vue?.viderQuestions()
        quiz.questions.forEachIndexed { index, q ->
            vue?.ajouterQuestion(index, q.texte, q.type, q.reponses, q.bonneReponse, q.reponseAttendue)
        }
        vue?.afficherNombreQuestions(quiz.questions.size)
        mettreAJourBouton()
    }

    private fun questionVide() = QuestionCreer(texte = "", type = TypeQuestion.CHOIX_MULTIPLE)

    private fun nouveauQuiz() = ModeleQuiz.QuizCreer(
        id = 0,
        titre = "",
        description = "",
        categorie = CategorieQuiz.SCIENCES,
        difficulte = Difficulte.MOYEN,
        acces = AccesQuiz.AVEC_CODE,
        questions = listOf(questionVide()),
        estPublie = false
    )
}
