package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.infraestructure.persistence.entities.SabiasQueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataSabiasQueRepository extends JpaRepository<SabiasQueEntity, Long> {
}