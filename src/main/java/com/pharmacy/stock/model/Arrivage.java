package com.pharmacy.stock.model;
import jakarta.persistence.*;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "arrivage")
public class Arrivage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(name = "date_entree", nullable = false)
    private LocalDateTime dateEntree;

    @Column(name = "quantite_initiale", nullable = false)
    private Integer quantiteInitiale;

    @Column(name = "quantite_restante", nullable = false)
    private Integer quantiteRestante;

    @Column(name = "prix_achat_unitaire", nullable = false)
    private BigDecimal prixAchatUnitaire;

    @ManyToOne
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    @ManyToOne
    @JoinColumn(name = "id_mouvement_entree", nullable = false)
    private Mouvement mouvementEntree;  // Lien vers l'ENTREE qui a créé ce lot
}