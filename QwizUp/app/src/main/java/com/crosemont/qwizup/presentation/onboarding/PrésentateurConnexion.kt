package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IPrésentateurConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion


class PrésentateurConnexion: IPrésentateurConnexion {
    override fun attacherVue(vue: IVueConnexion) {
    }

    override fun detacherVue() {
    }

    override fun surChampsModifiés(nom: String, motDePasse: String) {
    }

    override fun surConnexionClique(nom: String, motDePasse: String) {
    }

    override fun surCréerCompteClique() {
    }

    override fun surRetourClique() {
    }

}