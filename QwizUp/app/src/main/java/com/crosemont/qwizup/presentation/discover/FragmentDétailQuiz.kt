package com.crosemont.qwizup.presentation.discover

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.discover.ContratDécouvrir.MeilleurScore
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IPrésentateurDétailQuiz
import com.crosemont.qwizup.presentation.discover.ContratDétailQuiz.IVueDétailQuiz
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Vue de l'écran Détail du quiz.
 * Elle transmet les clics au présentateur et affiche ce qu'il lui envoie.
 */
class FragmentDétailQuiz : Fragment(R.layout.fragment_detail_quiz), IVueDétailQuiz {

    private lateinit var présentateur: IPrésentateurDétailQuiz

    private lateinit var couverture: View
    private lateinit var icône: ImageView
    private lateinit var texteCatégorie: TextView
    private lateinit var texteTitre: TextView
    private lateinit var texteNbQuestions: TextView
    private lateinit var texteDurée: TextView
    private lateinit var texteDescription: TextView
    private lateinit var bandeauMeilleurScore: View
    private lateinit var texteMeilleurScore: TextView
    private lateinit var boutonFavori: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        présentateur = FabriqueDétailQuiz.créerPrésentateur(requireContext().filesDir)

        couverture = view.findViewById(R.id.couverture)
        icône = view.findViewById(R.id.icone)
        texteCatégorie = view.findViewById(R.id.text_categorie)
        texteTitre = view.findViewById(R.id.text_titre)
        texteNbQuestions = view.findViewById(R.id.text_nb_questions)
        texteDurée = view.findViewById(R.id.text_duree)
        texteDescription = view.findViewById(R.id.text_description)
        bandeauMeilleurScore = view.findViewById(R.id.bandeau_meilleur_score)
        texteMeilleurScore = view.findViewById(R.id.text_meilleur_score)
        boutonFavori = view.findViewById(R.id.button_favori)

        // Chaque clic est transmis au présentateur
        view.findViewById<View>(R.id.button_retour).setOnClickListener { présentateur.surRetourClique() }
        boutonFavori.setOnClickListener { présentateur.surFavoriClique() }
        view.findViewById<View>(R.id.button_jouer).setOnClickListener { présentateur.surJouerClique() }

        présentateur.attacherVue(this)
        présentateur.chargerQuiz(arguments?.getInt("quizId", -1) ?: -1)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueDétailQuiz -----

    override fun afficherQuiz(
        catégorie: CatégorieQuiz, titre: String, description: String, nbQuestions: Int, duréeMinutes: Int
    ) {
        val couleur = ContextCompat.getColor(requireContext(), couleurCatégorie(catégorie))
        couverture.setBackgroundColor(couleur)
        icône.setImageResource(icôneCatégorie(catégorie))
        texteCatégorie.setText(nomCatégorie(catégorie))
        texteCatégorie.setTextColor(couleur)
        texteTitre.text = titre
        texteNbQuestions.text = getString(R.string.detail_nb_questions, nbQuestions)
        texteDurée.text = getString(R.string.detail_duree, duréeMinutes)
        texteDescription.text = description
        texteDescription.isVisible = description.isNotBlank()
    }

    override fun afficherFavori(estFavori: Boolean) {
        if (estFavori) {
            boutonFavori.setIconResource(R.drawable.ic_star_filled)
            boutonFavori.iconTint = null
            boutonFavori.contentDescription = getString(R.string.discover_remove_favorite)
        } else {
            boutonFavori.setIconResource(R.drawable.ic_star_outline)
            boutonFavori.iconTint = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.encre))
            boutonFavori.contentDescription = getString(R.string.discover_add_favorite)
        }
    }

    override fun afficherMeilleurScore(meilleurScore: MeilleurScore?) {
        bandeauMeilleurScore.isVisible = meilleurScore != null
        if (meilleurScore != null) {
            texteMeilleurScore.text =
                getString(R.string.detail_meilleur_score, meilleurScore.score, meilleurScore.total)
        }
    }

    override fun ouvrirJeu(quizId: Int) {
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_detail_vers_question, arguments)
    }

    override fun afficherQuizBientôt(titre: String) {
        afficherMessage(getString(R.string.discover_play_soon, titre))
    }

    override fun fermer() {
        findNavController().popBackStack()
    }

    override fun afficherErreur() {
        afficherMessage(getString(R.string.detail_erreur))
    }

    private fun afficherMessage(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage(message)
            .setPositiveButton(R.string.discover_ok, null)
            .show()
    }

    private fun nomCatégorie(catégorie: CatégorieQuiz): Int = when (catégorie) {
        CatégorieQuiz.SCIENCES -> R.string.category_sciences
        CatégorieQuiz.HISTOIRE -> R.string.category_history
        CatégorieQuiz.LANGUES -> R.string.category_languages
        CatégorieQuiz.GÉOGRAPHIE -> R.string.category_geography
        CatégorieQuiz.INFORMATIQUE -> R.string.category_informatique
        CatégorieQuiz.AUTRE -> R.string.category_autre
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
}
