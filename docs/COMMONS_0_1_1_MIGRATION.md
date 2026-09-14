# Commons 0.1.1 dependency migration — Phase 10D3B

Validated 2026-09-14. This record covers the exact Vintner dependency-pin
migration from Commons 0.1.0 to Commons 0.1.1. Historical Commons integration
and 0.1.0 migration records remain unchanged.

## Baselines and scope

- Vintner started from `8b44ec121e27b1b6c9c092ba0c5b6640e1e3d660` on
  `feat/1.4.0-vineyard-management`, six commits ahead of its remote. The
  migration branch is `phase/10d3b-commons-0.1.1`.
- Vintner remains version 1.4.0. All gameplay, knowledge content, specialist
  interfaces, registry identities, and newer 1.4.0 work remain Vintner-owned
  and unchanged by this phase.
- Pre-existing release/QA documentation and package work in the checkout was
  protected and excluded from this migration commit.
- Commons was inspected, not rebuilt or modified. The frozen artifact is
  `/Users/zachariaheverson/Developer/Minecraft/zenithgb-commons/build/libs/zenithgb-commons-0.1.1.jar`,
  317,167 bytes, SHA-256
  `745d86b146c323167837d14ed9ebb6e7cb4f5062d8a4fded8d0e95398b24b420`.
  Its packaged identity is `zenithgb_library` 0.1.1, Commons, protocol 2,
  LGPL-3.0-or-later, for Minecraft 26.2, Loader 0.19.3+, Fabric API
  0.158.0+26.2+, and Java 25+.

## Dependency change

Production metadata now requires exactly `zenithgb_library` 0.1.1. The Gradle
guard reads the supplied external JAR's `fabric.mod.json` and accepts only that
exact ID/version. Commons remains an external `implementation files(...)`
dependency: it is not embedded, shaded, nested, or copied into Vintner. Tilth
and Reeve remain optional and are not required dependencies.

The focused runtime identity assertion now expects the real loaded Commons
0.1.1 metadata. Module registration remains `vintner:vintner`, display name
Vintner, with the Vintner version read from runtime Fabric metadata. No
registration design or production Java changed.

## Validation

The following gates were run with the frozen Commons 0.1.1 artifact and no
version substitution:

- focused Commons contribution and module-registration GameTests;
- full standard and Serene Seasons GameTest suites;
- warnings-enabled clean build and the release/resource audit;
- disposable missing, old, development, wrong-ID, and wrong-version dependency
  rejections;
- dedicated-server and connected-client acceptance for Vintner plus Commons;
- combined Commons, migrated Tilth, and Vintner knowledge regression;
- immediate pre-migration versus final JAR comparison and final JAR inspection.

The final acceptance report records exact counts, runtime observations,
warnings, package hashes, and any environmental limitation. The current
contribution remains four categories and nine entries in its established order,
including the optional Vineyard Sites relation. No article or category resource
changed during this migration.

## Ownership and next consumer

Vintner retains grapes, cultivars, vines, vineyards, plots, estates, machines,
wine production and scoring, vintages, provenance, persistence, networking,
Almanac, Vintage Archives, and Estate Management Desk behavior. Standard and
Serene Seasons suites provide the gameplay regression gate; this phase adds no
gameplay behavior.

Tilth's accepted Phase 10D3A migration remains separate and unchanged. Reeve is
still pending its own Commons 0.1.1 migration in Phase 10D3C. This phase does
not claim three-consumer ecosystem acceptance, publication, tagging, upload, or
release approval.
