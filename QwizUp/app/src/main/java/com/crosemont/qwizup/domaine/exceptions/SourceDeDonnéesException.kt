package com.crosemont.qwizup.domaine.exceptions

/**
 * Lancée par une source de données quand elle ne peut pas fournir les données
 */
class SourceDeDonnéesException(message: String): RuntimeException(message)