package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IPrésentateurBienvenue
import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IVueBienvenue



class PrésentateurBienvenue : IPrésentateurBienvenue {
    override fun attacherVue(vue: IVueBienvenue) { }

    override fun detacherVue() { }

    override fun surCréerCompteClique() { }

    override fun surDéjàCompteClique() { }
}