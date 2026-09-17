package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Ideia;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {

    List<Ideia> findByOperadorIdOrderByCriadoEmDesc(String operadorId);

    List<Ideia> findAllByOrderByCriadoEmDesc();
}
