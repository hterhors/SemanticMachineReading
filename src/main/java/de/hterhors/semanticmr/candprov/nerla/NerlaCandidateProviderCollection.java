package de.hterhors.semanticmr.candprov.nerla;

import java.util.ArrayList;
import java.util.List;

public class NerlaCandidateProviderCollection {

	final private List<INERLACandidateProvider> candidateProvider = new ArrayList<>();

	public NerlaCandidateProviderCollection(INERLACandidateProvider p) {
		candidateProvider.add(p);
	}

	public List<INERLACandidateProvider> getCandidateProvider() {
		return candidateProvider;
	}

}
