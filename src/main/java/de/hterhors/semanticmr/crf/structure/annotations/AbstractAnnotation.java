package de.hterhors.semanticmr.crf.structure.annotations;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IDeepCopyable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;

public abstract class AbstractAnnotation implements IEvaluatable, IDeepCopyable {

	final public String toPrettyString() {
		return toPrettyString(0);
	}

	public abstract String toPrettyString(final int depth);

	public abstract EntityType getEntityType();

	private DocumentLinkedAnnotation asInstanceOfDocumentLinkedAnnotation = null;
	private LiteralAnnotation asInstanceOfLiteralAnnotation = null;
	private EntityTypeAnnotation asInstanceOfEntityTypeAnnotation = null;
	private EntityTemplate asInstanceOfEntityTemplate = null;

	/**
	 * Returns this annotation as DocumentLinkedAnnotation iff this annotation is
	 * instanceof DocumentLinkedAnnotation, else null;
	 * 
	 * @return this annotation as DocumentLinkedAnnotation or null
	 */
	final public DocumentLinkedAnnotation asInstanceOfDocumentLinkedAnnotation() {
		if (asInstanceOfDocumentLinkedAnnotation != null)
			return asInstanceOfDocumentLinkedAnnotation;

		if (this instanceof DocumentLinkedAnnotation) {
			return asInstanceOfDocumentLinkedAnnotation = (DocumentLinkedAnnotation) this;
		}
		throw new ClassCastException(
				"Annotation is not an instance of " + DocumentLinkedAnnotation.class.getSimpleName());
	}

	/**
	 * Returns this annotation as LiteralAnnotation iff this annotation is
	 * instanceof LiteralAnnotation, else null;
	 * 
	 * @return this annotation as LiteralAnnotation or null
	 */
	final public LiteralAnnotation asInstanceOfLiteralAnnotation() {
		if (asInstanceOfLiteralAnnotation != null)
			return asInstanceOfLiteralAnnotation;

		if (this instanceof LiteralAnnotation) {
			return asInstanceOfLiteralAnnotation = (LiteralAnnotation) this;
		}
		throw new ClassCastException("Annotation is not an instance of " + LiteralAnnotation.class.getSimpleName());
	}

	/**
	 * Returns this annotation as EntityTypeAnnotation iff this annotation is
	 * instanceof EntityTypeAnnotation, else null;
	 * 
	 * @return this annotation as EntityTypeAnnotation or null
	 */
	final public EntityTypeAnnotation asInstanceOfEntityTypeAnnotation() {
		if (asInstanceOfEntityTypeAnnotation != null)
			return asInstanceOfEntityTypeAnnotation;

		if (this instanceof DocumentLinkedAnnotation) {
			return asInstanceOfEntityTypeAnnotation = (EntityTypeAnnotation) this;
		}
		throw new ClassCastException("Annotation is not an instance of " + EntityTypeAnnotation.class.getSimpleName());
	}

	/**
	 * Returns this annotation as EntityTemplate iff this annotation is instanceof
	 * EntityTemplate, else null;
	 * 
	 * @return this annotation as EntityTemplate or null
	 */
	final public EntityTemplate asInstanceOfEntityTemplate() {
		if (asInstanceOfEntityTemplate != null)
			return asInstanceOfEntityTemplate;

		if (this instanceof EntityTemplate) {
			return asInstanceOfEntityTemplate = (EntityTemplate) this;
		}
		throw new ClassCastException("Annotation is not an instance of " + EntityTemplate.class.getSimpleName());
	}

	final public boolean isInstanceOfDocumentLinkedAnnotation() {
		return this instanceof DocumentLinkedAnnotation;
	}

	final public boolean isInstanceOfLiteralAnnotation() {
		return this instanceof DocumentLinkedAnnotation;
	}

	final public boolean isInstanceOfEntityTypeAnnotation() {
		return this instanceof EntityTypeAnnotation;
	}

	final public boolean isInstanceOfEntityTemplate() {
		return this instanceof EntityTemplate;

	}

}
