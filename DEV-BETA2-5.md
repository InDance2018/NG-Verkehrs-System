# 0.1.0-beta2-dev5 – Parkzonen abgesichert und Belegung sichtbar

Private Testfassung vom 16. Oktober 2026. Der Schilderausbau bleibt bei **181 festen Schildvarianten** pausiert.

## Änderungen

- Doppelzählungsschutz des Zonenrechners bleibt beim Laden gespeicherter Daten erhalten. Gleichgerichtete Buchungen desselben Fahrzeugs werden je Rechner innerhalb von 100 Ticks unterdrückt; Ein- und Ausfahrten bleiben getrennt möglich.
- Parkplatzanzeigen zeigen freie bzw. volle Plätze, belegte Plätze und Gesamtkapazität. Die Zonenübersicht zeigt vier Zonen pro Seite mit freien und belegten Plätzen; volle Zonen rot, fehlende Zonen OFFLINE.
- Die Gesamtsicht zeigt bei nicht erreichbaren Zonen OFFLINE statt einer scheinbar vollständigen Teilsumme. Die Menüdaten sind weiterhin ein Stand beim Öffnen.
- Aktuelle Buchungen werden in Weltdaten gespeichert; Fahrzeug-UUIDs dieser Buchungsliste werden nicht an die Anzeige-Clients übertragen.

## Prüfung

**Java-17-Build und Reobf erfolgreich; 52/52 Forge-Servertests und alle acht vorhandenen deterministischen Testprogramme bestanden.** JAR, Versionsmetadaten und neue deutsche/englische Übersetzungen geprüft.

Sechs neue Servertests prüfen volle/leere Zählergrenzen, gespeicherten Doppelzählungsschutz, abgelaufene und zukünftige Zeitstempel, benachbarte überlappende Sensoren, unabhängige Buchungen in getrennten Zonen, gespeicherte Rechner-/Anzeigenverbindungen sowie Fahrzeugfilter und Passagierausschluss.

**Offen:** Minecraft-Client-Sicht-/Bedienprüfung, echter Serverneustart, reale Chunk-Entladung, Mehrspielertest und Kompatibilität mit einer konkreten Fahrzeugmod. NBT-Ladetests simulieren das Wiederladen und ersetzen keinen echten Neustarttest. Im verfügbaren Teststand ist weiterhin keine konkrete Fahrzeugmod nachgewiesen.

## Kurzer Spieltest

1. Zwei Zonen mit 10 Plätzen/3 belegt und 5 Plätzen/5 belegt verbinden: Gesamt 7 frei, 8 belegt, 15 Plätze.
2. Benachbarte gleichgerichtete Sensoren derselben Zone mit demselben Fahrzeug überfahren: innerhalb von fünf Sekunden nur eine Buchung.
3. Volle und leere Zonen durch Ein-/Ausfahrt prüfen; Werte bleiben zwischen null und Kapazität.
4. Speichern, Server neu starten und Anzeigen, Zähler und Verbindungen prüfen.
5. Einen Zonenrechner entfernen: Gesamtsicht und Gesamttafel müssen OFFLINE melden.

Minecraft Java 1.20.2 / Forge 48.1.0 / Java 17. Clients und privater Testserver verwenden dieselbe Dev-Fassung. Keine Installation auf MC02 durch diesen Auftrag. Öffentliche Downloadfassung bleibt Beta 1; dieses Dokument veröffentlicht keine private Dev-JAR und keinen Quellcode.
