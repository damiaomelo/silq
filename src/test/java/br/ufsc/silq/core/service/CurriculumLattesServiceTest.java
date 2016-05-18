package br.ufsc.silq.core.service;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import br.ufsc.silq.Fixtures;
import br.ufsc.silq.WebContextTest;
import br.ufsc.silq.core.exception.SilqException;
import br.ufsc.silq.core.persistence.entities.CurriculumLattes;
import br.ufsc.silq.core.persistence.repository.CurriculumLattesRepository;

public class CurriculumLattesServiceTest extends WebContextTest {

	@Inject
	private CurriculumLattesService curriculumService;

	@Inject
	private CurriculumLattesRepository curriculumRepository;

	@Inject
	private UsuarioService usuarioService;

	@Test
	public void testSaveComReuso() throws SilqException {
		CurriculumLattes lattes1 = this.curriculumService.saveFromUpload(Fixtures.CHRISTIANE_ZIP_UPLOAD);
		CurriculumLattes lattes2 = this.curriculumService.saveFromUpload(Fixtures.CHRISTIANE_ZIP_UPLOAD);
		Assertions.assertThat(lattes2.getId()).isEqualTo(lattes1.getId());
	}

	@Test
	public void testIsCurriculumEmUso() throws SilqException {
		CurriculumLattes lattes1 = this.curriculumService.saveFromUpload(Fixtures.CHRISTIANE_ZIP_UPLOAD);
		Assertions.assertThat(this.curriculumService.isCurriculumEmUso(lattes1)).isFalse();

		this.loginUser();
		CurriculumLattes lattes2 = this.usuarioService.saveCurriculumUsuarioLogado(Fixtures.RAUL_ZIP_UPLOAD);
		Assertions.assertThat(this.curriculumService.isCurriculumEmUso(lattes2)).isTrue();

		this.usuarioService.removeCurriculumUsuarioLogado();
		Assertions.assertThat(this.curriculumService.isCurriculumEmUso(lattes2)).isFalse();
	}

	@Test
	public void testReleaseCurriculum() throws SilqException {
		this.loginUser();
		CurriculumLattes lattes2 = this.usuarioService.saveCurriculumUsuarioLogado(Fixtures.RAUL_ZIP_UPLOAD);

		Assertions.assertThat(this.curriculumRepository.count()).isEqualTo(1);
		this.curriculumService.releaseCurriculum(lattes2); // Não deve remover: currículo ainda em uso
		Assertions.assertThat(this.curriculumRepository.count()).isEqualTo(1);

		this.usuarioService.removeCurriculumUsuarioLogado();
		Assertions.assertThat(this.curriculumRepository.count()).isEqualTo(0);
	}
}
