# NG-Verkehr · Beta 2 dev10

Bauauftrag vom 17. Oktober 2026. Ausgangsstand: beta2-dev9. Status: als private dev10-Testfassung gebaut. 156/156 Servertests und zehn Ablaufprüfungen bestanden. Zwei echte Clients haben jeweils alle 20 Bedienoberflächen samt synchronisierter Einstellung geprüft. Echter Neustart und tatsächliche Chunk-Entladung/Rückkehr sind bestätigt.

## 20 Verkehrsbauten

1. E-Ladesäule mit Anschlussbelegung und Sitzungsanzeige.
2. Ladepark-Steuerung mit Übersicht der verbundenen Säulen.
3. Tanksäule mit Fahrzeugzuordnung und Mengenanforderung.
4. Tankstellen-Kasse mit Übersicht der Gerätevorgänge.
5. Waschanlagen-Einfahrt mit Fahrzeugprüfung und Freigabe.
6. Animierter Waschtunnel mit Programm und Ablaufstatus.
7. Versenkbarer Straßenpoller mit Hindernisschutz.
8. Zufahrtskarten-Leser mit widerrufbaren Berechtigungen.
9. Kennzeichen-Erkennung für berechtigte Zufahrten.
10. Lieferzonen-Steuerung mit Zeitfenster.
11. Haltestellenanzeige für Buslinie und nächste Ankunft.
12. Buslinien-Verwaltung mit Linie, Ziel und Takt.
13. Bus-Ankunftssensor.
14. Zeitgesteuerte Schulweg-Warnung.
15. Baustellen-Ausfahrtswarnung bei Fahrzeugerkennung.
16. Wetterabhängige Warnanlage.
17. Sicht-/Nebelsteuerung mit manuellem Sichtalarm und Wetterreaktion.
18. Tunnel-Notruf mit quittierbarem Alarm.
19. Dynamische Fluchtweg-Anzeige.
20. Verkehrsleitstelle mit Geräteübersicht und Sammelalarm.

## Umsetzung und Grenzen

Geräte, Bedienung, Speicherung, Rechte und Verbindungen werden in NG-Verkehr umgesetzt. Bestehende Fahrzeugzustände werden über die gemeinsame Abstimmung wiederverwendet. Fehlende externe Lade-, Kraftstoff-, Wasch- oder Zahlungsanbieter werden sichtbar als fehlend gemeldet; ohne bestätigte Gegenstelle keine Fahrzeugänderung oder kostenpflichtige Leistung. Starterbatterie wird nicht als Elektroantrieb behandelt. Nebel wird nicht als universell auslesbarer Minecraft-Messwert behauptet: Sichtalarm ist konfigurierbar.

Neue Wirtschaftsfunktionen bleiben im privaten Schnittstellen-Repository vorgemerkt. Dieser Bauauftrag betrifft NG-Verkehr und installiert keine neue Version auf MC02. Öffentliche Downloads bleiben bis zu einem gesonderten Veröffentlichungsauftrag unverändert.

## Prüfung

Gezielte Server-Funktionstests und Regressionstests, Ressourcenprüfung und Client-Bedienung. Tatsächliche Ergebnisse und offene Gegenstellen werden vor Lieferung nachgetragen.

## Ergebnis und verbleibende Anbindungen

Die Verkehrsseite aller 20 Geräte ist gebaut und geprüft. Der Waschablauf wurde mit einem echten Spieler und einem ausschließlich für Tests verwendeten Anbieter vollständig sowie mit Abbruch geprüft. Die Funktionsklassen und Ressourcen der Liefer-JAR entsprechen der Client-Testfassung; Testklassen sind aus der Lieferung entfernt.

Echte NG-Fahrzeuge-Gegenstellen für Kennzeichen, Kraftstoff, Reinigung und Elektroantrieb sowie der Wirtschaftsanbieter bleiben offen. Kostenpflichtiges Laden/Tanken/Waschen bleibt gesperrt. Die Ladeparksteuerung bietet Geräte-/Sitzungsübersicht; eine gemeinsame Leistungsbudget-Verteilung ist nicht enthalten. Busanzeigen zeigen Planzeiten und lokale Bus-Erkennung, keine Fahrzeugpositions-Prognose. Warnanlagen geben Hinweise und Redstone aus; sie erzwingen keine Fahrzeugbremsung.

MC02 und öffentliche Downloads wurden nicht geändert. Private JAR: `ng_verkehr-0.1.0-beta2-dev10.jar`, SHA-256: `7cc480dd73854a3ec0b6366ec69e8a07de7b8fb308b1fc3bded403d849161962`.
