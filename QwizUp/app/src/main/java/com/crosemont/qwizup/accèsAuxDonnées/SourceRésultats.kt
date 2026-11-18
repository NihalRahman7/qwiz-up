package com.crosemont.qwizup.accèsAuxDonnées

import com.crosemont.qwizup.domaine.entites.RésultatPartie

/**
 * Source de données des résultats de parties.
 * Le service ne connaît que cette interface : on peut changer de source sans le modifier.
 */
interface SourceRésultats {
    fun lireRésultats(): List<RésultatPartie>
    fun ajouterRésultat(resultat: RésultatPartie)
}