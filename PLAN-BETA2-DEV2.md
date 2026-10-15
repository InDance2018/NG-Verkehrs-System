# Ankündigung und Ergebnis: 0.1.0-beta2-dev2

**Stand: 15. Oktober 2026 · Status: umgesetzt als private Testfassung; Clienttest offen**

Auf Nutzerwunsch werden die folgenden sechs Aufgaben in **einer gemeinsamen privaten Dev-Fassung** gebündelt. Sie ersetzen die zuvor vorgeschlagene Aufteilung auf dev2, dev3 und dev4.

## Geplanter Umfang

1. **Schilderbrücken und Befestigungen:** Brückenstützen, Querträger und Aufhängungen für Autobahntafeln.
2. **Wegweisung erweitern:** beschriftbare Ausfahrtsschilder, Entfernungstafeln und Umleitungsschilder.
3. **Digitale Verkehrsanzeigen:** einstellbare Tempolimits, Stauwarnungen, rote Sperrkreuze und grüne Fahrstreifenpfeile.
4. **Gemeinsame Anzeigensteuerung:** mehrere Anzeigen verknüpfen und gemeinsam umschalten; Einstellungen und Verbindungen speichern und nach Neustarts wiederherstellen.
5. **Parkleitsystem:** beschriftbare Parkplatz-/Parkhauswegweiser mit Kapazität, freien Plätzen und Frei-/Besetztanzeige.
6. **Automatische Ein- und Ausfahrtszählung:** Erfassungspunkte, Zuordnung zum Parkplatz, Aktualisierung verbundener Anzeigen, manuelle Korrektur und dauerhafte Speicherung.

## Festgelegter Erkennungsumfang

**Fahrzeugerkennung:** Zunächst Boote/Loren, von Spielern besetzte MISC-Fahrzeugentitäten und per `ng_verkehr:parking_vehicles` erweiterbare Fahrzeugtypen. Konkrete Fremdmods sind noch nicht geprüft. [Implementierung und Grenzen](DEV-BETA2-2.md).

Mögliche Korrekturen aus dem noch offenen Client-Spieltest von **0.1.0-beta2-dev1** werden beim weiteren Ausbau berücksichtigt.

## Entwicklungs- und Veröffentlichungsstatus

- Die sechs Aufgaben wurden nach der Ankündigung und Nutzerfreigabe gemeinsam umgesetzt. Abschließender Reobf-Build erfolgreich; 37/37 Forge-Servertests bestanden.
- Speicherung, Synchronisierung, Zählung und Bearbeitungsrechte wurden automatisiert geprüft. Sicht-/Bedienprüfung im Client, reale Mehrspieler-/Neustarttests der neuen Systeme und konkrete Fahrzeugmods bleiben offen.
- [Vollständiger Prüfstand und Testanleitung](DEV-BETA2-2.md).
- Die öffentliche Downloadfassung bleibt **[0.1.0-beta1](https://github.com/InDance2018/NG-Verkehrs-System/releases/tag/v0.1.0-beta1)**.
- Dev-Fassungen werden während der Entwicklung nicht auf MC02 geladen.
- Ein Fertigstellungstermin ist nicht zugesagt.
