package de.hterhors.semanticmr.examples.olp2.corpus.qa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.olp2.corpus.preprocessing.XMLReader;

public class RuleBasedQA {

	public final static String[] question = new String[] { "Did the game ended in a draw?",
			"What was the score at halftime?", "What was the score at the end of the game?",
			"Which teams got at least one red card? ", "Which teams got at least one yellow card? ",
			"Which team got the most yellow cards?", "Which team got the most red cards?",
			"Which team got the first yellow card?", "Which team got the first red card?", "Which team won the game?",
			"Which team lost the game?", "Which player got the first yellow card?",
			"Which player got the first red card?", "Which player got the last yellow card?",
			"Which player got the last red card?", "Which player scored the first goal?",
			"Which player scored the last goal?", "In which minute were the first goal scored?",
			"In which minute were the last goal scored?", "In which minute was the first yellow card?",
			"In which minute was the last yellow card?", "In which minute was the first red card?",
			"In which minute was the last red card?", "In which minute was the first equalizer scored?",
			"In which minute was the last equalizer scored?", "How many equalizer were scored?",
			"How many minutes of the regular time were played in a draw?",
			"How many minutes of the regular time was Team A in the lead?",
			"How many minutes of the regular time was Team B in the lead?",
			"How many yellow cards were given in total?", "How many red cards were given in total?",
			"In which minute did player X score a goal?", "How many goals did player X scored?",
			"Did player X received a yellow card?", "Did player X received a red card?",
			"How many yellow cards received Team A?", "How many red cards received Team A?",
			"How many yellow cards received Team B?", "How many red cards received Team B?"

	};

	final public Map<Instance, List<QuestionAnswerPair>> questionsForInstances = new HashMap<>();

	final private XMLReader reader;
	public final static String DEFAULT_NOT_EXISTENT_ANSWER = "N/A";

