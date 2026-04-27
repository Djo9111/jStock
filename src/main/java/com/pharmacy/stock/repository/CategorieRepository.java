package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    Optional<Categorie> findByNom(String nom);
}