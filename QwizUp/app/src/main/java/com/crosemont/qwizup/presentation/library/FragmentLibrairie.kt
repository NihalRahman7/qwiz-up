package com.crosemont.qwizup.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.TypeBadge
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView

/**
 * Vue de l'écran Librairie.
 * Elle transmet les clics au présentateur et affiche les cartes qu'il lui envoie.
 */
class FragmentLibrairie : Fragment(), ContratLibrairie.IVueLibrary {

    private lateinit var présentateur: ContratLibrairie.IPrésentateurLibrary
    private lateinit var messageVide: TextView

    // Les cartes du XML
    private lateinit var cartes: List<MaterialCardView>
    private lateinit var titres: List<TextView>
    private lateinit var sousTitres: List<TextView>
    private lateinit var cartesBadges: List<MaterialCardView>
    private lateinit var textesBadges: List<TextView>

    // Le carré de la catégorie, et la ligne du bas de « Mes quiz » (code et Modifier)
    private lateinit var carrésCatégorie: List<MaterialCardView>
    private lateinit var icônesCatégorie: List<ImageView>
    private lateinit var lignesCréé: List<View>
    private lateinit var textesCode: List<TextView>
    private lateinit var boutonsModifier: List<View>

    private val quizIds = IntArray(NB_CARTES)
    private var prochaineCarte = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageVide = view.findViewById(R.id.messageVide)

        cartes = listOf(R.id.carte1, R.id.carte2, R.id.carte3)
            .map { view.findViewById<MaterialCardView>(it) }
        titres = listOf(R.id.titre1, R.id.titre2, R.id.titre3)
            .map { view.findViewById<TextView>(it) }
        sousTitres = listOf(R.id.soustitre1, R.id.soustitre2, R.id.soustitre3)
            .map { view.findViewById<TextView>(it) }
        cartesBadges = listOf(R.id.badge1, R.id.badge2, R.id.badge3)
            .map { view.findViewById<MaterialCardView>(it) }
        textesBadges = listOf(R.id.txtBadge1, R.id.txtBadge2, R.id.txtBadge3)
            .map { view.findViewById<TextView>(it) }
        carrésCatégorie = listOf(R.id.carre_categorie1, R.id.carre_categorie2, R.id.carre_categorie3)
            .map { view.findViewById<MaterialCardView>(it) }
        icônesCatégorie = listOf(R.id.icone_categorie1, R.id.icone_categorie2, R.id.icone_categorie3)
            .map { view.findViewById<ImageView>(it) }
        lignesCréé = listOf(R.id.ligne_cree1, R.id.ligne_cree2, R.id.ligne_cree3)
            .map { view.findViewById<View>(it) }
        textesCode = listOf(R.id.text_code1, R.id.text_code2, R.id.text_code3)
            .map { view.findViewById<TextView>(it) }
        boutonsModifier = listOf(R.id.button_modifier1, R.id.button_modifier2, R.id.button_modifier3)
            .map { view.findViewById<View>(it) }

        présentateur = FabriqueLibrairie.creerPresentateur(requireContext().filesDir)

