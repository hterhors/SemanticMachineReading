package de.hterhors.semanticmr.examples.olp2.corpus.qa;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.olp2.extraction.Olp2ExtractionMain;
import de.hterhors.semanticmr.init.specifications.SystemInitializer;

public class RuleBasedQA {

	public static final int NUM_OF_QUESTIONS = 31;

	final public List<Map<Instance, String>> instancesPerQuestion = new ArrayList<>();
	final public Map<Instance, Set<Integer>> moi = new HashMap<>();

	public static void main(String[] args) {

	}

	public RuleBasedQA() {

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/de/corpus/sf/"), shuffleCorpusDistributor);

		for (int i = 0; i < NUM_OF_QUESTIONS; i++) {
			instancesPerQuestion.add(new HashMap<>());
		}

		for (Instance instance : instanceProvider.getInstances()) {

			String answerQ1;
			if ((answerQ1 = answerQ1(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(0).put(instance, answerQ1);
			}

			String answerQ2;
			if ((answerQ2 = answerQ2(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(1).put(instance, answerQ2);
			}

			String answerQ3;
			if ((answerQ3 = answerQ3(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(2).put(instance, answerQ3);
			}

			String answerQ4;
			if ((answerQ4 = answerQ4(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(3).put(instance, answerQ4);
			}

			String answerQ5;
			if ((answerQ5 = answerQ5(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(4).put(instance, answerQ5);
			}

			String answerQ6;
			if ((answerQ6 = answerQ6(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(5).put(instance, answerQ6);
			}

			String answerQ7;
			if ((answerQ7 = answerQ7(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(6).put(instance, answerQ7);
			}

			String answerQ8;
			if ((answerQ8 = answerQ8(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(7).put(instance, answerQ8);
			}

			String answerQ9;
			if ((answerQ9 = answerQ9(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(8).put(instance, answerQ9);
			}

			String answerQ10;
			if ((answerQ10 = answerQ10(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(9).put(instance, answerQ10);
			}

			String answerQ11;
			if ((answerQ11 = answerQ11(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(10).put(instance, answerQ11);
			}

			String answerQ12;
			if ((answerQ12 = answerQ12(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(11).put(instance, answerQ12);
			}

			String answerQ13;
			if ((answerQ13 = answerQ13(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(12).put(instance, answerQ13);
			}

			String answerQ14;
			if ((answerQ14 = answerQ14(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(13).put(instance, answerQ14);
			}

			String answerQ15;
			if ((answerQ15 = answerQ15(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(14).put(instance, answerQ15);
			}

			String answerQ16;
			if ((answerQ16 = answerQ16(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(15).put(instance, answerQ16);
			}

			String answerQ17;
			if ((answerQ17 = answerQ17(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(16).put(instance, answerQ17);
			}

			String answerQ18;
			if ((answerQ18 = answerQ18(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(17).put(instance, answerQ18);
			}

			String answerQ19;
			if ((answerQ19 = answerQ19(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(18).put(instance, answerQ19);
			}

			String answerQ20;
			if ((answerQ20 = answerQ20(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(19).put(instance, answerQ20);
			}

			String answerQ21;
			if ((answerQ21 = answerQ21(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(20).put(instance, answerQ21);
			}

			String answerQ22;
			if ((answerQ22 = answerQ22(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(21).put(instance, answerQ22);
			}

			String answerQ23;
			if ((answerQ23 = answerQ23(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(22).put(instance, answerQ23);
			}

			String answerQ24;
			if ((answerQ24 = answerQ24(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(23).put(instance, answerQ24);
			}

			String answerQ25;
			if ((answerQ25 = answerQ25(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(24).put(instance, answerQ25);
			}

			String answerQ26;
			if ((answerQ26 = answerQ26(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(25).put(instance, answerQ26);
			}

			String answerQ27;
			if ((answerQ27 = answerQ27(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(26).put(instance, answerQ27);
			}

			String answerQ28;
			if ((answerQ28 = answerQ28(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(27).put(instance, answerQ28);
			}

			String answerQ29;
			if ((answerQ29 = answerQ29(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(28).put(instance, answerQ29);
			}

			String answerQ30;
			if ((answerQ30 = answerQ30(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(29).put(instance, answerQ30);
			}

			String answerQ31;
			if ((answerQ31 = answerQ31(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(30).put(instance, answerQ31);
			}
		}

		int qc = 1;
		for (Map<Instance, String> map : instancesPerQuestion) {
//			System.out.println("Question: " + qc + " -> " + map.keySet().size());
			for (Entry<Instance, String> e : map.entrySet()) {
				moi.putIfAbsent(e.getKey(), new HashSet<>());
				moi.get(e.getKey()).add(qc);
//				System.out.println("Question: " + qc + "->" + e.getKey().getName() + ": " + e.getValue());
			}

			qc++;
		}

//		for (Entry<Instance, Set<Integer>> map : moi.entrySet()) {
//			System.out.println(map.getKey().getName() + "\t" + map.getValue());
//		}
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
}
