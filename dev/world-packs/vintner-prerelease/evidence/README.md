# Manual evidence record

Store actual evidence externally at the path in worlds/README.md; this directory holds instructions only. Do not commit player UUIDs, saves, logs or screenshots.

For every run record:

- Run ID, operator(s), date/time, checklist and PASS / FAIL / PENDING / N/A per item.
- Exact Vintner branch/commit/mod version and JAR SHA-256; Minecraft, Java, Loader, Fabric API; Commons version/commit/JAR hash; all optional mods and hashes.
- Save source/copy/active-instance paths; frozen archive checksum; seed, dimension, world type and difficulty.
- World created/refreshed dates, hub/station XYZ, view/simulation/render distances, GUI scale, language, shaders/resource packs.
- Fixture census: cultivar/rootstock/mode/age; plot owner/name/dimension/bounds; bottle counts/servings/batch/vintage/quality; machine progress; ledger/reputation/facility before/after.
- Screenshot/log paths, expected versus actual result, reproduction steps and whether failure blocks release.
- Missing preconditions, weather/season limits, and whether actual chunk unloading was observed or merely attempted.

Keep before/after screenshots for persistence and owner isolation. Preserve a failing save before repair. Export server/client logs after a clean stop; keep each run separate. Client visual evidence, gameplay observation, automated tests and historical profiling are different evidence types.

Use file sizes to choose external archival storage; never add generated region/entity/POI files or session locks to Git. A completed checklist must link to evidence, not just a check mark.
