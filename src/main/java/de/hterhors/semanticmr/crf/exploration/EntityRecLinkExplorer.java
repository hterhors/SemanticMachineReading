package de.hterhors.semanticmr.crf.exploration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;

/**
 * @author hterhors
 *
 */
public class EntityRecLinkExplorer implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger("SlotFilling");

	final private HardConstraintsProvider hardConstraintsProvider;

//	public EntityRecLinkExplorer(HardConstraintsProvider hardConstraintsProvder) {
//		this.hardConstraintsProvider = hardConstraintsProvder;
//	}
	private final Map<CacheKey, Set<AbstractAnnotation>> cache = new HashMap<>();

	public EntityRecLinkExplorer(List<Instance> trainingInstances) {
		this.hardConstraintsProvider = null;
		Map<EntityType, Set<Integer>> countTokens = countTokens(trainingInstances);
		for (EntityType entityType : countTokens.keySet()) {
			maxTokens.put(entityType,
					(int) Math.round(countTokens.get(entityType).stream().mapToDouble(x -> x).average().getAsDouble()));
			log.info("max Tokens = " + entityType + " = " + maxTokens.get(entityType));
		}

	}

	public EntityRecLinkExplorer() {
		this.hardConstraintsProvider = null;
	}

	private Map<EntityType, Integer> maxTokens = new HashMap<>();

	private Map<EntityType, Set<Integer>> countTokens(List<Instance> trainingInstances) {
		Map<EntityType, Set<Integer>> map = new HashMap<>();

		for (Instance instance : trainingInstances) {
			for (AbstractAnnotation aa : instance.getGoldAnnotations().getAnnotations()) {
				tokenRecursive(map, aa);
			}
		}

		return map;
	}

	private void tokenRecursive(Map<EntityType, Set<Integer>> map, AbstractAnnotation aa) {
		if (aa.isInstanceOfEntityTemplate()) {

			AbstractAnnotation rootA = aa.asInstanceOfEntityTemplate().getRootAnnotation();

			if (rootA.isInstanceOfDocumentLinkedAnnotation()) {

				map.putIfAbsent(rootA.getEntityType(), new HashSet<>());
				map.get(rootA.getEntityType()).add(rootA.asInstanceOfDocumentLinkedAnnotation().relatedTokens.size());

			}

			for (AbstractAnnotation slotFillerValue : Stream
					.concat(aa.asInstanceOfEntityTemplate().streamSingleFillerSlotValues(),
							aa.asInstanceOfEntityTemplate().flatStreamMultiFillerSlotValues())
					.collect(Collectors.toSet())) {

				if (slotFillerValue.isInstanceOfEntityTemplate()) {

					AbstractAnnotation rootS = slotFillerValue.asInstanceOfEntityTemplate().getRootAnnotation();

					if (rootS.isInstanceOfDocumentLinkedAnnotation()) {

						map.putIfAbsent(rootS.getEntityType(), new HashSet<>());
						map.get(rootS.getEntityType())
								.add(rootS.asInstanceOfDocumentLinkedAnnotation().relatedTokens.size());

					}

				}

				tokenRecursive(map, slotFillerValue);

			}
		} else if (aa.isInstanceOfDocumentLinkedAnnotation()) {
			map.putIfAbsent(aa.getEntityType(), new HashSet<>());
			map.get(aa.getEntityType()).add(aa.asInstanceOfDocumentLinkedAnnotation().relatedTokens.size());
		}
	}

	/**
	 * Average number of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */
	private int averageNumberOfNewProposalStates = 16;

	public int MAX_WINDOW_SIZE = 10;
	public int MIN_WINDOW_SIZE = 1;

	private int sentenceIndex = 0;

	@Override
	public List<State> explore(State currentState) {

		final List<State> proposalStates = new ArrayList<>(averageNumberOfNewProposalStates);

		addNewAnnotation(proposalStates, currentState);
		removeAnnotation(proposalStates, currentState);

		updateAverage(proposalStates);
		return proposalStates;

	}

	private void removeAnnotation(List<State> proposalStates, State currentState) {

		for (int annotationIndex = 0; annotationIndex < currentState.getCurrentPredictions().getAnnotations()
				.size(); annotationIndex++) {
			proposalStates.add(currentState.deepRemoveCopy(annotationIndex));
		}

	}

	private void addNewAnnotation(final List<State> proposalStates, State currentState) {
		final List<DocumentToken> tokens = currentState.getInstance().getDocument().getSentenceByIndex(sentenceIndex);

		for (int windowSize = MIN_WINDOW_SIZE; windowSize <= MAX_WINDOW_SIZE; windowSize++) {

			for (int runIndex = 0; runIndex < tokens.size() - windowSize; runIndex++) {

				final DocumentToken fromToken = tokens.get(runIndex); // including
				final DocumentToken toToken = tokens.get(runIndex + windowSize - 1); // including

				/*
				 * Check some basic constraints.
				 */

//				if (windowSize==1&&fromToken.getText().equals("rats"))
//					continue;
//				if (windowSize==1&&fromToken.getText().equals("rat"))
//					continue;

				if (fromToken.isStopWord())
					continue;
				if (fromToken.isPunctuation())
					continue;

				/*
				 * TODO: Might check tokens in between.
				 */

				if (toToken.isStopWord())
					continue;

				if (toToken.isPunctuation())
					continue;

				if (fromToken.getSentenceIndex() != toToken.getSentenceIndex())
					continue;

				if (fromToken == toToken && currentState.containsAnnotationOnTokens(fromToken))
					continue;
				else if (currentState.containsAnnotationOnTokens(fromToken, toToken))
					continue;

//				final CacheKey key = new CacheKey(currentState.getInstance(), fromToken.getDocTokenIndex(),
//						toToken.getDocTokenIndex());
				Set<AbstractAnnotation> annotations;
//				if ((annotations = cache.get(key)) == null) {
				annotations = new HashSet<>();
				final String text = currentState.getInstance().getDocument().getContent(fromToken, toToken);

				for (EntityType entityType : currentState.getInstance().getEntityTypeCandidates(text)) {

					if (maxTokens.containsKey(entityType) && maxTokens.get(entityType) < windowSize)
						continue;

					try {
						AbstractAnnotation newCurrentPrediction = AnnotationBuilder.toAnnotation(
								currentState.getInstance().getDocument(), entityType, text,
								fromToken.getDocCharOffset());
						annotations.add(newCurrentPrediction);
						proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
					} catch (RuntimeException e) {
						e.printStackTrace();
					}

				}
//					cache.put(key, annotations);
//				} else {
//					for (AbstractAnnotation newCurrentPrediction : annotations) {
//						proposalStates.add(currentState.deepAddCopy(newCurrentPrediction));
//					}
//				}
			}
		}
	}

	static class CacheKey {
		final Instance instance;
		final int fromTokenOffset;
		final int toTokenOffset;

		public CacheKey(Instance instance, int fromTokenOffset, int toTokenOffset) {
			this.instance = instance;
			this.fromTokenOffset = fromTokenOffset;
			this.toTokenOffset = toTokenOffset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + fromTokenOffset;
			result = prime * result + ((instance == null) ? 0 : instance.hashCode());
			result = prime * result + toTokenOffset;
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
			CacheKey other = (CacheKey) obj;
			if (fromTokenOffset != other.fromTokenOffset)
				return false;
			if (instance == null) {
				if (other.instance != null)
					return false;
			} else if (!instance.equals(other.instance))
				return false;
			if (toTokenOffset != other.toTokenOffset)
				return false;
			return true;
		}

	}

	private void updateAverage(final List<State> proposalStates) {
		averageNumberOfNewProposalStates += proposalStates.size();
		averageNumberOfNewProposalStates /= 2;
	}

	@Override
	public boolean hasNext() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public State next() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void set(int sentenceIndex) {
		this.sentenceIndex = sentenceIndex;
	}

}
