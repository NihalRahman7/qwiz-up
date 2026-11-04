package com.crosemont.qwizup.presentation.home

import com.crosemont.qwizup.presentation.ContratVuePresentateur.IPresentateurHome
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IVueHome

class PresentateurHome : IPresentateurHome {

    private var vue: IVueHome? = null

    override fun attacherVue(vue: IVueHome) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    override fun surQuizClique(quizId: Int) {
        vue?.ouvrirQuiz(quizId)
    }

    override fun surCodeClique() {
        vue?.ouvrirRejoindre()
    }
}