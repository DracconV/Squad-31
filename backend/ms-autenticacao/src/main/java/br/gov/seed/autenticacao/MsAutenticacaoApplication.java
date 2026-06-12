package br.gov.seed.autenticacao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MsAutenticacaoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsAutenticacaoApplication.class, args);
	}

}
