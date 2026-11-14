package com.crosemont.qwizup.presentation
import com.crosemont.qwizup.presentation.library.ContratLibrary
import java.text.Normalizer

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
            3, ContratVuePresentateur.CategorieQuiz.GEOGRAPHIE, "Capitales d’Europe",
            "Associe chaque pays à sa capitale, de Lisbonne à Helsinki.", 12, 6
        ),
        CarteDiscover(
            1, ContratVuePresentateur.CategorieQuiz.SCIENCES, "Le système solaire",
            "Planètes, lunes et distances : teste tes connaissances.", 10, 5, estFavori = true
        ),
        CarteDiscover(
            4, ContratVuePresentateur.CategorieQuiz.HISTOIRE, "La Révolution tranquille",
            "Les grands changements du Québec des années 1960.", 8, 4
        ),
        CarteDiscover(
            5, ContratVuePresentateur.CategorieQuiz.LANGUES, "Verbes irréguliers anglais",
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
        Normalizer.normalize(texte, Normalizer.Form.NFD)
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
        val type: ContratVuePresentateur.TypeQuestion,
        val reponses: List<String> = listOf("", "", "", ""),   // choix multiple : A, B, C, D
        val bonneReponse: Int = 0,          // choix multiple : 0 à 3 ; vrai ou faux : 0 = vrai, 1 = faux
        val reponseAttendue: String = ""    // réponse écrite
    )

    data class QuizCreer(
        val id: Int,                        // 0 = nouveau quiz pas encore sauvegardé
        val titre: String,
        val description: String,
        val categorie: ContratVuePresentateur.CategorieQuiz,
        val difficulte: ContratVuePresentateur.Difficulte,
        val acces: ContratVuePresentateur.AccesQuiz,
        val questions: List<QuestionCreer>,
        val estPublie: Boolean
    )

    companion object {
        private fun choix(texte: String, reponses: List<String>, bonne: Int) =
            QuestionCreer(texte, ContratVuePresentateur.TypeQuestion.CHOIX_MULTIPLE, reponses, bonne)
        private fun vraiFaux(texte: String, vrai: Boolean) =
            QuestionCreer(texte, ContratVuePresentateur.TypeQuestion.VRAI_FAUX, bonneReponse = if (vrai) 0 else 1)
        private fun ecrite(texte: String, attendue: String) =
            QuestionCreer(texte, ContratVuePresentateur.TypeQuestion.REPONSE_ECRITE, reponseAttendue = attendue)

        // Données codées en dur, partagées par tous les ModeleQuiz :
        // l'écran Créer et l'éditeur voient donc les mêmes quiz
        private val quizCreer = mutableListOf(
            QuizCreer(
                1, "Le système solaire", "Planètes, lunes et distances.",
                ContratVuePresentateur.CategorieQuiz.SCIENCES, ContratVuePresentateur.Difficulte.MOYEN,
                ContratVuePresentateur.AccesQuiz.AVEC_CODE,
                listOf(
                    choix("Quelle est la plus grande planète du système solaire?",
                        listOf("Mars", "Jupiter", "Saturne", "Neptune"), 1),
                    vraiFaux("La Lune produit sa propre lumière.", vrai = false),
                    choix("Quelle planète est la plus proche du Soleil?", listOf("Vénus", "Mercure", "Mars", "Terre"), 1),
                    choix("Combien de planètes compte le système solaire?", listOf("7", "8", "9", "10"), 1),
                    vraiFaux("Mars est surnommée la planète rouge.", vrai = true)
                ),
                estPublie = false
            ),
            QuizCreer(
                2, "Bases de Kotlin", "Les notions de base du langage Kotlin.",
                ContratVuePresentateur.CategorieQuiz.INFORMATIQUE, ContratVuePresentateur.Difficulte.FACILE,
                ContratVuePresentateur.AccesQuiz.AVEC_CODE,
                listOf(
                    choix("Quel mot-clé déclare une variable qu'on ne peut pas réassigner?",
                        listOf("var", "val", "let", "const"), 1),
                    vraiFaux("Kotlin peut utiliser les bibliothèques Java.", vrai = true),
                    choix("Quel type représente du texte?", listOf("Int", "Char", "String", "Text"), 2),
                    choix("Quel mot-clé déclare une fonction?", listOf("function", "def", "fun", "fn"), 2),
                    vraiFaux("Une variable String peut être null par défaut.", vrai = false)
                ),
                estPublie = false
            ),
            QuizCreer(
                3, "Capitales d'Europe", "Associe chaque pays à sa capitale.",
                ContratVuePresentateur.CategorieQuiz.GEOGRAPHIE, ContratVuePresentateur.Difficulte.FACILE,
                ContratVuePresentateur.AccesQuiz.TOUT_LE_MONDE,
                listOf(
                    choix("Quelle est la capitale du Portugal?", listOf("Porto", "Lisbonne", "Madrid", "Faro"), 1),
                    choix("Quelle est la capitale de la Finlande?", listOf("Oslo", "Stockholm", "Helsinki", "Tallinn"), 2),
                    vraiFaux("Genève est la capitale de la Suisse.", vrai = false)
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
        val typeBadge: ContratLibrary.TypeBadge
    )

    // Données codées en dur
    private val favoris = listOf(
        CarteLibrary(1, "Le système solaire", "Science - 10 questions", "4/5", ContratLibrary.TypeBadge.PROGRESSION),
        CarteLibrary(2, "Capitales d'Europe", "Géographie - 12 questions", "Nouveau", ContratLibrary.TypeBadge.NOUVEAU),
        CarteLibrary(3, "Verbe irréguliers anglais", "Langues - 15 questions", "87%", ContratLibrary.TypeBadge.SCORE)
    )

    fun obternirFavoris(): List<CarteLibrary> = favoris
    fun obternirCompletes(): List<CarteLibrary> = emptyList()
    fun obternirCrees(): List<CarteLibrary> = emptyList()


    // Question (écran de jeu)
    /**
     * Retourne le quiz à jouer, avec les mêmes données que l'écran Créer.
     * Les réponses écrites sont retirées pour l'instant : le jeu gère seulement
     * le choix multiple et le vrai ou faux.
     */
    fun obtenirQuizAJouer(quizId: Int): QuizCreer? =
        obtenirQuizCreer(quizId)?.let { quiz ->
            quiz.copy(
                questions = quiz.questions.filter {
                    it.type != ContratVuePresentateur.TypeQuestion.REPONSE_ECRITE
                }
            )
        }


    // Rejoindre (code à 6 chiffres donné par l'enseignant·e)
    // Données codées en dur : chaque code ouvre un quiz (même numérotation que Créer)
    private val codesQuiz = mapOf(
        "111111" to 1,   // Le système solaire
        "222222" to 2,   // Bases de Kotlin
        "333333" to 3    // Capitales d'Europe
    )
    /** Retourne l'id du quiz qui correspond au code, ou null si le code n'existe pas. */
    fun trouverQuizParCode(code: String): Int? = codesQuiz[code.trim()]
}