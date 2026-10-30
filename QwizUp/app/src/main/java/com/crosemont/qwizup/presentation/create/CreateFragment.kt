package com.crosemont.qwizup.presentation.create

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePresentateur.PresentateurCreate
import com.crosemont.qwizup.presentation.ContratVuePresentateur.SectionCreer
import com.crosemont.qwizup.presentation.ContratVuePresentateur.VueCreate
import com.google.android.material.button.MaterialButton

class CreateFragment : Fragment(R.layout.fragment_create), VueCreate {

    private val presentateur: PresentateurCreate = CreatePresentateur()

    private lateinit var listeBrouillons: LinearLayout
    private lateinit var listePublies: LinearLayout
    private lateinit var flecheBrouillons: ImageView
    private lateinit var flechePublies: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeBrouillons = view.findViewById(R.id.liste_brouillons)
        listePublies = view.findViewById(R.id.liste_publies)
        flecheBrouillons = view.findViewById(R.id.button_derouler_brouillons)
        flechePublies = view.findViewById(R.id.button_derouler_publies)

        view.findViewById<MaterialButton>(R.id.button_nouveau_quiz)
            .setOnClickListener { presentateur.surNouveauQuizClique() }
        view.findViewById<MaterialButton>(R.id.button_voir_tout)
            .setOnClickListener { presentateur.surVoirToutClique() }
        flecheBrouillons.setOnClickListener { presentateur.surDeroulerClique(SectionCreer.BROUILLONS) }
        flechePublies.setOnClickListener { presentateur.surDeroulerClique(SectionCreer.PUBLIES) }

        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    private fun liste(section: SectionCreer) = when (section) {
        SectionCreer.BROUILLONS -> listeBrouillons
        SectionCreer.PUBLIES -> listePublies
    }

    private fun fleche(section: SectionCreer) = when (section) {
        SectionCreer.BROUILLONS -> flecheBrouillons
        SectionCreer.PUBLIES -> flechePublies
    }

    // ----- VueCreate -----

    override fun viderSection(section: SectionCreer) {
        liste(section).removeAllViews()
    }

    override fun ajouterCarteQuiz(section: SectionCreer, id: Int, titre: String) {
        val carte = layoutInflater.inflate(R.layout.item_quiz_creer, liste(section), false)
        carte.findViewById<TextView>(R.id.text_titre_quiz).text = titre
        carte.findViewById<MaterialButton>(R.id.button_continuer)
            .setOnClickListener { presentateur.surContinuerClique(id) }
        liste(section).addView(carte)
    }

    override fun afficherMessageVide(section: SectionCreer, message: String) {
        val texte = TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
            setPadding(0, 24, 0, 8)
        }
        liste(section).addView(texte)
    }

    override fun basculerSection(section: SectionCreer, ouverte: Boolean) {
        liste(section).isVisible = ouverte
        fleche(section).rotation = if (ouverte) 270f else 90f
    }

    override fun ouvrirEditeur(quizId: Int?) {
        // TODO: naviguer vers l'écran d'édition quand il existera
        val message = if (quizId == null) getString(R.string.creer_editeur_nouveau)
                      else getString(R.string.creer_editeur_existant, quizId)
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun ouvrirTousMesQuiz() {
        // TODO: naviguer vers la liste complète
        Toast.makeText(requireContext(), R.string.creer_tous_mes_quiz, Toast.LENGTH_SHORT).show()
    }
}
