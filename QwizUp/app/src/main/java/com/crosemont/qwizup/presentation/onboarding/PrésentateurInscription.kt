package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IPrésentateurInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IVueInscription

class PrésentateurInscription : IPrésentateurInscription {

    override fun attacherVue(vue: IVueInscription) {}

    override fun detacherVue() {}

    override fun surChampsModifiés(nom: String, courriel: String, motDePasse: String, confirmation: String) {}

    override fun surCréerCompteClique(nom: String, courriel: String, motDePasse: String) {}

    override fun surDéjàCompteClique() {}

    override fun surRetourClique() {}
}