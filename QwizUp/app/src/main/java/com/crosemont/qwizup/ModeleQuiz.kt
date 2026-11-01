package com.crosemont.qwizup
import com.crosemont.qwizup.presentation.ContratVuePresentateur

class ModeleQuiz {
    // Home
    // TODO: Faire le modèle avec des données codées en dur pour le fragment Home

    // Discover
    data class CarteDiscover(
        val id: Int,
        val categorie: ContratVuePresentateur.CategorieQuiz,
        val titre: String,
        val description: String,
        val nbQuestions: Int,
        val dureeMinutes: Int,
        val estFavori: Boolean = false
    )

    // Données codées en dur
    private val quizDiscover = mutableListOf(
        CarteDiscover(
            1, ContratVuePresentateur.CategorieQuiz.GEOGRAPHIE, "Capitales d’Europe",
            "Associe chaque pays à sa capitale, de Lisbonne à Helsinki.", 12, 6
        ),
        CarteDiscover(
            2, ContratVuePresentateur.CategorieQuiz.SCIENCES, "Le système solaire",
            "Planètes, lunes et distances : teste tes connaissances.", 10, 5, estFavori = true
        ),
        CarteDiscover(
            3, ContratVuePresentateur.CategorieQuiz.HISTOIRE, "La Révolution tranquille",
            "Les grands changements du Québec des années 1960.", 8, 4
        ),
        CarteDiscover(
            4, ContratVuePresentateur.CategorieQuiz.LANGUES, "Verbes irréguliers anglais",
            "Be, go, take… retrouve le passé des verbes les plus utilisés.", 15, 7
        )
    )

    /**
     * Retourne les quiz de la catégorie choisie (null = tous) dont le titre ou la description
     * contient le texte recherché. On ignore les majuscules et les accents.
     */
    fun obtenirQuizDiscover(
        categorie: ContratVuePresentateur.CategorieQuiz?,
        recherche: String
    ): List<CarteDiscover> {
        val texte = simplifier(recherche.trim())
        return quizDiscover.filter { quiz ->
            val bonneCategorie = categorie == null || quiz.categorie == categorie
            val contientTexte = texte.isEmpty() ||
                simplifier(quiz.titre).contains(texte) ||
                simplifier(quiz.description).contains(texte)
            bonneCategorie && contientTexte
        }
    }

    fun obtenirQuizDiscoverParId(quizId: Int): CarteDiscover? =
        quizDiscover.find { it.id == quizId }

    /** Ajoute le quiz aux favoris s'il n'y est pas, sinon le retire. */
    fun basculerFavoriDiscover(quizId: Int) {
        val index = quizDiscover.indexOfFirst { it.id == quizId }
        if (index != -1) {
            val quiz = quizDiscover[index]
            quizDiscover[index] = quiz.copy(estFavori = !quiz.estFavori)
        }
    }

    /** Met le texte en minuscules et enlève les accents (« Révolution » devient « revolution »). */
    private fun simplifier(texte: String): String =
        java.text.Normalizer.normalize(texte, java.text.Normalizer.Form.NFD)
            .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }
            .lowercase()

    // Create
    data class CarteCreate(
        val id: Int,
        val titre: String,
        val estPublie: Boolean,  // false = brouillon, true = publié
        val nbQuestions: Int
    )

    data class QuestionCreer(
        val texte: String,
        val estVraiFaux: Boolean,      // false = choix multiple
        val secondes: Int,
        val doublePoints: Boolean = false
    )

    data class QuizCreer(
        val id: Int,                   // 0 = nouveau quiz pas encore sauvegardé
        val titre: String,
        val description: String,
        val categorie: ContratVuePresentateur.CategorieQuiz,
        val difficulte: ContratVuePresentateur.Difficulte,
        val questions: List<QuestionCreer>,
        val estPublie: Boolean
    )

    companion object {
        // Données codées en dur, partagées par tous les ModeleQuiz :
        // l'écran Créer et l'éditeur voient donc les mêmes quiz
        private val quizCreer = mutableListOf(
            QuizCreer(
                1, "Le système solaire", "Planètes, lunes et distances.",
                ContratVuePresentateur.CategorieQuiz.SCIENCES, ContratVuePresentateur.Difficulte.MOYEN,
                listOf(
                    QuestionCreer("Quelle est la plus grande planète du système solaire?", false, 20),
                    QuestionCreer("Vrai ou faux : la Lune produit sa propre lumière.", true, 10, doublePoints = true)
                ),
                estPublie = false
            ),
            QuizCreer(
                2, "Bases de Kotlin", "Les notions de base du langage Kotlin.",
                ContratVuePresentateur.CategorieQuiz.SCIENCES, ContratVuePresentateur.Difficulte.FACILE,
                listOf(
                    QuestionCreer("Quel mot-clé déclare une variable qu'on ne peut pas réassigner?", false, 20),
                    QuestionCreer("Vrai ou faux : Kotlin peut utiliser les bibliothèques Java.", true, 10),
                    QuestionCreer("Quelle fonction affiche du texte dans la console?", false, 20)
                ),
                estPublie = false
            ),
            QuizCreer(
                3, "Capitales d'Europe", "Associe chaque pays à sa capitale.",
                ContratVuePresentateur.CategorieQuiz.GEOGRAPHIE, ContratVuePresentateur.Difficulte.FACILE,
                listOf(
                    QuestionCreer("Quelle est la capitale du Portugal?", false, 20),
                    QuestionCreer("Quelle est la capitale de la Finlande?", false, 20),
                    QuestionCreer("Vrai ou faux : Genève est la capitale de la Suisse.", true, 10, doublePoints = true)
                ),
                estPublie = true
            )
        )
        private var prochainIdCreer = 4
    }

    fun obtenirBrouillons(): List<CarteCreate> = quizCreer.filter { !it.estPublie }.map { it.enCarte() }
    fun obtenirPublies(): List<CarteCreate> = quizCreer.filter { it.estPublie }.map { it.enCarte() }

    fun obtenirQuizCreer(quizId: Int): QuizCreer? = quizCreer.find { it.id == quizId }

    /** Ajoute le quiz s'il est nouveau (id = 0), sinon remplace l'ancienne version. Retourne son id. */
    fun sauvegarderQuizCreer(quiz: QuizCreer): Int {
        val index = quizCreer.indexOfFirst { it.id == quiz.id }
        return if (index == -1) {
            val nouveau = quiz.copy(id = prochainIdCreer++)
            quizCreer.add(nouveau)
            nouveau.id
        } else {
            quizCreer[index] = quiz
            quiz.id
        }
    }

    private fun QuizCreer.enCarte() = CarteCreate(id, titre, estPublie, questions.size)

    // Library
    data class CarteLibrary(
        val id: Int,
        val titre: String,
        val sousTitre: String,
        val badge: String?,
        val typeBadge: ContratVuePresentateur.TypeBadge
    )

    // Données codées en dur
    private val favoris = listOf(
        CarteLibrary(1, "Le système solaire", "Science - 10 questions", "4/5", ContratVuePresentateur.TypeBadge.PROGRESSION),
        CarteLibrary(2, "Capitales d'Europe", "Géographie - 12 questions", "Nouveau", ContratVuePresentateur.TypeBadge.NOUVEAU),
        CarteLibrary(3, "Verbe irréguliers anglais", "Langues - 15 questions", "87%", ContratVuePresentateur.TypeBadge.SCORE)
    )

    fun obternirFavoris(): List<CarteLibrary> = favoris
    fun obternirCompletes(): List<CarteLibrary> = emptyList()
    fun obternirCrees(): List<CarteLibrary> = emptyList()
}