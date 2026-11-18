package com.crosemont.qwizup.presentation.rejoindre

import com.crosemont.qwizup.presentation.ModèleQuiz
import com.crosemont.qwizup.presentation.rejoindre.ContratRejoindre.IPrésentateurRejoindre
import com.crosemont.qwizup.presentation.rejoindre.ContratRejoindre.IVueRejoindre

class PrésentateurRejoindre(
    private val modele: ModèleQuiz = ModèleQuiz()
) : IPrésentateurRejoindre {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueRejoindre? = null

    override fun attacherVue(vue: IVueRejoindre) {
        this.vue = vue
        // Au départ, le champ est vide : le bouton Rejoindre est désactivé
        vue.activerRejoindre(false)
    }

    override fun detacherVue() {
        vue = null
    }

    // Un code valide contient exactement 6 chiffres. //
    private fun estFormatValide(code: String): Boolean =
        code.length == 6 && code.all { it.isDigit() }

    // Le texte du champ a changé : bouton actif seulement avec 6 chiffres. //
    override fun surCodeModifie(code: String) {
        vue?.effacerErreur()
        vue?.activerRejoindre(estFormatValide(code))
    }

    override fun surRejoindreClique(code: String) {
        val codeTrimmer = code.trim()
        val quizId =
            if (estFormatValide(codeTrimmer)) modele.trouverQuizParCode(codeTrimmer) else null
        if (quizId == null) {
            vue?.afficherCodeInvalide()
        } else {
            vue?.ouvrirQuiz(quizId)
        }
    }

    override fun surRetourClique() {
        vue?.fermer()
    }
}