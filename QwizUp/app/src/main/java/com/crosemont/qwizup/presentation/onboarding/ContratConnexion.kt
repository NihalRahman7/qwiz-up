package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

interface ContratConnexion {
    interface IVueConnexion : Vue {

        // Active ou desactive le bouton "Se connecter"
        fun activerConnexion(actif: Boolean)

        // Ouvre l'Accueil (Connexion a reussie)
        fun ouvrirAccueil()

        // Ouvre l'Inscription
        fun ouvrirInscription()

        // Ferme l'ecran
        fun fermer()
    }

    interface IPrésentateurConnexion : Présentateur<IVueConnexion>{
        // Champs modifier
        fun surChampsModifiés(nom: String, motDePasse: String)

        // L'utilisateur a toucher "Se connecter"
        fun surConnexionClique(nom: String, motDePasse: String)

        //L'utilisateur a toucher "Creer un compte"
        fun surCréerCompteClique()

        // L'utilisateur a touche le x
        fun surRetourClique()
    }
}