package com.crosemont.qwizup.presentation.discover

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePresentateur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class DiscoverFragment : Fragment(), ContratVuePresentateur.IVueDiscover {

    private lateinit var presentateur: ContratVuePresentateur.IPresentateurDiscover
    private lateinit var messageVide: TextView

    // Les cartes du XML
    private lateinit var cartes: List<MaterialCardView>
    private lateinit var categories: List<TextView>
    private lateinit var titres: List<TextView>
    private lateinit var descriptions: List<TextView>
    private lateinit var details: List<TextView>
    private lateinit var boutonsFavori: List<ImageButton>
    private lateinit var boutonsJouer: List<View>

    // Les puces de catégorie (null = « Tous »)
    private lateinit var puces: Map<CategorieQuiz?, Chip>

    private val quizIds = IntArray(NB_CARTES)
    private var prochaineCarte = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_discover, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageVide = view.findViewById(R.id.messageVide)

        cartes = listOf(R.id.carte1, R.id.carte2, R.id.carte3, R.id.carte4)
            .map { view.findViewById<MaterialCardView>(it) }
        categories = listOf(R.id.categorie1, R.id.categorie2, R.id.categorie3, R.id.categorie4)
            .map { view.findViewById<TextView>(it) }
        titres = listOf(R.id.titre1, R.id.titre2, R.id.titre3, R.id.titre4)
            .map { view.findViewById<TextView>(it) }
        descriptions = listOf(R.id.description1, R.id.description2, R.id.description3, R.id.description4)
            .map { view.findViewById<TextView>(it) }
        details = listOf(R.id.details1, R.id.details2, R.id.details3, R.id.details4)
            .map { view.findViewById<TextView>(it) }
        boutonsFavori = listOf(R.id.favori1, R.id.favori2, R.id.favori3, R.id.favori4)
            .map { view.findViewById<ImageButton>(it) }
        boutonsJouer = listOf(R.id.jouer1, R.id.jouer2, R.id.jouer3, R.id.jouer4)
            .map { view.findViewById<View>(it) }

        puces = mapOf(
            null to view.findViewById<Chip>(R.id.chip_all),
            CategorieQuiz.SCIENCES to view.findViewById<Chip>(R.id.chip_sciences),
            CategorieQuiz.HISTOIRE to view.findViewById<Chip>(R.id.chip_history),
            CategorieQuiz.LANGUES to view.findViewById<Chip>(R.id.chip_languages),
            CategorieQuiz.INFORMATIQUE to view.findViewById<Chip>(R.id.chip_informatique),
            CategorieQuiz.GEOGRAPHIE to view.findViewById<Chip>(R.id.chip_geography)

        )

        presentateur = PresentateurDiscover()

        boutonsFavori.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { presentateur.surFavoriClique(quizIds[index]) }
        }
        boutonsJouer.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { presentateur.surJouerClique(quizIds[index]) }
        }
        puces.forEach { (categorie, puce) ->
            puce.setOnClickListener { presentateur.surCategorieCliquee(categorie) }
        }
        view.findViewById<EditText>(R.id.edit_search).doAfterTextChanged { texte ->
            presentateur.surRechercheModifiee(texte?.toString().orEmpty())
        }

        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    override fun viderCartes() {
        prochaineCarte = 0
        messageVide.visibility = View.GONE
        cartes.forEach { it.visibility = View.GONE }
    }

    override fun ajouterCarteQuiz(
        id: Int,
        categorie: CategorieQuiz,
        titre: String,
        description: String,
        nbQuestions: Int,
        dureeMinutes: Int,
        estFavori: Boolean
    ) {
        if (prochaineCarte >= NB_CARTES) return // Le XML n'a que quatre cartes
        val i = prochaineCarte++

        quizIds[i] = id
        categories[i].setText(nomCategorie(categorie))
        titres[i].text = titre
        descriptions[i].text = description
        details[i].text = getString(R.string.discover_quiz_details, nbQuestions, dureeMinutes)

        // Étoile pleine (orange) si le quiz est dans les favoris, sinon étoile vide
        if (estFavori) {
            boutonsFavori[i].setImageResource(R.drawable.ic_star_filled)
            boutonsFavori[i].contentDescription = getString(R.string.discover_remove_favorite)
        } else {
            boutonsFavori[i].setImageResource(R.drawable.ic_star_outline)
            boutonsFavori[i].contentDescription = getString(R.string.discover_add_favorite)
        }

        cartes[i].visibility = View.VISIBLE
    }

    override fun afficherAucunResultat() {
        messageVide.visibility = View.VISIBLE
    }

    override fun afficherCategorieChoisie(categorie: CategorieQuiz?) {
        val contexte = requireContext()
        puces.forEach { (categoriePuce, puce) ->
            val choisie = categoriePuce == categorie
            val fond = if (choisie) R.color.text_primary else R.color.surface
            val texte = if (choisie) R.color.white else R.color.text_primary
            val contour = if (choisie) R.color.text_primary else R.color.border

            puce.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(contexte, fond))
            puce.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(contexte, contour))
            puce.setTextColor(ContextCompat.getColor(contexte, texte))
        }
    }

    override fun ouvrirQuiz(quizId: Int, titre: String) {
        // Ouvre l'écran de jeu et lui envoie le numéro du quiz
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_decouvrir_vers_question, arguments)
    }

    override fun afficherQuizBientot(titre: String) {
        Toast.makeText(requireContext(), getString(R.string.discover_play_soon, titre), Toast.LENGTH_SHORT).show()
    }

    override fun afficherChargement(visible: Boolean) {
        view?.findViewById<View>(R.id.progression)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    override fun afficherErreur(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    @StringRes
    private fun nomCategorie(categorie: CategorieQuiz): Int = when (categorie) {
        CategorieQuiz.SCIENCES -> R.string.category_sciences
        CategorieQuiz.HISTOIRE -> R.string.category_history
        CategorieQuiz.LANGUES -> R.string.category_languages
        CategorieQuiz.GEOGRAPHIE -> R.string.category_geography
        CategorieQuiz.INFORMATIQUE -> R.string.category_informatique
        CategorieQuiz.AUTRE -> R.string.category_autre
    }

    companion object {
        private const val NB_CARTES = 4
    }
}