	public RuleBasedQA(XMLReader reader, final List<Instance> instances) {

		this.reader = reader;

		for (Instance instance : instances) {

			questionsForInstances.putIfAbsent(instance, new ArrayList<>());

			int questionIndex = 0;

			String answer;
			if ((answer = answerQ1(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));
			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ2(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ3(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ4(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ5(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ6(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ7(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ8(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ9(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ10(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ11(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ12(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ13(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ14(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ15(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ16(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ17(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ18(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ19(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ20(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ21(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ22(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ23(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ24(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ25(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ26(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ27(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ28(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ29(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ30(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			if ((answer = answerQ31(instance.getGoldAnnotations().getAnnotations())) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
			questionIndex++;

			String q = question[questionIndex];
			int i = 0;

			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team1")) {

				if ((answer = answerQ32(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));
				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team2")) {

				if ((answer = answerQ32(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));
				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			questionIndex++;
			q = question[questionIndex];
			i = 0;
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team1")) {

				if ((answer = answerQ33(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));
				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team2")) {

				if ((answer = answerQ33(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));
				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			questionIndex++;
			q = question[questionIndex];
			i = 0;
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team1")) {

				if ((answer = answerQ34(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));

				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team2")) {

				if ((answer = answerQ34(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));

				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			questionIndex++;
			q = question[questionIndex];
			i = 0;
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team1")) {

				if ((answer = answerQ35(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));

				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}
			for (String player : reader.getPlayerAnnotations(instance.getName(), "Team2")) {

				if ((answer = answerQ35(instance.getGoldAnnotations().getAnnotations(), player)) != null) {
					questionsForInstances.get(instance)
							.add(new QuestionAnswerPair(q.replaceFirst("X", player), answer));
				} else {
//					questionsForInstances.get(instance)
//							.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
				}
				i++;
			}

			questionIndex++;

			if ((answer = answerQ36(instance.getGoldAnnotations().getAnnotations(),
					reader.getTeam(instance.getName(), "Team1"))) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}

			questionIndex++;

			if ((answer = answerQ37(instance.getGoldAnnotations().getAnnotations(),
					reader.getTeam(instance.getName(), "Team1"))) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}

			questionIndex++;

			if ((answer = answerQ36(instance.getGoldAnnotations().getAnnotations(),
					reader.getTeam(instance.getName(), "Team2"))) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}

			questionIndex++;

			if ((answer = answerQ37(instance.getGoldAnnotations().getAnnotations(),
					reader.getTeam(instance.getName(), "Team2"))) != null) {
				questionsForInstances.get(instance).add(new QuestionAnswerPair(question[questionIndex], answer));

			} else {
//				questionsForInstances.get(instance)
//						.add(new QuestionAnswerPair(question[questionIndex], DEFAULT_NOT_EXISTENT_ANSWER));
			}
		}

	}

	/**
	 * Ist das Spiel unentschieden ausgegangen
	 * 
	 * @param list
	 * 
	 * @return
	 */
	public static String answerQ1(List<AbstractAnnotation<?>> annotations) {

		int maxDraw = 0;
		int maxScore = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {
				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (scoreTeamA == scoreTeamB)
					maxDraw = Math.max(maxDraw, scoreTeamA);

				maxScore = Math.max(maxScore, scoreTeamA);
				maxScore = Math.max(maxScore, scoreTeamB);
			}
		}

		return String.valueOf(maxScore == maxDraw);

	}

	/**
	 * Was war der Punktestand zur Halbzeit
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ2(List<AbstractAnnotation<?>> annotations) {

		int maxTeamA = 0;
		int maxteamB = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				if (min > 45)
					continue;

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				maxTeamA = Math.max(maxTeamA, scoreTeamA);
				maxteamB = Math.max(maxteamB, scoreTeamB);
			}
		}
		return maxTeamA + ":" + maxteamB;

	}

	/**
	 * Was war der Punktestand zum Ende des Spiels
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ3(List<AbstractAnnotation<?>> annotations) {

		int maxTeamA = 0;
		int maxteamB = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {
				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				maxTeamA = Math.max(maxTeamA, scoreTeamA);
				maxteamB = Math.max(maxteamB, scoreTeamB);
			}
		}
		return maxTeamA + ":" + maxteamB;

	}

	/**
	 * Welche Teams haben Rotekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ4(List<AbstractAnnotation<?>> annotations) {

		Set<String> teamsWithRedCards = new HashSet<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("RedCard"))
				teamsWithRedCards.add(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
		}

		if (teamsWithRedCards.isEmpty())
			return null;

		String v = "";

		for (Iterator<String> iterator = teamsWithRedCards.iterator(); iterator.hasNext();) {
			v += iterator.next();
			if (iterator.hasNext())
				v += ",";
		}

		return v;

	}

	/**
	 * Welche Teams haben Gelbekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ5(List<AbstractAnnotation<?>> annotations) {

		Set<String> teamsWithYellowCards = new HashSet<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("YellowCard"))
				teamsWithYellowCards.add(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
		}

		if (teamsWithYellowCards.isEmpty())
			return null;

		String v = "";

		for (Iterator<String> iterator = teamsWithYellowCards.iterator(); iterator.hasNext();) {
			v += iterator.next();
			if (iterator.hasNext())
				v += ",";
		}
		return v;

	}

	/**
	 * Welches Team hat die meisten Gelbekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ6(List<AbstractAnnotation<?>> annotations) {

		Map<String, Integer> countYellowCards = new HashMap<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("YellowCard")) {
				final String team = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("cardForTeam")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;
				countYellowCards.put(team, countYellowCards.getOrDefault(team, 0) + 1);

			}
		}

		List<String> keys = new ArrayList<>(countYellowCards.keySet());

		if (keys.isEmpty())
			return null;

		if (keys.size() == 1)
			return keys.get(0);

		if (countYellowCards.get(keys.get(0)) > countYellowCards.get(keys.get(1)))
			return keys.get(0);
		else if (countYellowCards.get(keys.get(0)) < countYellowCards.get(keys.get(1)))
			return keys.get(1);
		else
			return null;
	}

	/**
	 * Welches Team hat die meisten Rotekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ7(List<AbstractAnnotation<?>> annotations) {

		Map<String, Integer> countRedCards = new HashMap<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("RedCard")) {
				final String team = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("cardForTeam")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;
				countRedCards.put(team, countRedCards.getOrDefault(team, 0) + 1);

			}
		}

		List<String> keys = new ArrayList<>(countRedCards.keySet());
		if (keys.isEmpty())
			return null;
		if (keys.size() == 1)
			return keys.get(0);
		if (countRedCards.get(keys.get(0)) > countRedCards.get(keys.get(1)))
			return keys.get(0);
		else if (countRedCards.get(keys.get(0)) < countRedCards.get(keys.get(1)))
			return keys.get(1);
		else
			return null;

	}

	/**
	 * Welches Team hat die erste Gelbekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ8(List<AbstractAnnotation<?>> annotations) {

		String teamFirstYellowCard = null;
		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

			if (min == minMin)
				return null;
			if (min < minMin) {
				teamFirstYellowCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("cardForTeam")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				minMin = min;
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;
		return teamFirstYellowCard;

	}

	/**
	 * Welches Team hat die erste Rotekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ9(List<AbstractAnnotation<?>> annotations) {

		String teamFirstYellowCard = null;
		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin)
				return null;
			if (min < minMin) {
				teamFirstYellowCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("cardForTeam")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				minMin = min;
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;
		return teamFirstYellowCard;

	}

	/**
	 * Welches Team hat das Spiel gewonnen
	 * 
	 * @param annotations
	 * @return the winning team or null on draw
	 */
	public static String answerQ10(List<AbstractAnnotation<?>> annotations) {

		int winnerTeamScore = -1;
		String winnerTeam = null;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("TeamStats")) {
				final int scoreTeam = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("finalScore"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				String team = abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("name"))
						.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				if (scoreTeam > 0 && scoreTeam == winnerTeamScore)
					return null;
				if (scoreTeam > 0 && scoreTeam > winnerTeamScore) {
					winnerTeamScore = scoreTeam;
					winnerTeam = team;
				}
			}
		}

		return winnerTeam;

	}

	/**
	 * Welches Team hat das Spiel verloren
	 * 
	 * @param annotations
	 * @return the losing team or null on draw
	 */
	public static String answerQ11(List<AbstractAnnotation<?>> annotations) {

		int loserTeamScore = Integer.MAX_VALUE;
		String loserTeam = null;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("TeamStats")) {
				final int scoreTeam = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("finalScore"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				String team = abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("name"))
						.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				if (scoreTeam > 0 && scoreTeam == loserTeamScore)
					return null;
				if (scoreTeam > 0 && scoreTeam < loserTeamScore) {
					loserTeamScore = scoreTeam;
					loserTeam = team;
				}
			}
		}

		return loserTeam;

	}

	/**
	 * Welcher Spieler hat die erste Gelbekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ12(List<AbstractAnnotation<?>> annotations) {

		String playerFirstYellowCard = null;
		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin)
				return null;
			if (min < minMin) {
				playerFirstYellowCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				minMin = min;
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;
		return playerFirstYellowCard;

	}

	/**
	 * Welcher Spieler hat die erste Rotekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ13(List<AbstractAnnotation<?>> annotations) {

		String playerFirstRedCard = null;
		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin)
				return null;
			if (min < minMin) {
				playerFirstRedCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				minMin = min;
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;
		return playerFirstRedCard;

	}

	/**
	 * Welcher Spieler hat die letzte Gelbekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ14(List<AbstractAnnotation<?>> annotations) {

		String playerLastYellowCard = null;
		int maxMin = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == maxMin) {
				return null;
			}
			if (min > maxMin) {
				playerLastYellowCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				maxMin = min;
			}
		}
		if (maxMin == 0)
			return null;
		return playerLastYellowCard;

	}

	/**
	 * Welcher Spieler hat die letzte Rotekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ15(List<AbstractAnnotation<?>> annotations) {

		String playerLastYellowCard = null;
		int maxMin = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == maxMin) {
				return null;
			}
			if (min > maxMin) {
				playerLastYellowCard = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				maxMin = min;
			}
		}

		if (maxMin == 0)
			return null;
		return playerLastYellowCard;

	}

	/**
	 * Welcher Spieler hat das erste Tor geschossen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ16(List<AbstractAnnotation<?>> annotations) {

		int minMin = Integer.MAX_VALUE;
		String scorer = null;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (min == minMin)
					return null;
				if (min < minMin) {
					scorer = abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scorer"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

					minMin = min;
				}
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;

		return scorer;
	}

	/**
	 * Welcher Spieler hat das letzte Tor geschossen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ17(List<AbstractAnnotation<?>> annotations) {

		int maxMin = 0;
		String scorer = null;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (min > maxMin) {
					scorer = abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scorer"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

					maxMin = min;
				}
			}
		}
		if (maxMin == 0)
			return null;

		return scorer;
	}

	/**
	 * Wann fiel das erste Tor
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ18(List<AbstractAnnotation<?>> annotations) {

		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (min < minMin) {
					minMin = min;
				}
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;

		return String.valueOf(minMin);
	}

	/**
	 * Wann fiel das letzte Tor
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ19(List<AbstractAnnotation<?>> annotations) {

		int maxMin = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				if (min > maxMin) {
					maxMin = min;
				}
			}
		}

		if (maxMin == 0)
			return null;

		return String.valueOf(maxMin);
	}

	/**
	 * Wann gab es die erst Gelbekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ20(List<AbstractAnnotation<?>> annotations) {

		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin) {
				return null;
			}
			if (min < minMin) {
				minMin = min;
			}
		}

		if (minMin == Integer.MAX_VALUE)
			return null;

		return String.valueOf(minMin);

	}

	/**
	 * Wann gab es die letzte Gelbekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ21(List<AbstractAnnotation<?>> annotations) {

		int maxMin = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min > maxMin) {
				maxMin = min;
			}
		}
		if (maxMin == 0)
			return null;
		return String.valueOf(maxMin);

	}

	/**
	 * Wann gab es die erst Rotekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ22(List<AbstractAnnotation<?>> annotations) {

		int minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min < minMin) {
				minMin = min;
			}
		}
		if (minMin == Integer.MAX_VALUE)
			return null;

		return String.valueOf(minMin);

	}

	/**
	 * Wann gab es die letzte Rotekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ23(List<AbstractAnnotation<?>> annotations) {

		int maxMin = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == maxMin) {
				return null;
			}
			if (min > maxMin) {
				maxMin = min;
			}
		}
		if (maxMin == 0)
			return null;
		return String.valueOf(maxMin);

	}

	/**
	 * Wann wurde der erste Ausgleichstreffer erzielt
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ24(List<AbstractAnnotation<?>> annotations) {

		Integer minMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (minMin.intValue() < min)
					continue;

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (scoreTeamA != scoreTeamB)
					continue;

				minMin = new Integer(min);

			}
		}

		if (minMin.equals(Integer.MAX_VALUE))
			return null;

		return String.valueOf(minMin);

	}

	/**
	 * Wann wurde der letzte Ausgleichstreffer erzielt
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ25(List<AbstractAnnotation<?>> annotations) {

		Integer maxMin = new Integer(0);

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (maxMin.intValue() > min)
					continue;

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (scoreTeamA != scoreTeamB)
					continue;

				maxMin = new Integer(min);
			}
		}
		if (maxMin.intValue() == 0)
			return null;

		return String.valueOf(maxMin);
	}

	/**
	 * Wie viele Ausgleichstreffer gab es
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ26(List<AbstractAnnotation<?>> annotations) {

		Integer count = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (scoreTeamA != scoreTeamB)
					continue;

				count++;
			}
		}

		return String.valueOf(count);
	}

	/**
	 * Wie viele Minuten wurde während der regulären Spielzeit unentschieden
	 * gespielt
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ27(List<AbstractAnnotation<?>> annotations) {

		class P implements Comparable<P> {
			int min;
			boolean draw;

			public P(int min, boolean draw) {
				this.min = min;
				this.draw = draw;
			}

			@Override
			public int compareTo(P o) {
				return Integer.compare(min, o.min);
			}
		}

		final List<P> pairs = new ArrayList<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				pairs.add(new P(min, scoreTeamA == scoreTeamB));
			}
		}

		pairs.add(new P(0, true));
		Collections.sort(pairs);
		if (pairs.isEmpty()) {
			pairs.add(new P(90, true));
		} else {
			pairs.add(new P(90, pairs.get(pairs.size() - 1).draw));
		}

		int sum = 0;

		for (int i = 0; i < pairs.size() - 1; i++) {
			if (pairs.get(i).draw) {
				sum += pairs.get(i + 1).min - pairs.get(i).min;
			}
		}

		return String.valueOf(sum);
	}

	/**
	 * Wie viele Minuten der regulären Spielzeit war Team A in Führung
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ28(List<AbstractAnnotation<?>> annotations) {

		class P implements Comparable<P> {
			int min;
			boolean lead;

			public P(int min, boolean draw) {
				this.min = min;
				this.lead = draw;
			}

			@Override
			public int compareTo(P o) {
				return Integer.compare(min, o.min);
			}
		}

		final List<P> pairs = new ArrayList<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				pairs.add(new P(min, scoreTeamA > scoreTeamB));
			}
		}

		pairs.add(new P(0, false));
		Collections.sort(pairs);
		if (pairs.isEmpty()) {
			pairs.add(new P(90, false));
		} else {
			pairs.add(new P(90, pairs.get(pairs.size() - 1).lead));
		}

		int sum = 0;

		for (int i = 0; i < pairs.size() - 1; i++) {
			if (pairs.get(i).lead) {
				sum += pairs.get(i + 1).min - pairs.get(i).min;
			}
		}

		return String.valueOf(sum);

	}

	/**
	 * Wie viele Minuten der regulären Spielzeit war Team B in Führung
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ29(List<AbstractAnnotation<?>> annotations) {

		class P implements Comparable<P> {
			int min;
			boolean lead;

			public P(int min, boolean draw) {
				this.min = min;
				this.lead = draw;
			}

			@Override
			public int compareTo(P o) {
				return Integer.compare(min, o.min);
			}
		}

		final List<P> pairs = new ArrayList<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				pairs.add(new P(min, scoreTeamA < scoreTeamB));
			}
		}

		pairs.add(new P(0, false));
		Collections.sort(pairs);
		if (pairs.isEmpty()) {
			pairs.add(new P(90, false));
		} else {
			pairs.add(new P(90, pairs.get(pairs.size() - 1).lead));
		}

		int sum = 0;

		for (int i = 0; i < pairs.size() - 1; i++) {
			if (pairs.get(i).lead) {
				sum += pairs.get(i + 1).min - pairs.get(i).min;
			}
		}

		return String.valueOf(sum);

	}

	/**
	 * Wie viele Gelbekarten gab es insgesammt
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ30(List<AbstractAnnotation<?>> annotations) {

		int count = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			count++;
		}
		return String.valueOf(count);

	}

	/**
	 * Wie viele Rotekarten gab es insgesammt
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ31(List<AbstractAnnotation<?>> annotations) {

		int count = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			count++;
		}

		return String.valueOf(count);

	}

	/**
	 * Wann hat Spieler X ein Tor gemacht
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ32(List<AbstractAnnotation<?>> annotations, String player) {

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				final String scoringPlayer = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("scorer")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				if (scoringPlayer.equals(player))
					return String.valueOf(min);

			}
		}
		return null;
	}

	/**
	 * Wie viele Tore hat Spieler X gemacht
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ33(List<AbstractAnnotation<?>> annotations, String player) {

		int count = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final String scoringPlayer = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("scorer")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

				if (scoringPlayer.equals(player))
					count++;

			}
		}

		return String.valueOf(count);
	}

	/**
	 * Hat Spieler X eine Rotekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ34(List<AbstractAnnotation<?>> annotations, final String player) {

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			String cardForPlayer = abstractAnnotation.asInstanceOfEntityTemplate()
					.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
					.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

			if (player.equals(cardForPlayer))
				return String.valueOf(true);

		}

		return String.valueOf(false);

	}

	/**
	 * Hat Spieler x eine Gelbekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ35(List<AbstractAnnotation<?>> annotations, final String player) {

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			String cardForPlayer = abstractAnnotation.asInstanceOfEntityTemplate()
					.getSingleFillerSlot(SlotType.get("player")).getSlotFiller()
					.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

			if (player.equals(cardForPlayer))
				return String.valueOf(true);

		}

		return String.valueOf(false);

	}

	/**
	 * How many yellow cards received Team A?
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ36(List<AbstractAnnotation<?>> annotations, String team) {

		int count = 0;
		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			if (!abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
					.getSlotFiller().getEntityType().entityTypeName.equals(team))
				continue;

			count++;
		}
		return String.valueOf(count);

	}

	/**
	 * How many red cards received Team A?
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ37(List<AbstractAnnotation<?>> annotations, String team) {

		int count = 0;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;
			if (!abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
					.getSlotFiller().getEntityType().entityTypeName.equals(team))
				continue;

			count++;
		}
		return String.valueOf(count);

	}

}
