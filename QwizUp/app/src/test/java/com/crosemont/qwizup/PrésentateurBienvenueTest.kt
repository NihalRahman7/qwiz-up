package com.crosemont.qwizup

import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IVueBienvenue
import com.crosemont.qwizup.presentation.onboarding.PrésentateurBienvenue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Écran Bienvenue : chaque bouton ouvre le bon écran.
 */
class PrésentateurBienvenueTest {

    // Fausse vue : retient quel écran a été ouvert
    private class FausseVue : IVueBienvenue {
        var inscriptionOuverte = false
        var connexionOuverte = false

        override fun ouvrirInscription() {
            inscriptionOuverte = true
        }

        override fun ouvrirConnexion() {
            connexionOuverte = true
        }
    }

    private lateinit var vue: FausseVue
    private lateinit var présentateur: PrésentateurBienvenue

    @Before
    fun préparer() {
        vue = FausseVue()
        présentateur = PrésentateurBienvenue()
        présentateur.attacherVue(vue)
    }

    @Test
    fun `Étant donné l'écran Bienvenue lorsqu'on touche Créer un compte, on obtient l'écran Inscription`() {
        présentateur.surCréerCompteClique()

        assertTrue(vue.inscriptionOuverte)
        assertFalse(vue.connexionOuverte)
    }

    @Test
    fun `Étant donné l'écran Bienvenue lorsqu'on touche J'ai déjà un compte, on obtient l'écran Connexion`() {
        présentateur.surDéjàCompteClique()

        assertTrue(vue.connexionOuverte)
        assertFalse(vue.inscriptionOuverte)
    }

    @Test
    fun `Étant donné une vue détachée lorsqu'on touche Créer un compte, on obtient aucun écran ouvert`() {
        présentateur.detacherVue()

        présentateur.surCréerCompteClique()

        assertFalse(vue.inscriptionOuverte)
    }
}