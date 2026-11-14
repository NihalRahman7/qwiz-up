package com.crosemont.qwizup.presentation.question

import com.crosemont.qwizup.presentation.ModeleQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IPresentateurQuestion
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IVueQuestion
import com.crosemont.qwizup.presentation.ContratVuePresentateur.NiveauResultat
import com.crosemont.qwizup.presentation.ContratVuePresentateur.TypeQuestion


class PresentateurQuestion(
    private val modele: ModeleQuiz = ModeleQuiz()
) : IPresentateurQuestion {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueQuestion? = null

    // État de la partie
    private var quizId = 0
    private var titreQuiz = ""
    private var questions: List<ModeleQuiz.QuestionCreer> = emptyList()
    private var indiceQuestion = 0     // question courante (0 = la première)
    private var score = 0
    private var aRepondu = false       // une seule réponse par question
    private var partieTerminee = false // vrai quand l'écran de résultat est affiché

    override fun attacherVue(vue: IVueQuestion) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    /** Débute une partie : charge le quiz et affiche la première question. */
    override fun surDemarrer(quizId: Int) {
        val quiz = modele.obtenirQuizAJouer(quizId)

        // Quiz introuvable ou sans question : on ferme l'écran
        if (quiz == null || quiz.questions.isEmpty()) {
            vue?.fermer()
            return
        }

        this.quizId = quizId
        titreQuiz = quiz.titre
        questions = quiz.questions
        indiceQuestion = 0
        score = 0
        partieTerminee = false
        afficherQuestionCourante()
    }

    // Envoie la question courante à la vue. //
    private fun afficherQuestionCourante() {
        aRepondu = false
        val question = questions[indiceQuestion]

        vue?.afficherQuestion(
            titreQuiz,
            indiceQuestion + 1,              // numéro affiché (commence à 1)
            questions.size,
            question.texte,
            question.type,
            // Vrai ou faux : la vue affiche elle-même « Vrai » et « Faux »
            if (question.type == TypeQuestion.CHOIX_MULTIPLE) question.reponses else emptyList()
        )
        vue?.afficherProgression(indiceQuestion * 100 / questions.size)
    }

    // Traite la réponse touchée par l'utilisateur. //
    override fun surReponseChoisie(indice: Int) {
        if (aRepondu) return             // déjà répondu : on ignore les autres clics
        aRepondu = true

        val question = questions[indiceQuestion]
        if (indice == question.bonneReponse) {
            score++
            vue?.afficherBonneReponse(indice)
        } else {
            vue?.afficherMauvaiseReponse(indice, question.bonneReponse)
        }

        vue?.afficherProgression((indiceQuestion + 1) * 100 / questions.size)
        vue?.activerSuivant(estDerniere = indiceQuestion == questions.lastIndex)
    }

    // Passe à la question suivante, ou affiche le résultat après la dernière. //
    override fun surSuivantClique() {
        if (!aRepondu) return            // il faut répondre avant de continuer

        if (indiceQuestion == questions.lastIndex) {
            partieTerminee = true
            vue?.afficherResultat(titreQuiz, score, questions.size, niveauPour(score, questions.size))
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

    // Choisit le message de fin selon le pourcentage de bonnes réponses. //
    private fun niveauPour(score: Int, total: Int): NiveauResultat {
        val pourcentage = score * 100 / total
        return when {
            pourcentage >= 80 -> NiveauResultat.EXCELLENT
            pourcentage >= 60 -> NiveauResultat.BIEN
            else -> NiveauResultat.A_PRATIQUER
        }
    }
}