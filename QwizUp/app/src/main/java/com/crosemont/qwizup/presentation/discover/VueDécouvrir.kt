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
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip

class VueDécouvrir : Fragment(), ContratDécouvrir.IVueDécouvrir {


    private lateinit var présentateur: ContratDécouvrir.IPrésentateurDécouvrir
    private lateinit var messageVide: TextView

    // Les cartes du XML
    private lateinit var cartes: List<MaterialCardView>
    private lateinit var catégories: List<TextView>
    private lateinit var titres: List<TextView>
    private lateinit var descriptions: List<TextView>
    private lateinit var détails: List<TextView>
    private lateinit var boutonsFavori: List<ImageButton>
    private lateinit var boutonsJouer: List<View>

    // Les puces de catégorie (null = « Tous »)
    private lateinit var puces: Map<CatégorieQuiz?, Chip>

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
        catégories = listOf(R.id.categorie1, R.id.categorie2, R.id.categorie3, R.id.categorie4)
            .map { view.findViewById<TextView>(it) }
        titres = listOf(R.id.titre1, R.id.titre2, R.id.titre3, R.id.titre4)
            .map { view.findViewById<TextView>(it) }
        descriptions =
            listOf(R.id.description1, R.id.description2, R.id.description3, R.id.description4)
                .map { view.findViewById<TextView>(it) }
        détails = listOf(R.id.details1, R.id.details2, R.id.details3, R.id.details4)
            .map { view.findViewById<TextView>(it) }
        boutonsFavori = listOf(R.id.favori1, R.id.favori2, R.id.favori3, R.id.favori4)
            .map { view.findViewById<ImageButton>(it) }
        boutonsJouer = listOf(R.id.jouer1, R.id.jouer2, R.id.jouer3, R.id.jouer4)
            .map { view.findViewById<View>(it) }

        puces = mapOf(
            null to view.findViewById<Chip>(R.id.chip_all),
            CatégorieQuiz.SCIENCES to view.findViewById<Chip>(R.id.chip_sciences),
            CatégorieQuiz.HISTOIRE to view.findViewById<Chip>(R.id.chip_history),
            CatégorieQuiz.LANGUES to view.findViewById<Chip>(R.id.chip_languages),
            CatégorieQuiz.INFORMATIQUE to view.findViewById<Chip>(R.id.chip_informatique),
            CatégorieQuiz.GÉOGRAPHIE to view.findViewById<Chip>(R.id.chip_geography)

        )

        présentateur = FabriqueDécouvrir.créerPrésentateur(requireContext().filesDir)

        boutonsFavori.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { présentateur.basculerFavori(quizIds[index]) }
        }
        boutonsJouer.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { présentateur.jouer(quizIds[index]) }
        }
        puces.forEach { (catégorie, puce) ->
            puce.setOnClickListener { présentateur.choisirCatégorie(catégorie) }
        }
        view.findViewById<EditText>(R.id.edit_search).doAfterTextChanged { texte ->
            présentateur.rechercher(texte?.toString().orEmpty())
        }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    override fun viderQuiz() {
        prochaineCarte = 0
        messageVide.visibility = View.GONE
        cartes.forEach { it.visibility = View.GONE }
    }

    override fun ajouterQuiz(
        id: Int,
        catégorie: CatégorieQuiz,
        titre: String,
        description: String,
        nbQuestions: Int,
        duréeMinutes: Int,
        estFavori: Boolean
    ) {
        if (prochaineCarte >= NB_CARTES) return // Le XML n'a que quatre cartes
        val i = prochaineCarte++

        quizIds[i] = id
        catégories[i].setText(nomCatégorie(catégorie))
        titres[i].text = titre
        descriptions[i].text = description
        détails[i].text = getString(R.string.discover_quiz_details, nbQuestions, duréeMinutes)

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

    override fun afficherAucunRésultat() {
        messageVide.visibility = View.VISIBLE
    }

    override fun afficherCatégorieChoisie(catégorie: CatégorieQuiz?) {
        val contexte = requireContext()
        puces.forEach { (catégoriePuce, puce) ->
            val choisie = catégoriePuce == catégorie
            val fond = if (choisie) R.color.text_primary else R.color.surface
            val texte = if (choisie) R.color.white else R.color.text_primary
            val contour = if (choisie) R.color.text_primary else R.color.border

            puce.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(contexte, fond))
            puce.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(contexte, contour))
            puce.setTextColor(ContextCompat.getColor(contexte, texte))
        }
    }

    override fun ouvrirQuiz(quizId: Int, titre: String) {
        // Ouvre l'écran de jeu et lui envoie le numéro du quiz
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_decouvrir_vers_question, arguments)
    }

    override fun afficherQuizBientôt(titre: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.discover_play_soon, titre),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun afficherErreur() {
        Toast.makeText(requireContext(), R.string.discover_error, Toast.LENGTH_SHORT).show()
    }

    override fun afficherChargement(visible: Boolean) {
        view?.findViewById<View>(R.id.progression)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    @StringRes
    private fun nomCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.string.category_sciences
        CatégorieQuiz.HISTOIRE -> R.string.category_history
        CatégorieQuiz.LANGUES -> R.string.category_languages
        CatégorieQuiz.GÉOGRAPHIE -> R.string.category_geography
        CatégorieQuiz.INFORMATIQUE -> R.string.category_informatique
        CatégorieQuiz.AUTRE -> R.string.category_autre
    }

    companion object {
        private const val NB_CARTES = 4
    }
}
