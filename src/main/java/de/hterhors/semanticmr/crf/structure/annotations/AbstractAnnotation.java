package de.hterhors.semanticmr.crf.structure.annotations;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IDeepCopyable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.eval.AbstractEvaluator;

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

		if (this instanceof EntityTypeAnnotation) {
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
		return this instanceof LiteralAnnotation;
	}

	final public boolean isInstanceOfEntityTypeAnnotation() {
		return this instanceof EntityTypeAnnotation;
	}

	final public boolean isInstanceOfEntityTemplate() {
		return this instanceof EntityTemplate;
	}

	@Override
	public Score evaluate(AbstractEvaluator evaluator, IEvaluatable otherVal) {
		return evaluate(evaluator.evaluationDetail, otherVal);
	}

	public AbstractAnnotation reduceToEntityType() {

		if (getEntityType().isLiteral)
			return this;

		if (isInstanceOfEntityTypeAnnotation())
			return AnnotationBuilder.toAnnotation(getEntityType());

		final EntityTemplate redAnn = new EntityTemplate(AnnotationBuilder.toAnnotation(getEntityType()));

		Map<SlotType, AbstractAnnotation> singleSlots = asInstanceOfEntityTemplate().filter().docLinkedAnnoation()
				.entityTypeAnnoation().literalAnnoation().entityTemplateAnnoation().singleSlots().nonEmpty().build()
				.getSingleAnnotations();

		for (Entry<SlotType, AbstractAnnotation> singleSlot : singleSlots.entrySet()) {
			redAnn.setSingleSlotFiller(singleSlot.getKey(), singleSlot.getValue().reduceToEntityType());
		}

		Map<SlotType, Set<AbstractAnnotation>> multiSlots = asInstanceOfEntityTemplate().filter().docLinkedAnnoation()
				.entityTypeAnnoation().literalAnnoation().entityTemplateAnnoation().multiSlots().nonEmpty().build()
				.getMultiAnnotations();

		for (Entry<SlotType, Set<AbstractAnnotation>> multiSlot : multiSlots.entrySet()) {
			for (AbstractAnnotation multiFiller : multiSlot.getValue()) {
				redAnn.addMultiSlotFiller(multiSlot.getKey(), multiFiller.reduceToEntityType());
			}
		}

		return redAnn;
	}

	/**
	 * Checks if the evaluation of this entity to the given entity is equals to
	 * 1.0D. This method is equal to calling evaluate().getF1() == 1.0D but runs
	 * faster as it breaks if a fp or fn appears.
	 * 
	 * @param evaluator
	 * @param entityTemplate
	 * @return
	 */
	@Override
	public boolean evaluateEquals(AbstractEvaluator evaluator, IEvaluatable otherVal) {
		return evaluateEquals(evaluator.evaluationDetail, otherVal);
	}
}
