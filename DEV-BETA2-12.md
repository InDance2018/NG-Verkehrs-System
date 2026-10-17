# NG-Verkehr · 0.1.0-beta2-dev12

**Testnachtrag vom 17.10.2026:** Der Projektinhaber bestätigt alle bisherigen DEV-Stände auf MC02 sowie den bestandenen Drei-Spieler-/Chunk-/Neustarttest. [Aktueller Testbericht](PRAXISTEST-2026-10-17.md). Ältere Testangaben im folgenden Dokument beschreiben den damaligen Prüfstand.


17. Oktober 2026 · Minecraft 1.20.2 · Forge 48.1.0 · Java 17.

Diese Wartungsfassung ergänzt die explizite Unterstützung von NG-Fahrzeuge 0.1.0-dev12. Sie enthält den Umfang von Verkehr dev11 und die 20 Betriebsgeräte aus dev10. Die unveränderliche Fahrzeugansicht wird für dev11 und dev12 verwendet; unbekannte Versionen bleiben gesperrt.

## Enthaltener Umfang

- Baustellen- und Verkehrsbetrieb aus dev7/dev8.
- Parktickets und Tarife aus dev9. Euro-Konten und Zahlungen bleiben Aufgabe des Wirtschaftssystems.
- 20 Betriebsgeräte: Lade-/Tankgeräte, Waschanlage, Poller, Karten-/Kennzeichenleser, Lieferzonen, Busanzeigen/-verwaltung/-sensor, Schulweg-/Baustellen-/Wetter-/Sichtwarnung, Tunnelnotruf, Fluchtweg und Leitstelle.
- Fahrer und Besitzer werden getrennt gelesen. Kennzeichenfreigaben bleiben an Fahrzeug- und Besitzer-UUID gebunden.
- Kostenloses Tanken und Waschen verwenden die vorhandenen Fahrzeugzustände und einen dauerhaften Wiederholschutz.
- Die elf Fahrzeugtypen von Fahrzeuge dev12 sind lesbar; Bus und Motorrad werden passend unterschieden.

## Prüfung dieser Wartungsfassung

16 gezielte Forge-Integrationstests mit dem aus GitHub-Commit c64dac83465389bfe71eac6473073e5476143c2b gebauten Fahrzeuge dev12: Fahrer/Besitzer, Kennzeichen, Tank/Reinigung, Berechtigungen, Buslinie, Versionssperre sowie Zustandszugriff bei allen elf Fahrzeugtypen. Zusätzlich bestanden: separate Schreib-/Lese-Serverprozesse, Wiederholung ohne zweite Tank-/Waschleistung, tatsächliche Chunk-Entladung und Rückkehr. MC02-Abschluss siehe Installationsnachweis.

Die früheren 171 Tests, zwei echten Clients und 20 Bedienansichten gehören zum historischen Verkehr-dev11/Fahrzeuge-dev11-Nachweis. Sie werden nicht als vollständiger Zwei-Client-Nachweis des neuen Versionspaars ausgegeben.

## Grenzen und Migration

Ungebundene Kennzeichenlisten älterer Verkehrsfassungen müssen mit dem tatsächlichen Fahrzeug neu registriert werden. Kopierte Kennzeichen übernehmen keine Zufahrt.

Elektroantrieb und Wirtschafts-Anbieter fehlen weiterhin; kostenpflichtige externe Dienstleistungen bleiben gesperrt. Die Starterbatterie ist kein Traktionsakku. Der Projektinhaber hat inzwischen alle bisherigen DEV-Stände auf MC02 sowie den Drei-Spieler-/Chunk-/Neustarttest als bestanden bestätigt; [Testnachtrag](PRAXISTEST-2026-10-17.md).

Beide Mods auf Client und Server ersetzen; ältere aktive JARs entfernen. Öffentliche Beta-1-Downloads bleiben separat. Diese Fassung ist eine Entwicklungs-/Testfassung.
