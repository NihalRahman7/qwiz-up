package com.crosemont.qwizup.presentation.question

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePrésentateur.TypeQuestion
import com.crosemont.qwizup.presentation.question.ContratQuestion.IPrésentateurQuestion
import com.crosemont.qwizup.presentation.question.ContratQuestion.IVueQuestion
import com.crosemont.qwizup.presentation.question.ContratQuestion.NiveauRésultat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator

/**
 * Vue de l'écran de jeu.
 * Elle affiche ce que le présentateur lui demande et lui transmet chaque clic.
 */
class FragmentQuestion : Fragment(R.layout.fragment_question), IVueQuestion {

    // Le présentateur qui gère la partie (créé dans onViewCreated, quand le contexte existe)
    private lateinit var présentateur: IPrésentateurQuestion

    // Les éléments du layout
    private lateinit var groupeJeu: View
    private lateinit var groupeRésultat: View
    private lateinit var texteTitreQuiz: TextView
    private lateinit var texteCompteur: TextView
    private lateinit var progression: LinearProgressIndicator
    private lateinit var texteNuméro: TextView
    private lateinit var texteÉnonce: TextView
    private lateinit var boutonsRéponses: List<MaterialButton>
    private lateinit var texteRétroaction: TextView
    private lateinit var boutonSuivant: MaterialButton
    private lateinit var texteScore: TextView
    private lateinit var texteRésultatTitre: TextView
    private lateinit var texteRésultatDetail: TextView

    // Les textes des réponses affichées, pour écrire la bonne réponse dans le message
    private var réponsesAffichées: List<String> = emptyList()

    private val lettres = listOf("A", "B", "C", "D")

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        présentateur = FabriqueQuestion.créerPresentateur(requireContext().filesDir)
        super.onViewCreated(view, savedInstanceState)

        // Récupère les éléments du layout
        groupeJeu = view.findViewById(R.id.groupe_jeu)
        groupeRésultat = view.findViewById(R.id.groupe_resultat)
        texteTitreQuiz = view.findViewById(R.id.text_titre_quiz)
        texteCompteur = view.findViewById(R.id.text_compteur)
        progression = view.findViewById(R.id.progression)
        texteNuméro = view.findViewById(R.id.text_numero_question)
        texteÉnonce = view.findViewById(R.id.text_enonce)
        boutonsRéponses = listOf(
            view.findViewById(R.id.button_reponse_a),
            view.findViewById(R.id.button_reponse_b),
            view.findViewById(R.id.button_reponse_c),
            view.findViewById(R.id.button_reponse_d)
        )
        texteRétroaction = view.findViewById(R.id.text_retroaction)
        boutonSuivant = view.findViewById(R.id.button_suivant)
        texteScore = view.findViewById(R.id.text_score)
        texteRésultatTitre = view.findViewById(R.id.text_resultat_titre)
        texteRésultatDetail = view.findViewById(R.id.text_resultat_detail)

        // Chaque clic est transmis au présentateur
        view.findViewById<MaterialButton>(R.id.button_quitter)
            .setOnClickListener { présentateur.surQuitterClique() }
        boutonsRéponses.forEachIndexed { indice, bouton ->
            bouton.setOnClickListener { présentateur.surRéponseChoisie(indice) }
        }
        boutonSuivant.setOnClickListener { présentateur.surSuivantClique() }
        view.findViewById<MaterialButton>(R.id.button_rejouer)
            .setOnClickListener { présentateur.surRejouerClique() }
        view.findViewById<MaterialButton>(R.id.button_retour_accueil)
            .setOnClickListener { présentateur.surQuitterClique() }

