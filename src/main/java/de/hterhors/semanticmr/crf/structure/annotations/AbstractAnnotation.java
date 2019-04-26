package de.hterhors.semanticmr.crf.structure.annotations;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IDeepCopyable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;

public abstract class AbstractAnnotation<T> implements IEvaluatable<T>, IDeepCopyable<AbstractAnnotation<T>> {

	final public String toPrettyString() {
		return toPrettyString(0);
	}

	public abstract String toPrettyString(final int depth);

	public abstract EntityType getEntityType();

	/**
	 * Returns this annotation as DocumentLinkedAnnotation iff this annotation is
	 * instanceof DocumentLinkedAnnotation, else null;
	 * 
	 * @return this annotation as DocumentLinkedAnnotation or null
	 */
	public DocumentLinkedAnnotation asInstanceOfDocumentLinkedAnnotation() {
		if (this instanceof DocumentLinkedAnnotation) {
			return (DocumentLinkedAnnotation) this;
		}
		return null;
	}

	/**
	 * Returns this annotation as LiteralAnnotation iff this annotation is
	 * instanceof LiteralAnnotation, else null;
	 * 
	 * @return this annotation as LiteralAnnotation or null
	 */
	public LiteralAnnotation asInstanceOfLiteralAnnotation() {
		if (this instanceof LiteralAnnotation) {
			return (LiteralAnnotation) this;
		}
		return null;
	}

	/**
	 * Returns this annotation as EntityTypeAnnotation iff this annotation is
	 * instanceof EntityTypeAnnotation, else null;
	 * 
	 * @return this annotation as EntityTypeAnnotation or null
	 */
	public EntityTypeAnnotation asInstanceOfEntityTypeAnnotation() {
		if (this instanceof EntityTypeAnnotation) {
			return (EntityTypeAnnotation) this;
		}
		return null;
	}

	/**
	 * Returns this annotation as EntityTemplate iff this annotation is instanceof
	 * EntityTemplate, else null;
	 * 
	 * @return this annotation as EntityTemplate or null
	 */
	public EntityTemplate asInstanceOfEntityTemplate() {
		if (this instanceof EntityTemplate) {
			return (EntityTemplate) this;
		}
		return null;
	}

}
