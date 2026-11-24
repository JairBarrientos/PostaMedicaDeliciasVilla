package com.postamedica.rest.infraestructure.persistence.repositories;

import com.postamedica.rest.domain.model.Feriado;
import com.postamedica.rest.domain.repository.FeriadoRepository;
import com.postamedica.rest.infraestructure.persistence.entities.FeriadoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaFeriadoRepository implements FeriadoRepository {

    private final SpringDataFeriadoRepository springDataRepo;

    @Override
    public Feriado save(Feriado f) {
        FeriadoEntity entity = FeriadoEntity.builder()
                .id(f.getId())
                .fecha(f.getFecha())
                .motivo(f.getMotivo())
                .build();
        return toDomain(springDataRepo.save(entity));
    }

    @Override
    public boolean existsByFecha(LocalDate fecha) {
        return springDataRepo.existsByFecha(fecha);
    }

    @Override
    public void deleteByFecha(LocalDate fecha) {
        springDataRepo.deleteByFecha(fecha);
    }

    @Override
    public List<Feriado> findByFechaBetween(LocalDate inicio, LocalDate fin) {
        return springDataRepo.findByFechaBetween(inicio, fin)
                .stream().map(this::toDomain).collect(Collectors.toList());
    }

    private Feriado toDomain(FeriadoEntity e) {
        Feriado f = new Feriado();
        f.setId(e.getId());
        f.setFecha(e.getFecha());
        f.setMotivo(e.getMotivo());
        return f;
    }
}