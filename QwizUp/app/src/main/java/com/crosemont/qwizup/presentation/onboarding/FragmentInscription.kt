package com.crosemont.qwizup.presentation.onboarding

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IPrésentateurInscription
import com.crosemont.qwizup.presentation.onboarding.ContratInscription.IVueInscription
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.widget.Toast

/**
 * Vue de l'écran Inscription.
 * Elle transmet les champs et les clics au présentateur et affiche sa réponse.
 */
class FragmentInscription : Fragment(R.layout.fragment_inscription), IVueInscription {

    private lateinit var présentateur: IPrésentateurInscription
    private lateinit var editNom: TextInputEditText
    private lateinit var editCourriel: TextInputEditText
    private lateinit var editMotDePasse: TextInputEditText
    private lateinit var editConfirmation: TextInputEditText
    private lateinit var champConfirmation: TextInputLayout
    private lateinit var boutonCréer: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        présentateur = FabriqueInscription.créerPrésentateur(requireContext().filesDir)
        super.onViewCreated(view, savedInstanceState)

        editNom = view.findViewById(R.id.edit_nom)
        editCourriel = view.findViewById(R.id.edit_courriel)
        editMotDePasse = view.findViewById(R.id.edit_mot_de_passe)
        editConfirmation = view.findViewById(R.id.edit_confirmation)
        champConfirmation = view.findViewById(R.id.champ_confirmation)
        boutonCréer = view.findViewById(R.id.button_creer_compte)

        // Chaque lettre tapée est transmise au présentateur
        editNom.doAfterTextChanged { envoyerChamps() }
        editCourriel.doAfterTextChanged { envoyerChamps() }
        editMotDePasse.doAfterTextChanged { envoyerChamps() }
        editConfirmation.doAfterTextChanged { envoyerChamps() }

        boutonCréer.setOnClickListener {
            présentateur.surCréerCompteClique(
                editNom.text.toString(),
                editCourriel.text.toString(),
                editMotDePasse.text.toString()
            )
        }
        view.findViewById<MaterialButton>(R.id.button_me_connecter)
            .setOnClickListener { présentateur.surDéjàCompteClique() }
        view.findViewById<MaterialButton>(R.id.button_retour)
            .setOnClickListener { présentateur.surRetourClique() }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    private fun envoyerChamps() {
        présentateur.surChampsModifiés(
            editNom.text.toString(),
            editCourriel.text.toString(),
            editMotDePasse.text.toString(),
            editConfirmation.text.toString()
        )
    }

    // ----- IVueInscription -----

    override fun activerCréation(actif: Boolean) {
        boutonCréer.isEnabled = actif
    }

    override fun afficherMotsDePasseIdentiques(identiques: Boolean) {
        champConfirmation.isEndIconVisible = identiques
        champConfirmation.helperText =
            if (identiques) getString(R.string.inscription_identiques) else null
    }

    override fun ouvrirAccueil() {
        findNavController().navigate(R.id.action_inscription_vers_accueil)
    }

    override fun ouvrirConnexion() {
        findNavController().navigate(R.id.action_inscription_vers_connexion)
    }

    override fun fermer() {
        findNavController().popBackStack()
    }

    override fun afficherErreur() {
        Toast.makeText(requireContext(), R.string.inscription_erreur, Toast.LENGTH_SHORT).show()
    }
}