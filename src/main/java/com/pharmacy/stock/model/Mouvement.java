package com.pharmacy.stock.model;

import com.pharmacy.stock.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité Mouvement - Journal de tous les flux de stock
 * Traçabilité complète : qui, quoi, quand, combien, pourquoi
 */
@Entity
@Table(name = "mouvement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mouvement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mouvement")
    private Long id;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 20)
    private TypeMouvement typeMouvement;

    /**
     * Quantité du mouvement
     * - Positif pour ENTREE, RETOUR (entrée client)
     * - Négatif pour SORTIE, PEREMPTION, AJUSTEMENT (si perte)
     */
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    /**
     * Prix unitaire du mouvement
     * - Pour ENTREE : prix d'achat fournisseur
     * - Pour SORTIE : coût moyen actuel du stock
     */
    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    /**
     * Montant total du mouvement
     * Calculé automatiquement : quantite × prixUnitaire
     */
    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    /**
     * Référence du document associé
     * Exemples : N° facture, bon de commande, PV destruction
     */
    @Column(name = "reference_document", length = 50)
    private String referenceDocument;

    /**
     * Observation ou commentaire sur le mouvement
     */
    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    /**
     * Produit concerné par ce mouvement
     * Relation Many-to-One avec Produit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    /**
     * Fournisseur (uniquement pour les ENTREES)
     * Relation Many-to-One avec Fournisseur
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    /**
     * Utilisateur qui a créé ce mouvement (audit)
     * Relation Many-to-One avec Utilisateur
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    /**
     * Constructeur sans relations (pour création)
     */
    public Mouvement(LocalDateTime dateMouvement, TypeMouvement typeMouvement,
                     Integer quantite, BigDecimal prixUnitaire,
                     String referenceDocument, String observation) {
        this.dateMouvement = dateMouvement;
        this.typeMouvement = typeMouvement;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
        this.referenceDocument = referenceDocument;
        this.observation = observation;
        calculerMontantTotal();
    }

    /**
     * Calcule automatiquement le montant total
     * Appelé avant l'insertion/mise à jour en base
     */
    @PrePersist
    @PreUpdate
    public void calculerMontantTotal() {
        if (quantite != null && prixUnitaire != null) {
            this.montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    /**
     * Vérifie si c'est un mouvement d'entrée
     */
    public boolean estEntree() {
        return typeMouvement == TypeMouvement.ENTREE || typeMouvement == TypeMouvement.RETOUR;
    }

    /**
     * Vérifie si c'est un mouvement de sortie
     */
    public boolean estSortie() {
        return typeMouvement == TypeMouvement.SORTIE ||
                typeMouvement == TypeMouvement.PEREMPTION ||
                typeMouvement == TypeMouvement.AJUSTEMENT;
    }

    /**
     * Valide le mouvement avant persistance
     * - ENTREE doit avoir un fournisseur
     * - SORTIE ne doit pas avoir de fournisseur
     * - ENTREE doit avoir quantite positive
     * - SORTIE doit avoir quantite négative
     */
    @PrePersist
    public void validerMouvement() {
        // Validation fournisseur
        if (typeMouvement == TypeMouvement.ENTREE && fournisseur == null) {
            throw new IllegalStateException("Un mouvement ENTREE doit avoir un fournisseur");
        }

        if (typeMouvement == TypeMouvement.SORTIE && fournisseur != null) {
            throw new IllegalStateException("Un mouvement SORTIE ne doit pas avoir de fournisseur");
        }

        // Validation quantité
        if (typeMouvement == TypeMouvement.ENTREE && quantite <= 0) {
            throw new IllegalStateException("Un mouvement ENTREE doit avoir une quantité positive");
        }

        if (typeMouvement == TypeMouvement.SORTIE && quantite >= 0) {
            throw new IllegalStateException("Un mouvement SORTIE doit avoir une quantité négative");
        }
    }
}