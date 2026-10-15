# NG-Verkehrs-System
## Deutsche Straßen und Verkehrstechnik für Minecraft

**NG-Verkehrs-System ist eine NeverGate-Eigenentwicklung für Minecraft Java 1.20.2 mit Forge.** Unser Ziel ist ein umfassendes deutsches Verkehrssystem: von Straßen und Verkehrszeichen über Ampeln bis zu digitalen Anzeigen und Parkleitsystemen.

Das Projekt befindet sich im Aufbau. Hier begleiten wir die Entwicklung von der Planung über erste Tests bis zur fertigen Mod.

> **Entwicklungsstand: frühe Testversion.** Der vollständige Funktionsumfang ist ein Entwicklungsziel. Die Mod ist noch keine fertige Veröffentlichung.

## Was entstehen soll

| Bereich | Geplanter Umfang |
|---|---|
| Verkehrszeichen | Alle deutschen Verkehrszeichen nach dem amtlichen Verkehrszeichenkatalog: Gefahrzeichen, Gebote, Verbote, Richtzeichen, Zusatzzeichen und Varianten |
| Wegweisung | Beschriftbare Ortsschilder, Straßen- und Autobahnwegweiser, Ausfahrten, Entfernungstafeln und Umleitungen |
| Straßenbau | Asphalt, Markierungen, Abbiegespuren, Gehwege, Bordsteine, Radwege und Verkehrsinseln |
| Straße und Autobahn | Deutsche Leitplanken mit Endstücken und Übergängen, Leitpfosten, Schutzwände, Schilderbrücken und Befestigungen |
| Baustellen | Baken, Absperrungen, Warnleuchten, mobile Ampeln und Richtungspfeiltafeln |
| Ampelsystem | Fahrzeug-, Fußgänger-, Fahrrad- und Richtungssignale, Anforderungstaster und einstellbare Kreuzungsschaltungen |
| Digitale Anzeigen | Wechselverkehrszeichen, Tempolimits, Stauwarnungen, Fahrstreifensperrungen und veränderbare Textanzeigen |
| Parkleitsystem | Parkplatz- und Parkhauswegweiser, Frei-/Besetztanzeigen, Restplatzanzeigen und Ein-/Ausfahrtszählung |

Die technischen Systeme sollen funktionieren: Ampeln schalten, Anzeigen lassen sich ändern und Parkanzeigen reagieren auf die erfasste Belegung. Ortsnamen, Ziele, Entfernungen und passende Zusatztexte sollen einstellbar sein.

**Eigener Code, eigene Modelle und eigene Texturen gehören zum Projektkonzept.** Die vollständige Zeichenabdeckung soll anhand des amtlichen Katalogs nachvollziehbar geprüft werden.

## Was bereits vorhanden ist

Stand: **15. Oktober 2026 · Entwicklungsfassung 0.1.0-dev15**

- Grundsystem mit **46 registrierten Bauteilen**.
- Erste Straßenblöcke mit weißen und gelben Linien, Richtungspfeilen und Fußgängerüberweg.
- Gehwegplatten, Bordsteinplatte, Schildermast und verbindbare Leitplanke.
- 14 Schildtafeln einschließlich einer unbeschrifteten Tafel; darunter erste Vorfahrt-, Park- und Geschwindigkeitszeichen.
- Fahrzeugampel mit vier Signalzuständen sowie ein Ampelsteuergerät.
- Links-/Rechts-/Geradeaus-Pfeilampeln, alle Kombinationen dieser Pfeile, Fahrradampel und gelbes Warnblinklicht (dev11/dev12).
- Kompaktes Steuergerät-Menü mit kleinerer Schrift, Gerätetabelle und Scrollen statt Seitenwechsel (dev13).
- Automatische Erkennung aller Ampeltypen und Taster mit Mittelpunkt und Radius 16/24/32/48 Blöcke (dev15).
- Ampel-Verknüpfer: Steuergerät und Ampel per Rechtsklick verbinden, zweite Fahrtrichtung mit Schleichen + Rechtsklick.
- Start/Stopp direkt am Steuergerät mit leerer Hand.
- Fußgängerampel mit rotem stehendem/grünem gehendem Symbol und gelber Anforderungstaster mit Warteanzeige (dev7).
- Beide neuen Geräte lassen sich mit dem vorhandenen Verknüpfer an dasselbe Steuergerät anschließen.
- Grundfunktionen für digitale Textanzeige und Parkplatzanzeige mit speicherbaren Werten.
- Überarbeitete Inventarsymbole für Schilder, Leitplanke, Ampel und Anzeigen.
- Alle 14 Schildtafeln mit passenden Konturen: Kreise, Dreiecke, Achteck, Raute und rechteckige Tafeln; ohne graue quadratische Außenflächen und mit geformten Metallrückseiten und schmalen Kanten.

