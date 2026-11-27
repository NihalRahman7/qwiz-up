package com.crosemont.qwizup.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IPrésentateurBienvenue
import com.crosemont.qwizup.presentation.onboarding.ContratBienvenue.IVueBienvenue
import com.google.android.material.button.MaterialButton

/**
 * Vue de l'écran Bienvenue.
 * Elle transmet les clics au présentateur et ouvre l'écran qu'il demande.
 */
class FragmentBienvenue : Fragment(R.layout.fragment_bienvenue), ContratBienvenue.IVueBienvenue {
    private val présentateur: IPrésentateurBienvenue = PrésentateurBienvenue()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.button_creer_compte)
            .setOnClickListener { présentateur.surCréerCompteClique() }
        view.findViewById<MaterialButton>(R.id.button_deja_compte)
            .setOnClickListener { présentateur.surDéjàCompteClique() }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    // ------- IVueBienvenue -------

    // Ouvrir l'ecran Inscription(TODO)
    override fun ouvrirInscription() {
        findNavController().navigate(R.id.action_bienvenue_vers_accueil)
    }

    // Ouvrir l'ecran Connexion(DONE)
    override fun ouvrirConnexion() {
        findNavController().navigate(R.id.action_bienvenue_vers_connexion)
    }
}