package de.hterhors.semanticmr.crf.exploration;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.hterhors.semanticmr.crf.exploration.constraints.HardConstraintsProvider;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.tools.AutomatedSectionifcation;
import de.hterhors.semanticmr.tools.AutomatedSectionifcation.ESection;

/**
 * @author hterhors
 *
 */
public class EntityRecLinkExplorerIterator implements IExplorationStrategy {
	private static Logger log = LogManager.getFormatterLogger(EntityRecLinkExplorerIterator.class);

	final private HardConstraintsProvider hardConstraintsProvider;

//	public EntityRecLinkExplorer(HardConstraintsProvider hardConstraintsProvder) {
//		this.hardConstraintsProvider = hardConstraintsProvder;
//	}

	public EntityRecLinkExplorerIterator() {
		this.hardConstraintsProvider = null;
	}

	/**
	 * Average number of new explored proposal states. This variable is used as
	 * initial size of the next new proposal state list.
	 */

	public int MAX_WINDOW_SIZE = 10;
	public int MIN_WINDOW_SIZE = 1;

	@Override
	public List<State> explore(State currentState) {
		return null;
	}

	private Iterator<EntityType> entityTypeIterator;
	private Iterator<AbstractAnnotation> removeIterator;
	private State currentState;
	private int windowSize;
	private int runIndex;
	private List<DocumentToken> tokens;
	private String text = "";
	private int removeIndex;
	private boolean newRunUpate;
	private DocumentToken fromToken;
	private DocumentToken toToken;
	private boolean surpassConditions = false;
	AutomatedSectionifcation sectionifcation;

	public void set(int sentenceIndex) {

	}

	public void set(State state) {
		this.currentState = state;
		this.sectionifcation = AutomatedSectionifcation.getInstance(currentState.getInstance());
		this.removeIterator = currentState.getCurrentPredictions().getAnnotations().iterator();
		this.windowSize = MIN_WINDOW_SIZE;
		this.runIndex = 0;
		this.removeIndex = 0;
		this.tokens = currentState.getInstance().getDocument().tokenList;
		this.fromToken = tokens.get(runIndex); // including
		this.toToken = tokens.get(runIndex + windowSize - 1); // including
		this.newRunUpate = true;
	}

	@Override
	public boolean hasNext() {
		return removeIterator.hasNext() || windowSize <= MAX_WINDOW_SIZE;
	}

	@Override
	public State next() {

		if (removeIterator.hasNext()) {
			removeIterator.next();
			return currentState.deepRemoveCopy(removeIndex++);
		}

		do {
			/*
			 * Check some basic constraints.
			 */
			if (!surpassConditions) {

				if (sectionifcation.getSection(fromToken.getSentenceIndex()) != ESection.RESULTS) {
					update();
					continue;
				}

				if (fromToken.isStopWord()) {
					update();
					continue;
				}

				if (fromToken.isPunctuation()) {
					update();
					continue;
				}

				if (toToken.isStopWord()) {
					update();
					continue;
				}

				if (toToken.isPunctuation()) {
					update();
					continue;
				}
				if (fromToken.getSentenceIndex() != toToken.getSentenceIndex()) {
					update();
					continue;
				}
				if (fromToken == toToken && currentState.containsAnnotationOnTokens(fromToken)) {
					update();
					continue;

				} else if (currentState.containsAnnotationOnTokens(fromToken, toToken)) {
					update();
					continue;
				}
				surpassConditions = true;
			}

			if (newRunUpate) {
				text = currentState.getInstance().getDocument().getContent(fromToken, toToken);
				entityTypeIterator = currentState.getInstance().getEntityTypeCandidates(text).iterator();
				newRunUpate = false;
			}

			if (entityTypeIterator.hasNext()) {
				AbstractAnnotation newCurrentPrediction = AnnotationBuilder.toAnnotation(
						currentState.getInstance().getDocument(), entityTypeIterator.next(), text,
						fromToken.getDocCharOffset());
				return currentState.deepAddCopy(newCurrentPrediction);
			} else {
				update();
			}

		} while (true);
	}

	private void update() {
		runIndex++;
		if (runIndex > tokens.size() - windowSize) {
			runIndex = 0;
			windowSize++;
		}
		fromToken = tokens.get(runIndex); // including
		toToken = tokens.get(runIndex + windowSize - 1); // including
		surpassConditions = false;
		newRunUpate = true;
	}

}
