package br.ufsc.silq.core.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.ufsc.silq.core.SilqConfig;
import br.ufsc.silq.core.data.Conceito;
import br.ufsc.silq.core.data.Conceituavel;
import br.ufsc.silq.core.data.NivelSimilaridade;
import br.ufsc.silq.core.data.SimilarityResult;
import br.ufsc.silq.core.forms.AvaliarForm;
import br.ufsc.silq.core.forms.QualisSearchForm;
import br.ufsc.silq.core.persistence.entities.QualisEvento;
import br.ufsc.silq.core.persistence.entities.QualisPeriodico;
import br.ufsc.silq.core.utils.SilqStringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço de similaridade para avaliação e consulta dos dados Qualis.
 * Encapsula as consultas nativas do PostgreSQL que realizam busca por similaridade.
 */
@Service
@Transactional
@Slf4j
public class SimilarityService {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Seta o nível de similaridade mínimo (threshold) que será usado para as queries de similaridade no banco.
	 * Utiliza a função nativa do PostgreSQL `set_limit()`.
	 *
	 * @param value Valor numérico de 0 a 1 representando o threshold de similaridade.
	 */
	public void setSimilarityThreshold(Float value) {
		Query query = this.em.createNativeQuery("SELECT set_limit(?1)");
		query.setParameter(1, value);
		query.getSingleResult();
	}

	/**
	 * Obtém os conceitos de um evento ou periódico realizando uma busca por similaridade na base Qualis.
	 * Utiliza as funções de similaridade nativas do PostgreSQL.
	 *
	 * @param conceituavel Trabalho ou artigo a ser avaliado.
	 * @param avaliarForm Opções de avaliação.
	 * @param tipoAvaliacao Tipo de avaliação (altera a tabela do banco a ser consultada).
	 * @return A lista de conceitos do veículo.
	 * @throws SQLException Caso haja um erro ao executar o SQL.
	 */
	public List<Conceito> getConceitos(Conceituavel conceituavel, @Valid AvaliarForm avaliarForm, TipoAvaliacao tipoAvaliacao) throws SQLException {
		this.setSimilarityThreshold(avaliarForm.getNivelSimilaridade().getValue());

		String sql = "SELECT " + tipoAvaliacao.getPk() + ", NO_TITULO, NO_ESTRATO, SIMILARITY(NO_TITULO, ?1) AS SML, NU_ANO";
		sql += " FROM " + tipoAvaliacao.getTable() + " WHERE NO_TITULO % ?1";
		sql += " AND NO_AREA_AVALIACAO = ?2";
		sql += " ORDER BY SML DESC, ABS(NU_ANO - ?3) ASC";
		sql += " LIMIT ?4";

		Query query = this.em.createNativeQuery(sql);
		query.setParameter(1, SilqStringUtils.normalizeString(conceituavel.getTituloVeiculo()));
		query.setParameter(2, avaliarForm.getArea().toUpperCase());
		query.setParameter(3, conceituavel.getAno());
		query.setParameter(4, SilqConfig.MAX_SIMILARITY_RESULTS);

		List<Object[]> results = query.getResultList();
		return results.stream()
				.map(this::mapResultToConceito)
				.collect(Collectors.toList());
	}

	private Conceito mapResultToConceito(Object[] obj) {
		return new Conceito(((BigDecimal) obj[0]).longValue(), (String) obj[1], (String) obj[2],
				new NivelSimilaridade((Float) obj[3]), (Integer) obj[4]);
	}

	/**
	 * Realiza uma busca nos dados Qualis dado uma query qualquer.
	 * Utiliza as funções de similaridade nativas do PostgreSQL.
	 *
	 * @param tipo Tipo de avaliação: {@link TipoAvaliacao}.
	 * @param form Form contendo a query e os filtros de busca.
	 * @param pageable Configurações de paginação.
	 * @return Uma lista de Object[] contendo os dados das tuplas similares à query.
	 */
	public List<Object[]> search(TipoAvaliacao tipo, @Valid QualisSearchForm form, Pageable pageable) {
		this.setSimilarityThreshold(0.1F);

		String sql = "SELECT *, SIMILARITY(NO_TITULO, ?1) AS SML";
		sql += " FROM " + tipo.getTable() + " WHERE NO_TITULO % ?1";

		if (StringUtils.isNotBlank(form.getArea())) {
			sql += " AND NO_AREA_AVALIACAO = ?4";
		}

		sql += " ORDER BY SML DESC";
		sql += " LIMIT ?2";
		sql += " OFFSET ?3";

		Query q = this.em.createNativeQuery(sql);
		q.setParameter(1, SilqStringUtils.normalizeString(form.getQuery()));
		q.setParameter(2, pageable.getPageSize());
		q.setParameter(3, pageable.getOffset());

		if (StringUtils.isNotBlank(form.getArea())) {
			q.setParameter(4, form.getArea().toUpperCase());
		}

		return q.getResultList();
	}

