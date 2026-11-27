package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Présentateur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Vue

interface ContratInscription {
    interface IVueInscription : Vue {

        // Active ou desactive le bouton "Creer mon compte"
        fun activerCréation(actif: Boolean)

        // Affiche ou cache le ✓ vert « Les deux mots de passe sont identiques »
        fun afficherMotsDePasseIdentiques(identiques: Boolean)

        // Ouvre l'Accueil (compte créé)
        fun ouvrirAccueil()

        // Ouvre l'ecran Connexion
        fun ouvrirConnexion()

        // Ferme l'ecran
        fun fermer()
    }

    interface IPrésentateurInscription : Présentateur<IVueInscription> {
        // Le texte d'un des quatre champs a changer
        fun surChampsModifiés(nom: String, courriel: String, motDePasse: String, confirmation: String)

        // L'utilisateur a toucher "Creer mon compte"
        fun surCréerCompteClique(nom: String, courriel: String, motDePasse: String)

        // L'utilisateur a toucher "Me connecter"
        fun surDéjàCompteClique()

        // L'utilisateur a toucher la fleche retour
        fun surRetourClique()
    }
}