### Was davon geprüft wurde

Die erste Entwicklungsfassung bestand drei automatisierte Servertests: sicheres Rot bei deaktivierter oder fehlender Steuerung, Speicherung der Parkplatzwerte sowie Speicherung und Längenbegrenzung der Anzeigetexte.

In Einzelspielertests wurden Straßenmarkierungen, erste Schilder, Fahrzeugampel, Anzeigen und verbundene Leitplanken sichtbar dargestellt. Das bestätigt die Darstellung dieser Teststände, noch keinen vollständigen Funktionstest aller Systeme.

**Sichtprüfung bestätigt:** Die neuen Leitplanken- und Ampelformen aus dev4 sind im Einzelspieler sichtbar dargestellt. In dev5 zeigen die getesteten Schilder passende Konturen ohne graue Außenflächen; dies wurde anhand eines Testbildes geprüft und vom Tester bestätigt. Build und Ressourcen-/Modellprüfungen sind bestanden. Die Bestätigung betrifft die gezeigten Ansichten, nicht sämtliche Schildrückseiten, Ausrichtungen oder Ampelphasen.

**Funktionstest dev6 bestätigt:** Der Tester meldet eine saubere deutsche Farbfolge, erfolgreiches Verknüpfen per Klick und problemloses Starten/Stoppen. Zwei Ampeln der Gruppen A und B schalten laut Test abwechselnd über ein gemeinsames Steuergerät. Vier automatisierte Servertests sind bestanden.

**Fußgängertest dev7 im Client bestätigt:** Rechtsklick auf den verbundenen Anforderungstaster speichert den Überquerungswunsch. Nach Ende der laufenden Fahrzeugphase folgen mindestens 2 Sekunden Allrot, 6 Sekunden Fußgängergrün und 4 Sekunden Räumzeit; danach startet die nächste Fahrzeuggruppe. Alle Fußgängersignale desselben Steuergeräts teilen sich zunächst diese gemeinsame Phase. Fünf automatisierte Servertests sowie ein Phasentest über 100.000 Ticks sind bestanden. Der Tester bestätigt den Ablauf im Einzelspieler: Fahrzeugampeln Rot → Fußgänger Grün → Fußgänger Rot → Fahrzeugverkehr wieder freigegeben. Diese Bestätigung betrifft die getestete gemeinsame Fußgängerphase; weitere Ansichten und Sonderfälle bleiben offen.

**Getrennte Überwege seit dev8 – Clienttest bestätigt:** Getrennte Fußgängerüberwege A/B sind eingebaut. Mit dem Verknüpfer werden Fußgängerampeln und zugehörige Taster per normalem Rechtsklick A oder per Schleichen + Rechtsklick B zugeordnet. Jeder Taster fordert nur den eigenen Überweg an. Beide Anforderungen werden in Eingangsreihenfolge nacheinander bedient; dazwischen bleiben beide Überwege Rot. Während jeder Fußgängerfreigabe bleiben beide Fahrzeuggruppen Rot. Nach höchstens zwei unterschiedlichen Überwegen wird die nächste Fahrzeuggruppe bedient; weitere Wünsche bleiben für den folgenden Durchlauf gespeichert.

