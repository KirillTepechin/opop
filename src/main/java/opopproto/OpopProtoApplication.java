package opopproto;

import opopproto.model.Head;
import opopproto.repository.HeadRepository;
import opopproto.service.HeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class OpopProtoApplication implements CommandLineRunner {
	@Autowired
	private HeadRepository headRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(OpopProtoApplication.class, args);
	}

	@Override
	public void run(String... args) {
		if (headRepository.findByLogin("user1") == null) {
			final Head head = Head.builder()
					.name("Кирилл")
					.surname("Тепечин")
					.patronymic("Дмитриевич")
					.password(passwordEncoder.encode("user1"))
					.login("user1")
					.build();
			headRepository.save(head);
		}
		if (headRepository.findByLogin("user2") == null) {
			final Head head = Head.builder()
					.name("Кирилл")
					.surname("Тепечин")
					.patronymic("Дмитриевич")
					.password(passwordEncoder.encode("user2"))
					.login("user2")
					.build();
			headRepository.save(head);
		}
	}
}
