package de.hterhors.semanticmr.examples.olp2.corpus.preprocessing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;

public class XMLReader {

	final private Map<String, String> deMap = new HashMap<>();
	final private Map<String, String> enMap = new HashMap<>();

	final private File xmlDir;

	public XMLReader(File xmlDir) throws Exception {

		fillDeMap();

		fillEnMap();

		this.xmlDir = xmlDir;

//		Set<String> player = new HashSet<>();
//		Set<String> teams = new HashSet<>();
//
//		for (File xmlFile : xmlDir.listFiles()) {
//
//			if (!xmlFile.getName().startsWith("en_"))
//				continue;
//
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(xmlFile);
//
//			doc.getDocumentElement().normalize();
//
//			player.addAll(readPlayer(doc, "Team1"));
//			player.addAll(readPlayer(doc, "Team2"));
////
//			teams.addAll(readTeams(doc, "Team1"));
//			teams.addAll(readTeams(doc, "Team2"));
////
////			readGoals(doc, xmlFile);
//		}

//		List<String> sortedPlayer = new ArrayList<>(player);
//		List<String> sortedTeams = new ArrayList<>(teams);

////		Collections.sort(sortedPlayer);
//		Collections.sort(sortedTeams);
////
//		for (String p : sortedTeams) {
//			System.out.println(p+"\tfalse");
////			System.out.println("Team\t" + p);
//		}
//		System.exit(1);

	}

	final public static File deMappingFile = new File("olp2/de_ISO-ALPHA-3.map");

	private void fillDeMap() throws IOException {
		List<String> lines = Files.readAllLines(deMappingFile.toPath());

		for (String line : lines) {

			String[] d = line.split("\t");

			if (d.length != 2)
				continue;

			deMap.put(d[1].trim(), d[0].trim());

		}
	}

	final public static File enMappingFile = new File("olp2/en_ISO-ALPHA-3.map");

	private void fillEnMap() throws IOException {
		List<String> lines = Files.readAllLines(enMappingFile.toPath());

		for (String line : lines) {

			String[] d = line.split("\t");

			if (d.length != 2)
				continue;

			enMap.put(d[1].trim(), d[0].trim());

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
				player.add(name(gk.getElementsByTagName("Name").item(0).getTextContent().trim()));
			}
			for (int i = 0; i < n.getElementsByTagName("OtherFootballPlayer").getLength(); i++) {

				Element of = (Element) n.getElementsByTagName("OtherFootballPlayer").item(i);

//				System.out.println(of.getElementsByTagName("Name").item(0).getTextContent());
				player.add(name(of.getElementsByTagName("Name").item(0).getTextContent().trim()));
			}
		}
		return player;
	}

	private static String name(String trim) {
		if (trim.isEmpty())
			return trim;

		String name = "";

		for (String n : trim.split(" ")) {
			name += Character.toUpperCase(n.substring(0, 1).charAt(0)) + n.toLowerCase().substring(1);
			name += " ";
		}

		return name.trim();
	}

	static int counter = 0;

