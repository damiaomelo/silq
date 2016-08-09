package br.ufsc.silq.core.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufsc.silq.core.persistence.entities.FeedbackEvento;
import br.ufsc.silq.core.persistence.entities.Usuario;

public interface FeedbackEventoRepository extends JpaRepository<FeedbackEvento, Long> {

	Optional<FeedbackEvento> findOneByQueryAndUsuario(String query, Usuario usuario);

}
