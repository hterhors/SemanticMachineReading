package de.hterhors.semanticmr.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.tools.AutomatedSectionifcation.ESection;

public class KeyTermExtractor {

	private KeyTermExtractor() {
	}

	public static Set<String> getKeyTerms(List<Instance> instances) {

		Map<String, List<String>> documents = new HashMap<>();
		for (Instance trainInstance : instances) {

			Set<Integer> invMSentences = new HashSet<>();

			for (DocumentLinkedAnnotation invM : trainInstance.getGoldAnnotations()
					.<DocumentLinkedAnnotation>getAnnotations()) {
				invMSentences.add(invM.getSentenceIndex());
			}
			AutomatedSectionifcation sec = AutomatedSectionifcation.getInstance(trainInstance);

			for (int sentenceIndex = 0; sentenceIndex < trainInstance.getDocument()
					.getNumberOfSentences(); sentenceIndex++) {

				if (sec.getSection(sentenceIndex) != ESection.RESULTS)
					continue;

				final String docName = String.valueOf(invMSentences.contains(sentenceIndex));
				documents.putIfAbsent(docName, new ArrayList<>());
				for (DocumentToken documentToken : trainInstance.getDocument().getSentenceByIndex(sentenceIndex)) {

					if (documentToken.isPunctuation() || documentToken.isStopWord())
						continue;
					if (documentToken.getLength() == 1)
						continue;
					if (documentToken.isNumber())
						continue;

					documents.get(docName).add(documentToken.getText());
				}

			}

		}
		if (!(documents.containsKey("true") && documents.containsKey("false")))
			return Collections.emptySet();

		Map<String, Double> frequendInvSentenceTerms = TFIDF.getTFs(documents.get("true"), true);
		Map<String, Double> frequendNOTInvSentenceTerms = TFIDF.getTFs(documents.get("false"), true);

		Set<String> keyTerms = new HashSet<>();

		for (String freqInvterm : frequendInvSentenceTerms.keySet()) {

			if (!(frequendInvSentenceTerms.get(freqInvterm) > frequendNOTInvSentenceTerms.getOrDefault(freqInvterm,
					0D)))
				continue;

			if (frequendInvSentenceTerms.get(freqInvterm) < 0.01)
				continue;

			keyTerms.add(freqInvterm);
		}
		return keyTerms;

	}
	
	

}
