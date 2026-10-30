package com.crosemont.qwizup.presentation.create;

import com.crosemont.qwizup.presentation.ContratVuePresentateur.PresentateurCreate;
import com.crosemont.qwizup.presentation.ContratVuePresentateur.SectionCreer;
import com.crosemont.qwizup.presentation.ContratVuePresentateur.VueCreate;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CreatePresentateur implements PresentateurCreate {

    // TODO: Données temporaires, à remplacer par le modèle (SQLite)
    private static class QuizTemporaire {
        final int id;
        final String titre;
        final boolean estPublie;

        QuizTemporaire(int id, String titre, boolean estPublie) {
            this.id = id;
            this.titre = titre;
            this.estPublie = estPublie;
        }
    }

    private final List<QuizTemporaire> mesQuiz = Arrays.asList(
            new QuizTemporaire(1, "Le système solaire", false),
            new QuizTemporaire(2, "Bases de Kotlin", false),
            new QuizTemporaire(3, "Capitales d'Europe", true)
    );

    private final Map<SectionCreer, Boolean> sectionsOuvertes = new EnumMap<>(SectionCreer.class);
    private VueCreate vue;

    public CreatePresentateur() {
        sectionsOuvertes.put(SectionCreer.BROUILLONS, true);
        sectionsOuvertes.put(SectionCreer.PUBLIES, true);
    }

    @Override
    public void attacherVue(VueCreate vue) {
        this.vue = vue;
        afficherSection(SectionCreer.BROUILLONS, false, "Aucun brouillon pour l'instant");
        afficherSection(SectionCreer.PUBLIES, true, "Aucun quiz publié pour l'instant");
    }

    @Override
    public void detacherVue() {
        this.vue = null;
    }

    private void afficherSection(SectionCreer section, boolean publie, String messageVide) {
        if (vue == null) return;
        vue.viderSection(section);
        boolean vide = true;
        for (QuizTemporaire quiz : mesQuiz) {
            if (quiz.estPublie == publie) {
                vue.ajouterCarteQuiz(section, quiz.id, quiz.titre);
                vide = false;
            }
        }
        if (vide) vue.afficherMessageVide(section, messageVide);
        vue.basculerSection(section, sectionsOuvertes.get(section));
    }

    @Override
    public void surNouveauQuizClique() {
        if (vue != null) vue.ouvrirEditeur(null);
    }

    @Override
    public void surContinuerClique(int quizId) {
        if (vue != null) vue.ouvrirEditeur(quizId);
    }

    @Override
    public void surDeroulerClique(SectionCreer section) {
        boolean ouverte = !sectionsOuvertes.get(section);
        sectionsOuvertes.put(section, ouverte);
        if (vue != null) vue.basculerSection(section, ouverte);
    }

    @Override
    public void surVoirToutClique() {
        if (vue != null) vue.ouvrirTousMesQuiz();
    }
}
