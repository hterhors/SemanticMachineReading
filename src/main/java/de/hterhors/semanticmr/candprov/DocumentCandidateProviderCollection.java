package de.hterhors.semanticmr.candprov;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.variables.Document;

public class DocumentCandidateProviderCollection {

	/**
	 * Ugly a.f.
	 * 
	 * Flag if this document independent candidate retrieval was already added.
	 * 
	 */
	private Set<Document> addedEntityTypeCandidateProvider = new HashSet<>();

	private EntityTypeCandidateProvider entityTypeCandidateProvider;

	private Map<Document, List<GeneralCandidateProvider>> literalCandidateProvider = new HashMap<>();
	private Map<Document, List<EntityTemplateCandidateProvider>> entityTemplateCandidateProvider = new HashMap<>();

	private Map<Document, List<ISlotFillerCandidateProvider<?>>> candidateProviderPerDocument = new HashMap<>();

	public void addLiteralCandidateProvider(GeneralCandidateProvider candidateProvider) {
		literalCandidateProvider.putIfAbsent(candidateProvider.getRelatedDocument(), new ArrayList<>());
		literalCandidateProvider.get(candidateProvider.getRelatedDocument()).add(candidateProvider);

		candidateProviderPerDocument.putIfAbsent(candidateProvider.getRelatedDocument(), new ArrayList<>());
		candidateProviderPerDocument.get(candidateProvider.getRelatedDocument()).add(candidateProvider);
	}

	public List<GeneralCandidateProvider> getLiteralCandidateProvider(Document relatedDocument) {
		return literalCandidateProvider.get(relatedDocument);
	}

	public void addEntityTemplateCandidateProvider(EntityTemplateCandidateProvider candidateProvider) {
		entityTemplateCandidateProvider.putIfAbsent(candidateProvider.getRelatedDocument(), new ArrayList<>());
		entityTemplateCandidateProvider.get(candidateProvider.getRelatedDocument()).add(candidateProvider);
		candidateProviderPerDocument.putIfAbsent(candidateProvider.getRelatedDocument(), new ArrayList<>());
		candidateProviderPerDocument.get(candidateProvider.getRelatedDocument()).add(candidateProvider);
	}

	public List<EntityTemplateCandidateProvider> getEntityTemplateCandidateProvider(Document relatedDocument) {
		return entityTemplateCandidateProvider.get(relatedDocument);
	}

	public void setEntityTypeCandidateProvider(EntityTypeCandidateProvider candidateProvider) {
		this.entityTypeCandidateProvider = candidateProvider;
	}

	public EntityTypeCandidateProvider getEntityTypeCandidateProvider() {
		return entityTypeCandidateProvider;

	}

	public List<ISlotFillerCandidateProvider<?>> getCandidateProviderForDocument(Document relatedDocument) {

		/**
		 * Ugly a.f.
		 */

		List<ISlotFillerCandidateProvider<?>> list = candidateProviderPerDocument.get(relatedDocument);

		if (list == null) {
			if (entityTypeCandidateProvider != null && !addedEntityTypeCandidateProvider.contains(relatedDocument)) {
				candidateProviderPerDocument.put(relatedDocument, new ArrayList<>());
				candidateProviderPerDocument.get(relatedDocument).add(entityTypeCandidateProvider);
				addedEntityTypeCandidateProvider.add(relatedDocument);
			}

			return Collections.emptyList();
		}

		if (entityTypeCandidateProvider != null && !addedEntityTypeCandidateProvider.contains(relatedDocument)) {
			list.add(entityTypeCandidateProvider);
			addedEntityTypeCandidateProvider.add(relatedDocument);
		}

		return list;
	}

}
