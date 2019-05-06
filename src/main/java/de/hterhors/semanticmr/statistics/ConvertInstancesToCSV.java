package de.hterhors.semanticmr.statistics;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.slots.MultiFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.examples.psink.normalization.WeightNormalization;
import de.hterhors.semanticmr.examples.psink.santo.ScioSanto2Json;
import de.hterhors.semanticmr.init.specifications.SystemScope;

/**
 * This class converts the instances of Results into one big csv table for the
 * guy from statistics.
 * 
 * @author hterhors
 *
 */
public class ConvertInstancesToCSV {

	private static final File instancesFileDir = new File("test");
	private static final String SPLITTER = "\t";

	public static void main(String[] args) throws IOException {

		new ConvertInstancesToCSV(instancesFileDir);
	}

	public ConvertInstancesToCSV(File instancesfiledir) throws IOException {

		SystemScope.Builder.getSpecsHandler().addScopeSpecification(ScioSanto2Json.systemsScope).build();

		InstanceProvider.removeEmptyInstances = true;
		InstanceProvider.removeInstancesWithToManyAnnotations = false;

		InstanceProvider instanceProvider = new InstanceProvider(instancesfiledir);

		Map<Instance, List<Map<String, String>>> collectedRows = new HashMap<>();

		for (Instance instance : instanceProvider.getInstances()) {

			List<Map<String, String>> rows = new ArrayList<>();

			for (AbstractAnnotation annotation : instance.getGoldAnnotations().getAnnotations()) {

				/**
				 * UGLY CHECK FOR EMPTY ANNOTATIONS!
				 */
				if (annotation.toPrettyString().equals("Result"))
					continue;

				Map<String, String> row = new HashMap<>();

				collectFlattenedInstance(row, "Result", annotation);

				rows.add(row);
			}
			collectedRows.put(instance, rows);
		}

		List<String> columnNames = collectedRows.values().stream().flatMap(a -> a.stream())
				.flatMap(m -> m.keySet().stream()).distinct().sorted().collect(Collectors.toList());

		System.out.println(columnNames.size());

		Map<String, Integer> columnIndicies = new HashMap<>();

		for (String coloumnName : columnNames) {
			columnIndicies.put(coloumnName, columnIndicies.size());
		}

		PrintStream ps = new PrintStream(new File("results_CORRUPTED.csv"));

		String header = "Document" + SPLITTER;
		for (String string : columnNames) {
			header += string;
			header += SPLITTER;
		}

		ps.println(header.trim());

		for (Entry<Instance, List<Map<String, String>>> rowsForInstance : collectedRows.entrySet()) {

			final String instanceName = rowsForInstance.getKey().getName();

			for (Map<String, String> columns : rowsForInstance.getValue()) {
				String[] csvFormat = new String[columnNames.size()];

				for (Entry<String, String> columnValuePair : columns.entrySet()) {

					final int coloumnIndex = columnIndicies.get(columnValuePair.getKey()).intValue();
					final String value = columnValuePair.getValue();
					csvFormat[coloumnIndex] = value;
				}

				StringBuffer row = new StringBuffer(instanceName);

				for (int i = 0; i < columnNames.size(); i++) {
					row.append(SPLITTER).append(csvFormat[i]);
				}
				ps.println(row.toString());
			}

		}

		ps.close();
	}

	private void collectFlattenedInstance(Map<String, String> values, final String path,
			AbstractAnnotation annotation) {

		if (values.get(path) != null)
			throw new IllegalStateException("Path already exists: " + path);

		if (annotation == null)
			values.put(path, "null");

		else if (annotation instanceof DocumentLinkedAnnotation) {

			if (annotation.getEntityType().isLiteral) {
				values.put(path, "\""
						+ ((DocumentLinkedAnnotation) annotation).getSurfaceForm().replaceAll("\"", "\\\\\"") + "\"");
			} else {
				values.put(path, annotation.getEntityType().entityName);
			}
		}

		else if (annotation instanceof EntityTemplate) {

			values.put(path, annotation.getEntityType().entityName);

			for (Entry<SlotType, SingleFillerSlot> sfs : ((EntityTemplate) annotation).getSingleFillerSlots()
					.entrySet()) {

				String p = path + "_" + sfs.getKey().slotName;

				SingleFillerSlot sf = sfs.getValue();

				AbstractAnnotation v = sf.getSlotFiller();

				collectFlattenedInstance(values, p, v);

			}
			for (Entry<SlotType, MultiFillerSlot> msf : ((EntityTemplate) annotation).getMultiFillerSlots()
					.entrySet()) {

				MultiFillerSlot mf = msf.getValue();

				LinkedHashSet<AbstractAnnotation> vs = mf.getSlotFiller();

				int count = 0;
				for (AbstractAnnotation v : vs) {
					String p = path + "_" + msf.getKey().slotName + "[" + count + "]";
					collectFlattenedInstance(values, p, v);
					count++;
				}

			}

		}

	}

}
