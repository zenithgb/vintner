# Vintner 1.4.0 automated validation

Candidate SHA-256:
`9149278b342a60f702f1069f11bdf37132069daf3a7b91ca4484cf45fb475ba6`

Source commit:
`71c35bd1f46f84b04ebed268eb5aa0d72f923439`

## Accepted evidence

| Gate | Result |
| --- | --- |
| Exact final-commit rebuild | PASS; byte-identical candidate |
| Standard GameTests | 197/197 PASS |
| Serene Seasons GameTests | 197/197 PASS |
| Clean build / warning-enabled compilation | PASS |
| Deterministic structure generation | PASS |
| Advancement audit | 218 total PASS |
| Release-resource audit | 2,025 JSON files, 196 recipes, 172 public wood-family blocks and 26 wood-preserving grapevine states PASS |
| Blockbench/model-family audit | 907 models PASS |
| Nursery and serving/contact audits | PASS |
| Exact dedicated server | Startup, pool injection, reload, save, clean stop and restart PASS |
| Natural Plains generation sample | 4/25 exact wineries; no duplicate winery in a structure start |
| Natural Winemaker behavior | Grape Press POI, villager claim and restart persistence PASS |
| Package inspection | Exact dependency metadata; Commons external; no GameTest entrypoint or temporary QA payload |

Durable evidence:

`/Users/zachariaheverson/Developer/Minecraft/vintner-release-evidence/1.4.0-plains-winery-spawn-investigation-20260924/`

Automated and exact-runtime evidence does not complete the three human sessions
listed in `ACCEPTANCE_STATE.md`.
