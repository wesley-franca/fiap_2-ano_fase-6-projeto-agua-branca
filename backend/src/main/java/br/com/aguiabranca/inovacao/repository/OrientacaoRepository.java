package br.com.aguiabranca.inovacao.repository;

import br.com.aguiabranca.inovacao.domain.Orientacao;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrientacaoRepository extends MongoRepository<Orientacao, String> {

    List<Orientacao> findByAtivoTrueOrderByCriadoEmDesc();

    Optional<Orientacao> findByIdAndAtivoTrue(String id);
}
