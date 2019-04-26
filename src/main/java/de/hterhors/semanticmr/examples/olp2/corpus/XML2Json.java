package de.hterhors.semanticmr.examples.olp2.corpus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML2Json {

	final static private Map<String, String> deMap = new HashMap<>();
	final static private Map<String, String> enMap = new HashMap<>();

	public static void main(String[] args) throws Exception {

		fillDeMap();

		fillEnMap();

		Set<String> player = new HashSet<>();
		Set<String> teams = new HashSet<>();
		Set<String> abrs = new HashSet<>();
		File xmlDir = new File("olp2/");
		
		for (File xmlFile : xmlDir.listFiles()) {

			if (!xmlFile.getName().startsWith("en_"))
				continue;

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);

			doc.getDocumentElement().normalize();

//			player.addAll(readPlayer(doc, "Team1"));
//			player.addAll(readPlayer(doc, "Team2"));
//
			teams.addAll(readTeams(doc, "Team1"));
			teams.addAll(readTeams(doc, "Team2"));
//
//			readGoals(doc, xmlFile);
		}

		List<String> sortedPlayer = new ArrayList<>(player);
		List<String> sortedTeams = new ArrayList<>(teams);
		List<String> sortedAbrs = new ArrayList<>(abrs);

		Collections.sort(sortedPlayer);
		Collections.sort(sortedTeams);
		Collections.sort(sortedAbrs);

		System.out.println(counter);

//		System.out.println(sortedAbrs.size());
//		System.out.println(sortedTeams.size());

//		sortedAbrs.forEach(System.out::println);

//			sortedPlayer.forEach(System.out::println);
//		System.out.println(sortedPlayer.size());

//		sortedTeams.forEach(System.out::println);

//		for (String string : sortedTeams) {
//			if (deMap.get(string) == null)
//				System.out.println(string);
//		}
		for (String string : sortedTeams) {
			if (enMap.get(string) == null)
				System.out.println(string);
		}
	}

	private static void fillDeMap() throws IOException {
		List<String> lines = Files
				.readAllLines(new File("src/main/resources/examples/olp2/de_ISO-ALPHA-3.map").toPath());

		for (String line : lines) {

			String[] d = line.split("\t");

			if (d.length != 2)
				continue;
			if (d[1].isEmpty())
				continue;

			deMap.put(d[0].trim(), d[1].trim());
//			deMap.put(d[2].trim(), d[0].trim());

		}
	}

	private static void fillEnMap() throws IOException {
		List<String> lines = Files
				.readAllLines(new File("src/main/resources/examples/olp2/en_ISO-ALPHA-3.map").toPath());

		for (String line : lines) {

			String[] d = line.split("\t");

			if (d.length != 3)
				continue;
			if (d[2].isEmpty())
				continue;

			enMap.put(d[0].trim(), d[2].trim());
//			enMap.put(d[2].trim(), d[0].trim());

		}
	}

	private static Set<String> readTeams(Document doc, String string) {
		NodeList nList = doc.getElementsByTagName("MatchInfo");
		Set<String> teams = new HashSet<>();
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Element nNode = (Element) nList.item(temp);

			Element n = (Element) nNode.getElementsByTagName(string).item(0);

			teams.add(n.getElementsByTagName("Name").item(0).getTextContent().trim());

		}
		return teams;
	}

	private static Set<String> readPlayer(Document doc, String string) {
		NodeList nList = doc.getElementsByTagName("Teams");
		Set<String> player = new HashSet<>();
		for (int temp = 0; temp < nList.getLength(); temp++) {

			Element nNode = (Element) nList.item(temp);

			Element n = (Element) nNode.getElementsByTagName(string).item(0);

//			System.out.println(n.getElementsByTagName("Name").item(0).getTextContent());

			Element gk = (Element) n.getElementsByTagName("Goalkeeper").item(0);

//			System.out.println(gk.getElementsByTagName("Name").item(0).getTextContent());
			if (gk == null)
				;
//				;System.out.println("WARN GOALKEEPER MISSING");
			else {
				player.add(gk.getElementsByTagName("Name").item(0).getTextContent().trim());
			}
			for (int i = 0; i < n.getElementsByTagName("OtherFootballPlayer").getLength(); i++) {

				Element of = (Element) n.getElementsByTagName("OtherFootballPlayer").item(i);

//				System.out.println(of.getElementsByTagName("Name").item(0).getTextContent());
				player.add(of.getElementsByTagName("Name").item(0).getTextContent().trim());
			}
		}
		return player;
	}

	static int counter = 0;

	private static void readGoals(Document doc, File xmlFile) {
		NodeList nList = doc.getElementsByTagName("Goal");

		for (int temp = 0; temp < nList.getLength(); temp++) {

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				if (enMap.get(eElement.getElementsByTagName("Team").item(0).getTextContent()) == null) {
					System.out.println("WARN: " + eElement.getElementsByTagName("Team").item(0).getTextContent().trim()
							+ "-->" + xmlFile);
					counter++;
				}
//				System.out.println("Scorer: " + eElement.getElementsByTagName("Scorer").item(0).getTextContent().trim());
//				System.out.println("Team: " + eElement.getElementsByTagName("Team").item(0).getTextContent().trim()
//						+ "--> " + enMap.get(eElement.getElementsByTagName("Team").item(0).getTextContent().trim()));
//				System.out.println("Team: " + eElement.getElementsByTagName("Team").item(0).getTextContent().trim()
//						+ "--> " + deMap.get(eElement.getElementsByTagName("Team").item(0).getTextContent().trim()));
//				System.out.println("Minute: " + eElement.getElementsByTagName("Minute").item(0).getTextContent().trim());
//				System.out.println(
//						"CurrentScore: " + eElement.getElementsByTagName("CurrentScore").item(0).getTextContent().trim());

			}
		}
	}
}
