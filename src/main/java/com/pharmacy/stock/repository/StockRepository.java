package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.Produit;
import com.pharmacy.stock.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByProduit(Produit produit);
    List<Stock> findByQuantiteDisponibleLessThanEqual(Integer seuil);
}