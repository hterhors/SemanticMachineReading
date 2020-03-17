package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;

public abstract class AbstractEvaluator {
	protected static Logger log = LogManager.getFormatterLogger(AbstractEvaluator.class);

	final public EEvaluationDetail evaluationDetail;

	Map<AbstractAnnotation, Map<AbstractAnnotation, Score>> cache = new HashMap<>();

	public AbstractEvaluator(EEvaluationDetail evaluationDetail) {
		this.evaluationDetail = evaluationDetail;
	}

	/**
	 * Always computes MIRCO score!
	 * 
	 * @param val
	 * @param otherVal
	 * @return
	 */
	public Score scoreSingle(final AbstractAnnotation val, final AbstractAnnotation otherVal) {
		Score score;
//		Map<AbstractAnnotation, Score> map;
//		if ((map = cache.get(val)) != null) {
//			if ((score = map.get(otherVal)) != null)
//				return score;
//		} else {
//			cache.put(val, map = new HashMap<>());
//		}
		if (val instanceof DocumentLinkedAnnotation
				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			score = ((DocumentLinkedAnnotation) val).evaluate(this, (DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			score = ((LiteralAnnotation) val).evaluate(this, (LiteralAnnotation) otherVal);
		} else if (val instanceof EntityTypeAnnotation
				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
			score = ((EntityTypeAnnotation) val).evaluate(this, (EntityTypeAnnotation) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			score = ((EntityTemplate) val).evaluate(this, (EntityTemplate) otherVal);
		} else if (val instanceof EntityTemplate && !(otherVal instanceof EntityTemplate)) {
			score = ((EntityTemplate) val).evaluate(this, otherVal);
		} else if (otherVal instanceof EntityTemplate && !(val instanceof EntityTemplate)) {
			score = ((EntityTemplate) otherVal).evaluate(this, val).invert();
		} else {
			/*
			 * Should never happen!
			 */
			throw new IllegalStateException("Illegal state detected during evaluation!");
		}
//		map.put(otherVal, score);
		return score;
	}

	public boolean evalEqualsSingle(final AbstractAnnotation val, final AbstractAnnotation otherVal) {
		if (val instanceof DocumentLinkedAnnotation
				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
			return ((DocumentLinkedAnnotation) val).evaluateEquals(this, (DocumentLinkedAnnotation) otherVal);
		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
			return ((LiteralAnnotation) val).evaluateEquals(this, (LiteralAnnotation) otherVal);
		} else if (val instanceof EntityTypeAnnotation
				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
			return ((EntityTypeAnnotation) val).evaluateEquals(this, (EntityTypeAnnotation) otherVal);
		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
			return ((EntityTemplate) val).evaluateEquals(this, (EntityTemplate) otherVal);
		} else if (val instanceof EntityTemplate && !(otherVal instanceof EntityTemplate)) {
			return ((EntityTemplate) val).evaluateEquals(this, otherVal);
		} else if (otherVal instanceof EntityTemplate && !(val instanceof EntityTemplate)) {
			return ((EntityTemplate) otherVal).evaluateEquals(this, val);
		} else {
			/*
			 * Should never happen!
			 */
			throw new IllegalStateException("Illegal state detected during evaluation!");
		}
	}

	protected abstract Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType);

	public Score scoreMultiValues(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType) {
		if (annotations.isEmpty() && otherAnnotations.isEmpty())
			return Score.getZero(scoreType);
		return scoreMax(annotations, otherAnnotations, scoreType);
	}

	protected abstract boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations);

	public boolean evalEqualsMultiValues(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations) {
		if (annotations.isEmpty() && otherAnnotations.isEmpty())
			return true;
		return evalEqualsMax(annotations, otherAnnotations);
	}

}
