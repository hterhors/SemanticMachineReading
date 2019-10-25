package de.hterhors.semanticmr.eval;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;

public abstract class AbstractEvaluator {
	protected static Logger log = LogManager.getFormatterLogger(AbstractEvaluator.class);

	final public EEvaluationDetail evaluationDetail;

	public AbstractEvaluator(EEvaluationDetail evaluationDetail) {
		this.evaluationDetail = evaluationDetail;
	}

	public Score scoreSingle(final AbstractAnnotation val, final AbstractAnnotation otherVal) {
		if (val instanceof DocumentLinkedAnnotation
				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			return ((DocumentLinkedAnnotation) val).evaluate(this, (DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			return ((LiteralAnnotation) val).evaluate(this, (LiteralAnnotation) otherVal);
		} else if (val instanceof EntityTypeAnnotation
				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
			return ((EntityTypeAnnotation) val).evaluate(this, (EntityTypeAnnotation) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).evaluate(this, (EntityTemplate) otherVal);
		} else if (val instanceof EntityTemplate && !(otherVal instanceof EntityTemplate)) {
			return ((EntityTemplate) val).evaluate(this, otherVal);
		} else if (otherVal instanceof EntityTemplate && !(val instanceof EntityTemplate)) {
			return ((EntityTemplate) otherVal).evaluate(this, val).invert();
		} else {
			/*
			 * Should never happen!
			 */
			throw new IllegalStateException("Illegal state detected during evaluation!");
		}
	}

	protected abstract Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations);

	public Score scoreMultiValues(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		return scoreMax(annotations, otherAnnotations);
	}

}
