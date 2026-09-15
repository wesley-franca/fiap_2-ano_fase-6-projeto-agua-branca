package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Projeto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjetoRepository extends MongoRepository<Projeto, String> {
}
