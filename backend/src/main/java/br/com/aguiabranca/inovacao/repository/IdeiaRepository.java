package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Ideia;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IdeiaRepository extends MongoRepository<Ideia, String> {
}
