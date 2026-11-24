package com.postamedica.rest.domain.repository;

import com.postamedica.rest.domain.model.SabiasQue;
import java.util.List;
import java.util.Optional;

public interface SabiasQueRepository {
    SabiasQue save(SabiasQue sabiasQue);
    Optional<SabiasQue> findById(Long id);
    List<SabiasQue> findAll();
    long count();
    boolean existsById(Long id);
    void deleteById(Long id);
}