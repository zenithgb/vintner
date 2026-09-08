package com.zenithgb.library.knowledge;

import com.zenithgb.library.module.ModuleRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.profiling.InactiveProfiler;

/** Test-only package bridge to Commons' real loader; never resets or publishes to its global catalogues. */
public final class VintnerContributionAssertions {
    private VintnerContributionAssertions() { }

    public static void verify(GameTestHelper helper) {
        var resources = helper.getLevel().getServer().getResourceManager();
        var loader = new KnowledgeContributionReloadListener(new KnowledgeCatalogue(),
                new KnowledgeCategoryCatalogue(), ModuleRegistry.getInstance()::modules);
        var candidate = loader.prepare(resources, InactiveProfiler.INSTANCE);
        var categoryIds = List.of("getting_started", "viticulture", "winemaking", "cellaring_quality");
        var names = List.of("Getting Started", "Viticulture", "Winemaking", "Cellaring & Quality");
        var membership = List.of(List.of("introduction"), List.of("grapevines", "vineyard_sites"),
                List.of("pressing_and_must", "fermentation"),
                List.of("ageing_and_treatments", "cellars", "wine_scoring", "vintages_and_archives"));
        var titles = Map.of("introduction", "Vintner", "grapevines", "Grapevines",
                "vineyard_sites", "Vineyard Sites", "pressing_and_must", "Pressing and Must",
                "fermentation", "Fermentation", "ageing_and_treatments", "Ageing and Treatments",
                "cellars", "Cellars", "wine_scoring", "Wine Scoring",
                "vintages_and_archives", "Vintages and Archives");
        var related = Map.of("introduction", List.of("grapevines", "pressing_and_must"),
                "grapevines", List.of("vineyard_sites", "pressing_and_must"),
                "vineyard_sites", List.of("grapevines"),
                "pressing_and_must", List.of("fermentation", "grapevines"),
                "fermentation", List.of("pressing_and_must", "ageing_and_treatments"),
                "ageing_and_treatments", List.of("fermentation", "cellars", "wine_scoring"),
                "cellars", List.of("ageing_and_treatments", "wine_scoring"),
                "wine_scoring", List.of("ageing_and_treatments", "vintages_and_archives"),
                "vintages_and_archives", List.of("wine_scoring", "cellars"));
        helper.assertTrue(candidate.categories().keySet().equals(new HashSet<>(ids(categoryIds))), "Exact four category IDs");
        helper.assertTrue(candidate.entries().keySet().equals(new HashSet<>(ids(List.copyOf(titles.keySet())))), "Exact nine entry IDs");
        var assigned = new HashSet<Identifier>();
        for (int i = 0; i < categoryIds.size(); i++) {
            var category = candidate.categories().get(id(categoryIds.get(i)));
            helper.assertTrue(category.module().equals(id("vintner")), "Category owner: " + category.id());
            helper.assertTrue(category.name().equals(names.get(i)) && category.order() == (i + 1) * 10, "Category name/order: " + category.id());
            helper.assertTrue(category.entries().equals(ids(membership.get(i))), "Exact category membership/order: " + category.id());
            for (var entryId : category.entries()) {
                helper.assertTrue(assigned.add(entryId), "Each entry belongs to exactly one category: " + entryId);
                var entry = candidate.entries().get(entryId);
                helper.assertTrue(entry.moduleId().equals(id("vintner")) && entry.categoryId().equals(category.id()), "Entry ownership/category: " + entryId);
                helper.assertTrue(entry.title().equals(titles.get(entryId.getPath())), "Entry title: " + entryId);
                helper.assertTrue(entry.requiresModuleIds().isEmpty(), "No conditional contribution: " + entryId);
                helper.assertTrue(entry.relatedEntryIds().equals(ids(related.get(entryId.getPath()))), "Exact internal related links: " + entryId);
                helper.assertTrue(candidate.entries().keySet().containsAll(entry.relatedEntryIds()), "Related targets resolve: " + entryId);
            }
        }
        helper.assertTrue(assigned.equals(candidate.entries().keySet()), "No unassigned entries");
        loader.apply(candidate, resources, InactiveProfiler.INSTANCE);
        var presentation = loader.presentation();
        helper.assertTrue(presentation.modules().size() == 1
                && presentation.modules().getFirst().id().equals(id("vintner"))
                && presentation.modules().getFirst().displayName().equals("Vintner"), "Assembly uses the real registered Vintner module");
        helper.assertTrue(presentation.categories().stream().map(KnowledgePresentation.Category::id).toList().equals(ids(categoryIds)), "Actual assembled category order");
        for (int i = 0; i < membership.size(); i++) {
            helper.assertTrue(presentation.categories().get(i).entries().stream().map(KnowledgeEntry::id).toList().equals(ids(membership.get(i))), "Actual assembled article order");
        }
    }

    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath("vintner", path); }
    private static List<Identifier> ids(List<String> paths) { return paths.stream().map(VintnerContributionAssertions::id).toList(); }
}
