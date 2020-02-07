package de.hterhors.semanticmr.candidateretrieval.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;

public class DictionaryFromInstanceHelper {

	public static Set<AbstractAnnotation> getAnnotationsForInstance(Instance instance,
			Map<EntityType, Set<String>> trainDictionary) {

		Set<AbstractAnnotation> annotations = new HashSet<>();

		String doc = instance.getDocument().documentContent;
		for (Entry<EntityType, Set<String>> e : trainDictionary.entrySet()) {

			for (String s : e.getValue()) {

				Matcher m = Pattern.compile(Pattern.quote(s)).matcher(doc);

				while (m.find()) {
					try {
						if (e.getKey().hasNoSlots())
							annotations.add(AnnotationBuilder.toAnnotation(instance.getDocument(), e.getKey().name,
									m.group(), m.start()));
						else
							annotations.add(new EntityTemplate(AnnotationBuilder.toAnnotation(instance.getDocument(),
									e.getKey().name, m.group(), m.start())));
					} catch (RuntimeException e2) {
					}

				}

			}

		}
		return annotations;
	}

	/**
	 * Creates a full recursive property dictionary of all available slots.
	 * 
	 * @param trainingInstances
	 * @return
	 */
	public static Map<EntityType, Set<String>> toDictionary(List<Instance> trainingInstances) {
		Map<EntityType, Set<String>> map = new HashMap<>();

		for (Instance instance : trainingInstances) {
			for (AbstractAnnotation aa : instance.getGoldAnnotations().getAnnotations()) {
				recursive(map, aa);
			}
		}

		return map;
	}

	private static void recursive(Map<EntityType, Set<String>> map, AbstractAnnotation aa) {
		if (aa.isInstanceOfEntityTemplate()) {

			AbstractAnnotation rootA = aa.asInstanceOfEntityTemplate().getRootAnnotation();

			if (rootA.isInstanceOfLiteralAnnotation()) {

				map.putIfAbsent(rootA.getEntityType(), new HashSet<>());
				String e = rootA.asInstanceOfLiteralAnnotation().getSurfaceForm();
				map.get(rootA.getEntityType()).add(e);

			}

			for (AbstractAnnotation slotFillerValue : Stream
					.concat(aa.asInstanceOfEntityTemplate().streamSingleFillerSlotValues(),
							aa.asInstanceOfEntityTemplate().flatStreamMultiFillerSlotValues())
					.collect(Collectors.toSet())) {

				if (slotFillerValue.isInstanceOfEntityTemplate()) {

					AbstractAnnotation rootS = slotFillerValue.asInstanceOfEntityTemplate().getRootAnnotation();

					if (rootS.isInstanceOfLiteralAnnotation()) {

						map.putIfAbsent(rootS.getEntityType(), new HashSet<>());
						String e = rootS.asInstanceOfLiteralAnnotation().getSurfaceForm();
						map.get(rootS.getEntityType()).add(e);

					}

				}

				recursive(map, slotFillerValue);

			}
		} else if (aa.isInstanceOfLiteralAnnotation()) {
			map.putIfAbsent(aa.getEntityType(), new HashSet<>());
			String e = aa.asInstanceOfLiteralAnnotation().getSurfaceForm();
			map.get(aa.getEntityType()).add(e);
		}
	}

}
