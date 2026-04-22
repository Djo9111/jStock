package com.pharmacy.stock.model;

import jakarta.persistence.*;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "allocation_sortie")
public class AllocationSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_mouvement_sortie", nullable = false)
    private Mouvement mouvementSortie;  // La sortie globale

    @ManyToOne
    @JoinColumn(name = "id_arrivage", nullable = false)
    private Arrivage arrivage;  // Le lot consommé

    @Column(name = "quantite_allouee", nullable = false)
    private Integer quantiteAllouee;  // Combien pris de ce lot

    @Column(name = "prix_cout_unitaire", nullable = false)
    private BigDecimal prixCoutUnitaire;  // Coût du lot (copié pour historique)

    // Calculé automatiquement
    public BigDecimal getMontantCout() {
        return prixCoutUnitaire.multiply(BigDecimal.valueOf(quantiteAllouee));
    }
}