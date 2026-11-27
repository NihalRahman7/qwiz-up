package com.crosemont.qwizup.domaine.service

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ModèleQuiz.QuizCréer
import com.crosemont.qwizup.accèsAuxDonnées.SourceQuizCréés
import kotlinx.coroutines.CancellationException

/**
 * Gère les quiz créés par l'utilisateur : les lister, en ouvrir un et les enregistrer.
 * La source de données est fournie de l'extérieur, par son interface.
 */
class ServiceCréation(private val source: SourceQuizCréés,
    // Fabrique un code au hasard (remplacé dans les tests pour choisir les codes)
                      private val générerCode: () -> String = { codeAuHasard() }
) {

    suspend fun obtenirBrouillons(): List<QuizCréer> = lire().filter { !it.estPublié }

    suspend fun obtenirPubliés(): List<QuizCréer> = lire().filter { it.estPublié }

    suspend fun obtenirQuiz(quizId: Int): QuizCréer =
        lire().find { it.id == quizId } ?: throw ServiceException("Le quiz $quizId n'existe pas")

    /**
     * Vérifie le quiz puis l'enregistre. Un nouveau quiz (id = 0) reçoit un id.
     * Retourne le quiz tel qu'il a été enregistré.
     */
    suspend fun sauvegarder(quiz: QuizCréer): QuizCréer {
        vérifier(quiz)
        val tous = lire()

        val estNouveau = quiz.id == 0
        if (!estNouveau && tous.none { it.id == quiz.id }) throw ServiceException("Le quiz ${quiz.id} n'existe pas")

        // L'éditeur ne connaît pas le code : on reprend celui déjà enregistré
        val ancienCode = quiz.code.ifBlank { tous.find { it.id == quiz.id }?.code.orEmpty() }

        val àEnregistrer = quiz.copy(
            id = if (estNouveau) (tous.maxOfOrNull { it.id } ?: 0) + 1 else quiz.id,
            titre = quiz.titre.trim(),
            // Le nom de catégorie sert seulement avec « Autre »
            categoriePerso = if (quiz.catégorie == CatégorieQuiz.AUTRE) quiz.categoriePerso.trim() else "",
            // Un quiz publié reçoit un code (gardé ensuite). Un brouillon garde le sien s'il en avait un.
            code = if (quiz.estPublié && ancienCode.isBlank()) nouveauCode(tous) else ancienCode
        )
        val nouvelleListe =
            if (estNouveau) tous + àEnregistrer
            else tous.map { if (it.id == àEnregistrer.id) àEnregistrer else it }

        écrire(nouvelleListe)
        return àEnregistrer
    }

    /**
     * Un brouillon a juste besoin d'un titre. Un quiz publié doit avoir des questions complètes,
     * et préciser sa catégorie s'il est dans « Autre ».
     */
    private fun vérifier(quiz: QuizCréer) {
        if (quiz.titre.isBlank()) throw ServiceException("Le titre du quiz est vide")
        if (!quiz.estPublié) return
        if (quiz.catégorie == CatégorieQuiz.AUTRE && quiz.categoriePerso.isBlank())
            throw ServiceException("Un quiz publié dans la catégorie Autre doit préciser sa catégorie")
        if (quiz.questions.isEmpty()) throw ServiceException("Un quiz publié doit avoir au moins une question")
        val incomplète = quiz.questions.indexOfFirst { !it.estComplete() }
        if (incomplète != -1) throw ServiceException("La question ${incomplète + 1} n'est pas complète")
    }

    /** Un code au hasard qu'aucun autre quiz n'utilise. */
    private fun nouveauCode(tous: List<QuizCréer>): String {
        val pris = tous.map { it.code }.toSet()
        repeat(100) {
            val code = générerCode()
            if (code !in pris) return code
        }
        throw ServiceException("Impossible de trouver un code libre")
    }

    private suspend fun lire(): List<QuizCréer> =
        try {
            source.lireQuiz()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ServiceException("Impossible de lire les quiz créés", e)
        }

    private suspend fun écrire(quiz: List<QuizCréer>) {
        try {
            source.écrireQuiz(quiz)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ServiceException("Impossible d'enregistrer le quiz", e)
        }
    }

    companion object {
        const val LONGUEUR_CODE = 5

        // Lettres et chiffres, sans I, O, 0 et 1 (faciles à confondre)
        private const val CARACTÈRES = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        fun codeAuHasard(): String = (1..LONGUEUR_CODE).map { CARACTÈRES.random() }.joinToString("")
    }
}
