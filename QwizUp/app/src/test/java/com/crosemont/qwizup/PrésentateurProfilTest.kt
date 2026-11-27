package com.crosemont.qwizup

import com.crosemont.qwizup.domaine.entites.Profil
import com.crosemont.qwizup.domaine.entites.Statistiques
import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.profil.ContratProfil.IModèleProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IVueProfil
import com.crosemont.qwizup.presentation.profil.PrésentateurProfil
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Écran Profil : affiche le profil, la photo et les stats,
 * enregistre le nom et la photo, et déconnecte.
 */
class PrésentateurProfilTest {

    private val maya = Profil("Maya", "maya_t", "maya@exemple.ca")

    // Faux modèle : garde tout en mémoire, peut être « en panne »
    private class FauxModèle(
        var profil: Profil?,
        var photo: ByteArray? = null,
        private val enPanne: Boolean = false
    ) : IModèleProfil {
        var déconnecté = false

        override suspend fun obtenirProfil(): Profil? {
            if (enPanne) throw ServiceException("Impossible de lire le profil")
            return profil
        }

        override suspend fun obtenirPhoto(): ByteArray? = photo

        override suspend fun obtenirStatistiques() = Statistiques(3, 4, 78)

        override suspend fun changerNomAffiché(nom: String) {
            if (nom.isBlank()) throw ServiceException("Le nom affiché est vide")
            profil = profil?.copy(nomAffiché = nom.trim())
        }

        override suspend fun changerPhoto(photo: ByteArray?) {
            this.photo = photo
        }

        override suspend fun déconnecter() {
            déconnecté = true
            profil = null
            photo = null
        }
    }

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : IVueProfil {
        var profil: Triple<String, String, String>? = null
        var invité = false
        var photo: ByteArray? = null
        var stats: Triple<Int, Int, Int>? = null
        var modificationOuverteAvec: String? = null
        var modificationFermée = false
        var nomInvalide = false
        var mesQuiz = false
        var langue = false
        var àPropos = false
        var bienvenue = false
        var fermée = false
        var erreur = false

        override fun afficherProfil(nomAffiché: String, nomUtilisateur: String, courriel: String) {
            profil = Triple(nomAffiché, nomUtilisateur, courriel)
        }
        override fun afficherInvité() { invité = true }
        override fun afficherPhoto(photo: ByteArray?) { this.photo = photo }
        override fun afficherStatistiques(série: Int, nbQuizComplétés: Int, tauxRéussite: Int) {
            stats = Triple(série, nbQuizComplétés, tauxRéussite)
        }
        override fun ouvrirModification(nomAffiché: String) { modificationOuverteAvec = nomAffiché }
        override fun fermerModification() { modificationFermée = true }
        override fun afficherNomInvalide() { nomInvalide = true }
        override fun ouvrirMesQuiz() { mesQuiz = true }
        override fun ouvrirLangue() { langue = true }
        override fun ouvrirÀPropos() { àPropos = true }
        override fun ouvrirBienvenue() { bienvenue = true }
        override fun fermer() { fermée = true }
        override fun afficherErreur() { erreur = true }
    }

    /** Ouvre l'écran Profil avec ce modèle, comme quand on touche l'avatar. */
    private fun ouvrir(modèle: FauxModèle): Pair<PrésentateurProfil, FausseVue> {
        val vue = FausseVue()
        val présentateur = PrésentateurProfil(modèle, Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)
        return présentateur to vue
    }

    @Test
    fun `Étant donné un profil lorsqu'on ouvre l'écran, on obtient le nom, le nom d'utilisateur et le courriel`() {
        val (_, vue) = ouvrir(FauxModèle(maya))

        assertEquals(Triple("Maya", "maya_t", "maya@exemple.ca"), vue.profil)
    }

    @Test
    fun `Étant donné aucun profil lorsqu'on ouvre l'écran, on obtient Invité`() {
        val (_, vue) = ouvrir(FauxModèle(null))

        assertTrue(vue.invité)
    }

    @Test
    fun `Étant donné des parties jouées lorsqu'on ouvre l'écran, on obtient les statistiques`() {
        val (_, vue) = ouvrir(FauxModèle(maya))

        assertEquals(Triple(3, 4, 78), vue.stats)
    }

    @Test
    fun `Étant donné un profil lorsqu'on touche Modifier, on obtient la feuille avec le nom actuel`() {
        val (présentateur, vue) = ouvrir(FauxModèle(maya))

        présentateur.surModifierClique()

        assertEquals("Maya", vue.modificationOuverteAvec)
    }

    @Test
    fun `Étant donné un nouveau nom lorsqu'on l'enregistre, on obtient la feuille fermée et le nouveau nom affiché`() {
        val (présentateur, vue) = ouvrir(FauxModèle(maya))

        présentateur.surNomEnregistré("Maya T.")

        assertTrue(vue.modificationFermée)
        assertEquals("Maya T.", vue.profil?.first)
    }

    @Test
    fun `Étant donné un nom vide lorsqu'on l'enregistre, on obtient nom invalide et la feuille reste ouverte`() {
        val (présentateur, vue) = ouvrir(FauxModèle(maya))

        présentateur.surNomEnregistré("   ")

        assertTrue(vue.nomInvalide)
        assertFalse(vue.modificationFermée)
    }

    @Test
    fun `Étant donné une photo choisie lorsqu'on la reçoit, on obtient la photo enregistrée et affichée`() {
        val modèle = FauxModèle(maya)
        val (présentateur, vue) = ouvrir(modèle)
        val photo = byteArrayOf(1, 2, 3)

        présentateur.surPhotoChoisie(photo)

        assertArrayEquals(photo, modèle.photo)
        assertArrayEquals(photo, vue.photo)
        assertTrue(vue.modificationFermée)
    }

    @Test
    fun `Étant donné une photo lorsqu'on la retire, on obtient plus de photo`() {
        val modèle = FauxModèle(maya, photo = byteArrayOf(1, 2, 3))
        val (présentateur, vue) = ouvrir(modèle)

        présentateur.surPhotoRetirée()

        assertNull(modèle.photo)
        assertNull(vue.photo)
    }

    @Test
    fun `Étant donné un profil lorsqu'on se déconnecte, on obtient le profil effacé et l'écran Bienvenue`() {
        val modèle = FauxModèle(maya)
        val (présentateur, vue) = ouvrir(modèle)

        présentateur.surDéconnexionClique()

        assertTrue(modèle.déconnecté)
        assertTrue(vue.bienvenue)
    }

    @Test
    fun `Étant donné un modèle en panne lorsqu'on ouvre l'écran, on obtient un message d'erreur`() {
        val (_, vue) = ouvrir(FauxModèle(maya, enPanne = true))

        assertTrue(vue.erreur)
    }

    @Test
    fun `Étant donné l'écran Profil lorsqu'on touche les lignes et la flèche, on obtient le bon écran`() {
        val (présentateur, vue) = ouvrir(FauxModèle(maya))

        présentateur.surMesQuizClique()
        présentateur.surLangueClique()
        présentateur.surÀProposClique()
        présentateur.surRetourClique()

        assertTrue(vue.mesQuiz)
        assertTrue(vue.langue)
        assertTrue(vue.àPropos)
        assertTrue(vue.fermée)
    }
}