        // Chaque clic est transmis au présentateur
        cartes.forEachIndexed { index, carte ->
            carte.setOnClickListener {
                présentateur.surQuizClique(quizIds[index], titres[index].text.toString())
            }
        }
        boutonsModifier.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { présentateur.surModifierClique(quizIds[index]) }
        }

        view.findViewById<MaterialButtonToggleGroup>(R.id.tabs)
            .addOnButtonCheckedListener { _, btnId, estCoche ->
                if (estCoche) {
                    when (btnId) {
                        R.id.btnFavoris -> présentateur.surOngletClique(OngletLibrairie.FAVORIS)
                        R.id.btnComplets -> présentateur.surOngletClique(OngletLibrairie.COMPLÉTÉS)
                        R.id.btnCrees -> présentateur.surOngletClique(OngletLibrairie.CRÉES)
                    }
                }
            }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    override fun viderCartes() {
        prochaineCarte = 0
        messageVide.visibility = View.GONE
        cartes.forEach { it.visibility = View.GONE }
    }

    override fun ajouterCarteQuiz(
        id: Int,
        titre: String,
        sousTitre: String,
        badge: String?,
        typeBadge: TypeBadge,
        catégorie: CatégorieQuiz?,
        code: String?,
        estCréé: Boolean
    ) {
        if (prochaineCarte >= NB_CARTES) return // Le XML n'a que trois cartes
        val i = prochaineCarte++
        val contexte = requireContext()

        quizIds[i] = id
        titres[i].text = titre
        sousTitres[i].text = sousTitre

        // Carré : couleur et icône de la catégorie (violet ▶ si on ne la connaît pas)
        if (catégorie == null) {
            carrésCatégorie[i].setCardBackgroundColor(ContextCompat.getColor(contexte, R.color.primaire))
            icônesCatégorie[i].setImageResource(R.drawable.ic_play)
        } else {
            carrésCatégorie[i].setCardBackgroundColor(ContextCompat.getColor(contexte, couleurCatégorie(catégorie)))
            icônesCatégorie[i].setImageResource(icôneCatégorie(catégorie))
        }

        if (badge == null) {
            cartesBadges[i].visibility = View.GONE
        } else {
            cartesBadges[i].visibility = View.VISIBLE
            textesBadges[i].text = badge
            val (fond, texte) = when (typeBadge) {
                TypeBadge.PROGRESSION -> R.color.primaire_clair to R.color.primaire_fonce
                TypeBadge.NOUVEAU -> R.color.soleil_clair to R.color.soleil_texte
                TypeBadge.SCORE -> R.color.menthe_clair to R.color.menthe_texte
            }
            cartesBadges[i].setCardBackgroundColor(ContextCompat.getColor(contexte, fond))
            textesBadges[i].setTextColor(ContextCompat.getColor(contexte, texte))
        }

        // Mes quiz : le code en violet avec un #, ou « Le code apparaît à la publication »
        lignesCréé[i].isVisible = estCréé
        if (code != null) {
            textesCode[i].text = code
            textesCode[i].setTextColor(ContextCompat.getColor(contexte, R.color.primaire_fonce))
            textesCode[i].setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_diese, 0, 0, 0)
            textesCode[i].textSize = 14f
        } else {
            textesCode[i].setText(R.string.librairie_code_a_venir)
            textesCode[i].setTextColor(ContextCompat.getColor(contexte, R.color.encre_secondaire))
            textesCode[i].setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            textesCode[i].textSize = 13f
        }

        cartes[i].visibility = View.VISIBLE
    }

    override fun afficherMessageVide(message: String) {
        messageVide.text = message
        messageVide.visibility = View.VISIBLE
    }

    override fun ouvrirDétails(quizId: Int, titre: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.librairie_quiz_soon, titre),
            Toast.LENGTH_SHORT
        ).show()
    }

    // Ouvre l'éditeur sur ce quiz (le même que « Continuer » dans Créer)
    override fun ouvrirÉditeur(quizId: Int) {
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_librairie_vers_editeur, arguments)
    }

    private fun couleurCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.color.categorie_sciences
        CatégorieQuiz.HISTOIRE -> R.color.categorie_histoire
        CatégorieQuiz.LANGUES -> R.color.categorie_langues
        CatégorieQuiz.GÉOGRAPHIE -> R.color.categorie_geographie
        CatégorieQuiz.INFORMATIQUE -> R.color.categorie_informatique
        CatégorieQuiz.AUTRE -> R.color.categorie_autre
    }

    private fun icôneCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.drawable.ic_couverture_sciences
        CatégorieQuiz.HISTOIRE -> R.drawable.ic_couverture_histoire
        CatégorieQuiz.LANGUES -> R.drawable.ic_couverture_langues
        CatégorieQuiz.GÉOGRAPHIE -> R.drawable.ic_globe
        CatégorieQuiz.INFORMATIQUE -> R.drawable.ic_couverture_informatique
        CatégorieQuiz.AUTRE -> R.drawable.ic_couverture_autre
    }

    companion object {
        private const val NB_CARTES = 3
    }
}