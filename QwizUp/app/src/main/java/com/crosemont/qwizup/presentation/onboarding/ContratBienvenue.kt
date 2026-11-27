package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

interface ContratBienvenue {
    interface IVueBienvenue : Vue {
        // Ouvre L'ecran d'inscription
        fun ouvrirInscription()

        // Ouvre L'ecran de connexion
        fun ouvrirConnexion()
    }

    interface IPrésentateurBienvenue : Présentateur<IVueBienvenue> {
        // L'utilisateur a toucher "Creer un compte"
        fun surCréerCompteClique()

        // L'utilisateur a toucher "J'ai deja un compte"
        fun surDéjàCompteClique()
    }

}