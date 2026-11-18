package com.crosemont.qwizup.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.home.ContratAccueil.IPresentateurAccueil
import com.crosemont.qwizup.presentation.home.ContratAccueil.IVueAccueil

class AccueilFragment : Fragment(R.layout.fragment_home), IVueAccueil {

    // Le présentateur qui gère la logique de cet écran (créé dans onViewCreated, quand le contexte existe)
    private lateinit var presentateur: IPresentateurAccueil

    // Les textes des statistiques
    private lateinit var texteSerie: TextView
    private lateinit var texteCompletes: TextView
    private lateinit var texteReussite: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presentateur = FabriqueAccueil.creerPresentateur(requireContext().filesDir)

        texteSerie = view.findViewById(R.id.text_streak)
        texteCompletes = view.findViewById(R.id.text_completed)
        texteReussite = view.findViewById(R.id.text_correct)

        // Chaque clic est transmis au présentateur.
        // Les id des quiz suivent la numérotation de Créer :
        // 1 = Le système solaire, 2 = Bases de Kotlin, 3 = Capitales d'Europe
        view.findViewById<View>(R.id.button_jouer_1)
            .setOnClickListener { presentateur.surQuizClique(1) }
        view.findViewById<View>(R.id.button_jouer_2)
            .setOnClickListener { presentateur.surQuizClique(2) }
        view.findViewById<View>(R.id.carte_suggestion)
            .setOnClickListener { presentateur.surQuizClique(3) }
        view.findViewById<View>(R.id.button_join_code)
            .setOnClickListener { presentateur.surCodeClique() }

        // L'écran est prêt : on le présente au présentateur (il charge alors les statistiques)
        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        // L'écran disparaît : le présentateur ne doit plus l'utiliser
        presentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueAccueil -----

    // Affiche les statistiques reçues du présentateur
    override fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxReussite: Int) {
        texteSerie.text = getString(R.string.home_streak_value, serie)
        texteCompletes.text = nbQuizCompletes.toString()
        texteReussite.text = getString(R.string.home_correct_value, tauxReussite)
    }

    // Le modèle n'a pas pu lire les statistiques
    override fun afficherErreur() {
        Toast.makeText(requireContext(), R.string.home_error, Toast.LENGTH_SHORT).show()
    }

    // Ouvre l'écran de jeu et lui envoie le numéro du quiz à jouer
    override fun ouvrirQuiz(quizId: Int) {
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_accueil_vers_question, arguments)
    }

    // Ouvre l'écran pour rejoindre un quiz avec un code
    override fun ouvrirRejoindre() {
        findNavController().navigate(R.id.action_accueil_vers_rejoindre)
    }
}