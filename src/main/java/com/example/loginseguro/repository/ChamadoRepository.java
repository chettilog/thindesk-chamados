package com.example.loginseguro.repository;

import com.example.loginseguro.model.Chamado;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChamadoRepository extends MongoRepository<Chamado, String> {
    List<Chamado> findBySolicitanteIdOrderByDataAberturaDesc(String solicitanteId);
}