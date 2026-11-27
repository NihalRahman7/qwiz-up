package com.crosemont.qwizup.presentation.profil

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.profil.ContratProfil.IPrésentateurProfil
import com.crosemont.qwizup.presentation.profil.ContratProfil.IVueProfil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream

/**
 * Vue de l'écran Profil et de sa feuille « Modifier mon profil ».
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

    // La feuille « Modifier mon profil » quand elle est ouverte
    private var feuille: BottomSheetDialog? = null
    private var champNom: TextInputLayout? = null

    // L'app Caméra du téléphone renvoie une petite photo
    private val prendrePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { photo ->
            if (photo != null) présentateur.surPhotoChoisie(enOctets(photo))
        }

    // Demande la permission de la caméra ; si l'utilisateur accepte, on ouvre la caméra
    private val demanderCaméra =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { accordée ->
            if (accordée) {
                prendrePhoto.launch(null)
            } else {
                Toast.makeText(requireContext(), R.string.modifier_permission_refusee, Toast.LENGTH_SHORT).show()
            }
        }

    // Le sélecteur de photos d'Android renvoie l'adresse de l'image choisie
    private val choisirPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { adresse ->
            if (adresse != null) {
                val photo = requireContext().contentResolver.openInputStream(adresse)
                    ?.use { BitmapFactory.decodeStream(it) }
                if (photo != null) présentateur.surPhotoChoisie(enOctets(photo))
            }
        }

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
        feuille?.dismiss()
        présentateur.detacherVue()
        super.onDestroyView()
    }

    // Ouvre la caméra si la permission est déjà donnée, sinon la demande d'abord
    private fun ouvrirCaméra() {
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            prendrePhoto.launch(null)
        } else {
            demanderCaméra.launch(Manifest.permission.CAMERA)
        }
    }

    // Réduit la photo (512 px au plus) et la transforme en octets JPEG pour la sauvegarder
    private fun enOctets(photo: Bitmap): ByteArray {
        val échelle = minOf(1f, 512f / maxOf(photo.width, photo.height))
        val petite = Bitmap.createScaledBitmap(
            photo,
            (photo.width * échelle).toInt(),
            (photo.height * échelle).toInt(),
            true
        )
        val sortie = ByteArrayOutputStream()
        petite.compress(Bitmap.CompressFormat.JPEG, 85, sortie)
        return sortie.toByteArray()
    }

    // ----- IVueProfil -----

    override fun afficherProfil(nomAffiché: String, nomUtilisateur: String, courriel: String) {
        texteNom.text = nomAffiché
        texteIdentifiant.text = getString(R.string.profil_identifiant, nomUtilisateur, courriel)
        texteIdentifiant.isVisible = true
        texteIdentifiant.text = if (courriel.isBlank()) {
            getString(R.string.profil_identifiant_sans_courriel, nomUtilisateur)
        } else {
            getString(R.string.profil_identifiant, nomUtilisateur, courriel)
        }    }

    override fun afficherInvité() {
        val invité = getString(R.string.profil_invite)
        texteNom.text = invité
        texteIdentifiant.isVisible = false
        texteInitiale.text = invité.take(1)
    }

    override fun afficherPhoto(photo: ByteArray?) {
        if (photo == null) {
            imagePhoto.setImageDrawable(null)
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

    override fun ouvrirModification(nomAffiché: String) {
        val vueFeuille = layoutInflater.inflate(R.layout.feuille_modifier_profil, null)
        val dialogue = BottomSheetDialog(requireContext())
        dialogue.setContentView(vueFeuille)

        // Aperçu : la même photo ou la même initiale que l'écran
        vueFeuille.findViewById<TextView>(R.id.text_initiale_feuille).text = texteInitiale.text
        val aperçu = vueFeuille.findViewById<ImageView>(R.id.image_photo_feuille)
        aperçu.setImageDrawable(imagePhoto.drawable)
        aperçu.isVisible = imagePhoto.isVisible
        vueFeuille.findViewById<View>(R.id.button_retirer_photo).isVisible = imagePhoto.isVisible

        // Le champ commence avec le nom actuel ; l'erreur s'efface dès qu'on tape
        val champ = vueFeuille.findViewById<TextInputLayout>(R.id.champ_nom_affiche)
        val edit = vueFeuille.findViewById<TextInputEditText>(R.id.edit_nom_affiche)
        edit.setText(nomAffiché)
        edit.doAfterTextChanged { champ.error = null }

        // Chaque clic est transmis au présentateur
        vueFeuille.findViewById<View>(R.id.button_prendre_photo).setOnClickListener { ouvrirCaméra() }
        vueFeuille.findViewById<View>(R.id.button_galerie).setOnClickListener {
            choisirPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        vueFeuille.findViewById<View>(R.id.button_retirer_photo)
            .setOnClickListener { présentateur.surPhotoRetirée() }
        vueFeuille.findViewById<View>(R.id.button_enregistrer)
            .setOnClickListener { présentateur.surNomEnregistré(edit.text.toString()) }
        vueFeuille.findViewById<View>(R.id.button_annuler).setOnClickListener { dialogue.dismiss() }
        vueFeuille.findViewById<View>(R.id.button_fermer_feuille).setOnClickListener { dialogue.dismiss() }

        dialogue.setOnDismissListener {
            feuille = null
            champNom = null
        }
        feuille = dialogue
        champNom = champ
        dialogue.show()
    }

    override fun fermerModification() {
        feuille?.dismiss()
    }

    override fun afficherNomInvalide() {
        champNom?.error = getString(R.string.modifier_nom_invalide)
    }

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