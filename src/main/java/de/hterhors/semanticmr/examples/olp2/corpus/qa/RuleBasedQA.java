package de.hterhors.semanticmr.examples.olp2.corpus.qa;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

	private static final int NUM_OF_QUESTIONS = 2;

	public static void main(String[] args) {
		SystemInitializer.initialize(Olp2ExtractionMain.de_specificationProvider).apply();

		AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
				.setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();

		InstanceProvider instanceProvider = new InstanceProvider(
				new File("src/main/resources/examples/olp2/de/corpus/instances/"), shuffleCorpusDistributor);

		final List<Set<Instance>> instancesPerQuestion = new ArrayList<>();

		for (int i = 0; i < NUM_OF_QUESTIONS; i++) {
			instancesPerQuestion.add(new HashSet<>());
		}

		for (Instance instance : instanceProvider.getInstances()) {
			if (answerQ1(instance.getGoldAnnotations().getAnnotations())) {
				instancesPerQuestion.get(0).add(instance);
			}

			String answerQ2 = null;
			if ((answerQ2 = answerQ2(instance.getGoldAnnotations().getAnnotations())) != null) {
				instancesPerQuestion.get(0).add(instance);
				System.out.println(answerQ2);
			}
		}
//		System.out.println(instancesPerQuestion.get(2));

	}

	/**
	 * Ist das Spiel unentschieden ausgegangen
	 * 
	 * @param list
	 * 
	 * @return
	 */
	public static boolean answerQ1(List<AbstractAnnotation<?>> annotations) {

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

		return maxScore == maxDraw;

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
	public static Set<String> answerQ4(List<AbstractAnnotation<?>> annotations) {

		Set<String> teamsWithRedCards = new HashSet<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("RedCard"))
				teamsWithRedCards.add(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
		}
		return teamsWithRedCards;

	}

	/**
	 * Welche Teams haben Gelbekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static Set<String> answerQ5(List<AbstractAnnotation<?>> annotations) {

		Set<String> teamsWithYellowCards = new HashSet<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("YellowCard"))
				teamsWithYellowCards.add(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardForTeam"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
		}
		return teamsWithYellowCards;

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

		return countYellowCards.entrySet().stream().max(new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.max(o1.getValue(), o2.getValue());
			}
		}).orElseGet(null).getKey();

	}

	/**
	 * Welches Team hat die meisten Rotekarten bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ7(List<AbstractAnnotation<?>> annotations) {

		Map<String, Integer> countYellowCards = new HashMap<>();

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("RedCard")) {
				final String team = abstractAnnotation.asInstanceOfEntityTemplate()
						.getSingleFillerSlot(SlotType.get("cardForTeam")).getSlotFiller()
						.asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;
				countYellowCards.put(team, countYellowCards.getOrDefault(team, 0) + 1);

			}
		}

		return countYellowCards.entrySet().stream().max(new Comparator<Entry<String, Integer>>() {

			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.max(o1.getValue(), o2.getValue());
			}
		}).orElseGet(null).getKey();

	}

	/**
	 * Welches Team hat die erste Gelbekarte bekommen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ8(List<AbstractAnnotation<?>> annotations) {

		String teamFirstYellowCard = null;
		int minMin = -1;

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
		int minMin = -1;

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
		int minMin = -1;

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
		int minMin = -1;

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
		int maxMin = Integer.MAX_VALUE;

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
		int maxMin = Integer.MAX_VALUE;

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
		return playerLastYellowCard;

	}

	/**
	 * Welcher Spieler hat das erste Tor geschossen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ16(List<AbstractAnnotation<?>> annotations) {

		int minMin = -1;
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
		return scorer;
	}

	/**
	 * Welcher Spieler hat das letzte Tor geschossen
	 * 
	 * @param annotations
	 * @return
	 */
	public static String answerQ17(List<AbstractAnnotation<?>> annotations) {

		int maxMin = Integer.MAX_VALUE;
		String scorer = null;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				if (min == maxMin) {
					return null;
				}

				if (min > maxMin) {
					scorer = abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scorer"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName;

					maxMin = min;
				}
			}
		}
		return scorer;
	}

	/**
	 * Wann fiel das erste Tor
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ18(List<AbstractAnnotation<?>> annotations) {

		int minMin = -1;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (min == minMin)
					return -1;
				if (min < minMin) {
					minMin = min;
				}
			}
		}
		return minMin;
	}

	/**
	 * Wann fiel das letzte Tor
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ19(List<AbstractAnnotation<?>> annotations) {

		int maxMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (min == maxMin)
					return -1;
				if (min > maxMin) {
					maxMin = min;
				}
			}
		}
		return maxMin;
	}

	/**
	 * Wann gab es die erst Gelbekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ20(List<AbstractAnnotation<?>> annotations) {

		int minMin = -1;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin) {
				return -1;
			}
			if (min < minMin) {
				minMin = min;
			}
		}
		return minMin;

	}

	/**
	 * Wann gab es die letzte Gelbekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ21(List<AbstractAnnotation<?>> annotations) {

		int maxMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("YellowCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == maxMin) {
				return -1;
			}
			if (min > maxMin) {
				maxMin = min;
			}
		}
		return maxMin;

	}

	/**
	 * Wann gab es die erst Rotekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ22(List<AbstractAnnotation<?>> annotations) {

		int minMin = -1;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == minMin) {
				return -1;
			}
			if (min < minMin) {
				minMin = min;
			}
		}
		return minMin;

	}

	/**
	 * Wann gab es die letzte Rotekarte
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ23(List<AbstractAnnotation<?>> annotations) {

		int maxMin = Integer.MAX_VALUE;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {
			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() != EntityType.get("RedCard"))
				continue;

			final int min = Integer.parseInt(
					abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("cardMinute"))
							.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
			if (min == maxMin) {
				return -1;
			}
			if (min > maxMin) {
				maxMin = min;
			}
		}
		return maxMin;

	}

	/**
	 * Wann wurde der erste Ausgleichstreffer erzielt
	 * 
	 * @param annotations
	 * @return
	 */
	public static int answerQ24(List<AbstractAnnotation<?>> annotations) {

		int minMin = -1;

		for (AbstractAnnotation<?> abstractAnnotation : annotations) {

			if (abstractAnnotation.asInstanceOfEntityTemplate().getEntityType() == EntityType.get("Goal")) {

				final int min = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("minute"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (minMin < min)
					continue;

				final int scoreTeamA = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamA"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);
				final int scoreTeamB = Integer.parseInt(
						abstractAnnotation.asInstanceOfEntityTemplate().getSingleFillerSlot(SlotType.get("scoreTeamB"))
								.getSlotFiller().asInstanceOfEntityTypeAnnotation().entityType.entityTypeName);

				if (scoreTeamA != scoreTeamB)
					continue;

				minMin = min;

			}
		}
		return minMin;

	}

}
