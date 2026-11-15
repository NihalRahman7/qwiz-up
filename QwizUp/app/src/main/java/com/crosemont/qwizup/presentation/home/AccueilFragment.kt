package com.crosemont.qwizup.presentation.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.home.ContratAccueil.IPresentateurAccueil
import com.crosemont.qwizup.presentation.home.ContratAccueil.IVueAccueil

class AccueilFragment : Fragment(R.layout.fragment_home), IVueAccueil {

    // Le présentateur qui gère la logique de cet écran
    private val presentateur: IPresentateurAccueil = PresentateurAccueil()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Chaque clic est transmis au présentateur.
        // Les id des quiz suivent la numérotation de Créer :
        // 1 = Le système solaire, 2 = Bases de Kotlin, 3 = Capitales d'Europe
        view.findViewById<View>(R.id.button_jouer_1).setOnClickListener { presentateur.surQuizClique(1) }
        view.findViewById<View>(R.id.button_jouer_2).setOnClickListener { presentateur.surQuizClique(2) }
        view.findViewById<View>(R.id.carte_suggestion).setOnClickListener { presentateur.surQuizClique(3) }
        view.findViewById<View>(R.id.button_join_code).setOnClickListener { presentateur.surCodeClique() }

        // L'écran est prêt : on le présente au présentateur
        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        // L'écran disparaît : le présentateur ne doit plus l'utiliser
        presentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueAccueil -----

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