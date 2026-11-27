package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IPrésentateurInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IVueInscription

class PrésentateurInscription : IPrésentateurInscription {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueInscription? = null

    override fun attacherVue(vue: IVueInscription) {
        this.vue = vue
        // Bouton désactivé, pas de crochet vert
        vue.activerCréation(false)
        vue.afficherMotsDePasseIdentiques(false)
    }

    override fun detacherVue() {
        vue = null
    }

    // Un mot de passe valide a 8 caractères ou plus, avec au moins un chiffre
    private fun estMotDePasseValide(motDePasse: String): Boolean =
        motDePasse.length >= 8 && motDePasse.any { it.isDigit() }

    override fun surChampsModifiés(nom: String, courriel: String, motDePasse: String, confirmation: String) {
        val identiques = confirmation.isNotEmpty() && confirmation == motDePasse
        vue?.afficherMotsDePasseIdentiques(identiques)

        // Bouton actif seulement quand tout est valide
        vue?.activerCréation(
            nom.isNotBlank() && courriel.contains("@") && estMotDePasseValide(motDePasse) && identiques
        )
    }

    // Creer le compte avec l'API (TODO)
    override fun surCréerCompteClique(nom: String, courriel: String, motDePasse: String) {
        vue?.ouvrirAccueil()
    }

    override fun surDéjàCompteClique() {
        vue?.ouvrirConnexion()
    }

    override fun surRetourClique() {
        vue?.fermer()
    }
}