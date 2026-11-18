package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion
import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.question.ContratQuestion.IPresentateurQuestion
import com.crosemont.qwizup.presentation.question.ContratQuestion.IVueQuestion
import com.crosemont.qwizup.presentation.question.ContratQuestion.NiveauResultat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class PresentateurQuestion(
    private val modele: ContratQuestion.IModeleQuestion,
    private val filPrincipal: CoroutineContext = Dispatchers.Main,   // pour mettre la vue à jour
    private val filArrierePlan: CoroutineContext = Dispatchers.IO,   // pour appeler le modèle
) : IPresentateurQuestion {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueQuestion? = null

    // Les coroutines du présentateur vivent tant que la vue est attachée
    private var coroutines: CoroutineScope? = null

    // État de la partie
    private var quizId = 0
    private var titreQuiz = ""
    private var questions: List<ModeleQuiz.QuestionCreer> = emptyList()
    private var indiceQuestion = 0     // question courante (0 = la première)
    private var score = 0
    private var aRepondu = false       // une seule réponse par question
    private var partieTerminee = false // vrai quand l'écran de résultat est affiché
    private var selection = emptySet<Int>()  // réponses cochées (question à plusieurs réponses)

    override fun attacherVue(vue: IVueQuestion) {
        this.vue = vue
        coroutines = CoroutineScope(filPrincipal + Job())
    }

    override fun detacherVue() {
        coroutines?.cancel()   // arrête le travail en cours : la vue n'existe plus
        coroutines = null
        vue = null
    }

    /** Débute une partie : charge le quiz en arrière-plan et affiche la première question. */
    override fun surDemarrer(quizId: Int) {
        lancer {
            val quiz = withContext(filArrierePlan) { modele.obtenirQuizAJouer(quizId) }

            // Quiz introuvable ou sans question : on ferme l'écran
            if (quiz == null || quiz.questions.isEmpty()) {
                vue?.fermer()
                return@lancer
            }

            this.quizId = quizId
            titreQuiz = quiz.titre
            questions = quiz.questions
            indiceQuestion = 0
            score = 0
            partieTerminee = false
            afficherQuestionCourante()
        }
    }

    // Envoie la question courante à la vue. //
    private fun afficherQuestionCourante() {
        aRepondu = false
        selection = emptySet()
        val question = questions[indiceQuestion]

        vue?.afficherQuestion(
            titreQuiz,
            indiceQuestion + 1,              // numéro affiché (commence à 1)
            questions.size,
            question.texte,
            question.type,
            // Vrai ou faux : la vue affiche elle-même « Vrai » et « Faux »
            if (question.type == TypeQuestion.CHOIX_MULTIPLE) question.reponses else emptyList(),
            plusieursReponses(question)
        )
        vue?.afficherProgression(indiceQuestion * 100 / questions.size)
    }

    // Traite la réponse touchée par l'utilisateur. //
    override fun surReponseChoisie(indice: Int) {
        if (aRepondu || questions.isEmpty()) return   // déjà répondu ou quiz pas encore chargé

        val question = questions[indiceQuestion]
        if (plusieursReponses(question)) {
            // Plusieurs bonnes réponses : on coche ou décoche, puis le joueur touche « Valider »
            selection = if (indice in selection) selection - indice else selection + indice
            vue?.afficherSelection(selection)
            vue?.activerValider(selection.isNotEmpty())
        } else {
            valider(setOf(indice))       // une seule bonne réponse : on corrige tout de suite
        }
    }

    /** Corrige la réponse : c'est bon seulement si le joueur a choisi toutes les bonnes réponses, sans erreur. */
    private fun valider(choisies: Set<Int>) {
        aRepondu = true
        val question = questions[indiceQuestion]
        val estBonne = choisies == question.bonnesReponses
        if (estBonne) score++
        vue?.afficherCorrection(choisies, question.bonnesReponses, estBonne)

        vue?.afficherProgression((indiceQuestion + 1) * 100 / questions.size)
        vue?.activerSuivant(estDerniere = indiceQuestion == questions.lastIndex)
    }

    // Passe à la question suivante, ou affiche le résultat après la dernière. //
    override fun surSuivantClique() {
        if (questions.isEmpty()) return   // quiz pas encore chargé

        if (!aRepondu) {
            // Question à plusieurs réponses : le bouton sert à « Valider »
            if (selection.isNotEmpty()) valider(selection)
            return
        }

        if (indiceQuestion == questions.lastIndex) {
            partieTerminee = true
            vue?.afficherResultat(
                titreQuiz,
                score,
                questions.size,
                niveauPour(score, questions.size)
            )
            enregistrerPartie(quizId, score, questions.size)
        } else {
            indiceQuestion++
            afficherQuestionCourante()
        }
    }

    // Recommence le même quiz depuis le début. //
    override fun surRejouerClique() {
        surDemarrer(quizId)
    }

    // Quitte le quiz et retourne à l'écran précédent. //
    override fun surQuitterClique() {
        // On demande une confirmation seulement si le joueur perdrait sa progression
        val partieEnCours = !partieTerminee && (indiceQuestion > 0 || aRepondu)
        if (partieEnCours) vue?.demanderConfirmationQuitter()
        else vue?.fermer()
    }

    // Le joueur a confirmé qu'il veut quitter la partie. //
    override fun surQuitterConfirme() {
        vue?.fermer()
    }

    // Sauvegarde le résultat en arrière-plan pour les statistiques de l'accueil
    private fun enregistrerPartie(quizId: Int, score: Int, total: Int) {
        lancer {
            // NonCancellable : la sauvegarde se termine même si le joueur quitte l'écran tout de suite
            withContext(filArrierePlan + NonCancellable) {
                modele.enregistrerPartie(quizId, score, total)
            }
        }
    }

    /** Vrai si la question a plusieurs bonnes réponses (le joueur coche puis valide). */
    private fun plusieursReponses(question: ModeleQuiz.QuestionCreer) =
        question.type == TypeQuestion.CHOIX_MULTIPLE && question.bonnesReponses.size > 1

    // Choisit le message de fin selon le pourcentage de bonnes réponses. //
    private fun niveauPour(score: Int, total: Int): NiveauResultat {
        val pourcentage = score * 100 / total
        return when {
            pourcentage >= 80 -> NiveauResultat.EXCELLENT
            pourcentage >= 60 -> NiveauResultat.BIEN
            else -> NiveauResultat.A_PRATIQUER
        }
    }

    // Lance une coroutine sur le fil principal. Si le modèle échoue, la vue affiche une erreur.
    private fun lancer(travail: suspend () -> Unit): Job? =
        coroutines?.launch {
            try {
                travail()
            } catch (e: CancellationException) {
                throw e   // la coroutine a été annulée : ce n'est pas une erreur
            } catch (e: Exception) {
                vue?.afficherErreur()
            }
        }
}