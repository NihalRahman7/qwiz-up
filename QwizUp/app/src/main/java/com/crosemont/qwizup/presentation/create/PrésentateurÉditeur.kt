package com.crosemont.qwizup.presentation.create

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ModèleQuiz.QuestionCréer
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer
import com.crosemont.qwizup.presentation.create.ContratCréer.ErreurEditeur
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrésentateurÉditeur(
    private val modèle: ContratCréer.IModèleCréer,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,   // pour mettre la vue à jour
    private val filArrièrePlan: CoroutineContext = Dispatchers.IO,   // pour appeler le modèle
) : ContratCréer.IPrésentateurÉditeur {

    private var vue: ContratCréer.IVueÉditeur? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    // Vrai pendant l'enregistrement : évite d'enregistrer deux fois si on touche le bouton deux fois
    private var enregistrementEnCours = false

    // Le quiz en cours de modification (pas encore sauvegardé dans le modèle)
    private var quiz = nouveauQuiz()

    // Le quiz tel qu'il était à l'ouverture : sert à savoir s'il y a des changements non sauvegardés
    private var quizInitial = nouveauQuiz()

    override fun attacherVue(vue: ContratCréer.IVueÉditeur) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
    }

    override fun detacherVue() {
        coroutines?.cancel()   // arrête le travail en cours : la vue n'existe plus
        coroutines = null
        vue = null
    }

    override fun chargerQuiz(quizId: Int?) {
        if (quizId == null) {
            afficherQuiz(nouveauQuiz())
            return
        }
        // Quiz existant : on le demande au modèle en arrière-plan
        lancer(siErreur = { vue?.afficherErreurChargement() }) {
            val quizCharge = withContext(filArrièrePlan) { modèle.obtenirQuiz(quizId) }
            afficherQuiz(quizCharge)
        }
    }

    private fun afficherQuiz(quizAAfficher: QuizCréer) {
        quiz = quizAAfficher
        quizInitial = quiz

        vue?.afficherEnTete(nouveauQuiz = quiz.id == 0)
        vue?.afficherInfosQuiz(quiz.titre, quiz.description)
        vue?.afficherCategorieChoisie(quiz.catégorie)
        vue?.afficherCouverture(quiz.catégorie)
        vue?.afficherCategoriePerso(visible = quiz.catégorie == CatégorieQuiz.AUTRE, nom = quiz.categoriePerso)
        vue?.afficherDifficulteChoisie(quiz.difficulté)
        vue?.afficherAccesChoisi(quiz.accès)
        vue?.afficherPublication(quiz.estPublié)
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

    override fun surCategorieCliquee(catégorie: CatégorieQuiz) {
        quiz = quiz.copy(catégorie = catégorie)
        vue?.afficherCategorieChoisie(catégorie)
        vue?.afficherCouverture(catégorie)
        vue?.afficherCategoriePerso(visible = catégorie == CatégorieQuiz.AUTRE, nom = quiz.categoriePerso)
        mettreAJourBouton()
    }

    override fun surCategoriePersoModifiee(nom: String) {
        quiz = quiz.copy(categoriePerso = nom)
        if (nom.isNotBlank()) vue?.effacerErreurs()
        mettreAJourBouton()
    }

    override fun surDifficulteCliquee(difficulté: Difficulté) {
        quiz = quiz.copy(difficulté = difficulté)
        vue?.afficherDifficulteChoisie(difficulté)
    }

    override fun surAccesClique(accès: AccèsQuiz) {
        quiz = quiz.copy(accès = accès)
        vue?.afficherAccesChoisi(accès)
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
        modifierQuestion(index) { it.copy(type = type, bonnesRéponses = setOf(0)) }
        afficherQuestions()   // la carte change de forme : on la redessine
    }

    override fun surTexteQuestionModifie(index: Int, texte: String) {
        modifierQuestion(index) { it.copy(texte = texte) }
    }

    override fun surReponseModifiee(index: Int, numeroReponse: Int, texte: String) {
        modifierQuestion(index) { question ->
            question.copy(réponses = question.réponses.mapIndexed { i, r -> if (i == numeroReponse) texte else r })
        }
    }

    override fun surBonneReponseCliquee(index: Int, numeroReponse: Int) {
        val question = quiz.questions.getOrNull(index) ?: return
        val nouvelles = if (question.type == TypeQuestion.CHOIX_MULTIPLE) {
            // Choix multiple : on coche ou on décoche (plusieurs bonnes réponses possibles)
            if (numeroReponse in question.bonnesRéponses) question.bonnesRéponses - numeroReponse
            else question.bonnesRéponses + numeroReponse
        } else {
            setOf(numeroReponse)   // Vrai ou faux : une seule bonne réponse
        }
        modifierQuestion(index) { it.copy(bonnesRéponses = nouvelles) }
        vue?.afficherBonnesReponses(index, nouvelles)
    }

    override fun surReponseAttendueModifiee(index: Int, texte: String) {
        modifierQuestion(index) { it.copy(réponseAttendue = texte) }
    }

    // ----- Sauvegarde -----

    override fun surPublicationCochee(cochee: Boolean) {
        quiz = quiz.copy(estPublié = cochee)
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
        if (enregistrementEnCours) return
        enregistrementEnCours = true

        val aSauvegarder = quiz.copy(titre = quiz.titre.trim())
        lancer(siErreur = { vue?.afficherErreurSauvegarde() }) {
            try {
                // Le service vérifie et enregistre le quiz en arrière-plan, puis retourne son id
                val id = withContext(filArrièrePlan) { modèle.sauvegarderQuiz(aSauvegarder) }
                quiz = aSauvegarder.copy(id = id)
                quizInitial = quiz
                if (quiz.estPublié) vue?.confirmerPublication(quiz.titre)
                else vue?.confirmerSauvegarde()
            } finally {
                enregistrementEnCours = false
            }
        }
    }

    /** Ce qui empêche d'enregistrer le quiz (numéro de question à partir de 1). */
    private data class Manque(val erreur: ErreurEditeur, val numeroQuestion: Int = 0)

    /** Un brouillon a juste besoin d'un titre ; pour publier, il faut aussi des questions complètes. */
    private fun ceQuiManque(): Manque? {
        if (quiz.titre.isBlank()) return Manque(ErreurEditeur.TITRE_VIDE)
        if (!quiz.estPublié) return null
        if (quiz.catégorie == CatégorieQuiz.AUTRE && quiz.categoriePerso.isBlank()) return Manque(ErreurEditeur.CATEGORIE_VIDE)
        if (quiz.questions.isEmpty()) return Manque(ErreurEditeur.AUCUNE_QUESTION)
        val incomplete = quiz.questions.indexOfFirst { !it.estComplete() }
        if (incomplete != -1) return Manque(ErreurEditeur.QUESTION_INCOMPLETE, incomplete + 1)
        return null
    }

    /**
     * Met à jour le texte du bouton et le rend gris s'il manque quelque chose.
     * Le message qui explique quoi apparaît seulement quand l'utilisateur a commencé à modifier le quiz.
     */
    private fun mettreAJourBouton() {
        val manque = ceQuiManque()
        val aCommence = quiz != quizInitial
        vue?.afficherBoutonEnregistrer(
            publier = quiz.estPublié,
            actif = manque == null,
            raison = if (aCommence) manque?.erreur else null,
            numeroQuestion = manque?.numeroQuestion ?: 0
        )
    }

    // ----- Fermeture -----

    override fun surFermerClique() {
        if (quiz != quizInitial) vue?.demanderConfirmationQuitter()
        else vue?.fermer()
    }

    override fun surQuitterConfirme() {
        vue?.fermer()
    }

    private fun modifierQuestion(index: Int, changement: (QuestionCréer) -> QuestionCréer) {
        if (index !in quiz.questions.indices) return
        quiz = quiz.copy(questions = quiz.questions.mapIndexed { i, q -> if (i == index) changement(q) else q })
        mettreAJourBouton()
    }

    private fun afficherQuestions() {
        vue?.viderQuestions()
        quiz.questions.forEachIndexed { index, q ->
            vue?.ajouterQuestion(index, q.texte, q.type, q.réponses, q.bonnesRéponses, q.réponseAttendue)
        }
        vue?.afficherNombreQuestions(quiz.questions.size)
        mettreAJourBouton()
    }

    private fun questionVide() = QuestionCréer(texte = "", type = TypeQuestion.CHOIX_MULTIPLE)

    private fun nouveauQuiz() = QuizCréer(
        id = 0,
        titre = "",
        description = "",
        catégorie = CatégorieQuiz.SCIENCES,
        difficulté = Difficulté.MOYEN,
        accès = AccèsQuiz.AVEC_CODE,
        questions = listOf(questionVide()),
        estPublié = false
    )

    // Lance une coroutine sur le fil principal. Si le modèle échoue, on appelle siErreur.
    private fun lancer(siErreur: () -> Unit, travail: suspend () -> Unit): Job? =
        coroutines?.launch {
            try {
                travail()
            } catch (e: CancellationException) {
                throw e   // la coroutine a été annulée : ce n'est pas une erreur
            } catch (e: Exception) {
                siErreur()
            }
        }
}