//<Goals>
//<Goal>
//  <Scorer>TANA Elijah</Scorer>
//  <Team>ZAM</Team>
//  <Minute>1</Minute>
//  <CurrentScore>1:0</CurrentScore>
//</Goal>
	public List<EntityTemplate> readGoals(String xmlFile) throws Exception {

		final File file = new File(xmlDir, xmlFile + ".xml");

		if (!file.exists())
			return Collections.emptyList();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName("Goal");

		List<EntityTemplate> templateEntities = new ArrayList<>();

		for (int temp = 0; temp < nList.getLength(); temp++) {

			EntityTemplate templateEntity = new EntityTemplate(AnnotationBuilder.toAnnotation(EntityType.get("Goal")));
			templateEntities.add(templateEntity);

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				Map<String, String> map = xmlFile.startsWith("de_") ? deMap : enMap;

				final String scorerEntityTyeName = name(
						eElement.getElementsByTagName("Scorer").item(0).getTextContent().trim());
				final String teamEntityTyeName = map
						.get(eElement.getElementsByTagName("Team").item(0).getTextContent().trim());
				final String minuteEntityTyeName = eElement.getElementsByTagName("Minute").item(0).getTextContent()
						.trim();
				final String currentScoreA = eElement.getElementsByTagName("CurrentScore").item(0).getTextContent()
						.trim().split(":")[0];
				final String currentScoreB = eElement.getElementsByTagName("CurrentScore").item(0).getTextContent()
						.trim().split(":")[1];

				templateEntity.setSingleSlotFiller(SlotType.get("scorer"),
						AnnotationBuilder.toAnnotation(scorerEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("forTeam"),
						AnnotationBuilder.toAnnotation(teamEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("minute"),
						AnnotationBuilder.toAnnotation(minuteEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("scoreTeamA"),
						AnnotationBuilder.toAnnotation(currentScoreA));
				templateEntity.setSingleSlotFiller(SlotType.get("scoreTeamB"),
						AnnotationBuilder.toAnnotation(currentScoreB));

			}
		}
		return templateEntities;
	}

//	 <Cards>
//	    <YellowCard>
//	      <Player>TANA Elijah</Player>
//	      <Team>ZAM</Team>
//	      <Minute>27</Minute>
//	    </YellowCard>
	public List<EntityTemplate> readYellowCards(String xmlFile) throws Exception {
		return readCard(xmlFile, "YellowCard");
	}

	public List<EntityTemplate> readRedCards(String xmlFile) throws Exception {
		return readCard(xmlFile, "RedCard");
	}

	private List<EntityTemplate> readCard(String xmlFile, final String card)
			throws ParserConfigurationException, SAXException, IOException {
		final File file = new File(xmlDir, xmlFile + ".xml");

		if (!file.exists())
			return Collections.emptyList();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName(card);

		List<EntityTemplate> templateEntities = new ArrayList<>();

		for (int temp = 0; temp < nList.getLength(); temp++) {

			EntityTemplate templateEntity = new EntityTemplate(AnnotationBuilder.toAnnotation(EntityType.get(card)));
			templateEntities.add(templateEntity);

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				Map<String, String> map = xmlFile.startsWith("de_") ? deMap : enMap;

				final String scorerEntityTyeName = name(
						eElement.getElementsByTagName("Player").item(0).getTextContent().trim());
				final String teamEntityTyeName = map
						.get(eElement.getElementsByTagName("Team").item(0).getTextContent().trim());
				final String minuteEntityTyeName = eElement.getElementsByTagName("Minute").item(0).getTextContent()
						.trim();

				templateEntity.setSingleSlotFiller(SlotType.get("player"),
						AnnotationBuilder.toAnnotation(scorerEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("forTeam"),
						AnnotationBuilder.toAnnotation(teamEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("minute"),
						AnnotationBuilder.toAnnotation(minuteEntityTyeName));

			}
		}
		return templateEntities;
	}

//	<Team1>
//    <Name>Peru</Name>
//    <FinalScoreResult>4</FinalScoreResult>
//  </Team1>
//  <Team2>
//    <Name>Paraguay</Name>
//    <FinalScoreResult>1</FinalScoreResult>
//  </Team2>
	public List<EntityTemplate> readTeamAStats(String xmlFile)
			throws ParserConfigurationException, SAXException, IOException {
		return readTeamStats(xmlFile, "Team1");
	}

	public List<EntityTemplate> readTeamBStats(String xmlFile)
			throws ParserConfigurationException, SAXException, IOException {
		return readTeamStats(xmlFile, "Team2");
	}

	private List<EntityTemplate> readTeamStats(String xmlFile, final String team)
			throws ParserConfigurationException, SAXException, IOException {
		final File file = new File(xmlDir, xmlFile + ".xml");

		if (!file.exists())
			return Collections.emptyList();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);

		doc.getDocumentElement().normalize();

		NodeList nList = doc.getElementsByTagName(team);

		List<EntityTemplate> templateEntities = new ArrayList<>();

		for (int temp = 0; temp < nList.getLength(); temp++) {

			EntityTemplate templateEntity = new EntityTemplate(AnnotationBuilder.toAnnotation(EntityType.get(team)));
			templateEntities.add(templateEntity);

			Node nNode = nList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				Element eElement = (Element) nNode;

				final String teamEntityTyeName = eElement.getElementsByTagName("Name").item(0).getTextContent().trim();
				final String minuteEntityTyeName = eElement.getElementsByTagName("FinalScoreResult").item(0)
						.getTextContent().trim();

				templateEntity.setSingleSlotFiller(SlotType.get("name"),
						AnnotationBuilder.toAnnotation(teamEntityTyeName));
				templateEntity.setSingleSlotFiller(SlotType.get("finalScore"),
						AnnotationBuilder.toAnnotation(minuteEntityTyeName));

			}
		}
		return templateEntities;
	}

}