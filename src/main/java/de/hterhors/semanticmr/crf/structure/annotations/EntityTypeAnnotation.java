package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

/**
 * Annotation object for literal based slots that are NOT linked to the
 * document.
 * 
 * @author hterhors
 *
 */
public class EntityTypeAnnotation extends AbstractAnnotation {

	/**
	 * Defines the entity type of this annotation.
	 */
	public final EntityType entityType;

	protected EntityTypeAnnotation(EntityType entityType) {
		this.entityType = entityType;
	}

	private final static Map<EntityType, EntityTypeAnnotation> factory = new HashMap<>();

	public static EntityTypeAnnotation get(EntityType entityType) {

		EntityTypeAnnotation entityTypeAnnotation;

		if ((entityTypeAnnotation = factory.get(entityType)) == null) {
			entityTypeAnnotation = new EntityTypeAnnotation(entityType);
			factory.put(entityType, entityTypeAnnotation);
		}

		return entityTypeAnnotation;
	}

	@Override
	@SuppressWarnings("unchecked")
	public EntityTypeAnnotation deepCopy() {
		return this;
	}

	@Override
	public String toPrettyString(final int depth) {
		return entityType.entityName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
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
		EntityTypeAnnotation other = (EntityTypeAnnotation) obj;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		return true;
	}

	final protected boolean equalsEvalETA(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!getClass().isAssignableFrom(obj.getClass()))
			return false;
		EntityTypeAnnotation other = (EntityTypeAnnotation) obj;
		if (entityType == null) {
			if (other.entityType != null)
				return false;
		} else if (!entityType.equals(other.entityType))
			return false;
		return true;
	}

	@Override
	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable otherVal) {

		if (otherVal == null)
			return Score.FN;

		if (evaluator.evaluationDetail == EEvaluationDetail.DOCUMENT_LINKED
				|| evaluator.evaluationDetail == EEvaluationDetail.LITERAL
				|| evaluator.evaluationDetail == EEvaluationDetail.ENTITY_TYPE) {

			if (getClass() == otherVal.getClass()) {
				if (getClass() != EntityTypeAnnotation.class)
					return equalsEvalETA(otherVal) ? Score.TP : Score.FN_FP;
				else
					return equals(otherVal) ? Score.TP : Score.FN_FP;
			} else {
				if (this.getClass().isAssignableFrom(otherVal.getClass())) {
					return this.equalsEvalETA(otherVal) ? Score.TP : Score.FN_FP;
				} else if (otherVal.getClass().isAssignableFrom(this.getClass())) {
					return ((EntityTypeAnnotation) otherVal).equalsEvalETA(this) ? Score.TP : Score.FN_FP;
				}
			}
			return Score.FN_FP;
		}

		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + evaluator.evaluationDetail);

	}

	@Override
	public EntityType getEntityType() {
		return entityType;
	}

	@Override
	public String toString() {
		return "EntityTypeAnnotation [entityType=" + entityType + "]";
	}



}