	public BigInteger searchCount(TipoAvaliacao tipo, @Valid QualisSearchForm form) {
		this.setSimilarityThreshold(0.1F);

		String sql = "SELECT COUNT(*) AS C";
		sql += " FROM " + tipo.getTable() + " WHERE NO_TITULO % ?1";
		if (StringUtils.isNotBlank(form.getArea())) {
			sql += " AND NO_AREA_AVALIACAO = ?2";
		}

		Query q = this.em.createNativeQuery(sql);
		q.setParameter(1, SilqStringUtils.normalizeString(form.getQuery()));
		if (StringUtils.isNotBlank(form.getArea())) {
			q.setParameter(2, form.getArea().toUpperCase());
		}

		return (BigInteger) q.getSingleResult();
	}

	/**
	 * Mapeia um Object[] retornado por {@link #search} para o tipo {@link QualisEvento}.
	 * O método não checa se conteúdo do parâmetro está bem formado.
	 *
	 * @param result Um Object[] contendo as informações do evento, na ordem correta.
	 * @return Uma nova entidade {@link QualisEvento}.
	 */
	public SimilarityResult<QualisEvento> mapResultToEvento(Object[] result) {
		QualisEvento evento = new QualisEvento();
		evento.setId(((BigDecimal) result[0]).longValue());
		evento.setSigla((String) result[1]);
		evento.setTitulo((String) result[2]);
		evento.setIndiceH(((BigDecimal) result[3]).intValue());
		evento.setEstrato((String) result[4]);
		evento.setAreaAvaliacao((String) result[5]);
		evento.setAno((Integer) result[6]);
		return new SimilarityResult<>(evento, new NivelSimilaridade((Float) result[7]));
	}

	/**
	 * Mapeia um Object[] retornado por {@link #search} para o tipo {@link QualisPeriodico}.
	 * O método não checa se conteúdo do parâmetro está bem formado.
	 *
	 * @param result Um Object[] contendo as informações do periódico, na ordem correta.
	 * @return Uma nova entidade {@link QualisPeriodico}.
	 */
	public SimilarityResult<QualisPeriodico> mapResultToPeriodico(Object[] result) {
		QualisPeriodico periodico = new QualisPeriodico();
		periodico.setId(((BigDecimal) result[0]).longValue());
		periodico.setIssn((String) result[1]);
		periodico.setTitulo((String) result[2]);
		periodico.setEstrato((String) result[3]);
		periodico.setAreaAvaliacao((String) result[4]);
		periodico.setAno((Integer) result[5]);
		return new SimilarityResult<>(periodico, new NivelSimilaridade((Float) result[6]));
	}

	/**
	 * Calcula a similaridade textual entre duas strings.
	 *
	 * @param s1 Primeira string.
	 * @param s2 Segunda string.
	 * @return O {@link NivelSimilaridade} calculado entre as duas strings.
	 */
	public NivelSimilaridade calcularSimilaridade(String s1, String s2) {
		Query q = this.em.createNativeQuery("SELECT similarity(?1, ?2)");
		q.setParameter(1, s1);
		q.setParameter(2, s2);
		Float value = (Float) q.getSingleResult();
		return new NivelSimilaridade(value);
	}

	/**
	 * Tipos de avaliação.
	 */
	@AllArgsConstructor
	@Getter
	public enum TipoAvaliacao {
		PERIODICO("TB_QUALIS_PERIODICO", "CO_SEQ_PERIODICO"),
		EVENTO("TB_QUALIS_EVENTO", "CO_SEQ_EVENTO");

		/**
		 * Nome da tabela utilizada para avaliação.
		 */
		private final String table;

		/**
		 * Nome da coluna chave primária da tabela.
		 */
		private final String pk;
	}
}
