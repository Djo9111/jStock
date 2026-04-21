package com.pharmacy.stock.enums;

/**
 * Méthodes de valorisation du stock
 */
public enum MethodeValorisation {
    /**
     * First In First Out - Premier entré, premier sorti
     * Le coût des sorties est celui des lots les plus anciens
     */
    FIFO,

    /**
     * Coût Moyen Unitaire Pondéré
     * Le coût moyen est recalculé à chaque entrée
     * nouveau_cmup = (valeur_stock_actuel + valeur_entree) / (qte_actuelle + qte_entree)
     */
    CMUP
}