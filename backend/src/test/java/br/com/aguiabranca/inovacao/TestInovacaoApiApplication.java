package br.com.aguiabranca.inovacao;

import org.springframework.boot.SpringApplication;

public class TestInovacaoApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(InovacaoApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
