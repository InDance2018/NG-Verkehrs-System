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

Stand: **15. Oktober 2026 · Entwicklungsfassung 0.1.0-dev8**

- Grundsystem mit **37 registrierten Bauteilen**.
- Erste Straßenblöcke mit weißen und gelben Linien, Richtungspfeilen und Fußgängerüberweg.
- Gehwegplatten, Bordsteinplatte, Schildermast und verbindbare Leitplanke.
- 14 Schildtafeln einschließlich einer unbeschrifteten Tafel; darunter erste Vorfahrt-, Park- und Geschwindigkeitszeichen.
- Fahrzeugampel mit vier Signalzuständen sowie ein Ampelsteuergerät.
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

**Neu in dev8 – Clienttest ausstehend:** Getrennte Fußgängerüberwege A/B sind eingebaut. Mit dem Verknüpfer werden Fußgängerampeln und zugehörige Taster per normalem Rechtsklick A oder per Schleichen + Rechtsklick B zugeordnet. Jeder Taster fordert nur den eigenen Überweg an. Beide Anforderungen werden in Eingangsreihenfolge nacheinander bedient; dazwischen bleiben beide Überwege Rot. Während jeder Fußgängerfreigabe bleiben beide Fahrzeuggruppen Rot. Nach höchstens zwei unterschiedlichen Überwegen wird die nächste Fahrzeuggruppe bedient; weitere Wünsche bleiben für den folgenden Durchlauf gespeichert.

Sechs automatisierte Servertests und zwei Phasentests über je 100.000 Ticks sind bestanden. Die neue A/B-Bedienung muss noch im Client getestet werden. **Bestehende dev7-Verbindungen gehören zunächst zu Überweg A:** Ampeln und Taster des zweiten Überwegs einmal schleichend neu verbinden. Parallele Fahrzeugfreigaben sind ein späterer Ausbau.

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
