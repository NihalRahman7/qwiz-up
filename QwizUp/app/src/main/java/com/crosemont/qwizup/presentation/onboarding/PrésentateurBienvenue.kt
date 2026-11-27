package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IPrésentateurBienvenue
import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IVueBienvenue



class PrésentateurBienvenue : IPrésentateurBienvenue {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueBienvenue? = null

    override fun attacherVue(vue: IVueBienvenue) {
        this.vue = vue
    }

    override fun detacherVue() {
        vue = null
    }

    override fun surCréerCompteClique() {
        vue?.ouvrirInscription()
    }

    override fun surDéjàCompteClique() {
        vue?.ouvrirConnexion()
    }
}