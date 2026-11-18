package com.crosemont.qwizup.presentation.create

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.create.ContratCréer.ErreurEditeur
import com.crosemont.qwizup.presentation.create.ContratCréer.IPrésentateurÉditeur
import com.crosemont.qwizup.presentation.create.ContratCréer.IVueÉditeur
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ÉditeurQuizFragment : Fragment(R.layout.fragment_editeur_quiz), IVueÉditeur {

    // La Fabrique assemble source → service → modèle → présentateur
    private val presentateur: IPrésentateurÉditeur = FabriqueCréer.créerPrésentateurÉditeur()

    private lateinit var texteTitreEcran: TextView
    private lateinit var texteNombreQuestions: TextView
    private lateinit var zoneCouverture: LinearLayout
    private lateinit var imageCouverture: ImageView
    private lateinit var champTitre: TextInputLayout
    private lateinit var saisieTitre: TextInputEditText
    private lateinit var saisieDescription: TextInputEditText
    private lateinit var champCategoriePerso: TextInputLayout
    private lateinit var saisieCategoriePerso: TextInputEditText
    private lateinit var groupeCategories: ChipGroup
    private lateinit var groupeDifficulte: MaterialButtonToggleGroup
    private lateinit var groupeAcces: MaterialButtonToggleGroup
    private lateinit var texteAideAcces: TextView
    private lateinit var listeQuestions: LinearLayout
    private lateinit var casePublier: MaterialCheckBox
    private lateinit var boutonEnregistrer: MaterialButton
    private lateinit var texteRaisonBouton: TextView

    // Une carte par question, dans l'ordre
    private val cartesQuestions = mutableListOf<View>()

    // Quel élément du XML correspond à quelle valeur
    private val chipsCategories = mapOf(
        CatégorieQuiz.SCIENCES to R.id.chip_editeur_sciences,
        CatégorieQuiz.HISTOIRE to R.id.chip_editeur_histoire,
        CatégorieQuiz.LANGUES to R.id.chip_editeur_langues,
        CatégorieQuiz.INFORMATIQUE to R.id.chip_editeur_informatique,
        CatégorieQuiz.GÉOGRAPHIE to R.id.chip_editeur_geographie,
        CatégorieQuiz.AUTRE to R.id.chip_editeur_autre
    )
    private val boutonsDifficulte = mapOf(
        Difficulté.FACILE to R.id.button_facile,
        Difficulté.MOYEN to R.id.button_moyen,
        Difficulté.DIFFICILE to R.id.button_difficile
    )
    private val boutonsAcces = mapOf(
        AccèsQuiz.TOUT_LE_MONDE to R.id.button_acces_tous,
        AccèsQuiz.AVEC_CODE to R.id.button_acces_code
    )
    private val boutonsType = mapOf(
        TypeQuestion.CHOIX_MULTIPLE to R.id.button_type_choix,
        TypeQuestion.VRAI_FAUX to R.id.button_type_vrai_faux,
        TypeQuestion.REPONSE_ECRITE to R.id.button_type_ecrite
    )
    private val lignesReponses = listOf(R.id.ligne_reponse_1, R.id.ligne_reponse_2, R.id.ligne_reponse_3, R.id.ligne_reponse_4)
    private val casesReponses = listOf(R.id.case_reponse_1, R.id.case_reponse_2, R.id.case_reponse_3, R.id.case_reponse_4)
    private val saisiesReponses = listOf(R.id.saisie_reponse_1, R.id.saisie_reponse_2, R.id.saisie_reponse_3, R.id.saisie_reponse_4)
    private val lettres = listOf("A", "B", "C", "D")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        texteTitreEcran = view.findViewById(R.id.text_titre_ecran)
        texteNombreQuestions = view.findViewById(R.id.text_nombre_questions)
        zoneCouverture = view.findViewById(R.id.zone_couverture)
        imageCouverture = view.findViewById(R.id.image_couverture)
        champTitre = view.findViewById(R.id.champ_titre)
        saisieTitre = view.findViewById(R.id.saisie_titre)
        saisieDescription = view.findViewById(R.id.saisie_description)
        groupeCategories = view.findViewById(R.id.groupe_categories)
        champCategoriePerso = view.findViewById(R.id.champ_categorie_perso)
        saisieCategoriePerso = view.findViewById(R.id.saisie_categorie_perso)
        groupeDifficulte = view.findViewById(R.id.groupe_difficulte)
        groupeAcces = view.findViewById(R.id.groupe_acces)
        texteAideAcces = view.findViewById(R.id.text_aide_acces)
        listeQuestions = view.findViewById(R.id.liste_questions)

        view.findViewById<ImageButton>(R.id.button_fermer)
            .setOnClickListener { presentateur.surFermerClique() }

        // Le bouton retour du téléphone demande aussi une confirmation
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            presentateur.surFermerClique()
        }

        saisieTitre.doAfterTextChanged { presentateur.surTitreModifie(it?.toString().orEmpty()) }
        saisieDescription.doAfterTextChanged { presentateur.surDescriptionModifiee(it?.toString().orEmpty()) }

        chipsCategories.forEach { (catégorie, id) ->
            view.findViewById<Chip>(id).setOnClickListener { presentateur.surCategorieCliquee(catégorie) }
        }
        saisieCategoriePerso.doAfterTextChanged { presentateur.surCategoriePersoModifiee(it?.toString().orEmpty()) }
        boutonsDifficulte.forEach { (difficulté, id) ->
            view.findViewById<MaterialButton>(id).setOnClickListener { presentateur.surDifficulteCliquee(difficulté) }
        }
        boutonsAcces.forEach { (accès, id) ->
            view.findViewById<MaterialButton>(id).setOnClickListener { presentateur.surAccesClique(accès) }
        }

        view.findViewById<MaterialButton>(R.id.button_ajouter_question)
            .setOnClickListener { presentateur.surAjouterQuestionClique() }
        casePublier = view.findViewById(R.id.case_publier)
        boutonEnregistrer = view.findViewById(R.id.button_enregistrer)
        texteRaisonBouton = view.findViewById(R.id.text_raison_bouton)
        casePublier.setOnCheckedChangeListener { _, cochee -> presentateur.surPublicationCochee(cochee) }
        boutonEnregistrer.setOnClickListener { presentateur.surEnregistrerClique() }

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

    private fun couleur(id: Int) = ContextCompat.getColor(requireContext(), id)

    // ----- IVueÉditeur : infos du quiz -----

    override fun afficherEnTete(nouveauQuiz: Boolean) {
        texteTitreEcran.setText(if (nouveauQuiz) R.string.editeur_titre_nouveau else R.string.editeur_titre_modifier)
    }

    override fun afficherInfosQuiz(titre: String, description: String) {
        saisieTitre.setText(titre)
        saisieDescription.setText(description)
    }

    override fun afficherCategorieChoisie(catégorie: CatégorieQuiz) {
        groupeCategories.check(chipsCategories.getValue(catégorie))
    }

    override fun afficherCategoriePerso(visible: Boolean, nom: String) {
        champCategoriePerso.isVisible = visible
        // On remplit seulement si le texte est différent, pour ne pas déplacer le curseur pendant qu'on écrit
        if (saisieCategoriePerso.text?.toString() != nom) saisieCategoriePerso.setText(nom)
    }

    override fun afficherCouverture(catégorie: CatégorieQuiz) {
        val (image, fond) = when (catégorie) {
            CatégorieQuiz.SCIENCES -> R.drawable.ic_couverture_sciences to R.color.light_green
            CatégorieQuiz.HISTOIRE -> R.drawable.ic_couverture_histoire to R.color.light_orange
            CatégorieQuiz.LANGUES -> R.drawable.ic_couverture_langues to R.color.light_blue
            CatégorieQuiz.INFORMATIQUE -> R.drawable.ic_couverture_informatique to R.color.badge_blue_bg
            CatégorieQuiz.GÉOGRAPHIE -> R.drawable.ic_globe to R.color.badge_green_bg
            CatégorieQuiz.AUTRE -> R.drawable.ic_couverture_autre to R.color.badge_gray_bg
        }
        // L'image de couverture est toujours l'icône de la catégorie
        imageCouverture.setImageResource(image)
        // Change seulement la couleur de fond : le contour pointillé reste
        (zoneCouverture.background.mutate() as GradientDrawable).setColor(couleur(fond))
    }

    override fun afficherDifficulteChoisie(difficulté: Difficulté) {
        groupeDifficulte.check(boutonsDifficulte.getValue(difficulté))
    }

    override fun afficherAccesChoisi(accès: AccèsQuiz) {
        groupeAcces.check(boutonsAcces.getValue(accès))
        texteAideAcces.setText(
            if (accès == AccèsQuiz.AVEC_CODE) R.string.editeur_acces_code_aide else R.string.editeur_acces_tous_aide
        )
    }

    // ----- IVueÉditeur : questions -----

    override fun viderQuestions() {
        listeQuestions.removeAllViews()
        cartesQuestions.clear()
    }

    override fun ajouterQuestion(
        index: Int,
        texte: String,
        type: TypeQuestion,
        réponses: List<String>,
        bonnesRéponses: Set<Int>,
        réponseAttendue: String
    ) {
        val carte = layoutInflater.inflate(R.layout.item_question_editeur, listeQuestions, false)
        val numero = index + 1

        carte.findViewById<TextView>(R.id.text_numero_question).text = getString(R.string.editeur_question_numero, numero)
        carte.findViewById<ImageButton>(R.id.button_supprimer_question).apply {
            contentDescription = getString(R.string.editeur_supprimer_question, numero)
            setOnClickListener { presentateur.surSupprimerQuestionClique(index) }
        }

        // Type de question
        carte.findViewById<MaterialButtonToggleGroup>(R.id.groupe_type).check(boutonsType.getValue(type))
        boutonsType.forEach { (typeBouton, id) ->
            carte.findViewById<MaterialButton>(id).setOnClickListener { presentateur.surTypeQuestionClique(index, typeBouton) }
        }

        // Texte de la question (on remplit avant d'écouter, pour ne pas renvoyer la même valeur)
        val saisieQuestion = carte.findViewById<EditText>(R.id.saisie_question)
        saisieQuestion.setText(texte)
        saisieQuestion.doAfterTextChanged { presentateur.surTexteQuestionModifie(index, it?.toString().orEmpty()) }

        val zoneChoix = carte.findViewById<View>(R.id.zone_choix)
        val zoneEcrite = carte.findViewById<View>(R.id.zone_ecrite)
        zoneChoix.isVisible = type != TypeQuestion.REPONSE_ECRITE
        zoneEcrite.isVisible = type == TypeQuestion.REPONSE_ECRITE
        carte.findViewById<TextView>(R.id.text_coche_bonne).setText(
            if (type == TypeQuestion.CHOIX_MULTIPLE) R.string.editeur_coche_bonnes else R.string.editeur_coche_bonne
        )

        when (type) {
            TypeQuestion.CHOIX_MULTIPLE -> lignesReponses.indices.forEach { i ->
                val saisie = carte.findViewById<EditText>(saisiesReponses[i])
                saisie.hint = getString(R.string.editeur_reponse_lettre, lettres[i])
                saisie.setText(réponses.getOrElse(i) { "" })
                saisie.doAfterTextChanged { presentateur.surReponseModifiee(index, i, it?.toString().orEmpty()) }
                preparerRadio(carte, index, i)
            }
            TypeQuestion.VRAI_FAUX -> lignesReponses.indices.forEach { i ->
                val visible = i < 2
                carte.findViewById<View>(lignesReponses[i]).isVisible = visible
                if (visible) {
                    carte.findViewById<EditText>(saisiesReponses[i]).apply {
                        setText(if (i == 0) R.string.editeur_vrai else R.string.editeur_faux)
                        isEnabled = false   // « Vrai » et « Faux » ne se modifient pas
                    }
                    preparerRadio(carte, index, i)
                }
            }
            TypeQuestion.REPONSE_ECRITE -> {
                val saisie = carte.findViewById<EditText>(R.id.saisie_reponse_attendue)
                saisie.setText(réponseAttendue)
                saisie.doAfterTextChanged { presentateur.surReponseAttendueModifiee(index, it?.toString().orEmpty()) }
            }
        }

        cartesQuestions.add(carte)
        listeQuestions.addView(carte)
        afficherBonnesReponses(index, bonnesRéponses)
    }

    private fun preparerRadio(carte: View, index: Int, numeroReponse: Int) {
        val radio = carte.findViewById<CompoundButton>(casesReponses[numeroReponse])
        radio.contentDescription = getString(R.string.editeur_choisir_bonne, numeroReponse + 1)
        radio.setOnClickListener { presentateur.surBonneReponseCliquee(index, numeroReponse) }
        // Toucher la ligne choisit aussi la réponse (pratique en vrai ou faux)
        carte.findViewById<View>(lignesReponses[numeroReponse])
            .setOnClickListener { presentateur.surBonneReponseCliquee(index, numeroReponse) }
    }

    override fun afficherBonnesReponses(index: Int, bonnesRéponses: Set<Int>) {
        val carte = cartesQuestions.getOrNull(index) ?: return
        lignesReponses.indices.forEach { i ->
            val choisie = i in bonnesRéponses
            carte.findViewById<CompoundButton>(casesReponses[i]).isChecked = choisie
            val fond = carte.findViewById<View>(lignesReponses[i]).background.mutate() as GradientDrawable
            fond.setColor(couleur(if (choisie) R.color.light_green else R.color.surface))
            fond.setStroke(
                (2 * resources.displayMetrics.density).toInt(),
                couleur(if (choisie) R.color.primary_green else R.color.border)
            )
        }
    }

    override fun afficherNombreQuestions(nombre: Int) {
        texteNombreQuestions.text = resources.getQuantityString(R.plurals.editeur_nombre_questions, nombre, nombre)
    }

    // ----- IVueÉditeur : erreurs et actions -----

    override fun afficherErreur(erreur: ErreurEditeur) {
        when (erreur) {
            ErreurEditeur.TITRE_VIDE -> {
                champTitre.error = getString(R.string.editeur_erreur_titre)
                saisieTitre.requestFocus()
            }
            ErreurEditeur.CATEGORIE_VIDE -> {
                champCategoriePerso.error = getString(R.string.editeur_erreur_categorie)
                saisieCategoriePerso.requestFocus()
            }
            ErreurEditeur.AUCUNE_QUESTION ->
                Toast.makeText(requireContext(), R.string.editeur_erreur_questions, Toast.LENGTH_SHORT).show()
            ErreurEditeur.QUESTION_INCOMPLETE -> Unit   // géré par afficherErreurQuestion
        }
    }

    override fun afficherErreurQuestion(numero: Int) {
        Toast.makeText(requireContext(), getString(R.string.editeur_erreur_question, numero), Toast.LENGTH_SHORT).show()
        cartesQuestions.getOrNull(numero - 1)?.findViewById<EditText>(R.id.saisie_question)?.requestFocus()
    }

    override fun effacerErreurs() {
        champTitre.error = null
        champCategoriePerso.error = null
    }

    override fun confirmerSauvegarde() {
        Toast.makeText(requireContext(), R.string.editeur_sauvegarde_ok, Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun confirmerPublication(titre: String) {
        Toast.makeText(requireContext(), getString(R.string.editeur_publie_ok, titre), Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun demanderConfirmationSuppression(index: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.editeur_confirmer_suppression_titre, index + 1))
            .setMessage(R.string.editeur_confirmer_suppression_message)
            .setNegativeButton(R.string.action_annuler, null)
            .setPositiveButton(R.string.action_supprimer) { _, _ ->
                presentateur.surSuppressionConfirmee(index)
            }
            .show()
    }

    override fun demanderConfirmationQuitter() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.editeur_confirmer_quitter_titre)
            .setMessage(R.string.editeur_confirmer_quitter_message)
            .setNegativeButton(R.string.action_annuler, null)
            .setNeutralButton(R.string.editeur_enregistrer) { _, _ -> presentateur.surEnregistrerClique() }
            .setPositiveButton(R.string.action_quitter) { _, _ -> presentateur.surQuitterConfirme() }
            .show()
    }

    override fun fermer() {
        findNavController().popBackStack()
    }

    override fun afficherPublication(estPublié: Boolean) {
        casePublier.isChecked = estPublié
    }

    override fun afficherBoutonEnregistrer(publier: Boolean, actif: Boolean, raison: ErreurEditeur?, numeroQuestion: Int) {
        boutonEnregistrer.setText(if (publier) R.string.editeur_publier else R.string.editeur_enregistrer_brouillon)
        boutonEnregistrer.isEnabled = actif
        texteRaisonBouton.isVisible = raison != null
        texteRaisonBouton.text = when (raison) {
            ErreurEditeur.TITRE_VIDE -> getString(R.string.editeur_erreur_titre)
            ErreurEditeur.CATEGORIE_VIDE -> getString(R.string.editeur_erreur_categorie)
            ErreurEditeur.AUCUNE_QUESTION -> getString(R.string.editeur_erreur_questions)
            ErreurEditeur.QUESTION_INCOMPLETE -> getString(R.string.editeur_erreur_question, numeroQuestion)
            null -> ""
        }
    }

    override fun afficherErreurChargement() {
        Toast.makeText(requireContext(), R.string.editeur_erreur_chargement, Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    override fun afficherErreurSauvegarde() {
        Toast.makeText(requireContext(), R.string.editeur_erreur_sauvegarde, Toast.LENGTH_LONG).show()
    }
}
