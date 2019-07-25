package de.hterhors.semanticmr.candprov.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
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
							annotations.add(AnnotationBuilder.toAnnotation(instance.getDocument(),
									e.getKey().entityName, m.group(), m.start()));
						else
							annotations.add(new EntityTemplate(AnnotationBuilder.toAnnotation(instance.getDocument(),
									e.getKey().entityName, m.group(), m.start())));
					} catch (RuntimeException e2) {
					}

				}

			}

		}

		return annotations;
	}

	public static Map<EntityType, Set<String>> toDictionary(List<Instance> trainingInstances) {
		Map<EntityType, Set<String>> map = new HashMap<>();

		for (Instance instance : trainingInstances) {

			for (AbstractAnnotation aa : instance.getGoldAnnotations().getAnnotations()) {

				if (aa.isInstanceOfEntityTemplate()) {

					AbstractAnnotation rootA = aa.asInstanceOfEntityTemplate().getRootAnnotation();

					if (rootA.isInstanceOfLiteralAnnotation()) {

						map.putIfAbsent(rootA.getEntityType(), new HashSet<>());
						String e = rootA.asInstanceOfLiteralAnnotation().getSurfaceForm();
						map.get(rootA.getEntityType()).add(e);

					}

					for (AbstractAnnotation slotFillerValue : aa.asInstanceOfEntityTemplate()
							.getAllSlotFillerValues()) {

						if (slotFillerValue.isInstanceOfEntityTemplate()) {

							AbstractAnnotation rootS = slotFillerValue.asInstanceOfEntityTemplate().getRootAnnotation();

							if (rootS.isInstanceOfLiteralAnnotation()) {

								map.putIfAbsent(rootS.getEntityType(), new HashSet<>());
								String e = rootS.asInstanceOfLiteralAnnotation().getSurfaceForm();
								map.get(rootS.getEntityType()).add(e);

							}
						}

						if (slotFillerValue.isInstanceOfLiteralAnnotation()) {
							map.putIfAbsent(slotFillerValue.getEntityType(), new HashSet<>());
							String e = slotFillerValue.asInstanceOfLiteralAnnotation().getSurfaceForm();
							map.get(slotFillerValue.getEntityType()).add(e);
						}

					}
				}

			}

		}

		return map;
	}

}
