# Praxistest und Dokumentationsabschluss · 17. Oktober 2026

## Ergebnis: bestanden

**Praxistest vom 17. Oktober 2026 bestanden – vom Projektinhaber bestätigt:** Alle bisherigen DEV-Stände beider Projekte wurden auf MC02 getestet. Zusätzlich bestanden auf einem externen Minecraft-1.20.2-Server der Test mit drei Spielern, 15 Minuten Chunk-Laden/-Entladen und vier Serverneustarts nach dem Aufbau. Nach den Neustarts waren alle Aufbauten vorhanden und funktionsfähig.

Die Bestätigung stammt von InDance2018 aus den Rückmeldungen vom 17. Oktober 2026, zuletzt um 20:04 Uhr (Europe/Berlin). Dies dokumentiert den vom Projektinhaber durchgeführten Spieltest.

| Prüfung | Umgebung / Umfang | Ergebnis |
|---|---|---|
| Bisherige DEV-Stände und beschriebene Funktionen beider Projekte | MC02; vom Projektinhaber ausdrücklich bestätigt | Bestanden |
| Mehrspielerbetrieb | Externer Minecraft-1.20.2-Server mit drei Spielern | Bestanden |
| Chunk-Laden und -Entladen | 15 Minuten, von drei Spielern durchgeführt | Bestanden |
| Speicherung und Wiederherstellung | Vier Serverneustarts nach dem Aufbau | Bestanden |
| Aufbauten nach dem Neustart | Alles weiterhin vorhanden und funktionsfähig | Bestanden |

Damit sind die bisher als offen geführten allgemeinen Spiel-, Mehrspieler-, Chunk- und Neustartprüfungen im gemeldeten Prüfumfang bestätigt. Historische Berichte beschreiben weiterhin ihren damaligen Stand; diese spätere Bestätigung ergänzt sie.

## Aktuell dokumentierte Fassungen

| Projekt | Aktueller MC02-Stand | SHA-256 der installierten Lieferdatei |
|---|---|---|
| NG-Verkehr | 0.1.0-beta2-dev12 | `d0ec90f17fbbbdddce741e69875a6be325cd4665cc408c6dc960c2535880fd17` |
| NG-Fahrzeuge | 0.1.0-dev12 | `d9eca5c61e640a6d649cf37af822a62280baa9182d8491b73c9f47c95b62ded2` |

Der [MC02-Installationsnachweis](MC02-2026-10-17.md) enthält Sicherung, Installation, Modliste, Freigabe und Laufzeitprüfung. Für den externen Server wurde Minecraft 1.20.2 genannt; eine separate JAR-Prüfsumme oder genaue Serverdistribution wurde nicht übermittelt.

## Bereits durchgeführte technische Prüfungen

- NG-Fahrzeuge dev12 aus dem öffentlichen Quellstand neu gebaut: 129/129 Forge-Servertests bestanden.
- NG-Verkehr beta2-dev12 mit NG-Fahrzeuge dev12: 16 gezielte Integrationstests bestanden, einschließlich Zustandszugriff auf alle elf Fahrzeugtypen.
- Separate Schreib-/Lese-Serverprozesse, tatsächliche Chunk-Entladung/-Rückkehr sowie dauerhafter Wiederholschutz für Tank-/Waschleistungen geprüft.
- Die Produktionsdateien wurden bei der MC02-Installation bytegenau zurückgelesen; beide Versionen sind in der Laufzeitinventur geladen.

Diese technischen Nachweise und der oben bestätigte Praxistest sind unterschiedliche, sich ergänzende Prüfungen. Historische Build-Prüfsummen bleiben unverändert erhalten.

## Neue Webkataloge und vier PDF-Handbücher

| Projekt | Webkatalog | Einträge | Kategorien | Anleitungen | PDF DE / EN |
|---|---|---:|---:|---:|---|
| NG-Verkehr | https://nevergate.de/ng-verkehr/ | 350 | 14 | 29 | 73 / 70 Seiten |
| NG-Fahrzeuge | https://nevergate.de/ng-fahrzeuge/ | 39 | 5 | 16 | 15 / 15 Seiten |

Der abgebrochene Katalogauftrag wurde wiederhergestellt und mit den tatsächlichen dev12-Modell-, Sprach- und Rezeptdateien abgeglichen. Der vorherige öffentliche Verkehrskatalog mit 98 Einträgen wurde ersetzt; der Fahrzeugkatalog wurde neu im gleichen Stil veröffentlicht. Alle Einträge besitzen Modellbilder, deutsche und englische Beschreibungen, Bezugs-/Rezeptangaben und Verweise auf passende Anleitungen.

Beide Kataloge sind auf der NeverGate-Startseite und untereinander verlinkt. Die vier PDFs wurden vollständig gerendert und visuell geprüft; 173 Seiten insgesamt, ohne abgeschnittene Texte an den Seitenrändern. Vor der Veröffentlichung wurden 403 Dateien per SHA-256 geprüft und die bisherige Verkehrsseite gesichert. Öffentliche Seiten, Daten, Skripte, Gestaltung und alle vier PDF-Downloads wurden nach der Veröffentlichung mit HTTP 200 und passenden Prüfsummen bestätigt. Suche, Detailansichten, Sprachwechsel und Zurücksetzen wurden im Browser geprüft.

Die Katalog- und PDF-Ausgabe wurde vor der späteren Praxistestbestätigung veröffentlicht. Der aktuelle Teststatus wird durch diesen Nachtrag ergänzt. Die [Katalogablage](docs/catalogue/README.md) enthält die Original-PDFs, Katalogdaten, Webdateien als ZIP und Prüfsummen.

## Unveränderter Entwicklungsumfang

Die Fassungen bleiben Entwicklungsfassungen. Eine neue stabile Version oder öffentliche Mod-JAR-Veröffentlichung wird mit diesem Dokumentationsabschluss nicht erklärt. Euro-Zahlungen benötigen weiterhin das NG-Wirtschaftssystem; ein Elektroantrieb ist weiterhin nicht enthalten.
