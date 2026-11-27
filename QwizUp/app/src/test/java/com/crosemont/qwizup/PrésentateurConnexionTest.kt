package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IModèleConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion
import com.crosemont.qwizup.presentation.onboarding.PrésentateurConnexion
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Écran Connexion : le bouton s'active seulement avec les deux champs remplis,
 * l'utilisateur est retenu sur l'appareil, et chaque bouton ouvre le bon écran.
 */
class PrésentateurConnexionTest {

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : IVueConnexion {
        var boutonActif: Boolean? = null
        var accueilOuvert = false
        var inscriptionOuverte = false
        var fermée = false
        var erreur = false

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

        override fun afficherErreur() {
            erreur = true
        }
    }

    // Faux modèle : retient qui s'est connecté, peut être « en panne »
    private class FauxModèle(private val enPanne: Boolean = false) : IModèleConnexion {
        var connecté: String? = null

        override suspend fun connecter(nomUtilisateur: String) {
            if (enPanne) throw ServiceException("Impossible de sauvegarder le profil")
            connecté = nomUtilisateur
        }
    }

    private lateinit var vue: FausseVue
    private lateinit var modèle: FauxModèle
    private lateinit var présentateur: PrésentateurConnexion

    @Before
    fun préparer() {
        vue = FausseVue()
        modèle = FauxModèle()
        présentateur = PrésentateurConnexion(modèle, Dispatchers.Unconfined, Dispatchers.Unconfined)
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
    fun `Étant donné des champs remplis lorsqu'on touche Me connecter, on obtient l'utilisateur retenu sur l'appareil`() {
        présentateur.surConnexionClique("allan", "motdepasse")

        assertEquals("allan", modèle.connecté)
    }

    @Test
    fun `Étant donné une sauvegarde impossible lorsqu'on touche Me connecter, on obtient une erreur et on reste sur l'écran`() {
        val vueEnPanne = FausseVue()
        val présentateurEnPanne =
            PrésentateurConnexion(FauxModèle(enPanne = true), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateurEnPanne.attacherVue(vueEnPanne)

        présentateurEnPanne.surConnexionClique("allan", "motdepasse")

        assertTrue(vueEnPanne.erreur)
        assertFalse(vueEnPanne.accueilOuvert)
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