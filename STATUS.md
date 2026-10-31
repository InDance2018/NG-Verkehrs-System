# NG-Verkehr · aktueller Stand

Stand: **31.10.2026** · Minecraft Java 1.20.2 · Forge 48.1.0 · Java 17.

| Bereich | Stand |
|---|---|
| Öffentlicher Download | [0.1.0-beta1](https://github.com/InDance2018/NG-Verkehrs-System/releases/tag/v0.1.0-beta1), erster öffentlicher Beta-Release |
| Vollständig dokumentierter Entwicklungsstand | 0.1.0-beta2-dev12 vom 17.10.2026 |
| Neuerer erfasster Testbuild | 0.1.0-beta2-montage2; kein zusätzlicher öffentlicher Release und kein vollständiger neuer Quell-/Client-/Testnachweis im Repository |
| Fahrzeuganbindung | beta2-dev12 mit Fahrzeuge dev12: 16 gezielte gemeinsame Servertests |
| Gemeinsame City-Wirtschaft | Weiterhin offen; keine bestätigte neue Zahlungs-/Leistungsgegenstelle |

## Vorhandene Funktionen

Straßen, Markierungen, 181 feste Schildvarianten, Ampelsteuerung, Baustellen- und Verkehrsbetriebsgeräte, Parkplätze/-zonen, Sensoren, Schranken, Höhenkontrollen, Tickets/Tarife, Service- und Busgeräte. Die vorhandene Oberfläche und Bedienung werden in den datierten Entwicklungsberichten beschrieben.

## Nachweise und Grenzen

- [beta2-dev12 und Integration](DEV-BETA2-12.md): 16 gezielte Tests mit Fahrzeuge dev12, getrennte Serverprozesse für Fahrzeug-/Belegpersistenz, tatsächliche Chunk-Rückkehr und Wiederholschutz.
- [Praxistest vom 17.10.](PRAXISTEST-2026-10-17.md): vom Eigentümer bestätigt, drei Spieler, 15 Minuten Chunk-Laden/-Entladen, vier Neustarts. Dieser Nachweis gilt für die damals getesteten Builds und wird nicht auf montage2 übertragen.
- montage2 wurde als neuerer Testbuild erfasst. Zugehörige vollständige Änderungsbeschreibung, Quell-/Artefaktzuordnung, passende Clientfassung und eigenständige Abnahme sind nachzutragen. Aus dem Versionsnamen werden keine Funktionen abgeleitet.
- NG-Verkehr bleibt ohne NG-City und NG-Fahrzeuge eigenständig. Elektro-Ladung und kostenpflichtige externe Services brauchen passende Gegenstellen.
- NG-City dev-0.2.10 besitzt eine dokumentierte Eigentümerabnahme im Einzel- und Mehrspieler vom 21.10.; das bestätigt noch keine gemeinsame Mod-Wirtschaft.

## Weiterführend

[Entwicklungsplan](ROADMAP.md) · [Änderungen](CHANGELOG.md) · [Webhandbuch](https://nevergate.de/ng-verkehr/) · [Katalogarchiv](docs/catalogue/README.md) · [Planung der Modfamilie](PLANUNG-2026-10-21.md).

## Offen

Neue Testbuilds vollständig nachführen; Sattelzug-/Parkgeometrien gemeinsam erproben; City-Zahlungsadapter und Elektro-Gegenstelle prüfen; vollständige Zeichenabdeckung und stabile Gesamtveröffentlichung bleiben offen. Der weitere feste Schilderausbau bleibt nach letztem belegten Auftrag pausiert.

## Einordnung

Dokumentationsabgleich vom 31.10.2026. Versionsnachweise, berichtete Versuche, Nutzerabnahmen und öffentliche Releases werden getrennt geführt. Bestehende Tests werden mit ihrem ursprünglichen Datum übernommen; diese Aktualisierung führt keinen neuen Build oder Spieltest aus und veröffentlicht keine neue Binärdatei.
