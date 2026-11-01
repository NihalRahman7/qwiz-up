package com.crosemont.qwizup.presentation.create

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePresentateur.CategorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePresentateur.Difficulte
import com.crosemont.qwizup.presentation.ContratVuePresentateur.ErreurEditeur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IPresentateurEditeur
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IVueEditeur
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class EditeurQuizFragment : Fragment(R.layout.fragment_editeur_quiz), IVueEditeur {

    private val presentateur: IPresentateurEditeur = PresentateurEditeur()

    private lateinit var texteTitreEcran: TextView
    private lateinit var texteStatut: TextView
    private lateinit var champTitre: TextInputLayout
    private lateinit var saisieTitre: TextInputEditText
    private lateinit var saisieDescription: TextInputEditText
    private lateinit var groupeCategories: ChipGroup
    private lateinit var groupeDifficulte: MaterialButtonToggleGroup
    private lateinit var listeQuestions: LinearLayout
    private lateinit var texteNombreQuestions: TextView

    // Quel élément du XML correspond à quelle valeur
    private val chipsCategories = mapOf(
        CategorieQuiz.SCIENCES to R.id.chip_editeur_sciences,
        CategorieQuiz.HISTOIRE to R.id.chip_editeur_histoire,
        CategorieQuiz.LANGUES to R.id.chip_editeur_langues,
        CategorieQuiz.GEOGRAPHIE to R.id.chip_editeur_geographie
    )
    private val boutonsDifficulte = mapOf(
        Difficulte.FACILE to R.id.button_facile,
        Difficulte.MOYEN to R.id.button_moyen,
        Difficulte.DIFFICILE to R.id.button_difficile
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texteTitreEcran = view.findViewById(R.id.text_titre_ecran)
        texteStatut = view.findViewById(R.id.text_statut)
        champTitre = view.findViewById(R.id.champ_titre)
        saisieTitre = view.findViewById(R.id.saisie_titre)
        saisieDescription = view.findViewById(R.id.saisie_description)
        groupeCategories = view.findViewById(R.id.groupe_categories)
        groupeDifficulte = view.findViewById(R.id.groupe_difficulte)
        listeQuestions = view.findViewById(R.id.liste_questions)
        texteNombreQuestions = view.findViewById(R.id.text_nombre_questions)

        view.findViewById<ImageButton>(R.id.button_retour)
            .setOnClickListener { findNavController().popBackStack() }

        saisieTitre.doAfterTextChanged { presentateur.surTitreModifie(it?.toString().orEmpty()) }
        saisieDescription.doAfterTextChanged { presentateur.surDescriptionModifiee(it?.toString().orEmpty()) }

        chipsCategories.forEach { (categorie, id) ->
            view.findViewById<Chip>(id).setOnClickListener { presentateur.surCategorieCliquee(categorie) }
        }
        boutonsDifficulte.forEach { (difficulte, id) ->
            view.findViewById<MaterialButton>(id).setOnClickListener { presentateur.surDifficulteCliquee(difficulte) }
        }

        view.findViewById<MaterialButton>(R.id.button_prendre_photo)
            .setOnClickListener { presentateur.surPrendrePhotoClique() }
        view.findViewById<MaterialButton>(R.id.button_galerie)
            .setOnClickListener { presentateur.surGalerieClique() }
        view.findViewById<MaterialButton>(R.id.button_ajouter_question)
            .setOnClickListener { presentateur.surAjouterQuestionClique() }
        view.findViewById<MaterialButton>(R.id.button_sauvegarder)
            .setOnClickListener { presentateur.surSauvegarderClique() }
        view.findViewById<MaterialButton>(R.id.button_publier)
            .setOnClickListener { presentateur.surPublierClique() }

        // La barre du bas de l'app est cachée ici : on garde nos boutons au-dessus de la barre système
        val barreActions = view.findViewById<LinearLayout>(R.id.barre_actions)
        val paddingBas = barreActions.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(barreActions) { v, insets ->
            v.updatePadding(bottom = paddingBas + insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom)
            insets
        }

        presentateur.attacherVue(this)

        // -1 = nouveau quiz (voir nav_graph.xml)
        val quizId = arguments?.getInt("quizId", -1) ?: -1
        presentateur.chargerQuiz(if (quizId == -1) null else quizId)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueEditeur -----

    override fun afficherEnTete(nouveauQuiz: Boolean, estPublie: Boolean) {
        texteTitreEcran.setText(if (nouveauQuiz) R.string.editeur_titre_nouveau else R.string.editeur_titre_modifier)
        if (estPublie) {
            texteStatut.setText(R.string.editeur_badge_publie)
            texteStatut.setBackgroundResource(R.drawable.bg_badge_publie)
            texteStatut.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_text))
        } else {
            texteStatut.setText(R.string.editeur_badge_brouillon)
            texteStatut.setBackgroundResource(R.drawable.bg_badge_brouillon)
            texteStatut.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_text))
        }
    }

    override fun afficherInfosQuiz(titre: String, description: String) {
        saisieTitre.setText(titre)
        saisieDescription.setText(description)
    }

    override fun afficherCategorieChoisie(categorie: CategorieQuiz) {
        groupeCategories.check(chipsCategories.getValue(categorie))
    }

    override fun afficherDifficulteChoisie(difficulte: Difficulte) {
        groupeDifficulte.check(boutonsDifficulte.getValue(difficulte))
    }

    override fun viderQuestions() {
        listeQuestions.removeAllViews()
    }

    override fun ajouterQuestion(numero: Int, texte: String, estVraiFaux: Boolean, secondes: Int, doublePoints: Boolean) {
        val ligne = layoutInflater.inflate(R.layout.item_question_editeur, listeQuestions, false)
        val type = getString(if (estVraiFaux) R.string.editeur_type_vrai_faux else R.string.editeur_type_choix_multiple)
        val points = getString(if (doublePoints) R.string.editeur_points_double else R.string.editeur_points_standard)

        ligne.findViewById<TextView>(R.id.text_numero).text = numero.toString()
        ligne.findViewById<TextView>(R.id.text_question).text = texte
        ligne.findViewById<TextView>(R.id.text_details).text =
            getString(R.string.editeur_details_question, type, secondes, points)
        ligne.findViewById<ImageButton>(R.id.button_modifier_question).apply {
            contentDescription = getString(R.string.editeur_modifier_question, numero)
            setOnClickListener { presentateur.surModifierQuestionClique(numero) }
        }
        listeQuestions.addView(ligne)
    }

    override fun afficherAucuneQuestion() {
        val texte = TextView(requireContext()).apply {
            setText(R.string.editeur_aucune_question)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            setPadding(0, 24, 0, 8)
        }
        listeQuestions.addView(texte)
    }

    override fun afficherNombreQuestions(nombre: Int) {
        texteNombreQuestions.text = resources.getQuantityString(R.plurals.editeur_nombre_questions, nombre, nombre)
    }

    override fun afficherErreur(erreur: ErreurEditeur) {
        when (erreur) {
            ErreurEditeur.TITRE_VIDE -> {
                champTitre.error = getString(R.string.editeur_erreur_titre)
                saisieTitre.requestFocus()
            }
            ErreurEditeur.AUCUNE_QUESTION ->
                Toast.makeText(requireContext(), R.string.editeur_erreur_questions, Toast.LENGTH_SHORT).show()
        }
    }

    override fun effacerErreurs() {
        champTitre.error = null
    }

    override fun ouvrirAppareilPhoto() {
        // TODO: ouvrir l'appareil photo (Intent vers une autre app)
        Toast.makeText(requireContext(), R.string.editeur_photo_bientot, Toast.LENGTH_SHORT).show()
    }

    override fun ouvrirGalerie() {
        // TODO: ouvrir la galerie
        Toast.makeText(requireContext(), R.string.editeur_photo_bientot, Toast.LENGTH_SHORT).show()
    }

    override fun ouvrirEditeurQuestion(numero: Int?) {
        // TODO: naviguer vers le fragment « Ajouter une question »
        Toast.makeText(requireContext(), R.string.editeur_question_bientot, Toast.LENGTH_SHORT).show()
    }

    override fun confirmerSauvegarde() {
        Toast.makeText(requireContext(), R.string.editeur_sauvegarde_ok, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun confirmerPublication(titre: String) {
        Toast.makeText(requireContext(), getString(R.string.editeur_publie_ok, titre), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
}
