package com.pharmacy.stock.enums;

/**
 * Énumération des types de mouvements de stock
 */
public enum TypeMouvement {
    /**
     * Entrée de stock (achat, réception commande)
     */
    ENTREE,

    /**
     * Sortie de stock (vente, consommation)
     */
    SORTIE,

    /**
     * Ajustement d'inventaire (correction écart physique)
     */
    AJUSTEMENT,

    /**
     * Retour de produit (client ou fournisseur)
     */
    RETOUR,

    /**
     * Produit périmé ou détruit
     */
    PEREMPTION
}