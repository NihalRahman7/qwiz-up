package com.crosemont.qwizup.presentation.create

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.AccèsQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.CatégorieQuiz
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.Difficulté
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.create.ContratCréer.ErreurEditeur
import com.crosemont.qwizup.presentation.create.ContratCréer.IPrésentateurÉditeur
import com.crosemont.qwizup.presentation.create.ContratCréer.IVueÉditeur
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

    // ----- Les 4 étapes (seulement dans la vue : le présentateur ne change pas) -----

    // L'étape affichée, de 1 à 4
    private var étape = 1

    private lateinit var blocsÉtapes: List<View>
    private lateinit var barresÉtapes: List<View>
    private lateinit var libellésÉtapes: List<TextView>
    private lateinit var texteÉtape: TextView
    private lateinit var texteTitreÉtape: TextView
    private lateinit var boutonÉtapePrécédente: MaterialButton
    private lateinit var boutonÉtapeSuivante: MaterialButton
    private lateinit var zonePublier: View
    private lateinit var défilement: NestedScrollView
    private lateinit var texteRésuméTitre: TextView
    private lateinit var texteRésuméDétails: TextView
    private lateinit var texteRésuméQuestions: TextView

    private val titresÉtapes = listOf(
        R.string.creer_etape_infos,
        R.string.creer_etape_questions,
        R.string.creer_etape_options,
        R.string.creer_etape_publier
    )

    // Couleur forte, couleur pâle et icône de chaque puce de catégorie
    private val styleCatégories = mapOf(
        CatégorieQuiz.SCIENCES to Triple(R.color.categorie_sciences, R.color.categorie_sciences_clair, R.drawable.ic_couverture_sciences),
        CatégorieQuiz.HISTOIRE to Triple(R.color.categorie_histoire, R.color.categorie_histoire_clair, R.drawable.ic_couverture_histoire),
        CatégorieQuiz.LANGUES to Triple(R.color.categorie_langues, R.color.categorie_langues_clair, R.drawable.ic_couverture_langues),
        CatégorieQuiz.GÉOGRAPHIE to Triple(R.color.categorie_geographie, R.color.categorie_geographie_clair, R.drawable.ic_globe),
        CatégorieQuiz.INFORMATIQUE to Triple(R.color.categorie_informatique, R.color.categorie_informatique_clair, R.drawable.ic_couverture_informatique),
        CatégorieQuiz.AUTRE to Triple(R.color.categorie_autre, R.color.categorie_autre_clair, R.drawable.ic_couverture_autre)
    )

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

        // Le bouton retour du téléphone revient à l'étape d'avant ;
        // à l'étape 1, il demande une confirmation pour quitter (comme le ✕)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (étape > 1) afficherÉtape(étape - 1) else presentateur.surFermerClique()
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

        // Les 4 étapes
        blocsÉtapes = listOf(R.id.etape_infos, R.id.etape_questions, R.id.etape_options, R.id.etape_publier)
            .map { view.findViewById<View>(it) }
        barresÉtapes = listOf(R.id.barre_etape_1, R.id.barre_etape_2, R.id.barre_etape_3, R.id.barre_etape_4)
            .map { view.findViewById<View>(it) }
        libellésÉtapes = listOf(R.id.libelle_etape_1, R.id.libelle_etape_2, R.id.libelle_etape_3, R.id.libelle_etape_4)
            .map { view.findViewById<TextView>(it) }
        texteÉtape = view.findViewById(R.id.text_etape)
        texteTitreÉtape = view.findViewById(R.id.text_titre_etape)
        boutonÉtapePrécédente = view.findViewById(R.id.button_etape_precedente)
        boutonÉtapeSuivante = view.findViewById(R.id.button_etape_suivante)
        zonePublier = view.findViewById(R.id.zone_publier)
        défilement = view.findViewById(R.id.defilement_editeur)
        texteRésuméTitre = view.findViewById(R.id.text_resume_titre)
        texteRésuméDétails = view.findViewById(R.id.text_resume_details)
        texteRésuméQuestions = view.findViewById(R.id.text_resume_questions)

        boutonÉtapePrécédente.setOnClickListener { afficherÉtape(étape - 1) }
        boutonÉtapeSuivante.setOnClickListener { afficherÉtape(étape + 1) }
        colorerPucesCatégories(view)
        afficherÉtape(1)

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

    // ----- Les 4 étapes -----

    /** Montre seulement le bloc de l'étape demandée, et met à jour l'en-tête et les boutons. */
    private fun afficherÉtape(numéro: Int) {
        étape = numéro.coerceIn(1, NB_ÉTAPES)

        blocsÉtapes.forEachIndexed { i, bloc -> bloc.isVisible = i == étape - 1 }
        barresÉtapes.forEachIndexed { i, barre ->
            barre.backgroundTintList =
                ColorStateList.valueOf(couleur(if (i < étape) R.color.primaire else R.color.bordure))
        }
        libellésÉtapes.forEachIndexed { i, libellé ->
            libellé.setTextColor(couleur(if (i == étape - 1) R.color.primaire else R.color.encre_secondaire))
        }
        mettreÀJourEnTête()

        // Retour pâle à l'étape 1 ; Suivant caché à l'étape 4, où on publie
        boutonÉtapePrécédente.isEnabled = étape > 1
        boutonÉtapePrécédente.alpha = if (étape > 1) 1f else 0.5f
        boutonÉtapeSuivante.isVisible = étape < NB_ÉTAPES
        zonePublier.isVisible = étape == NB_ÉTAPES
        if (étape == NB_ÉTAPES) remplirRésumé()

        défilement.scrollTo(0, 0)
    }

    /** « Nouveau quiz · Étape 1 sur 4 » et le titre de l'étape. */
    private fun mettreÀJourEnTête() {
        texteÉtape.text = getString(R.string.creer_etape_entete, texteTitreEcran.text, étape)
        texteTitreÉtape.setText(titresÉtapes[étape - 1])
    }

    /** Le résumé de l'étape 4, lu dans les champs des étapes 1 à 3. */
    private fun remplirRésumé() {
        val vue = requireView()
        texteRésuméTitre.text = saisieTitre.text?.toString()?.trim()
            .takeUnless { it.isNullOrEmpty() } ?: getString(R.string.creer_resume_sans_titre)

        // Catégorie (le nom précisé si c'est « Autre »), difficulté et accès choisis
        val catégorie = vue.findViewById<Chip?>(groupeCategories.checkedChipId)?.text?.toString().orEmpty()
        val nomCatégorie = if (groupeCategories.checkedChipId == R.id.chip_editeur_autre)
            saisieCategoriePerso.text?.toString()?.trim().takeUnless { it.isNullOrEmpty() } ?: catégorie
        else catégorie
        val difficulté = vue.findViewById<MaterialButton?>(groupeDifficulte.checkedButtonId)?.text?.toString().orEmpty()
        val accès = vue.findViewById<MaterialButton?>(groupeAcces.checkedButtonId)?.text?.toString().orEmpty()
        texteRésuméDétails.text = listOf(nomCatégorie, difficulté, accès)
            .filter { it.isNotBlank() }
            .joinToString(" · ")

        texteRésuméQuestions.text = texteNombreQuestions.text
    }

    /** Chaque puce prend la couleur de sa catégorie : pâle avec icône de couleur, forte et blanche une fois choisie. */
    private fun colorerPucesCatégories(vue: View) {
        val choisie = intArrayOf(android.R.attr.state_checked)
        val autre = intArrayOf()
        chipsCategories.forEach { (catégorie, id) ->
            val (forte, pâle, icône) = styleCatégories.getValue(catégorie)
            vue.findViewById<Chip>(id).apply {
                chipBackgroundColor = ColorStateList(arrayOf(choisie, autre), intArrayOf(couleur(forte), couleur(pâle)))
                setTextColor(ColorStateList(arrayOf(choisie, autre), intArrayOf(couleur(R.color.white), couleur(R.color.encre))))
                setChipIconResource(icône)
                isChipIconVisible = true
                chipIconTint = ColorStateList(arrayOf(choisie, autre), intArrayOf(couleur(R.color.white), couleur(forte)))
                isCheckedIconVisible = false
                setCloseIconResource(R.drawable.ic_coche)
                closeIconTint = ColorStateList.valueOf(couleur(R.color.white))
                isCloseIconVisible = isChecked
                setOnCheckedChangeListener { puce, cochée -> (puce as Chip).isCloseIconVisible = cochée }
            }
        }
    }

    // ----- IVueÉditeur : infos du quiz -----

    override fun afficherEnTete(nouveauQuiz: Boolean) {
        texteTitreEcran.setText(if (nouveauQuiz) R.string.editeur_titre_nouveau else R.string.editeur_titre_modifier)
        mettreÀJourEnTête()
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
            CatégorieQuiz.SCIENCES -> R.drawable.ic_couverture_sciences to R.color.categorie_sciences_clair
            CatégorieQuiz.HISTOIRE -> R.drawable.ic_couverture_histoire to R.color.categorie_histoire_clair
            CatégorieQuiz.LANGUES -> R.drawable.ic_couverture_langues to R.color.categorie_langues_clair
            CatégorieQuiz.INFORMATIQUE -> R.drawable.ic_couverture_informatique to R.color.categorie_informatique_clair
            CatégorieQuiz.GÉOGRAPHIE -> R.drawable.ic_globe to R.color.categorie_geographie_clair
            CatégorieQuiz.AUTRE -> R.drawable.ic_couverture_autre to R.color.categorie_autre_clair
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
            fond.setColor(couleur(if (choisie) R.color.primaire_clair else R.color.white))
            fond.setStroke(
                (2 * resources.displayMetrics.density).toInt(),
                couleur(if (choisie) R.color.primaire else R.color.bordure)
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

    override fun demanderConfirmationQuitter(peutEnregistrer: Boolean, publier: Boolean) {
        val dialogue = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.editeur_confirmer_quitter_titre)
            .setNegativeButton(R.string.action_annuler, null)
            .setPositiveButton(R.string.action_quitter) { _, _ -> presentateur.surQuitterConfirme() }

        if (peutEnregistrer) {
            // Le bouton dit exactement ce qu'il va faire, comme le gros bouton en bas
            dialogue.setMessage(R.string.editeur_confirmer_quitter_message)
                .setNeutralButton(if (publier) R.string.editeur_publier else R.string.editeur_enregistrer_brouillon) { _, _ ->
                    presentateur.surEnregistrerClique()
                }
        } else {
            // Il manque quelque chose : pas de bouton qui ne marcherait pas
            dialogue.setMessage(R.string.editeur_confirmer_quitter_message_court)
        }
        dialogue.show()
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

    companion object {
        private const val NB_ÉTAPES = 4
    }
}