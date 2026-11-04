package com.crosemont.qwizup.presentation.rejoindre

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IPresentateurRejoindre
import com.crosemont.qwizup.presentation.ContratVuePresentateur.IVueRejoindre
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Vue de l'écran Rejoindre.
 * Elle transmet le code (tapé ou scanné) au présentateur et affiche sa réponse.
 */
class RejoindreFragment : Fragment(R.layout.fragment_rejoindre), IVueRejoindre {

    // Le présentateur qui vérifie les codes
    private val presentateur: IPresentateurRejoindre = PresentateurRejoindre()

    private lateinit var champCode: TextInputLayout
    private lateinit var editCode: TextInputEditText
    private lateinit var boutonRejoindre: MaterialButton

    // Ouvre la caméra ; quand un code QR est lu, son texte est envoyé au présentateur
    private val scanneur = registerForActivityResult(ScanContract()) { resultat ->
        resultat.contents?.let { contenu -> presentateur.surCodeScanne(contenu) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        champCode = view.findViewById(R.id.champ_code)
        editCode = view.findViewById(R.id.edit_code)
        boutonRejoindre = view.findViewById(R.id.button_rejoindre)

        // Chaque chiffre tapé est transmis au présentateur
        editCode.doAfterTextChanged { texte -> presentateur.surCodeModifie(texte.toString()) }

        // La touche « OK » du clavier fait la même chose que le bouton Rejoindre
        editCode.setOnEditorActionListener { _, action, _ ->
            if (action == EditorInfo.IME_ACTION_GO && boutonRejoindre.isEnabled) {
                presentateur.surRejoindreClique(editCode.text.toString())
                true
            } else {
                false
            }
        }

        boutonRejoindre.setOnClickListener { presentateur.surRejoindreClique(editCode.text.toString()) }
        view.findViewById<MaterialButton>(R.id.button_scanner)
            .setOnClickListener { presentateur.surScannerClique() }
        view.findViewById<MaterialButton>(R.id.button_retour)
            .setOnClickListener { presentateur.surRetourClique() }

        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    // ----- IVueRejoindre -----

    override fun activerRejoindre(actif: Boolean) {
        boutonRejoindre.isEnabled = actif
    }

    override fun afficherCodeInvalide() {
        champCode.error = getString(R.string.rejoindre_code_invalide)
    }

    override fun effacerErreur() {
        champCode.error = null
    }

    override fun ouvrirScanneur() {
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)   // seulement les codes QR
            .setPrompt(getString(R.string.rejoindre_scanner_invite))
            .setBeepEnabled(false)
            .setOrientationLocked(false)
        scanneur.launch(options)
    }

    override fun ouvrirQuiz(quizId: Int) {
        val arguments = Bundle().apply { putInt("quizId", quizId) }
        findNavController().navigate(R.id.action_rejoindre_vers_question, arguments)
    }

    override fun fermer() {
        findNavController().popBackStack()
    }
}