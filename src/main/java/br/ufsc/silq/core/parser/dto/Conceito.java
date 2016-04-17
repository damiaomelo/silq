package br.ufsc.silq.core.parser.dto;

import lombok.Data;

@Data
public class Conceito implements Comparable<Conceito> {

	private final String tituloVeiculo;
	private final String conceito;
	private final String similaridade;
	private final Integer ano;

	@Override
	public int compareTo(Conceito o) {
		return o.getSimilaridade().compareTo(this.getSimilaridade());
	}

}
