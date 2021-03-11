package de.hterhors.semanticmr.eval;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;

public abstract class AbstractEvaluator {
	protected static Logger log = LogManager.getFormatterLogger("SlotFilling");

	final public EEvaluationDetail evaluationDetail;

	Map<AbstractAnnotation, Map<AbstractAnnotation, Score>> cache = new HashMap<>();

	public AbstractEvaluator(EEvaluationDetail evaluationDetail) {
		this.evaluationDetail = evaluationDetail;
	}

//---------------------------------------
//MACRO	Root = 0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000
//MACRO	hasAge = 0.850	0.850	0.850	1.000	1.000	1.000	0.850	0.850	0.850
//MACRO	hasWeight = 0.925	0.925	0.925	1.000	1.000	1.000	0.925	0.925	0.925
//MACRO	hasOrganismSpecies = 0.950	0.950	0.950	1.000	1.000	1.000	0.950	0.950	0.950
//MACRO	hasAgeCategory = 0.900	0.900	0.900	1.000	1.000	1.000	0.900	0.900	0.900
//MACRO	hasGender = 0.925	0.925	0.925	1.000	1.000	1.000	0.925	0.925	0.925
//MACRO	Cardinality = 1.000	1.000	1.000	1.000	1.000	1.000	1.000	1.000	1.000
//MACRO	Overall = 0.943	0.943	0.943	1.000	1.000	1.000	0.943	0.943	0.943
//Score: SPECIES_GENDER_WEIGHT_AGE_CATEGORY_AGE	0.95	0.94	0.95
//modelName: OrganismModel-1272030006
//CRFStatistics [context=Train, getTotalDuration()=15677]
//CRFStatistics [context=Test, getTotalDuration()=338]

//---------------------------------------
//MACRO	Root = 0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000
//MACRO	hasAge = 0.462	0.462	0.462	1.000	1.000	1.000	0.462	0.462	0.462
//MACRO	hasWeight = 0.909	0.909	0.909	1.000	1.000	1.000	0.909	0.909	0.909
//MACRO	hasOrganismSpecies = 0.950	0.950	0.950	1.000	1.000	1.000	0.950	0.950	0.950
//MACRO	hasAgeCategory = 0.889	0.889	0.889	1.000	1.000	1.000	0.889	0.889	0.889
//MACRO	hasGender = 0.944	0.944	0.944	1.000	1.000	1.000	0.944	0.944	0.944
//MACRO	Cardinality = 1.000	1.000	1.000	1.000	1.000	1.000	1.000	1.000	1.000
//MACRO	Overall = 0.921	0.930	0.911	1.000	1.000	1.000	0.921	0.930	0.911
//Score: SPECIES_GENDER_WEIGHT_AGE_CATEGORY_AGE	0.93	0.93	0.93
//modelName: OrganismModel-859137999
//CRFStatistics [context=Train, getTotalDuration()=15952]
//CRFStatistics [context=Test, getTotalDuration()=353]

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
		if (val == null && otherVal == null)
			return Score.TN;

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
			throw new IllegalStateException("Illegal state detected during evaluation: " + val + "\t" + otherVal);
		}
//		map.put(otherVal, score);
		return score;
	}

//	public void scoreDetailedSingle(final AbstractAnnotation val, final AbstractAnnotation otherVal,
//			Map<EntityType, Score> detailedScore) {
////		Map<AbstractAnnotation, Score> map;
////		if ((map = cache.get(val)) != null) {
////			if ((score = map.get(otherVal)) != null)
////				return score;
////		} else {
////			cache.put(val, map = new HashMap<>());
////		}
//		if (val == null && otherVal == null)
//			return;
////			return Score.TN;
//
//		if (val instanceof DocumentLinkedAnnotation
//				&& (otherVal instanceof DocumentLinkedAnnotation || otherVal == null)) {
//			((DocumentLinkedAnnotation) val).evaluateDetailed(this, (DocumentLinkedAnnotation) otherVal, detailedScore);
//		} else if (val instanceof LiteralAnnotation && (otherVal instanceof LiteralAnnotation || otherVal == null)) {
//			((LiteralAnnotation) val).evaluateDetailed(this, (LiteralAnnotation) otherVal, detailedScore);
//		} else if (val instanceof EntityTypeAnnotation
//				&& (otherVal instanceof EntityTypeAnnotation || otherVal == null)) {
//			 ((EntityTypeAnnotation) val).evaluateDetailed(this, (EntityTypeAnnotation) otherVal, detailedScore);
//		} else if (val instanceof EntityTemplate && (otherVal instanceof EntityTemplate || otherVal == null)) {
//			 ((EntityTemplate) val).evaluateDetailed(this, (EntityTemplate, detailedScore) otherVal);
//		} else if (val instanceof EntityTemplate && !(otherVal instanceof EntityTemplate)) {
//			 ((EntityTemplate) val).evaluateDetailed(this, otherVal, detailedScore);
//		} else if (otherVal instanceof EntityTemplate && !(val instanceof EntityTemplate)) {
//			((EntityTemplate) otherVal).evaluateDetailed(this, val, detailedScore).invert();
//		} else {
//			/*
//			 * Should never happen!
//			 */
//			throw new IllegalStateException("Illegal state detected during evaluation: " + val + "\t" + otherVal);
//		}
////		map.put(otherVal, score);
//	}

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

	public abstract List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> annotations,
			Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoreType);

}
