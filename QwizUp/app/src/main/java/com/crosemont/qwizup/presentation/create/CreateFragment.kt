package com.crosemont.qwizup.presentation.create

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.create.ContratCréer.IPrésentateurCréer
import com.crosemont.qwizup.presentation.create.ContratCréer.IVueCréer
import com.crosemont.qwizup.presentation.create.ContratCréer.OngletCreer
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class CreateFragment : Fragment(R.layout.fragment_create), IVueCréer {

    // La Fabrique assemble source → service → modèle → présentateur
    private val presentateur: IPrésentateurCréer = FabriqueCréer.créerPrésentateurCréer()

    private lateinit var groupeOnglets: MaterialButtonToggleGroup
    private lateinit var ongletBrouillons: MaterialButton
    private lateinit var ongletPublies: MaterialButton
    private lateinit var listeQuiz: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupeOnglets = view.findViewById(R.id.groupe_onglets_creer)
        ongletBrouillons = view.findViewById(R.id.button_onglet_brouillons)
        ongletPublies = view.findViewById(R.id.button_onglet_publies)
        listeQuiz = view.findViewById(R.id.liste_quiz)

        view.findViewById<View>(R.id.button_nouveau_quiz)
            .setOnClickListener { presentateur.surNouveauQuizClique() }
        ongletBrouillons.setOnClickListener { presentateur.surOngletClique(OngletCreer.BROUILLONS) }
        ongletPublies.setOnClickListener { presentateur.surOngletClique(OngletCreer.PUBLIES) }

        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueCréer -----

    override fun afficherCompteurs(nbBrouillons: Int, nbPublies: Int) {
        ongletBrouillons.text = getString(R.string.creer_onglet_brouillons, nbBrouillons)
        ongletPublies.text = getString(R.string.creer_onglet_publies, nbPublies)
    }

    override fun afficherOngletChoisi(onglet: OngletCreer) {
        groupeOnglets.check(
            when (onglet) {
                OngletCreer.BROUILLONS -> R.id.button_onglet_brouillons
                OngletCreer.PUBLIES -> R.id.button_onglet_publies
            }
        )
    }

    override fun viderCartes() {
        listeQuiz.removeAllViews()
    }

    override fun ajouterCarteQuiz(id: Int, titre: String, nbQuestions: Int, estPublie: Boolean) {
        val carte = layoutInflater.inflate(R.layout.item_quiz_creer, listeQuiz, false)
        carte.findViewById<TextView>(R.id.text_titre_quiz).text = titre
        carte.findViewById<TextView>(R.id.text_details_quiz).text =
            resources.getQuantityString(R.plurals.editeur_nombre_questions, nbQuestions, nbQuestions)
        carte.findViewById<MaterialButton>(R.id.button_continuer).apply {
            setText(if (estPublie) R.string.creer_modifier else R.string.creer_continuer)
            setOnClickListener { presentateur.surQuizClique(id) }
        }
        listeQuiz.addView(carte)
    }

    override fun afficherListeVide(onglet: OngletCreer) {
        val texte = TextView(requireContext()).apply {
            setText(
                if (onglet == OngletCreer.BROUILLONS) R.string.creer_liste_vide_brouillons
                else R.string.creer_liste_vide_publies
            )
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            setPadding(0, 32, 0, 8)
        }
        listeQuiz.addView(texte)
    }

    override fun ouvrirEditeur(quizId: Int?) {
        // -1 = nouveau quiz (un argument de navigation ne peut pas être null)
        val args = bundleOf("quizId" to (quizId ?: -1))
        findNavController().navigate(R.id.action_creer_vers_editeur, args)
    }

    override fun afficherErreurChargement() {
        Toast.makeText(requireContext(), R.string.creer_erreur_chargement, Toast.LENGTH_LONG).show()
    }
}
