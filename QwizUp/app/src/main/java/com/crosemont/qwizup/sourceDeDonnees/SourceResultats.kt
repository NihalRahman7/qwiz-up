package com.crosemont.qwizup.sourceDeDonnees

import com.crosemont.qwizup.domaine.entites.ResultatPartie

/**
 * Source de données des résultats de parties.
 * Le service ne connaît que cette interface : on peut changer de source sans le modifier.
 */
interface SourceResultats {
    fun lireResultats(): List<ResultatPartie>
    fun ajouterResultat(resultat: ResultatPartie)
}