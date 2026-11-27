package com.crosemont.qwizup.presentation.profil

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.profil.ContratProfil.IPrésentateurProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IVueProfil
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Vue de l'écran Profil.
 * Elle transmet les clics au présentateur et affiche ce qu'il lui envoie.
 */
class FragmentProfil : Fragment(R.layout.fragment_profil), IVueProfil {

    private lateinit var présentateur: IPrésentateurProfil

    private lateinit var texteInitiale: TextView
    private lateinit var imagePhoto: ImageView
    private lateinit var texteNom: TextView
    private lateinit var texteIdentifiant: TextView
    private lateinit var texteSérie: TextView
    private lateinit var texteComplétés: TextView
    private lateinit var texteRéussite: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        présentateur = FabriqueProfil.créerPrésentateur(requireContext().filesDir)

        texteInitiale = view.findViewById(R.id.text_initiale)
        imagePhoto = view.findViewById(R.id.image_photo)
        texteNom = view.findViewById(R.id.text_nom)
        texteIdentifiant = view.findViewById(R.id.text_identifiant)
        texteSérie = view.findViewById(R.id.text_serie)
        texteComplétés = view.findViewById(R.id.text_completes)
        texteRéussite = view.findViewById(R.id.text_reussite)

        // Chaque clic est transmis au présentateur
        view.findViewById<View>(R.id.button_retour).setOnClickListener { présentateur.surRetourClique() }
        view.findViewById<View>(R.id.button_modifier).setOnClickListener { présentateur.surModifierClique() }
        view.findViewById<View>(R.id.button_changer_photo).setOnClickListener { présentateur.surModifierClique() }
        view.findViewById<View>(R.id.ligne_mes_quiz).setOnClickListener { présentateur.surMesQuizClique() }
        view.findViewById<View>(R.id.ligne_langue).setOnClickListener { présentateur.surLangueClique() }
        view.findViewById<View>(R.id.ligne_a_propos).setOnClickListener { présentateur.surÀProposClique() }
        view.findViewById<View>(R.id.button_deconnexion).setOnClickListener { présentateur.surDéconnexionClique() }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueProfil -----

    override fun afficherProfil(nomAffiché: String, nomUtilisateur: String, courriel: String) {
        texteNom.text = nomAffiché
        texteIdentifiant.text = getString(R.string.profil_identifiant, nomUtilisateur, courriel)
        texteIdentifiant.isVisible = true
        texteInitiale.text = nomAffiché.take(1).uppercase()
    }

    override fun afficherInvité() {
        val invité = getString(R.string.profil_invite)
        texteNom.text = invité
        texteIdentifiant.isVisible = false
        texteInitiale.text = invité.take(1)
    }

    override fun afficherPhoto(photo: ByteArray?) {
        if (photo == null) {
            imagePhoto.isVisible = false
        } else {
            imagePhoto.setImageBitmap(BitmapFactory.decodeByteArray(photo, 0, photo.size))
            imagePhoto.isVisible = true
        }
    }

    override fun afficherStatistiques(série: Int, nbQuizComplétés: Int, tauxRéussite: Int) {
        texteSérie.text = getString(R.string.home_streak_value, série)
        texteComplétés.text = nbQuizComplétés.toString()
        texteRéussite.text = getString(R.string.home_correct_value, tauxRéussite)
    }

    // TODO Partie 3 : la feuille « Modifier mon profil »
    override fun ouvrirModification(nomAffiché: String) {}

    override fun fermerModification() {}

    override fun afficherNomInvalide() {}

    override fun ouvrirMesQuiz() {
        findNavController().navigate(R.id.action_profil_vers_creer)
    }

    // Ouvre les réglages de langue de l'app (Android 13+), sinon ceux du téléphone
    override fun ouvrirLangue() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                .setData(Uri.fromParts("package", requireContext().packageName, null))
        } else {
            Intent(Settings.ACTION_LOCALE_SETTINGS)
        }
        startActivity(intent)
    }

    override fun ouvrirÀPropos() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.profil_a_propos)
            .setMessage(R.string.profil_a_propos_texte)
            .setPositiveButton(R.string.profil_ok, null)
            .show()
    }

    override fun ouvrirBienvenue() {
        findNavController().navigate(R.id.action_profil_vers_bienvenue)
    }

    override fun fermer() {
        findNavController().popBackStack()
    }

    override fun afficherErreur() {
        Toast.makeText(requireContext(), R.string.profil_erreur, Toast.LENGTH_SHORT).show()
    }
}