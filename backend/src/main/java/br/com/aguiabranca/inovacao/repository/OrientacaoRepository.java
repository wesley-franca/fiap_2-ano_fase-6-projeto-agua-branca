package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Orientacao;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrientacaoRepository extends MongoRepository<Orientacao, String> {
}
