package de.hterhors.semanticmr.candprov.nerla;

import java.util.ArrayList;
import java.util.List;

public class NerlaCandidateProviderCollection {

	final private List<INerlaCandidateProvider> candidateProvider = new ArrayList<>();

	public NerlaCandidateProviderCollection(INerlaCandidateProvider p) {
		candidateProvider.add(p);
	}

	public List<INerlaCandidateProvider> getCandidateProvider() {
		return candidateProvider;
	}

}
