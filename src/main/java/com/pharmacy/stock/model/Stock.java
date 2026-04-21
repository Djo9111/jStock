package com.pharmacy.stock.model;

import com.pharmacy.stock.enums.MethodeValorisation;
import com.pharmacy.stock.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Entité Stock - État actuel du stock d'un produit
 * Mis à jour automatiquement après chaque mouvement
 */
@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long id;

    @Column(name = "quantite_disponible", nullable = false)
    private Integer quantiteDisponible;

    /**
     * Coût moyen unitaire pondéré (CMUP)
     * Recalculé à chaque entrée si méthode = CMUP
     */
    @Column(name = "cout_moyen_unitaire", nullable = false, precision = 10, scale = 2)
    private BigDecimal coutMoyenUnitaire;

    /**
     * Valeur totale du stock
     * Calculé : quantiteDisponible × coutMoyenUnitaire
     */
    @Column(name = "valeur_stock_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valeurStockTotal;

    @Column(name = "date_derniere_maj", nullable = false)
    private LocalDateTime dateDerniereMaj;

    /**
     * Produit associé à ce stock
     * Relation One-to-One avec Produit
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false, unique = true)
    private Produit produit;

    /**
     * Constructeur pour initialisation du stock
     */
    public Stock(Produit produit, Integer quantiteInitiale, BigDecimal coutInitial) {
        this.produit = produit;
        this.quantiteDisponible = quantiteInitiale;
        this.coutMoyenUnitaire = coutInitial;
        this.dateDerniereMaj = LocalDateTime.now();
        calculerValeurTotal();
    }

    /**
     * Calcule la valeur totale du stock
     */
    public void calculerValeurTotal() {
        if (quantiteDisponible != null && coutMoyenUnitaire != null) {
            this.valeurStockTotal = coutMoyenUnitaire
                    .multiply(BigDecimal.valueOf(quantiteDisponible))
                    .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Met à jour le stock après un mouvement
     * @param mouvement Le mouvement à appliquer
     */
    public void mettreAJourAvecMouvement(Mouvement mouvement) {
        TypeMouvement type = mouvement.getTypeMouvement();
        Integer quantiteMouvement = mouvement.getQuantite();
        BigDecimal prixMouvement = mouvement.getPrixUnitaire();

        // Cas 1 : ENTREE - Recalculer CMUP si nécessaire
        if (type == TypeMouvement.ENTREE) {
            if (produit.getMethodeValorisation() == MethodeValorisation.CMUP) {
                calculerNouveauCMUP(quantiteMouvement, prixMouvement);
            }
            quantiteDisponible += quantiteMouvement;
        }

        // Cas 2 : SORTIE, PEREMPTION, AJUSTEMENT - Décrémenter stock
        else if (type == TypeMouvement.SORTIE ||
                type == TypeMouvement.PEREMPTION ||
                type == TypeMouvement.AJUSTEMENT) {
            // La quantité est déjà négative dans le mouvement
            quantiteDisponible += quantiteMouvement;

            // Vérification : ne pas avoir de stock négatif
            if (quantiteDisponible < 0) {
                throw new IllegalStateException(
                        "Stock insuffisant pour ce mouvement. Stock actuel: " +
                                (quantiteDisponible - quantiteMouvement) +
                                ", Sortie demandée: " + Math.abs(quantiteMouvement)
                );
            }
        }

        // Cas 3 : RETOUR (client) - Réintégrer au stock
        else if (type == TypeMouvement.RETOUR) {
            quantiteDisponible += quantiteMouvement;
        }

        // Mise à jour finale
        calculerValeurTotal();
        this.dateDerniereMaj = LocalDateTime.now();
    }

    /**
     * Calcule le nouveau CMUP lors d'une entrée
     * Formule : nouveau_cmup = (valeur_stock_actuel + valeur_entree) / (qte_actuelle + qte_entree)
     *
     * @param quantiteEntree Quantité entrée
     * @param prixEntree Prix unitaire d'achat
     */
    public void calculerNouveauCMUP(Integer quantiteEntree, BigDecimal prixEntree) {
        // Valeur actuelle du stock
        BigDecimal valeurActuelle = coutMoyenUnitaire.multiply(
                BigDecimal.valueOf(quantiteDisponible)
        );

        // Valeur de l'entrée
        BigDecimal valeurEntree = prixEntree.multiply(
                BigDecimal.valueOf(quantiteEntree)
        );

        // Nouvelle valeur totale
        BigDecimal nouvelleValeur = valeurActuelle.add(valeurEntree);

        // Nouvelle quantité totale
        int nouvelleQuantite = quantiteDisponible + quantiteEntree;

        // Nouveau CMUP
        if (nouvelleQuantite > 0) {
            this.coutMoyenUnitaire = nouvelleValeur
                    .divide(BigDecimal.valueOf(nouvelleQuantite), 2, RoundingMode.HALF_UP);
        }
    }

    /**
     * Vérifie si le stock est vide
     */
    public boolean estVide() {
        return quantiteDisponible == null || quantiteDisponible == 0;
    }

    /**
     * Vérifie si le stock est sous le seuil d'alerte
     */
    public boolean estSousSeuilAlerte() {
        if (produit == null) {
            return false;
        }
        return quantiteDisponible <= produit.getSeuilAlerte();
    }

    /**
     * Retourne le pourcentage du seuil d'alerte atteint
     * Utile pour affichage graphique (jauge)
     */
    public double getPourcentageSeuilAlerte() {
        if (produit == null || produit.getSeuilAlerte() == 0) {
            return 100.0;
        }
        return (quantiteDisponible.doubleValue() / produit.getSeuilAlerte()) * 100.0;
    }
}