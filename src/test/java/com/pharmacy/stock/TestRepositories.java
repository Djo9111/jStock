package com.pharmacy.stock;

import com.pharmacy.stock.enums.MethodeValorisation;
import com.pharmacy.stock.enums.Role;
import com.pharmacy.stock.enums.TypeMouvement;
import com.pharmacy.stock.model.*;
import com.pharmacy.stock.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TestRepositories {

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private MouvementRepository mouvementRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ArrivageRepository arrivageRepository;

    @Autowired
    private AllocationSortieRepository allocationSortieRepository;

    @Test
    void testCategorieRepository() {
        // Créer une catégorie
        Categorie categorie = new Categorie("Médicaments", "Produits pharmaceutiques");
        categorieRepository.save(categorie);

        // Vérifier qu'elle est sauvegardée
        assertThat(categorie.getId()).isNotNull();

        // Rechercher par nom
        Categorie found = categorieRepository.findByNom("Médicaments").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getNom()).isEqualTo("Médicaments");

        System.out.println("✅ CategorieRepository fonctionne !");
    }

    @Test
    void testFournisseurRepository() {
        // Créer un fournisseur
        Fournisseur fournisseur = new Fournisseur(
                "Pharma Distributeurs Dakar",
                "33 822 XX XX",
                "contact@pharma.sn",
                "Dakar, Sénégal"
        );
        fournisseurRepository.save(fournisseur);

        // Vérifier
        assertThat(fournisseur.getId()).isNotNull();

        // Rechercher par nom
        List<Fournisseur> found = fournisseurRepository.findByNomContainingIgnoreCase("pharma");
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getNom()).contains("Pharma");

        System.out.println("✅ FournisseurRepository fonctionne !");
    }

    @Test
    void testUtilisateurRepository() {
        // Créer un utilisateur
        Utilisateur utilisateur = new Utilisateur(
                "Fatou Diop",
                "fatou@pharmacy.sn",
                "password123",
                Role.GESTIONNAIRE
        );
        utilisateurRepository.save(utilisateur);

        // Vérifier
        assertThat(utilisateur.getId()).isNotNull();

        // Rechercher par email
        Utilisateur found = utilisateurRepository.findByEmail("fatou@pharmacy.sn").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getNomComplet()).isEqualTo("Fatou Diop");

        // Rechercher par rôle
        List<Utilisateur> gestionnaires = utilisateurRepository.findByRole(Role.GESTIONNAIRE);
        assertThat(gestionnaires).hasSize(1);

        System.out.println("✅ UtilisateurRepository fonctionne !");
    }

    @Test
    void testProduitRepository() {
        // Créer une catégorie d'abord
        Categorie categorie = new Categorie("Médicaments", "Produits pharmaceutiques");
        categorieRepository.save(categorie);

        // Créer un produit
        Produit produit = new Produit(
                "Paracétamol 500mg",
                "Anti-douleur et antipyrétique",
                "3401234567890",
                50,
                MethodeValorisation.CMUP
        );
        produit.setCategorie(categorie);
        produitRepository.save(produit);

        // Vérifier
        assertThat(produit.getId()).isNotNull();

        // Rechercher par code-barre
        Produit found = produitRepository.findByCodeBarre("3401234567890").orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getNom()).isEqualTo("Paracétamol 500mg");

        // Rechercher par catégorie
        List<Produit> produits = produitRepository.findByCategorie(categorie);
        assertThat(produits).hasSize(1);

        System.out.println("✅ ProduitRepository fonctionne !");
    }

    @Test
    void testMouvementRepository() {
        // Préparer les données
        Categorie categorie = new Categorie("Médicaments", "Produits pharmaceutiques");
        categorieRepository.save(categorie);

        Produit produit = new Produit("Paracétamol 500mg", "Anti-douleur", "3401234567890", 50, MethodeValorisation.CMUP);
        produit.setCategorie(categorie);
        produitRepository.save(produit);

        Fournisseur fournisseur = new Fournisseur("Pharma Dist", "33 822 XX XX", "contact@pharma.sn", "Dakar");
        fournisseurRepository.save(fournisseur);

        Utilisateur utilisateur = new Utilisateur("Fatou Diop", "fatou@pharmacy.sn", "password123", Role.GESTIONNAIRE);
        utilisateurRepository.save(utilisateur);

        // Créer un mouvement
        Mouvement mouvement = new Mouvement(
                LocalDateTime.now(),
                TypeMouvement.ENTREE,
                10,
                new BigDecimal("1000"),
                "FACT-2026-001",
                "Première commande"
        );
        mouvement.setProduit(produit);
        mouvement.setFournisseur(fournisseur);
        mouvement.setUtilisateur(utilisateur);
        mouvementRepository.save(mouvement);

        // Vérifier
        assertThat(mouvement.getId()).isNotNull();
        assertThat(mouvement.getMontantTotal()).isEqualByComparingTo(new BigDecimal("10000")); // 10 × 1000

        // Rechercher par type
        List<Mouvement> entrees = mouvementRepository.findByTypeMouvement(TypeMouvement.ENTREE);
        assertThat(entrees).hasSize(1);

        // Rechercher par produit
        List<Mouvement> mouvementsProduit = mouvementRepository.findByProduitOrderByDateMouvementDesc(produit);
        assertThat(mouvementsProduit).hasSize(1);

        System.out.println("✅ MouvementRepository fonctionne !");
    }

    @Test
    void testStockRepository() {
        // Préparer les données
        Categorie categorie = new Categorie("Médicaments", "Produits pharmaceutiques");
        categorieRepository.save(categorie);

        Produit produit = new Produit("Paracétamol 500mg", "Anti-douleur", "3401234567890", 50, MethodeValorisation.CMUP);
        produit.setCategorie(categorie);
        produitRepository.save(produit);

        // Créer un stock
        Stock stock = new Stock(produit, 100, new BigDecimal("1000"));
        stockRepository.save(stock);

        // Vérifier
        assertThat(stock.getId()).isNotNull();
        assertThat(stock.getValeurStockTotal()).isEqualByComparingTo(new BigDecimal("100000")); // 100 × 1000

        // Rechercher par produit
        Stock found = stockRepository.findByProduit(produit).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getQuantiteDisponible()).isEqualTo(100);

        System.out.println("✅ StockRepository fonctionne !");
    }

    @Test
    void testArrivageRepository() {
        // Préparer les données
        Categorie categorie = new Categorie("Médicaments", "Produits pharmaceutiques");
        categorieRepository.save(categorie);

        Produit produit = new Produit("Nescafé", "Café soluble", "3401999999999", 20, MethodeValorisation.FIFO);
        produit.setCategorie(categorie);
        produitRepository.save(produit);

        Fournisseur fournisseur = new Fournisseur("Nestlé Sénégal", "33 800 XX XX", "nestle@sn.com", "Dakar");
        fournisseurRepository.save(fournisseur);

        Utilisateur utilisateur = new Utilisateur("Moussa Sarr", "moussa@pharmacy.sn", "password123", Role.GESTIONNAIRE);
        utilisateurRepository.save(utilisateur);

        Mouvement mouvement = new Mouvement(LocalDateTime.now(), TypeMouvement.ENTREE, 5, new BigDecimal("1000"), "FACT-001", "Commande 1");
        mouvement.setProduit(produit);
        mouvement.setFournisseur(fournisseur);
        mouvement.setUtilisateur(utilisateur);
        mouvementRepository.save(mouvement);

        // Créer un arrivage (lot)
        Arrivage arrivage = new Arrivage(
                produit,
                LocalDateTime.now(),
                5,
                new BigDecimal("1000"),
                fournisseur,
                mouvement
        );
        arrivage.setNumeroLot("LOT-2026-001");
        arrivageRepository.save(arrivage);

        // Vérifier
        assertThat(arrivage.getId()).isNotNull();
        assertThat(arrivage.getQuantiteRestante()).isEqualTo(5);
        assertThat(arrivage.estEpuise()).isFalse();

        // Rechercher les lots disponibles pour FIFO
        List<Arrivage> lotsDisponibles = arrivageRepository
                .findByProduitAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(produit, 0);
        assertThat(lotsDisponibles).hasSize(1);

        System.out.println("✅ ArrivageRepository fonctionne !");
    }

    @Test
    void testScenarioCompletNescafe() {
        System.out.println("\n🎯 TEST SCÉNARIO COMPLET NESCAFÉ\n");

        // 1. Données de base
        Categorie categorie = new Categorie("Produits alimentaires", "Café, thé, etc.");
        categorieRepository.save(categorie);

        Produit nescafe = new Produit("Nescafé 200g", "Café soluble", "3401987654321", 10, MethodeValorisation.FIFO);
        nescafe.setCategorie(categorie);
        produitRepository.save(nescafe);

        Fournisseur fournisseurA = new Fournisseur("Fournisseur A", "33 800 AA AA", "fa@sn.com", "Dakar");
        fournisseurRepository.save(fournisseurA);

        Fournisseur fournisseurB = new Fournisseur("Fournisseur B", "33 800 BB BB", "fb@sn.com", "Dakar");
        fournisseurRepository.save(fournisseurB);

        Utilisateur gestionnaire = new Utilisateur("Fatou Diop", "fatou@pharmacy.sn", "password123", Role.GESTIONNAIRE);
        utilisateurRepository.save(gestionnaire);

        // 2. ENTREE 01/10 : 5 à 1000 FCFA
        System.out.println("📦 ENTREE 01/10 : 5 Nescafé à 1000 FCFA");

        Mouvement m1 = new Mouvement(
                LocalDateTime.of(2026, 10, 1, 10, 0),
                TypeMouvement.ENTREE,
                5,
                new BigDecimal("1000"),
                "FACT-001",
                "Première commande"
        );
        m1.setProduit(nescafe);
        m1.setFournisseur(fournisseurA);
        m1.setUtilisateur(gestionnaire);
        mouvementRepository.save(m1);

        Arrivage a1 = new Arrivage(nescafe, m1.getDateMouvement(), 5, new BigDecimal("1000"), fournisseurA, m1);
        a1.setNumeroLot("LOT-A-001");
        arrivageRepository.save(a1);

        System.out.println("   → Arrivage A1 créé : 5 unités à 1000 FCFA");

        // 3. ENTREE 05/10 : 3 à 1200 FCFA
        System.out.println("📦 ENTREE 05/10 : 3 Nescafé à 1200 FCFA");

        Mouvement m2 = new Mouvement(
                LocalDateTime.of(2026, 10, 5, 14, 0),
                TypeMouvement.ENTREE,
                3,
                new BigDecimal("1200"),
                "FACT-002",
                "Deuxième commande"
        );
        m2.setProduit(nescafe);
        m2.setFournisseur(fournisseurB);
        m2.setUtilisateur(gestionnaire);
        mouvementRepository.save(m2);

        Arrivage a2 = new Arrivage(nescafe, m2.getDateMouvement(), 3, new BigDecimal("1200"), fournisseurB, m2);
        a2.setNumeroLot("LOT-B-001");
        arrivageRepository.save(a2);

        System.out.println("   → Arrivage A2 créé : 3 unités à 1200 FCFA");

        // 4. SORTIE 10/10 : 6 unités en FIFO
        System.out.println("🛒 SORTIE 10/10 : 6 Nescafé (FIFO)");

        Mouvement m3 = new Mouvement(
                LocalDateTime.of(2026, 10, 10, 16, 0),
                TypeMouvement.SORTIE,
                -6,
                new BigDecimal("2000"), // Prix de vente
                "VENTE-001",
                "Vente client"
        );
        m3.setProduit(nescafe);
        m3.setUtilisateur(gestionnaire);
        mouvementRepository.save(m3);

        // Appliquer FIFO
        List<Arrivage> lots = arrivageRepository
                .findByProduitAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(nescafe, 0);

        int quantiteAConsommer = 6;
        for (Arrivage lot : lots) {
            if (quantiteAConsommer == 0) break;

            int qtePrise = Math.min(lot.getQuantiteRestante(), quantiteAConsommer);

            AllocationSortie alloc = new AllocationSortie(m3, lot, qtePrise, lot.getPrixAchatUnitaire());
            allocationSortieRepository.save(alloc);

            lot.consommer(qtePrise);
            arrivageRepository.save(lot);

            quantiteAConsommer -= qtePrise;

            System.out.println("   → Allocation : " + qtePrise + " de " + lot.getNumeroLot() +
                    " à " + lot.getPrixAchatUnitaire() + " = " + alloc.getMontantCout() + " FCFA");
        }

        // 5. Vérifications finales
        System.out.println("\n📊 VÉRIFICATIONS FINALES :");

        // Vérifier allocations
        List<AllocationSortie> allocations = allocationSortieRepository.findByMouvementSortie(m3);
        assertThat(allocations).hasSize(2);

        BigDecimal coutTotal = allocations.stream()
                .map(AllocationSortie::getMontantCout)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("   Coût total exact = " + coutTotal + " FCFA");
        assertThat(coutTotal).isEqualByComparingTo(new BigDecimal("6200")); // 5×1000 + 1×1200

        BigDecimal prixVente = new BigDecimal("12000"); // 6×2000
        BigDecimal marge = prixVente.subtract(coutTotal);
        System.out.println("   Prix vente = " + prixVente + " FCFA");
        System.out.println("   Marge brute = " + marge + " FCFA");
        assertThat(marge).isEqualByComparingTo(new BigDecimal("5800"));

        // Vérifier arrivages
        Arrivage a1Updated = arrivageRepository.findById(a1.getId()).orElseThrow();
        Arrivage a2Updated = arrivageRepository.findById(a2.getId()).orElseThrow();

        System.out.println("   Arrivage A1 restant : " + a1Updated.getQuantiteRestante() + " (épuisé : " + a1Updated.estEpuise() + ")");
        System.out.println("   Arrivage A2 restant : " + a2Updated.getQuantiteRestante());

        assertThat(a1Updated.estEpuise()).isTrue();
        assertThat(a2Updated.getQuantiteRestante()).isEqualTo(2);

        System.out.println("\n✅ SCÉNARIO NESCAFÉ COMPLET RÉUSSI !");
    }
}