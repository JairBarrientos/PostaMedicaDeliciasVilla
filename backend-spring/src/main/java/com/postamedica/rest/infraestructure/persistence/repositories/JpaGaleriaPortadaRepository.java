package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.GaleriaPortada;
import com.postamedica.rest.domain.repository.GaleriaPortadaRepository;
import com.postamedica.rest.infraestructure.persistence.entities.GaleriaPortadaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaGaleriaPortadaRepository implements GaleriaPortadaRepository {

    private final SpringDataGaleriaPortadaRepository springDataRepo;

    @Override
    public GaleriaPortada save(GaleriaPortada g) {
        return springDataRepo.save(GaleriaPortadaEntity.fromDomain(g)).toDomain();
    }

    @Override
    public Optional<GaleriaPortada> findById(Long id) {
        return springDataRepo.findById(id).map(GaleriaPortadaEntity::toDomain);
    }

    @Override
    public List<GaleriaPortada> findAllOrdenado() {
        return springDataRepo.findAllByOrderByOrdenAsc()
                .stream().map(GaleriaPortadaEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByOrden(Integer orden) {
        return springDataRepo.existsByOrden(orden);
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public long count() {
        return springDataRepo.count();
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }
}