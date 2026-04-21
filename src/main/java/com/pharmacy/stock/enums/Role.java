package com.pharmacy.stock.enums;

/**
 * Rôles des utilisateurs du système
 */
public enum Role {
    /**
     * Administrateur - Tous les droits
     */
    ADMIN,

    /**
     * Gestionnaire de stock - Peut créer/modifier produits et mouvements
     */
    GESTIONNAIRE,

    /**
     * Vendeur - Consultation uniquement et enregistrement des ventes
     */
    VENDEUR
}