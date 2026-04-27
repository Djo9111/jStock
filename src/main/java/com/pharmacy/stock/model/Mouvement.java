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
     * - Positif pour ENTREE, RETOUR
     * - Négatif pour SORTIE, PEREMPTION, AJUSTEMENT
     */
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    /**
     * Prix unitaire du mouvement
     */
    @Column(name = "prix_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    /**
     * Montant total = quantite × prixUnitaire
     */
    @Column(name = "montant_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "reference_document", length = 50)
    private String referenceDocument;

    @Column(name = "observation", columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    /**
     * Constructeur métier
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
     * 🔥 UNE SEULE méthode lifecycle JPA (corrige ton bug)
     */
    @PrePersist
    @PreUpdate
    public void prePersistOrUpdate() {
        valider();
        calculerMontantTotal();
        initialiserDate();
    }

    /**
     * Validation métier
     */
    private void valider() {
        if (typeMouvement == null) {
            throw new IllegalStateException("Le type de mouvement est obligatoire");
        }

        if (quantite == null || quantite == 0) {
            throw new IllegalStateException("La quantité ne peut pas être nulle ou zéro");
        }

        // ENTREE
        if (typeMouvement == TypeMouvement.ENTREE) {
            if (fournisseur == null) {
                throw new IllegalStateException("Un mouvement ENTREE doit avoir un fournisseur");
            }
            if (quantite <= 0) {
                throw new IllegalStateException("Un mouvement ENTREE doit avoir une quantité positive");
            }
        }

        // SORTIE
        if (typeMouvement == TypeMouvement.SORTIE) {
            if (fournisseur != null) {
                throw new IllegalStateException("Un mouvement SORTIE ne doit pas avoir de fournisseur");
            }
            if (quantite >= 0) {
                throw new IllegalStateException("Un mouvement SORTIE doit avoir une quantité négative");
            }
        }
    }

    /**
     * Calcul automatique du montant
     */
    private void calculerMontantTotal() {
        if (quantite != null && prixUnitaire != null) {
            montantTotal = prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }

    /**
     * Initialisation automatique de la date
     */
    private void initialiserDate() {
        if (dateMouvement == null) {
            dateMouvement = LocalDateTime.now();
        }
    }

    /**
     * Helpers métier
     */
    public boolean estEntree() {
        return typeMouvement == TypeMouvement.ENTREE || typeMouvement == TypeMouvement.RETOUR;
    }

    public boolean estSortie() {
        return typeMouvement == TypeMouvement.SORTIE ||
                typeMouvement == TypeMouvement.PEREMPTION ||
                typeMouvement == TypeMouvement.AJUSTEMENT;
    }
}