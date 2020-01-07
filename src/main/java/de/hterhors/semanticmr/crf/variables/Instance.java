package de.hterhors.semanticmr.crf.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
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

	public Set<EntityTypeAnnotation> getEntityTypeCandidates(EntityType entityType) {
		return entityTypeCandidates.getOrDefault(entityType,Collections.emptySet());
	}

	public Set<AbstractAnnotation> getSlotTypeCandidates(SlotType slotType) {
		return slotTypeCandidates.getOrDefault(slotType,Collections.emptySet());
	}

	public void addCandidateAnnotations(Collection<? extends AbstractAnnotation> candidates) {
		for (AbstractAnnotation candidate : candidates) {
			addCandidateAnnotation(candidate);
		}
	}

	public void addCandidateAnnotation(AbstractAnnotation candidate) {

		if (!candidate.isInstanceOfEntityTemplate()) {
			candidate = candidate.getEntityType().hasNoSlots() ? candidate
					: new EntityTemplate(candidate.asInstanceOfEntityTypeAnnotation());
		}

		for (SlotType slotType : candidate.getEntityType().getSlotFillerOfSlotTypes()) {
			slotTypeCandidates.putIfAbsent(slotType, new HashSet<>());
			if (slotType.matchesEntityType(candidate.getEntityType())) {
				slotTypeCandidates.get(slotType).add(candidate);
			}
		}

		for (EntityType relatedEntitytype : candidate.getEntityType().getHierarchicalEntityTypes()) {
			if (candidate.isInstanceOfEntityTypeAnnotation()) {
				entityTypeCandidates.putIfAbsent(relatedEntitytype, new HashSet<>());
				entityTypeCandidates.get(relatedEntitytype).add((EntityTypeAnnotation) candidate);
			} else {
				entityTypeCandidates.putIfAbsent(relatedEntitytype, new HashSet<>());
				entityTypeCandidates.get(relatedEntitytype).add(((EntityTemplate) candidate).getRootAnnotation());
			}
		}

	}

	private final Map<EntityType, Set<EntityTypeAnnotation>> entityTypeCandidates = new HashMap<>();

	private final Map<SlotType, Set<AbstractAnnotation>> slotTypeCandidates = new HashMap<>();

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
				+ ", document=" + document + ", goldAnnotations=" + groundTruth + "]";
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
