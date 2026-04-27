package com.pharmacy.stock.repository;

import com.pharmacy.stock.model.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FournisseurRepository extends JpaRepository<Fournisseur, Long> {
    List<Fournisseur> findByNomContainingIgnoreCase(String nom);
}