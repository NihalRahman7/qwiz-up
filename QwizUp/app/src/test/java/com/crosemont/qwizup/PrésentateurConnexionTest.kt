package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion
import com.crosemont.qwizup.presentation.onboarding.PrésentateurConnexion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Écran Connexion : le bouton s'active seulement avec les deux champs remplis,
 * et chaque bouton ouvre le bon écran.
 */
class PrésentateurConnexionTest {

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : IVueConnexion {
        var boutonActif: Boolean? = null
        var accueilOuvert = false
        var inscriptionOuverte = false
        var fermée = false

        override fun activerConnexion(actif: Boolean) {
            boutonActif = actif
        }

        override fun ouvrirAccueil() {
            accueilOuvert = true
        }

        override fun ouvrirInscription() {
            inscriptionOuverte = true
        }

        override fun fermer() {
            fermée = true
        }
    }

    private lateinit var vue: FausseVue
    private lateinit var présentateur: PrésentateurConnexion

    @Before
    fun préparer() {
        vue = FausseVue()
        présentateur = PrésentateurConnexion()
        présentateur.attacherVue(vue)
    }

    @Test
    fun `Étant donné l'écran Connexion lorsqu'on l'ouvre, on obtient le bouton Me connecter désactivé`() {
        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné seulement le nom rempli lorsqu'on tape, on obtient le bouton désactivé`() {
        présentateur.surChampsModifiés("allan", "")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné les deux champs remplis lorsqu'on tape, on obtient le bouton activé`() {
        présentateur.surChampsModifiés("allan", "motdepasse")

        assertEquals(true, vue.boutonActif)
    }

    @Test
    fun `Étant donné des champs remplis lorsqu'on touche Me connecter, on obtient l'Accueil`() {
        présentateur.surConnexionClique("allan", "motdepasse")

        assertTrue(vue.accueilOuvert)
    }

    @Test
    fun `Étant donné l'écran Connexion lorsqu'on touche Créer un compte, on obtient l'écran Inscription`() {
        présentateur.surCréerCompteClique()

        assertTrue(vue.inscriptionOuverte)
    }

    @Test
    fun `Étant donné l'écran Connexion lorsqu'on revient en arrière, on obtient l'écran fermé`() {
        présentateur.surRetourClique()

        assertTrue(vue.fermée)
    }
}