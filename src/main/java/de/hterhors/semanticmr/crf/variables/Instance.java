package de.hterhors.semanticmr.crf.variables;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.candidateretrieval.nerla.NerlCandidateRetrieval;
import de.hterhors.semanticmr.candidateretrieval.sf.SlotFillingCandidateRetrieval;
import de.hterhors.semanticmr.candidateretrieval.sf.SlotFillingCandidateRetrieval.IFilter;
import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.exploration.SlotFillingExplorer.EExplorationMode;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.exce.MismatchingDocumentsException;

/**
 * The Instance object couples a document and the gold annotation of that
 * document. This class is used during training as training instance, during
 * testing as testing instance and during prediction as prediction instance.
 * 
 * @author hterhors
 *
 */
public class Instance implements Comparable<Instance> {

	/**
	 * The instance document.
	 */
	private final Document document;

	/**
	 * The corresponding gold annotation.
	 */
	private final Annotations groundTruth;

	private final SlotFillingCandidateRetrieval slotFillingCandidateRetrieval = new SlotFillingCandidateRetrieval();

	public void removeCandidateAnnotation(IFilter filter) {
		slotFillingCandidateRetrieval.filterOutAnnotationCandidates(filter);
	}

	public void addCandidateAnnotations(Collection<? extends AbstractAnnotation> candidates) {
		slotFillingCandidateRetrieval.addCandidateAnnotations(candidates);
	}

	public void addCandidateAnnotation(AbstractAnnotation candidate) {
		slotFillingCandidateRetrieval.addCandidateAnnotation(candidate);
	}

	private final NerlCandidateRetrieval nerlaCandidateRetrieval = new NerlCandidateRetrieval();

	public Set<EntityTypeAnnotation> getEntityTypeCandidates(EExplorationMode samplingMode, EntityType entityType) {
		return slotFillingCandidateRetrieval.getEntityTypeCandidates(samplingMode, entityType);
	}

	public Set<AbstractAnnotation> getSlotTypeCandidates(EExplorationMode samplingMode, SlotType slotType) {
		return slotFillingCandidateRetrieval.getSlotTypeCandidates(samplingMode, slotType);
	}

	public void addCandidates(final File dictionaryFile) {
		nerlaCandidateRetrieval.addCandidates(dictionaryFile);
	}

	public void addCandidates(Map<EntityType, Set<String>> dictionary) {
		nerlaCandidateRetrieval.addCandidates(dictionary);
	}

	public void addCandidate(EntityType entityType, Set<String> words) {
		nerlaCandidateRetrieval.addCandidate(entityType, words);
	}

	public void addCandidate(EntityType entityType, String word) {
		nerlaCandidateRetrieval.addCandidate(entityType, word);
	}

	public void addCandidate(EntityType entityType) {
		nerlaCandidateRetrieval.addCandidate(entityType);
	}

	public void addCandidates(Set<EntityType> entityTypes) {
		nerlaCandidateRetrieval.addCandidates(entityTypes);
	}

	public Set<EntityType> getEntityTypeCandidates(String text) {
		return nerlaCandidateRetrieval.getEntityTypeCandidates(text);
	}

	/**
	 * The context of this instance, whether it belongs to train dev or test set.
	 */
	private final EInstanceContext originalContext;

	private EInstanceContext redistributedContext = EInstanceContext.UNSPECIFIED;

	public static interface GoldModificationRule {
		public AbstractAnnotation modify(AbstractAnnotation goldAnnotation);
	}

	public Instance(EInstanceContext context, Document document, Annotations goldAnnotations,
			Collection<GoldModificationRule> modifyRules) {

		this.originalContext = context == null ? EInstanceContext.UNSPECIFIED : context;

		this.document = document;

		this.groundTruth = new Annotations(goldAnnotations, modifyRules).unmodifiable();

		for (AbstractAnnotation a : this.groundTruth.getAnnotations()) {
			if (a instanceof DocumentLinkedAnnotation) {
				if (!a.asInstanceOfDocumentLinkedAnnotation().document.equals(document))
					throw new MismatchingDocumentsException("Not all " + DocumentLinkedAnnotation.class.getSimpleName()
							+ "s are defined on the instance's document.");
			}
		}
	}

	public Instance(EInstanceContext context, Document document, Annotations goldAnnotations) {
		this(context, document, goldAnnotations, Collections.emptySet());
	}

	public String getName() {
		return getDocument().documentID;
	}

	public EInstanceContext getOriginalContext() {
		return originalContext;
	}

	public Document getDocument() {
		return document;
	}

	public Annotations getGoldAnnotations() {
		return groundTruth;
	}

	@Override
	public String toString() {
		return "Instance [originalContext=" + originalContext
				+ (redistributedContext != EInstanceContext.UNSPECIFIED
						? ", redistributedContext=" + redistributedContext
						: "")
				+ ", document=" + document ;//+ ", goldAnnotations=" + groundTruth + "]";
	}

	@Override
	public int compareTo(Instance o) {
		return document.documentID.compareTo(o.document.documentID);
	}

	public void setRedistributedContext(EInstanceContext redistributedContext) {
		this.redistributedContext = redistributedContext;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((document == null) ? 0 : document.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Instance other = (Instance) obj;
		if (document == null) {
			if (other.document != null)
				return false;
		} else if (!document.equals(other.document))
			return false;
		return true;
	}

}
