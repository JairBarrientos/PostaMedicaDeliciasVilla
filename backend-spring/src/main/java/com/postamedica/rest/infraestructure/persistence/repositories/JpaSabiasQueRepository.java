package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.SabiasQue;
import com.postamedica.rest.domain.repository.SabiasQueRepository;
import com.postamedica.rest.infraestructure.persistence.entities.SabiasQueEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaSabiasQueRepository implements SabiasQueRepository {

    private final SpringDataSabiasQueRepository springDataRepo;

    @Override
    public SabiasQue save(SabiasQue sabiasQue) {
        SabiasQueEntity entity = SabiasQueEntity.fromDomain(sabiasQue);
        return springDataRepo.save(entity).toDomain();
    }

    @Override
    public Optional<SabiasQue> findById(Long id) {
        return springDataRepo.findById(id).map(SabiasQueEntity::toDomain);
    }

    @Override
    public List<SabiasQue> findAll() {
        return springDataRepo.findAll().stream()
                .map(SabiasQueEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return springDataRepo.count();
    }

    @Override
    public boolean existsById(Long id) {
        return springDataRepo.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        springDataRepo.deleteById(id);
    }
}