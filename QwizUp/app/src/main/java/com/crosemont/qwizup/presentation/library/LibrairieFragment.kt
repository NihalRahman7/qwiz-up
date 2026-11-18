package com.crosemont.qwizup.presentation.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.crosemont.qwizup.R
import com.crosemont.qwizup.presentation.library.ContratLibrairie.OngletLibrairie
import com.crosemont.qwizup.presentation.library.ContratLibrairie.TypeBadge
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.card.MaterialCardView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LibrairieFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LibrairieFragment : Fragment(), ContratLibrairie.IVueLibrary {

    private lateinit var presentateur: ContratLibrairie.IPresentateurLibrary
    private lateinit var messageVide: TextView

    // Les cartes du XML
    private lateinit var cartes: List<MaterialCardView>
    private lateinit var titres: List<TextView>
    private lateinit var sousTitres: List<TextView>
    private lateinit var cartesBadges: List<MaterialCardView>
    private lateinit var textesBadges: List<TextView>

    private val quizIds = IntArray(NB_CARTES)
    private var prochaineCarte = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messageVide = view.findViewById(R.id.messageVide)

        cartes = listOf(R.id.carte1, R.id.carte2, R.id.carte3)
            .map { view.findViewById<MaterialCardView>(it) }
        titres = listOf(R.id.titre1, R.id.titre2, R.id.titre3)
            .map { view.findViewById<TextView>(it) }
        sousTitres = listOf(R.id.soustitre1, R.id.soustitre2, R.id.soustitre3)
            .map { view.findViewById<TextView>(it) }
        cartesBadges = listOf(R.id.badge1, R.id.badge2, R.id.badge3)
            .map { view.findViewById<MaterialCardView>(it) }
        textesBadges = listOf(R.id.txtBadge1, R.id.txtBadge2, R.id.txtBadge3)
            .map { view.findViewById<TextView>(it) }

        presentateur = FabriqueLibrairie.creerPresentateur(requireContext().filesDir)

        cartes.forEachIndexed { index, carte ->
            carte.setOnClickListener {
                presentateur.surQuizClique(
                    quizIds[index],
                    titres[index].text.toString()
                )
            }
        }

        view.findViewById<MaterialButtonToggleGroup>(R.id.tabs)
            .addOnButtonCheckedListener { _, btnId, estCoche ->
                if (estCoche) {
                    when (btnId) {
                        R.id.btnFavoris -> presentateur.surOngletClique(OngletLibrairie.FAVORIS)
                        R.id.btnComplets -> presentateur.surOngletClique(OngletLibrairie.COMPLETES)
                        R.id.btnCrees -> presentateur.surOngletClique(OngletLibrairie.CREES)
                    }
                }
            }

        presentateur.attacherVue(this)
    }

    override fun onDestroyView() {
        presentateur.detacherVue()
        super.onDestroyView()
    }

    override fun viderCartes() {
        prochaineCarte = 0
        messageVide.visibility = View.GONE
        cartes.forEach { it.visibility = View.GONE }
    }

    override fun ajouterCarteQuiz(
        id: Int,
        titre: String,
        sousTitre: String,
        badge: String?,
        typeBadge: ContratLibrairie.TypeBadge
    ) {
        if (prochaineCarte >= NB_CARTES) return // Le XML n'a que trois cartes
        val i = prochaineCarte++
        val contexte = requireContext()

        quizIds[i] = id
        titres[i].text = titre
        sousTitres[i].text = sousTitre

        if (badge == null) {
            cartesBadges[i].visibility = View.GONE
        } else {
            cartesBadges[i].visibility = View.VISIBLE
            textesBadges[i].text = badge
            val (fond, texte) = when (typeBadge) {
                TypeBadge.PROGRESSION -> R.color.light_green to R.color.primary_green
                TypeBadge.NOUVEAU -> R.color.light_blue to R.color.blue_text
                TypeBadge.SCORE -> R.color.border to R.color.text_primary
            }
            cartesBadges[i].setCardBackgroundColor(ContextCompat.getColor(contexte, fond))
            textesBadges[i].setTextColor(ContextCompat.getColor(contexte, texte))
        }

        cartes[i].visibility = View.VISIBLE
    }

    override fun afficherMessageVide(message: String) {
        messageVide.text = message
        messageVide.visibility = View.VISIBLE
    }

    override fun ouvrirDetails(quizId: Int, titre: String) {
        Toast.makeText(
            requireContext(),
            getString(R.string.librairie_quiz_soon, titre),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        private const val NB_CARTES = 3

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LibraryFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LibrairieFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}