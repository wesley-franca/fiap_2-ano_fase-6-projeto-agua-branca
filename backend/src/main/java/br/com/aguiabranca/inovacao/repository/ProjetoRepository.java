package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {

    List<Projeto> findAllByOrderByCriadoEmDesc();

    boolean existsByIdeiaOrigemId(String ideiaOrigemId);
}
