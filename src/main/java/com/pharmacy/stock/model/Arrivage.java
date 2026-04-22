package com.pharmacy.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Arrivage - Représente un lot physique reçu
 * 
 * Un arrivage est créé à chaque ENTREE de stock.
 * Il permet de tracer l'origine exacte de chaque unité en stock
 * et d'appliquer des méthodes de valorisation comme FIFO/FEFO.
 * 
 * Exemple :
 * - Arrivage A1 : 5 Paracétamol à 1000 FCFA le 01/10
 * - Arrivage A2 : 3 Paracétamol à 1200 FCFA le 05/10
 * 
 * Lors d'une sortie de 6 unités en FIFO :
 * → 5 viennent de A1, 1 vient de A2
 */
@Entity
@Table(name = "arrivage")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arrivage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_arrivage")
    private Long id;
    
    /**
     * Date d'entrée du lot en stock
     * Utilisée pour l'ordre FIFO (First In First Out)
     */
    @Column(name = "date_entree", nullable = false)
    private LocalDateTime dateEntree;
    
    /**
     * Quantité initiale reçue lors de l'entrée
     * Reste inchangée (historique)
     */
    @Column(name = "quantite_initiale", nullable = false)
    private Integer quantiteInitiale;
    
    /**
     * Quantité restante actuellement disponible dans ce lot
     * Décrémentée à chaque sortie consommant ce lot
     */
    @Column(name = "quantite_restante", nullable = false)
    private Integer quantiteRestante;
    
    /**
     * Prix d'achat unitaire de ce lot
     * Utilisé pour calculer le coût exact des sorties
     */
    @Column(name = "prix_achat_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixAchatUnitaire;
    
    /**
     * Numéro de lot fournisseur (optionnel)
     * Exemple : "LOT-2024-1234" pour traçabilité pharmaceutique
     */
    @Column(name = "numero_lot", length = 50)
    private String numeroLot;
    
    /**
     * Date de péremption du lot (optionnel)
     * Permet la gestion FEFO (First Expired First Out)
     */
    @Column(name = "date_peremption")
    private LocalDateTime datePeremption;
    
    /**
     * Produit concerné par cet arrivage
     * Relation Many-to-One avec Produit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;
    
    /**
     * Fournisseur de ce lot
     * Relation Many-to-One avec Fournisseur
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;
    
    /**
     * Mouvement d'ENTREE qui a créé cet arrivage
     * Relation Many-to-One avec Mouvement
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mouvement_entree", nullable = false)
    private Mouvement mouvementEntree;
    
    /**
     * Liste des allocations de sortie qui ont consommé ce lot
     * Relation bidirectionnelle 1-N avec AllocationSortie
     */
    @OneToMany(mappedBy = "arrivage", cascade = CascadeType.ALL)
    private List<AllocationSortie> allocations = new ArrayList<>();
    
    /**
     * Constructeur pour création d'un arrivage
     */
    public Arrivage(Produit produit, LocalDateTime dateEntree, Integer quantite, 
                    BigDecimal prixAchat, Fournisseur fournisseur, Mouvement mouvementEntree) {
        this.produit = produit;
        this.dateEntree = dateEntree;
        this.quantiteInitiale = quantite;
        this.quantiteRestante = quantite;
        this.prixAchatUnitaire = prixAchat;
        this.fournisseur = fournisseur;
        this.mouvementEntree = mouvementEntree;
    }
    
    /**
     * Vérifie si le lot est épuisé
     */
    public boolean estEpuise() {
        return quantiteRestante == null || quantiteRestante == 0;
    }
    
    /**
     * Vérifie si le lot est périmé
     */
    public boolean estPerime() {
        if (datePeremption == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(datePeremption);
    }
    
    /**
     * Vérifie si le lot arrive bientôt à expiration (< 30 jours)
     */
    public boolean procheDeLaPeremption() {
        if (datePeremption == null) {
            return false;
        }
        LocalDateTime dans30Jours = LocalDateTime.now().plusDays(30);
        return datePeremption.isBefore(dans30Jours);
    }
    
    /**
     * Calcule la valeur restante du lot
     * = quantiteRestante × prixAchatUnitaire
     */
    public BigDecimal getValeurRestante() {
        if (quantiteRestante == null || prixAchatUnitaire == null) {
            return BigDecimal.ZERO;
        }
        return prixAchatUnitaire.multiply(BigDecimal.valueOf(quantiteRestante));
    }
    
    /**
     * Consomme une quantité de ce lot lors d'une sortie
     * 
     * @param quantite Quantité à consommer
     * @throws IllegalStateException Si stock insuffisant
     */
    public void consommer(Integer quantite) {
        if (quantite > quantiteRestante) {
            throw new IllegalStateException(
                "Stock insuffisant dans le lot. Disponible: " + quantiteRestante + 
                ", Demandé: " + quantite
            );
        }
        this.quantiteRestante -= quantite;
    }
    
    /**
     * Retourne une quantité à ce lot (cas d'un retour)
     * 
     * @param quantite Quantité à restituer
     */
    public void restituer(Integer quantite) {
        this.quantiteRestante += quantite;
        
        // Ne pas dépasser la quantité initiale
        if (this.quantiteRestante > this.quantiteInitiale) {
            throw new IllegalStateException(
                "Impossible de restituer : cela dépasserait la quantité initiale du lot"
            );
        }
    }
    
    /**
     * Retourne le taux de consommation du lot (en %)
     */
    public double getTauxConsommation() {
        if (quantiteInitiale == 0) {
            return 0.0;
        }
        int quantiteConsommee = quantiteInitiale - quantiteRestante;
        return (quantiteConsommee * 100.0) / quantiteInitiale;
    }
}
