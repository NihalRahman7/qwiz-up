package com.crosemont.qwizup.presentation.onboarding

import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IPrésentateurConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion


class PrésentateurConnexion: IPrésentateurConnexion {

    // La vue est null quand l'écran n'est pas affiché
    private var vue: IVueConnexion? = null

    override fun attacherVue(vue: IVueConnexion) {
        this.vue = vue

        // Champs son vides, bouton est desactive
        vue.activerConnexion(false)
    }

    override fun detacherVue() {
        vue = null
    }

    // Bouton actif seulement quand les deux champs sont remplis
    override fun surChampsModifiés(nom: String, motDePasse: String) {
        vue?.activerConnexion(nom.isNotBlank() && motDePasse.isNotBlank())
    }

    // Verifier le nom et le mot de passe avec l'API(TODO)
    override fun surConnexionClique(nom: String, motDePasse: String) {
        vue?.ouvrirAccueil()
    }

    override fun surCréerCompteClique() {
        vue?.ouvrirInscription()
    }

    override fun surRetourClique() {
        vue?.fermer()
    }

}