package com.pharmacy.stock.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Categorie - Classification des produits
 * Exemples : Médicaments, Matériel médical, Parapharmacie
 */
@Entity
@Table(name = "categorie")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Liste des produits de cette catégorie
     * Relation bidirectionnelle 1-N avec Produit
     */
    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Produit> produits = new ArrayList<>();

    /**
     * Constructeur sans la liste de produits (pour création)
     */
    public Categorie(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    /**
     * Ajouter un produit à cette catégorie
     */
    public void ajouterProduit(Produit produit) {
        produits.add(produit);
        produit.setCategorie(this);
    }

    /**
     * Retirer un produit de cette catégorie
     */
    public void retirerProduit(Produit produit) {
        produits.remove(produit);
        produit.setCategorie(null);
    }
}