package de.hterhors.semanticmr.crf.structure.annotations.filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTemplate;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.LiteralAnnotation;
import de.hterhors.semanticmr.crf.structure.slots.MultiFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SingleFillerSlot;
import de.hterhors.semanticmr.crf.structure.slots.SlotType;

public class EntityTemplateAnnotationFilter implements IAnnotationFilter {

	final private Map<SlotType, AbstractAnnotation<? extends AbstractAnnotation<?>>> singleAnnotations;
	final private Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> multiAnnotations;
	final private Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> mergedAnnotations;

	public EntityTemplateAnnotationFilter(
			Map<SlotType, AbstractAnnotation<? extends AbstractAnnotation<?>>> singleAnnotations,
			Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> multiAnnotations,
			Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> mergedAnnotations) {
		this.singleAnnotations = singleAnnotations;
		this.multiAnnotations = multiAnnotations;
		this.mergedAnnotations = mergedAnnotations;
	}

	public Map<SlotType, AbstractAnnotation<? extends AbstractAnnotation<?>>> getSingleAnnotations() {
		return singleAnnotations;
	}

	public Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> getMultiAnnotations() {
		return multiAnnotations;
	}

	public Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> getMergedAnnotations() {
		return mergedAnnotations;
	}

	public static class Builder implements IBuilder<EntityTemplateAnnotationFilter> {

		final private EntityTemplate entityTemplate;
		private boolean multiSlots;
		private boolean singleSlots;
		private boolean nonEmpty;
		private boolean docLinkedAnnoation;
		private boolean literalAnnoation;
		private boolean entityTypeAnnoation;
		private boolean entityTemplateAnnoation;
		private boolean merge;

		private Map<SlotType, AbstractAnnotation<? extends AbstractAnnotation<?>>> singleAnnotations;
		private Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> multiAnnotations;
		private Map<SlotType, Set<AbstractAnnotation<? extends AbstractAnnotation<?>>>> mergedAnnotations;

		public Builder(EntityTemplate entityTemplate) {
			this.entityTemplate = entityTemplate;
		}

		public Builder singleSlots() {
			this.singleSlots = true;
			return this;
		}

		public Builder multiSlots() {
			this.multiSlots = true;
			return this;
		}

		public Builder nonEmpty() {
			this.nonEmpty = true;
			return this;
		}

		public Builder docLinkedAnnoation() {
			this.docLinkedAnnoation = true;
			return this;
		}

		public Builder literalAnnoation() {
			this.literalAnnoation = true;
			return this;
		}

		public Builder entityTypeAnnoation() {
			this.entityTypeAnnoation = true;
			return this;
		}

		public Builder entityTemplateAnnoation() {
			this.entityTemplateAnnoation = true;
			return this;
		}

		public Builder merge() {
			this.merge = true;
			return this;
		}

		public EntityTemplateAnnotationFilter build() {

			if (merge) {
				mergedAnnotations = new HashMap<>();
			} else {
				mergedAnnotations = Collections.emptyMap();
			}

			if (multiSlots) {
				multiAnnotations = new HashMap<>();

				for (SlotType slotType : entityTemplate.getMultiFillerSlots().keySet()) {

					final MultiFillerSlot slot = entityTemplate.getMultiFillerSlot(slotType);

					if (nonEmpty && !slot.containsSlotFiller())
						continue;

					for (AbstractAnnotation<? extends AbstractAnnotation<?>> slotFiller : slot.getSlotFiller()) {

						if (docLinkedAnnoation) {

							final DocumentLinkedAnnotation dla = slotFiller.asInstanceOfDocumentLinkedAnnotation();

							if (dla == null)
								continue;

							if (merge) {
								mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
								mergedAnnotations.get(slotType).add(dla);
							}
							multiAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							multiAnnotations.get(slotType).add(dla);
						}

						if (literalAnnoation) {

							final LiteralAnnotation la = slotFiller.asInstanceOfLiteralAnnotation();

							if (la == null)
								continue;

							if (merge) {
								mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
								mergedAnnotations.get(slotType).add(la);
							}

							multiAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							multiAnnotations.get(slotType).add(la);
						}
						if (entityTypeAnnoation) {

							final EntityTypeAnnotation eta = slotFiller.asInstanceOfEntityTypeAnnotation();

							if (eta == null)
								continue;

							if (merge) {
								mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
								mergedAnnotations.get(slotType).add(eta);
							}

							multiAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							multiAnnotations.get(slotType).add(eta);
						}
						if (entityTemplateAnnoation) {

							final EntityTemplate et = slotFiller.asInstanceOfEntityTemplate();

							if (et == null)
								continue;

							if (merge) {
								mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
								mergedAnnotations.get(slotType).add(et);
							}

							multiAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							multiAnnotations.get(slotType).add(et);
						}
					}
				}
			} else {
				multiAnnotations = Collections.emptyMap();
			}

			if (singleSlots) {
				singleAnnotations = new HashMap<>();
				for (SlotType slotType : entityTemplate.getSingleFillerSlots().keySet()) {

					final SingleFillerSlot slot = entityTemplate.getSingleFillerSlot(slotType);

					if (nonEmpty && !slot.containsSlotFiller())
						continue;

					if (docLinkedAnnoation) {

						final DocumentLinkedAnnotation slotFiller = slot.getSlotFiller()
								.asInstanceOfDocumentLinkedAnnotation();

						if (slotFiller == null)
							continue;

						if (merge) {
							mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							mergedAnnotations.get(slotType).add(slotFiller);
						}
						singleAnnotations.put(slotType, slotFiller);
					}
					if (literalAnnoation) {

						final LiteralAnnotation slotFiller = slot.getSlotFiller().asInstanceOfLiteralAnnotation();

						if (slotFiller == null)
							continue;

						if (merge) {
							mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							mergedAnnotations.get(slotType).add(slotFiller);
						}
						singleAnnotations.put(slotType, slotFiller);
					}
					if (entityTypeAnnoation) {

						final EntityTypeAnnotation slotFiller = slot.getSlotFiller().asInstanceOfEntityTypeAnnotation();

						if (slotFiller == null)
							continue;

						if (merge) {
							mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							mergedAnnotations.get(slotType).add(slotFiller);
						}
						singleAnnotations.put(slotType, slotFiller);
					}
					if (entityTemplateAnnoation) {

						final EntityTemplate slotFiller = slot.getSlotFiller().asInstanceOfEntityTemplate();

						if (slotFiller == null)
							continue;

						if (merge) {
							mergedAnnotations.putIfAbsent(slotType, new LinkedHashSet<>());
							mergedAnnotations.get(slotType).add(slotFiller);
						}
						singleAnnotations.put(slotType, slotFiller);
					}
				}
			} else {
				singleAnnotations = Collections.emptyMap();
			}

			return new EntityTemplateAnnotationFilter(singleAnnotations, multiAnnotations, mergedAnnotations);
		}

	}

}
