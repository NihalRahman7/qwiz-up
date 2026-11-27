package com.crosemont.qwizup.presentation.discover

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class VueDécouvrir : Fragment(), ContratDécouvrir.IVueDécouvrir {

    private lateinit var présentateur: ContratDécouvrir.IPrésentateurDécouvrir
    private lateinit var messageVide: TextView

    // Les cartes du XML (toucher une carte ouvre le détail du quiz)
    private lateinit var cartes: List<MaterialCardView>
    private lateinit var catégories: List<TextView>
    private lateinit var titres: List<TextView>
    private lateinit var descriptions: List<TextView>
    private lateinit var détails: List<TextView>
    private lateinit var scores: List<TextView>
    private lateinit var boutonsFavori: List<ImageButton>

    // La couverture de chaque carte : couleur et grande icône de la catégorie
    private lateinit var couvertures: List<View>
    private lateinit var icônes: List<ImageView>

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
        descriptions = listOf(R.id.description1, R.id.description2, R.id.description3, R.id.description4)
            .map { view.findViewById<TextView>(it) }
        détails = listOf(R.id.details1, R.id.details2, R.id.details3, R.id.details4)
            .map { view.findViewById<TextView>(it) }
        scores = listOf(R.id.score1, R.id.score2, R.id.score3, R.id.score4)
            .map { view.findViewById<TextView>(it) }
        boutonsFavori = listOf(R.id.favori1, R.id.favori2, R.id.favori3, R.id.favori4)
            .map { view.findViewById<ImageButton>(it) }
        couvertures = listOf(R.id.couverture1, R.id.couverture2, R.id.couverture3, R.id.couverture4)
            .map { view.findViewById<View>(it) }
        icônes = listOf(R.id.icone1, R.id.icone2, R.id.icone3, R.id.icone4)
            .map { view.findViewById<ImageView>(it) }

        puces = mapOf(
            null to view.findViewById<Chip>(R.id.chip_all),
            CatégorieQuiz.SCIENCES to view.findViewById<Chip>(R.id.chip_sciences),
            CatégorieQuiz.HISTOIRE to view.findViewById<Chip>(R.id.chip_history),
            CatégorieQuiz.LANGUES to view.findViewById<Chip>(R.id.chip_languages),
            CatégorieQuiz.INFORMATIQUE to view.findViewById<Chip>(R.id.chip_informatique),
            CatégorieQuiz.GÉOGRAPHIE to view.findViewById<Chip>(R.id.chip_geography),
            CatégorieQuiz.AUTRE to view.findViewById<Chip>(R.id.chip_autre)
        )

        présentateur = FabriqueDécouvrir.créerPrésentateur(requireContext().filesDir)

        boutonsFavori.forEachIndexed { index, bouton ->
            bouton.setOnClickListener { présentateur.basculerFavori(quizIds[index]) }
        }
        // Toucher une carte ouvre le détail de ce quiz
        cartes.forEachIndexed { index, carte ->
            carte.setOnClickListener { présentateur.ouvrirDétail(quizIds[index]) }
        }
        puces.forEach { (catégorie, puce) ->
            puce.setOnClickListener { présentateur.choisirCatégorie(catégorie) }
        }
        view.findViewById<EditText>(R.id.edit_search).doAfterTextChanged { texte ->
            présentateur.rechercher(texte?.toString().orEmpty())
        }

        // Les cartes restent cachées jusqu'à ce que les quiz arrivent
        cartes.forEach { it.visibility = View.GONE }

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
        estFavori: Boolean,
        meilleurScore: MeilleurScore?
    ) {
        if (prochaineCarte >= NB_CARTES) return // Le XML n'a que quatre cartes
        val i = prochaineCarte++
        val contexte = requireContext()

        quizIds[i] = id
        catégories[i].setText(nomCatégorie(catégorie))
        titres[i].text = titre
        descriptions[i].text = description
        détails[i].text = getString(R.string.discover_quiz_details, nbQuestions, duréeMinutes)

        // Couverture de la couleur de la catégorie, avec sa grande icône pâle
        couvertures[i].setBackgroundColor(ContextCompat.getColor(contexte, couleurCatégorie(catégorie)))
        icônes[i].setImageResource(icôneCatégorie(catégorie))

        // Étoile pleine (jaune) si le quiz est dans les favoris, sinon étoile vide (blanche)
        if (estFavori) {
            boutonsFavori[i].setImageResource(R.drawable.ic_star_filled)
            boutonsFavori[i].imageTintList = null
            boutonsFavori[i].contentDescription = getString(R.string.discover_remove_favorite)
        } else {
            boutonsFavori[i].setImageResource(R.drawable.ic_star_outline)
            boutonsFavori[i].imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(contexte, R.color.white))
            boutonsFavori[i].contentDescription = getString(R.string.discover_add_favorite)
        }

        // Meilleur score en pourcentage (en vert) si le quiz a déjà été joué
        if (meilleurScore != null && meilleurScore.total > 0) {
            val pourcentage = meilleurScore.score * 100 / meilleurScore.total
            scores[i].text = getString(R.string.discover_best_score, pourcentage)
            scores[i].setTextColor(ContextCompat.getColor(contexte, R.color.menthe_texte))
        } else {
            scores[i].setText(R.string.discover_not_played)
            scores[i].setTextColor(ContextCompat.getColor(contexte, R.color.encre_secondaire))
        }

        cartes[i].visibility = View.VISIBLE
    }

    override fun afficherAucunRésultat() {
        messageVide.setText(R.string.discover_no_quiz)
        messageVide.visibility = View.VISIBLE
    }

    override fun afficherAucunFavori() {
        messageVide.setText(R.string.discover_no_favorite)
        messageVide.visibility = View.VISIBLE
    }

    override fun afficherCatégorieChoisie(catégorie: CatégorieQuiz?) {
        puces.forEach { (catégoriePuce, puce) -> colorer(puce, choisie = catégoriePuce == catégorie) }
    }

    // Il n'y a plus de puce Favoris dans Découvrir (les favoris sont dans la Librairie)
    override fun afficherFavorisChoisis() {
        puces.values.forEach { colorer(it, choisie = false) }
    }

    // Puce choisie : fond foncé et texte blanc. Les autres : fond blanc.
    private fun colorer(puce: Chip, choisie: Boolean) {
        val contexte = requireContext()
        val fond = if (choisie) R.color.encre else R.color.white
        val texte = if (choisie) R.color.white else R.color.encre
        val contour = if (choisie) R.color.encre else R.color.bordure

        puce.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(contexte, fond))
        puce.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(contexte, contour))
        puce.setTextColor(ContextCompat.getColor(contexte, texte))
    }

    override fun ouvrirQuiz(quizId: Int, titre: String) {
        // Ouvre l'écran de jeu et lui envoie le numéro du quiz
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_decouvrir_vers_question, arguments)
    }

    // Ouvre l'écran Détail du quiz et lui envoie le numéro du quiz
    override fun ouvrirDétail(quizId: Int) {
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_decouvrir_vers_detail, arguments)
    }

    // Les messages restent affichés jusqu'à ce que l'utilisateur appuie sur OK
    override fun afficherQuizBientôt(titre: String) {
        afficherMessage(getString(R.string.discover_play_soon, titre))
    }

    override fun afficherErreur() {
        afficherMessage(getString(R.string.discover_error))
    }

    override fun afficherChargement(visible: Boolean) {
        view?.findViewById<View>(R.id.indicateurChargement)?.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

    private fun afficherMessage(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.discover_ok, null)
            .show()
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

    // Couleur forte de la couverture, une par catégorie
    @ColorRes
    private fun couleurCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.color.categorie_sciences
        CatégorieQuiz.HISTOIRE -> R.color.categorie_histoire
        CatégorieQuiz.LANGUES -> R.color.categorie_langues
        CatégorieQuiz.GÉOGRAPHIE -> R.color.categorie_geographie
        CatégorieQuiz.INFORMATIQUE -> R.color.categorie_informatique
        CatégorieQuiz.AUTRE -> R.color.categorie_autre
    }

    // Grande icône pâle de la couverture, une par catégorie
    @DrawableRes
    private fun icôneCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.drawable.ic_couverture_sciences
        CatégorieQuiz.HISTOIRE -> R.drawable.ic_couverture_histoire
        CatégorieQuiz.LANGUES -> R.drawable.ic_couverture_langues
        CatégorieQuiz.GÉOGRAPHIE -> R.drawable.ic_globe
        CatégorieQuiz.INFORMATIQUE -> R.drawable.ic_couverture_informatique
        CatégorieQuiz.AUTRE -> R.drawable.ic_couverture_autre
    }

    companion object {
        private const val NB_CARTES = 4
    }
}