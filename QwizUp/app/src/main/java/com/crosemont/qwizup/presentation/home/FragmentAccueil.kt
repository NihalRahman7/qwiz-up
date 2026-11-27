package com.crosemont.qwizup.presentation.home

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.home.ContratAccueil.IPrésentateurAccueil
import com.crosemont.qwizup.presentation.home.ContratAccueil.IVueAccueil

class FragmentAccueil : Fragment(R.layout.fragment_home), IVueAccueil {

    // Le présentateur qui gère la logique de cet écran (créé dans onViewCreated, quand le contexte existe)
    private lateinit var présentateur: IPrésentateurAccueil

    // Les textes des statistiques
    private lateinit var texteSérie: TextView

    private lateinit var texteSériePastille: TextView
    private lateinit var texteComplètes: TextView
    private lateinit var texteRéussite: TextView

    // La section « Continuer » : son titre et une carte par quiz en cours (id du quiz → carte)
    private lateinit var titreContinuer: View
    private lateinit var cartesEnCours: Map<Int, View>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        présentateur = FabriqueAccueil.créerPrésentateur(requireContext().filesDir)

        texteSérie = view.findViewById(R.id.text_streak)
        texteComplètes = view.findViewById(R.id.text_completed)
        texteRéussite = view.findViewById(R.id.text_correct)
        titreContinuer = view.findViewById(R.id.text_titre_continuer)
        cartesEnCours = mapOf(
            1 to view.findViewById(R.id.carte_en_cours_1),
            2 to view.findViewById(R.id.carte_en_cours_2),
            101 to view.findViewById(R.id.carte_en_cours_3)
        )

        // Chaque clic est transmis au présentateur.
        // Les id des quiz suivent la numérotation de Créer :
        // 1 = Le système solaire, 2 = Bases de Kotlin, 3 = Capitales d'Europe
        // 101 à 103 : quiz de test (QuizÀJouerBidon)
        view.findViewById<View>(R.id.button_jouer_1)
            .setOnClickListener { présentateur.surQuizClique(1) }
        view.findViewById<View>(R.id.button_jouer_2)
            .setOnClickListener { présentateur.surQuizClique(2) }
        view.findViewById<View>(R.id.button_jouer_3)
            .setOnClickListener { présentateur.surQuizClique(101) }
        view.findViewById<View>(R.id.carte_suggestion)
            .setOnClickListener { présentateur.surQuizClique(3) }
        view.findViewById<View>(R.id.carte_suggestion_2)
            .setOnClickListener { présentateur.surQuizClique(102) }
        view.findViewById<View>(R.id.carte_suggestion_3)
            .setOnClickListener { présentateur.surQuizClique(103) }
        view.findViewById<View>(R.id.button_join_code)
            .setOnClickListener { présentateur.surCodeClique() }
        view.findViewById<View>(R.id.carte_champ_code)
            .setOnClickListener { présentateur.surCodeClique() }
        view.findViewById<View>(R.id.text_avatar)
            .setOnClickListener { présentateur.surProfilClique() }
        view.findViewById<View>(R.id.text_tout_voir).setOnClickListener {
            requireActivity()
                .findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
                .selectedItemId = R.id.discoverFragment
        }

        // L'écran est prêt : on le présente au présentateur (il charge alors les statistiques)
        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        // L'écran disparaît : le présentateur ne doit plus l'utiliser
        présentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueAccueil -----

    // Affiche les statistiques reçues du présentateur
    override fun afficherStatistiques(serie: Int, nbQuizCompletes: Int, tauxRéussite: Int) {
        texteSérie.text = getString(R.string.home_streak_value, serie)
        texteComplètes.text = nbQuizCompletes.toString()
        texteRéussite.text = getString(R.string.home_correct_value, tauxRéussite)
        texteSériePastille.text = resources.getQuantityString(R.plurals.home_serie_pastille, serie, serie)
    }

    // Cache la carte d'un quiz qui n'est plus en cours (ex. réussi à 100 %, il est dans Complétés)
    override fun afficherQuizEnCours(quizIds: List<Int>) {
        cartesEnCours.forEach { (quizId, carte) ->
            carte.visibility = if (quizId in quizIds) View.VISIBLE else View.GONE
        }
        titreContinuer.visibility = if (quizIds.isEmpty()) View.GONE else View.VISIBLE
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

    // Ouvre l'écran Profil
    override fun ouvrirProfil() {
        findNavController().navigate(R.id.action_accueil_vers_profil)
    }
}