Sechs automatisierte Servertests und zwei Phasentests über je 100.000 Ticks sind bestanden. Der Tester bestätigt getrennt reagierende Taster sowie den Wechsel von Fahrzeugrot zu Fußgängergrün. **Bestehende dev7-Verbindungen gehören zunächst zu Überweg A:** Ampeln und Taster des zweiten Überwegs einmal schleichend neu verbinden. Parallele Fahrzeugfreigaben sind ein späterer Ausbau.

**Mastmontage seit dev9 – vom Tester bestätigt:** Der kleinere Taster lässt sich direkt seitlich an einen NG-Schildermast setzen. Dazu mit dem Taster-Blockitem auf die gewünschte Mastseite rechtsklicken; anschließend wie bisher mit dem Verknüpfer A/B zuordnen. Mast und Taster sitzen ohne Abstand in derselben Blockposition. Beim normalen Abbauen werden beide Gegenstände zurückgegeben. Wandmontage bleibt möglich. Sieben automatisierte Servertests sind bestanden.

**Bedienmenü dev10 – Clienttest vom Tester bestätigt:** Leere Hand + Rechtsklick am Steuergerät öffnet ein Bedienmenü mit Geräteliste, Status, Start/Stopp, Zufahrten Nord/Ost/Süd/West, Fahrtrichtung und Fußgängerüberweg A/B. Schleichen + Rechtsklick bleibt direkter Start/Stopp-Zugriff. Zuordnungen speichern sofort und stoppen die Steuerung; anschließend bewusst neu starten. Neue Geräte erscheinen nach erneutem Öffnen.

Geradeaus nutzt das bisherige Grundprogramm Nord/Süd = A und Ost/West = B. In dev10 waren Abbiegezuordnungen zunächst vorbereitet. Seit dev11 erhalten Linksabbieger eigene geschützte Phasen; seit dev12 gilt dies auch für Rechtsabbieger und Kombipfeile. Acht automatisierte Servertests sind bestanden. Der Tester bestätigt im Minecraft-Client: Menüanzeige, Geräteauswahl, Start/Stopp sowie Wechsel eines Tasters von Überweg A auf B. Die Änderung stoppt die Steuerung; nach dem Neustart fordert der Taster den zugewiesenen Überweg an. Die vorbereiteten Fahrzeug-Zufahrten und Abbiegezuordnungen sind damit noch nicht vollständig im Client geprüft.

**Linksabbieger dev11 – Clienttest bestätigt:** Der Tester bestätigt, dass die Pfeilampel schaltet und alle anderen Ampeln dabei Rot bleiben.

**Weitere Ampeltypen dev12:** Rechts-, Geradeaus- und Kombipfeile (Geradeaus/Links, Geradeaus/Rechts, Links/Rechts, alle drei Richtungen), Fahrradampel und gelbes Warnblinklicht. Fahrzeug-/Fahrrad-Geradeaus laufen mit A/B; Abbieger und Kombipfeile erhalten eigene geschützte Phasen. Je Sonderphase 2 Sekunden Rot-Vorlauf, 1 Sekunde Rot-Gelb, 10 Sekunden Grün, 3 Sekunden Gelb, 2 Sekunden Räumzeit. Alle übrigen regelnden Signale Rot. Warnlicht blinkt unabhängig davon. Elf Servertests und ein Test aller 24 geschützten Zufahrt-/Routenkombinationen über 200.000 Ticks bestanden. Der Tester bestätigt die leuchtende Darstellung der neuen Modelle.

**Kompaktes Menü dev13 – vom Tester für den normalen Betrieb akzeptiert:** Schrift auf 75 Prozent verkleinert, schmale Gerätezeilen, kleine Schaltflächen, feste Statusspalte und scrollbare Tabelle statt fünf Geräten pro Seite. Direkte N/O/S/W- und Überweg-A/B-Tasten. Zwölf Servertests einschließlich Auswahl aller 256 Geräteplätze über echte Paketserialisierung bestanden.