        // Le bouton retour du téléphone passe aussi par le présentateur (confirmation si partie en cours)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            présentateur.surQuitterClique()
        }

        // Démarre la partie avec le quiz reçu de l'Accueil
        présentateur.attacherVue(this)
        présentateur.surDémarrer(arguments?.getInt("quizId", 1) ?: 1)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    /** Change la couleur de fond et de bordure d'un bouton de réponse. */
    private fun colorer(bouton: MaterialButton, @ColorRes fond: Int, @ColorRes bordure: Int) {
        bouton.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), fond))
        bouton.strokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), bordure))
    }

    /** Affiche le message sous les réponses, avec ses couleurs. */
    private fun afficherMessage(message: String, @ColorRes fond: Int, @ColorRes texte: Int) {
        texteRétroaction.text = message
        texteRétroaction.setBackgroundColor(ContextCompat.getColor(requireContext(), fond))
        texteRétroaction.setTextColor(ContextCompat.getColor(requireContext(), texte))
        texteRétroaction.isVisible = true
    }

    /** Empêche de toucher les réponses après avoir répondu. */
    private fun bloquerRéponses() {
        boutonsRéponses.forEach { it.isClickable = false }
    }

    // ----- IVueQuestion -----

    override fun afficherQuestion(
        titreQuiz: String,
        numero: Int,
        total: Int,
        énoncé: String,
        type: TypeQuestion,
        réponses: List<String>,
        plusieursRéponses: Boolean
    ) {
        groupeJeu.isVisible = true
        groupeRésultat.isVisible = false

        texteTitreQuiz.text = titreQuiz
        texteCompteur.text = getString(R.string.question_compteur, numero, total)
        texteNuméro.text = getString(R.string.question_numero, numero)
        texteÉnonce.text = énoncé

        // Vrai ou faux : 2 boutons ; choix multiple : les réponses reçues
        réponsesAffichées =
            if (type == TypeQuestion.VRAI_FAUX) listOf(
                getString(R.string.question_vrai),
                getString(R.string.question_faux)
            )
            else réponses

        boutonsRéponses.forEachIndexed { indice, bouton ->
            if (indice < réponsesAffichées.size) {
                bouton.isVisible = true
                bouton.isClickable = true
                bouton.text =
                    getString(R.string.question_reponse, lettres[indice], réponsesAffichées[indice])
                colorer(bouton, R.color.surface, R.color.border)
            } else {
                bouton.isVisible = false
            }
        }

        // Nouvelle question : pas de message, bouton Suivant désactivé
        texteRétroaction.isVisible = false
        boutonSuivant.isEnabled = false
        boutonSuivant.text = getString(
            if (plusieursRéponses) R.string.question_choisir_plusieurs else R.string.question_choisir
        )
    }

    override fun afficherProgression(pourcentage: Int) {
        progression.setProgressCompat(pourcentage, true)
    }

    override fun afficherSélection(indices: Set<Int>) {
        // Réponses cochées en bleu (pas en vert, pour ne pas donner la réponse)
        réponsesAffichées.indices.forEach { i ->
            if (i in indices) colorer(boutonsRéponses[i], R.color.light_blue, R.color.blue_text)
            else colorer(boutonsRéponses[i], R.color.surface, R.color.border)
        }
    }

    override fun activerValider(actif: Boolean) {
        boutonSuivant.isEnabled = actif
        boutonSuivant.text =
            getString(if (actif) R.string.question_valider else R.string.question_choisir_plusieurs)
    }

    override fun afficherCorrection(choisies: Set<Int>, bonnes: Set<Int>, estBonne: Boolean) {
        bloquerRéponses()
        réponsesAffichées.indices.forEach { i ->
            when (i) {
                in bonnes -> colorer(boutonsRéponses[i], R.color.light_green, R.color.primary_green)
                in choisies -> colorer(
                    boutonsRéponses[i],
                    R.color.light_orange,
                    R.color.accent_orange
                )

                else -> colorer(boutonsRéponses[i], R.color.surface, R.color.border)
            }
        }

        if (estBonne) {
            afficherMessage(
                getString(R.string.question_bonne),
                R.color.light_green,
                R.color.primary_green
            )
        } else {
            val message = if (bonnes.size == 1) {
                getString(R.string.question_mauvaise, réponsesAffichées[bonnes.first()])
            } else {
                getString(
                    R.string.question_mauvaise_plusieurs,
                    bonnes.sorted().joinToString { "« ${réponsesAffichées[it]} »" })
            }
            afficherMessage(message, R.color.light_orange, R.color.red_text)
        }
    }

    override fun activerSuivant(estDerniere: Boolean) {
        boutonSuivant.isEnabled = true
        boutonSuivant.text = getString(
            if (estDerniere) R.string.question_voir_resultat else R.string.question_suivante
        )
    }

    override fun afficherRésultat(
        titreQuiz: String,
        score: Int,
        total: Int,
        niveau: NiveauRésultat
    ) {
        // Cache la partie jeu et montre la partie résultat
        groupeJeu.isVisible = false
        groupeRésultat.isVisible = true

        texteScore.text = getString(R.string.question_compteur, score, total)
        texteRésultatTitre.text = getString(
            when (niveau) {
                NiveauRésultat.EXCELLENT -> R.string.resultat_excellent
                NiveauRésultat.BIEN -> R.string.resultat_bien
                NiveauRésultat.A_PRATIQUER -> R.string.resultat_a_pratiquer
            }
        )
        texteRésultatDetail.text = getString(R.string.resultat_detail, titreQuiz)
    }

    override fun demanderConfirmationQuitter() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.question_confirmer_quitter_titre)
            .setMessage(R.string.question_confirmer_quitter_message)
            .setNegativeButton(R.string.action_annuler, null)
            .setPositiveButton(R.string.action_quitter) { _, _ -> présentateur.surQuitterConfirme() }
            .show()
    }

    override fun afficherErreur() {
        Toast.makeText(requireContext(), R.string.question_erreur, Toast.LENGTH_SHORT).show()
    }

    override fun fermer() {
        // Retourne à l'écran précédent (l'Accueil)
        findNavController().popBackStack()
    }
}