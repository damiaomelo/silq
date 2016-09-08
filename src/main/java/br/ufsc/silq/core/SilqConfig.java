package br.ufsc.silq.core;

public class SilqConfig {

	/**
	 * Número máximo de conceitos atribuídos a um trabalho/artigo avaliado.
	 */
	public static final int MAX_SIMILARITY_RESULTS = 5;

	/**
	 * Seta se o serviço de avaliação deve tentar avaliar artigos usando similaridade do título
	 * do veículo (equivalente à avaliação de trabalhos) caso a avaliação por ISSN não retorne nenhum conceito.
	 * TODO (bonetti): provar que isso ajuda
	 */
	public static final boolean AVALIAR_ARTIGO_POR_SIMILARIDADE = false;

	/**
	 * Indica se o algoritmo de avaliação deve considerar feedbacks para rankeamento dos resultados.
	 */
	public static final boolean AVALIAR_USANDO_FEEDBACK = true;
}
