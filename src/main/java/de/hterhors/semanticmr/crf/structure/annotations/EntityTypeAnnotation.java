package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.HashMap;
import java.util.Map;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.eval.EEvaluationMode;

/**
 * Annotation object for literal based slots that are NOT linked to the
 * document.
 * 
 * @author hterhors
 *
 */
public class EntityTypeAnnotation extends AbstractSlotFiller<EntityTypeAnnotation> {

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

	public EntityTypeAnnotation deepCopy() {
		return this;
	}

	@Override
	public String toPrettyString(final int depth) {
		return entityType.entityTypeName;
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

	protected boolean equalsEval(Object obj) {
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
	public Score evaluate(EEvaluationMode mode, EntityTypeAnnotation otherVal) {
		if (otherVal == null) {
			return Score.FN;
		} else {
			switch (mode) {
			case DOCUMENT_LINKED:
			case LITERAL:
				if (equals(otherVal))
					return Score.TP;
				return Score.FN_FP;
			case ENTITY_TYPE:
				if (getClass() == otherVal.getClass() && equals(otherVal))
					return Score.TP;

				if ((this.getClass() == DocumentLinkedAnnotation.class || this.getClass() == LiteralAnnotation.class)
						&& otherVal.equalsEval(this))
					return Score.TP;

				if ((otherVal.getClass() == DocumentLinkedAnnotation.class
						|| otherVal.getClass() == LiteralAnnotation.class) && this.equalsEval(otherVal))
					return Score.TP;

				return Score.FN_FP;
			}
		}
		throw new IllegalStateException("Unkown or unhandled evaluation mode: " + mode);

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
