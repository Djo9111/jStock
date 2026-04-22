package com.pharmacy.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entité AllocationSortie - Décomposition d'une sortie par lot consommé
 * 
 * Permet de tracer exactement quels lots (arrivages) ont été consommés
 * lors d'une sortie de stock, et en quelle quantité.
 * 
 * Exemple concret (cas Nescafé) :
 * 
 * Arrivage A1 : 5 Nescafé à 1000 FCFA le 01/10
 * Arrivage A2 : 3 Nescafé à 1200 FCFA le 05/10
 * 
 * Mouvement M3 (10/10) : Sortie de 6 Nescafé
 * → AllocationSortie AL1 : M3 consomme 5 de A1 à 1000 = 5000 FCFA
 * → AllocationSortie AL2 : M3 consomme 1 de A2 à 1200 = 1200 FCFA
 * 
 * Coût total exact de la sortie M3 = 5000 + 1200 = 6200 FCFA
 * 
 * Cette table est la clé de la traçabilité fine et du calcul
 * exact du coût de revient (COGS - Cost of Goods Sold).
 */
@Entity
@Table(name = "allocation_sortie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationSortie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_allocation")
    private Long id;
    
    /**
     * Quantité allouée (consommée) de ce lot pour cette sortie
     * 
     * Exemple : Si on sort 6 unités et que ce lot fournit 5,
     * alors quantiteAllouee = 5
     */
    @Column(name = "quantite_allouee", nullable = false)
    private Integer quantiteAllouee;
    
    /**
     * Prix de coût unitaire du lot au moment de la sortie
     * 
     * Copié depuis arrivage.prixAchatUnitaire pour historique
     * (au cas où le lot serait modifié/supprimé plus tard)
     */
    @Column(name = "prix_cout_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixCoutUnitaire;
    
    /**
     * Mouvement de SORTIE concerné
     * Relation Many-to-One avec Mouvement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mouvement_sortie", nullable = false)
    private Mouvement mouvementSortie;
    
    /**
     * Arrivage (lot) consommé par cette allocation
     * Relation Many-to-One avec Arrivage
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_arrivage", nullable = false)
    private Arrivage arrivage;
    
    /**
     * Constructeur pour création d'une allocation
     */
    public AllocationSortie(Mouvement mouvementSortie, Arrivage arrivage, 
                           Integer quantite, BigDecimal prixCout) {
        this.mouvementSortie = mouvementSortie;
        this.arrivage = arrivage;
        this.quantiteAllouee = quantite;
        this.prixCoutUnitaire = prixCout;
    }
    
    /**
     * Calcule le montant total du coût pour cette allocation
     * = quantiteAllouee × prixCoutUnitaire
     * 
     * Exemple : 5 × 1000 = 5000 FCFA
     */
    public BigDecimal getMontantCout() {
        if (quantiteAllouee == null || prixCoutUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixCoutUnitaire.multiply(BigDecimal.valueOf(quantiteAllouee));
    }
    
    /**
     * Retourne le produit concerné (via l'arrivage)
     */
    public Produit getProduit() {
        return arrivage != null ? arrivage.getProduit() : null;
    }
    
    /**
     * Retourne le fournisseur du lot (via l'arrivage)
     */
    public Fournisseur getFournisseur() {
        return arrivage != null ? arrivage.getFournisseur() : null;
    }
    
    /**
     * Retourne la date d'entrée du lot consommé
     */
    public java.time.LocalDateTime getDateEntreeLot() {
        return arrivage != null ? arrivage.getDateEntree() : null;
    }
    
    /**
     * Validation avant persistance
     * Vérifie que le mouvement est bien une SORTIE
     */
    @PrePersist
    @PreUpdate
    public void validerAllocation() {
        if (mouvementSortie != null && !mouvementSortie.estSortie()) {
            throw new IllegalStateException(
                "AllocationSortie ne peut être liée qu'à un mouvement de type SORTIE"
            );
        }
        
        if (quantiteAllouee == null || quantiteAllouee <= 0) {
            throw new IllegalStateException(
                "La quantité allouée doit être strictement positive"
            );
        }
    }
}