**Automatische Erkennung dev14/dev15 – im Einzelspieleraufbau bestätigt:** Steuergerät auswählen, erstes Gerät einmal verbinden und mit dem Verknüpfer per Schleichen/Rechtsklick einen Bodenblock als Kreuzungsmitte festlegen. Radius direkt zwischen 16, 24, 32 und 48 Blöcken wählen; Standard 32. Die Automatik prüft etwa alle zwei Sekunden geladene Weltbereiche, ohne zusätzliche Chunks zu laden. Fahrzeug-/Pfeil-/Fahrradampeln werden nach Position und Ausrichtung zugeordnet. Gegenüberliegende Zufahrten gehören zur selben Fahrzeuggruppe; Querverkehr zur anderen.

Seit dev15 werden auch Fußgängerampeln, Taster und Warnblinklichter automatisch verbunden. Fußgängergruppen folgen der N/S- bzw. O/W-Linsenachse. Taster übernehmen die Gruppe der nahen Fußgängerampel desselben Steuergeräts; ohne nahe Lampe bzw. bei Gleichstand entscheidet die eigene Ausrichtungsachse. Manuelle Korrekturen und fremde Verbindungen bleiben erhalten. Neue/geänderte Zuordnungen stoppen auf Rot; nach Prüfung bewusst starten. Bei überlappenden Automatikbereichen bleiben neue uneindeutige Funde offen.

**Sechzehn automatisierte Servertests bestanden.** Der Tester meldet nach dem Aufbau mit neuem Steuergerät: Erkennung, Zuordnung und Schaltablauf laufen im getesteten Einzelspieleraufbau. Das bestätigt diesen Aufbau, keine pauschale Freigabe sämtlicher Sondergeometrien, Chunkwechsel oder Mehrspielerfälle.

### Als Nächstes angekündigt: Verbindungen lösen und zurücksetzen

Nach bestätigter dev15 und erteilter Freigabe geplant:
- Ausgewählte Ampel oder Taster einzeln trennen.
- Alle Verbindungen eines Steuergeräts nach kurzer Bestätigung lösen.
- Automatische Wiederverbindung nach bewusstem Trennen verhindern.
- Getrennte regelnde Signale Rot; Taster und Warnblinklichter aus.
- Gespeicherte Verbindungen auch bei derzeit ungeladenen Geräten zuverlässig entwerten, ohne Weltbereiche zwangsweise zu laden.

Dieser Ausbau ist angekündigt und freigegeben; Build und Clienttest stehen noch aus. Es gibt weiterhin keine öffentliche GitHub-Test-JAR.

Weitere Ansichten, Neustart-/Mehrspielertests und der vollständige Ausbau bleiben offen.

## Technik und Testversionen

| Voraussetzung | Aktueller Entwicklungsstand |
|---|---|
| Spiel | Minecraft Java 1.20.2 |
| Modloader | Forge 48.1.0 als bisherige Testbasis |
| Java | 17 |
| Mod-ID | `ng_verkehr` |

Aktuell finden die Spieltests im Einzelspieler statt. Testfassungen können sich noch ändern. Vor dem Austausch einer Test-JAR Minecraft schließen und die vorherige Fassung entfernen, damit nicht zwei Versionen gleichzeitig geladen werden.

Dieses Repository enthält zunächst die **Projektankündigung und Entwicklungsdokumentation**. Öffentliche JAR-Downloads und Quellcode sind hier derzeit noch nicht bereitgestellt. Ein Download wird erst angekündigt, wenn er tatsächlich veröffentlicht wurde.

## Entwicklung verfolgen und Fehler melden

- [Entwicklungsplan](ROADMAP.md)
- [Versionsübersicht](CHANGELOG.md)
- [Fehler und Vorschläge](https://github.com/InDance2018/NG-Verkehrs-System/issues)

Bei Fehlern bitte die Mod-Version, Minecraft-/Forge-Version, eine kurze Beschreibung und die Schritte zum Nachstellen angeben. Ein Screenshot oder der relevante Log-Ausschnitt hilft bei der Prüfung.

Ein Veröffentlichungstermin für die fertige Mod steht noch nicht fest. Neue Entwicklungsstände werden hier dokumentiert.

---
**Ein Projekt von NeverGate · GitHub: [InDance2018](https://github.com/InDance2018)**
