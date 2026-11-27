import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IPrésentateurConnexion
import com.crosemont.qwizup.presentation.onboarding.ContratConnexion.IVueConnexion
import com.crosemont.qwizup.presentation.onboarding.PrésentateurConnexion
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Vue de l'écran Connexion.
 * Elle transmet les champs et les clics au présentateur et affiche sa réponse.
 */
class FragmentConnexion : Fragment(R.layout.fragment_connexion), IVueConnexion {

    private val présentateur: IPrésentateurConnexion = PrésentateurConnexion()

    private lateinit var editNom: TextInputEditText
    private lateinit var editMotDePasse: TextInputEditText
    private lateinit var boutonConnexion: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editNom = view.findViewById(R.id.edit_nom)
        editMotDePasse = view.findViewById(R.id.edit_mot_de_passe)
        boutonConnexion = view.findViewById(R.id.button_connexion)

        // Chaque lettre tapée est transmise au présentateur
        editNom.doAfterTextChanged { envoyerChamps() }
        editMotDePasse.doAfterTextChanged { envoyerChamps() }

        boutonConnexion.setOnClickListener {
            présentateur.surConnexionClique(editNom.text.toString(), editMotDePasse.text.toString())
        }
        view.findViewById<MaterialButton>(R.id.button_creer_compte)
            .setOnClickListener { présentateur.surCréerCompteClique() }

        présentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        présentateur.detacherVue()
        super.onDestroyView()
    }

    private fun envoyerChamps() {
        présentateur.surChampsModifiés(editNom.text.toString(), editMotDePasse.text.toString())
    }

    // ----- IVueConnexion -----

    override fun activerConnexion(actif: Boolean) {
        boutonConnexion.isEnabled = actif
    }

    override fun ouvrirAccueil() {
        findNavController().navigate(R.id.action_connexion_vers_accueil)
    }

    // Ouvrir l'ecran insription (TODO)
    override fun ouvrirInscription() {
    }

    override fun fermer() {
        findNavController().popBackStack()
    }
}