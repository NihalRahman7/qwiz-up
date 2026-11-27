import com.crosemont.qwizup.domaine.service.ServiceException
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IModèleInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IVueInscription
import com.crosemont.qwizup.presentation.onboarding.PrésentateurInscription
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Écran Inscription : le bouton s'active seulement quand tout est valide,
 * le ✓ vert apparaît quand les mots de passe sont identiques,
 * le profil est créé, et chaque bouton ouvre le bon écran.
 */
class PrésentateurInscriptionTest {

    // Fausse vue : retient ce que le présentateur lui a demandé
    private class FausseVue : IVueInscription {
        var boutonActif: Boolean? = null
        var identiques: Boolean? = null
        var accueilOuvert = false
        var connexionOuverte = false
        var fermée = false
        var erreur = false

        override fun activerCréation(actif: Boolean) {
            boutonActif = actif
        }

        override fun afficherMotsDePasseIdentiques(identiques: Boolean) {
            this.identiques = identiques
        }

        override fun ouvrirAccueil() {
            accueilOuvert = true
        }

        override fun ouvrirConnexion() {
            connexionOuverte = true
        }

        override fun fermer() {
            fermée = true
        }

        override fun afficherErreur() {
            erreur = true
        }
    }

    // Faux modèle : retient le profil créé, peut être « en panne »
    private class FauxModèle(private val enPanne: Boolean = false) : IModèleInscription {
        var profilCréé: Pair<String, String>? = null

        override suspend fun créerProfil(nomUtilisateur: String, courriel: String) {
            if (enPanne) throw ServiceException("Impossible de sauvegarder le profil")
            profilCréé = nomUtilisateur to courriel
        }
    }

    private lateinit var vue: FausseVue
    private lateinit var modèle: FauxModèle
    private lateinit var présentateur: PrésentateurInscription

    @Before
    fun préparer() {
        vue = FausseVue()
        modèle = FauxModèle()
        présentateur = PrésentateurInscription(modèle, Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateur.attacherVue(vue)
    }

    @Test
    fun `Étant donné l'écran Inscription lorsqu'on l'ouvre, on obtient le bouton désactivé`() {
        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné des champs tous valides lorsqu'on tape, on obtient le bouton activé et le crochet vert`() {
        présentateur.surChampsModifiés("maya_t", "maya@exemple.ca", "motdepasse1", "motdepasse1")

        assertEquals(true, vue.boutonActif)
        assertEquals(true, vue.identiques)
    }

    @Test
    fun `Étant donné un courriel sans arobase lorsqu'on tape, on obtient le bouton désactivé`() {
        présentateur.surChampsModifiés("maya_t", "maya.exemple.ca", "motdepasse1", "motdepasse1")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné un mot de passe de moins de 8 caractères lorsqu'on tape, on obtient le bouton désactivé`() {
        présentateur.surChampsModifiés("maya_t", "maya@exemple.ca", "mdp1", "mdp1")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné un mot de passe sans chiffre lorsqu'on tape, on obtient le bouton désactivé`() {
        présentateur.surChampsModifiés("maya_t", "maya@exemple.ca", "motdepasse", "motdepasse")

        assertEquals(false, vue.boutonActif)
    }

    @Test
    fun `Étant donné une confirmation différente lorsqu'on tape, on obtient le bouton désactivé sans crochet vert`() {
        présentateur.surChampsModifiés("maya_t", "maya@exemple.ca", "motdepasse1", "motdepasse2")

        assertEquals(false, vue.boutonActif)
        assertEquals(false, vue.identiques)
    }

    @Test
    fun `Étant donné des champs valides lorsqu'on touche Créer mon compte, on obtient l'Accueil`() {
        présentateur.surCréerCompteClique("maya_t", "maya@exemple.ca", "motdepasse1")

        assertTrue(vue.accueilOuvert)
    }

    @Test
    fun `Étant donné des champs valides lorsqu'on touche Créer mon compte, on obtient le profil créé avec le nom et le courriel`() {
        présentateur.surCréerCompteClique("maya_t", "maya@exemple.ca", "motdepasse1")

        assertEquals("maya_t" to "maya@exemple.ca", modèle.profilCréé)
    }

    @Test
    fun `Étant donné une sauvegarde impossible lorsqu'on touche Créer mon compte, on obtient une erreur et on reste sur l'écran`() {
        val vueEnPanne = FausseVue()
        val présentateurEnPanne =
            PrésentateurInscription(FauxModèle(enPanne = true), Dispatchers.Unconfined, Dispatchers.Unconfined)
        présentateurEnPanne.attacherVue(vueEnPanne)

        présentateurEnPanne.surCréerCompteClique("maya_t", "maya@exemple.ca", "motdepasse1")

        assertTrue(vueEnPanne.erreur)
        assertFalse(vueEnPanne.accueilOuvert)
    }

    @Test
    fun `Étant donné l'écran Inscription lorsqu'on touche Me connecter, on obtient l'écran Connexion`() {
        présentateur.surDéjàCompteClique()

        assertTrue(vue.connexionOuverte)
    }

    @Test
    fun `Étant donné l'écran Inscription lorsqu'on touche la flèche retour, on obtient l'écran fermé`() {
        présentateur.surRetourClique()

        assertTrue(vue.fermée)
    }
}