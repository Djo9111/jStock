package com.pharmacy.stock.model;

import com.pharmacy.stock.enums.MethodeValorisation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Produit - Catalogue des produits
 */
@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Long id;
    
    @Column(name = "nom", nullable = false, length = 200)
    private String nom;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "code_barre", unique = true, length = 50)
    private String codeBarre;
    
    @Column(name = "seuil_alerte", nullable = false)
    private Integer seuilAlerte;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "methode_valorisation", nullable = false, length = 10)
    private MethodeValorisation methodeValorisation;
    
    /**
     * Catégorie du produit
     * Relation Many-to-One avec Categorie
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;
    
    /**
     * Historique des mouvements de ce produit
     * Relation bidirectionnelle 1-N avec Mouvement
     */
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mouvement> mouvements = new ArrayList<>();
    
    /**
     * État de stock actuel de ce produit
     * Relation bidirectionnelle 1-1 avec Stock
     */
    @OneToOne(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;
    
    /**
     * Liste des arrivages (lots) de ce produit
     * Relation bidirectionnelle 1-N avec Arrivage
     */
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    private List<Arrivage> arrivages = new ArrayList<>();
    
    /**
     * Constructeur sans relations (pour création)
     */
    public Produit(String nom, String description, String codeBarre, 
                   Integer seuilAlerte, MethodeValorisation methodeValorisation) {
        this.nom = nom;
        this.description = description;
        this.codeBarre = codeBarre;
        this.seuilAlerte = seuilAlerte;
        this.methodeValorisation = methodeValorisation;
    }
    
    /**
     * Vérifie si le produit est sous le seuil d'alerte
     */
    public boolean estSousSeuilAlerte() {
        if (stock == null) {
            return false;
        }
        return stock.getQuantiteDisponible() <= seuilAlerte;
    }
    
    /**
     * Retourne la quantité en stock (0 si pas de stock)
     */
    public Integer getQuantiteEnStock() {
        return stock != null ? stock.getQuantiteDisponible() : 0;
    }
    
    /**
     * Ajouter un mouvement à l'historique de ce produit
     */
    public void ajouterMouvement(Mouvement mouvement) {
        mouvements.add(mouvement);
        mouvement.setProduit(this);
    }
    
    /**
     * Définir le stock de ce produit
     */
    public void setStock(Stock stock) {
        this.stock = stock;
        if (stock != null) {
            stock.setProduit(this);
        }
    }
    
    /**
     * Ajouter un arrivage (lot) à ce produit
     */
    public void ajouterArrivage(Arrivage arrivage) {
        arrivages.add(arrivage);
        arrivage.setProduit(this);
    }
    
    /**
     * Retourne les arrivages non épuisés, triés par date (FIFO)
     */
    public List<Arrivage> getArrivagesDisponibles() {
        return arrivages.stream()
            .filter(a -> !a.estEpuise())
            .sorted((a1, a2) -> a1.getDateEntree().compareTo(a2.getDateEntree()))
            .toList();
    }
    
    /**
     * Retourne les arrivages périmés ou proches de la péremption
     */
    public List<Arrivage> getArrivagesPerimes() {
        return arrivages.stream()
            .filter(Arrivage::estPerime)
            .toList();
    }
    
    /**
     * Retourne les arrivages proches de la péremption (< 30 jours)
     */
    public List<Arrivage> getArrivagesProchesPeremption() {
        return arrivages.stream()
            .filter(a -> !a.estPerime() && a.procheDeLaPeremption())
            .sorted((a1, a2) -> a1.getDatePeremption().compareTo(a2.getDatePeremption()))
            .toList();
    }
}
