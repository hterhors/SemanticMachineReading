package de.hterhors.semanticmr.projects.olp2.corpus.preprocessing;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.hterhors.semanticmr.corpus.EInstanceContext;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.init.reader.csv.CSVScopeReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JsonInstanceIO;
import de.hterhors.semanticmr.json.converter.InstancesToJsonInstanceWrapper;
import de.hterhors.semanticmr.projects.olp2.corpus.preprocessing.CrossRefReader.CrossRef;
import de.hterhors.semanticmr.projects.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.tokenizer.StandardDocumentTokenizer;

public class StartPreprocessing {

	private static final File de_entities = new File("projects/olp2/de/specs/csv/entities.csv");
	private static final File de_slots = new File("projects/olp2/de/specs/csv/slots.csv");
	private static final File de_hierarchies = new File(
			"projects/olp2/de/specs/csv/hierarchies.csv");
	private static final File de_structure = new File("projects/olp2/de/specs/csv/structures.csv");

	private static final File en_entities = new File("projects/olp2/en/specs/csv/entities.csv");
	private static final File en_slots = new File("projects/olp2/en/specs/csv/slots.csv");
	private static final File en_hierarchies = new File(
			"projects/olp2/en/specs/csv/hierarchies.csv");
	private static final File en_structure = new File("projects/olp2/en/specs/csv/structures.csv");

	public final static CSVScopeReader de_specificationProvider = new CSVScopeReader(de_entities, de_hierarchies,
			de_slots, de_structure);

	public final static CSVScopeReader en_specificationProvider = new CSVScopeReader(en_entities, en_hierarchies,
			en_slots, en_structure);

	public static void main(String[] args) throws Exception {
		new StartPreprocessing("de");
	}

	public StartPreprocessing(String language) throws Exception {
		if (language.equals("en"))
			en();
		if (language.equals("de"))
			de();
	}

	public static void de() throws Exception {
		SystemScope.Builder.getSpecsHandler().addScopeSpecification(de_specificationProvider).build();

		CrossRefReader crr = new CrossRefReader(new File("olp2/Crossref/"));
		TextReader tr = new TextReader(new File("olp2/Text/"));
		XMLReader xml2json = new XMLReader(new File("olp2/SemiStructured/"));

		for (Entry<String, CrossRef> crossRefEntry : crr.de_crossRefMap.entrySet()) {
			final String docContent = getContent(tr.de_textMap, crossRefEntry);

			InstancesToJsonInstanceWrapper w = new InstancesToJsonInstanceWrapper(
					Arrays.asList(getInstances(xml2json, docContent, crossRefEntry.getValue().structID)));

			JsonInstanceIO io = new JsonInstanceIO(true);

			final String ins = io.writeInstances(w.convertToWrapperInstances());

			PrintStream ps = new PrintStream(
					new File("projects/olp2/de/corpus/sf/" + crossRefEntry.getKey() + ".json"));
			ps.println(ins);
			ps.close();
		}
	}

	public static void en() throws Exception {
		SystemScope.Builder.getSpecsHandler().addScopeSpecification(en_specificationProvider).apply()
				.registerNormalizationFunction(new WeightNormalization()).build();

		CrossRefReader crr = new CrossRefReader(new File("olp2/Crossref/"));

		TextReader tr = new TextReader(new File("olp2/Text/"));
		XMLReader xml2json = new XMLReader(new File("olp2/SemiStructured/"));

		for (Entry<String, CrossRef> crossRefEntry : crr.en_crossRefMap.entrySet()) {
			final String docContent = getContent(tr.en_textMap, crossRefEntry);

			InstancesToJsonInstanceWrapper w = new InstancesToJsonInstanceWrapper(
					Arrays.asList(getInstances(xml2json, docContent, crossRefEntry.getValue().structID)));

			JsonInstanceIO io = new JsonInstanceIO(true);

			final String ins = io.writeInstances(w.convertToWrapperInstances());

			PrintStream ps = new PrintStream(
					new File("projects/olp2/en/corpus/sf/" + crossRefEntry.getKey() + ".json"));
			ps.println(ins);
			ps.close();

		}
	}

	private static Instance getInstances(XMLReader xml2json, String docContent, String structureID) throws Exception {
		final List<AbstractAnnotation> annotations = new ArrayList<>();
		for (EntityTemplate et : xml2json.readGoals(structureID)) {
			annotations.add(et);
		}
		for (EntityTemplate et : xml2json.readRedCards(structureID)) {
			annotations.add(et);
		}
		for (EntityTemplate et : xml2json.readYellowCards(structureID)) {
			annotations.add(et);
		}
		for (EntityTemplate et : xml2json.readTeamAStats(structureID)) {
			annotations.add(et);
		}
		for (EntityTemplate et : xml2json.readTeamBStats(structureID)) {
			annotations.add(et);
		}
		return new Instance(EInstanceContext.UNSPECIFIED,
				new Document(structureID, StandardDocumentTokenizer.tokenizeDocumentsContent(docContent)),
				new Annotations(annotations));
	}

	public static String getContent(Map<String, String> textMap, Entry<String, CrossRef> crossRefEntry) {
		String content = "";
		for (String crossRef2Text : crossRefEntry.getValue().crossRefMap) {
			content += textMap.get(crossRef2Text);
			content += "\n";
		}
		return content.trim();
	}

}
