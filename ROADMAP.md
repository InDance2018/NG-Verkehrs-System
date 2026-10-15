# Entwicklungsplan

**NG-Verkehrs-System · Stand: 15. Oktober 2026**

Der vollständige gewünschte Umfang bleibt das Entwicklungsziel. Die Reihenfolge kann sich durch Tests und technische Abhängigkeiten ändern. Eine Testfassung bedeutet noch keine abgeschlossene Entwicklungsstufe.

## 1. Leitplanke und Fahrzeugampel – neue Formen sichtbar bestätigt

- [x] Erste Leitplanke mit Verbindungen zwischen benachbarten Blöcken erstellt.
- [x] Neues Leitplankenmodell mit abgestuftem Wellenprofil und Stahlpfosten in dev4 gebaut.
- [x] Neues Fahrzeugampelmodell mit dunklem Gehäuse, drei runden Signalflächen, Sonnenblenden und Halterungen in dev4 gebaut.
- [x] Build, Ressourcenverweise, vier modellierte Signalzustände und symmetrische Leitplankenanschlüsse geprüft.
- [x] Neue Leitplanken- und Ampelform in einer Einzelspieleransicht sichtbar bestätigt.
- [x] Deutsche Farbfolge sowie Verknüpfen und Start/Stopp im Client vom Tester bestätigt (dev6).
- [x] Zwei Ampeln an einem Steuergerät mit abwechselnden Gruppen A/B im Client vom Tester bestätigt.
- [ ] Weitere Ausrichtungen und Neustart-/Mehrspielerverhalten prüfen.
- [ ] Weitere im Test festgestellte Fehler beheben.

**Die neuen Bauformen sind in den gezeigten Ansichten bestätigt. Umfassende Funktions- und Mehransichtenprüfungen bleiben offen.**

## 2. Grundsystem und Bedienung ausbauen

- [x] Grundsystem mit 35 Bauteilen und ersten Rezepten aufgebaut.
- [x] Deutsche Bezeichnungen und Inventarsymbole für die bisherigen Testbauteile angelegt.
- [x] Erste automatische Tests für Ampelausfall und gespeicherte Anzeigen bestanden.
- [ ] Alle bisherigen Bauteile im Client auf Darstellung, Platzierung und Bedienung prüfen.
- [x] Alle 14 Schildtafeln ohne graue quadratische Außenflächen modelliert (dev5), einschließlich geformter Metallrückseiten und schmaler Kanten.
- [x] Sichtprüfung der gezeigten Schildformen im Client vom Tester bestätigt.
- [ ] Sämtliche Schildvarianten, Rückseiten und Ausrichtungen im Spiel prüfen.
- [ ] Anzeigenlesbarkeit in verschiedenen Richtungen und Entfernungen prüfen.
- [ ] Bedienung und Einstellungen verständlich dokumentieren.

## 3. Deutsche Verkehrszeichen vollständig erfassen und umsetzen

- [ ] Amtlichen Verkehrszeichenkatalog einschließlich Varianten als Abdeckungsnachweis abgleichen.
- [ ] Gefahrzeichen vollständig umsetzen.
- [ ] Gebots- und Verbotszeichen vollständig umsetzen.
- [ ] Richtzeichen vollständig umsetzen.
- [ ] Zusatzzeichen vollständig umsetzen.
- [ ] Markierungen und Verkehrseinrichtungen ergänzen.
- [ ] Einstellbare Beschriftungen für geeignete Zeichen ergänzen.
- [ ] Jede umgesetzte Katalogposition mit Modell, Textur und Teststatus dokumentieren.

Erste Zeichen existieren bereits. Die vollständige Katalogabdeckung ist noch offen.

## 4. Straßen, Autobahn und Wegweisung

- [ ] Weitere Straßenbeläge, Markierungen und Abbiegespuren ergänzen.
- [ ] Gehwege, Bordsteine, Radwege und Verkehrsinseln ausbauen.
- [ ] Leitplanken-Endstücke, Übergänge und weitere Bauformen ergänzen.
- [ ] Leitpfosten, Schutzwände und Befestigungen ergänzen.
- [ ] Schilderbrücken und Autobahnwegweiser ergänzen.
- [ ] Ortsschilder, Ausfahrten, Entfernungstafeln und Umleitungen mit einstellbaren Zielen ergänzen.

## 5. Ampelsystem

- [x] Grundbedienung und Phasenwechsel mit zwei Fahrzeugampeln der Gruppen A/B im Client bestätigt.
- [ ] Erweiterte Funktions-, Neustart- und Mehrspielertests durchführen.
- [x] Fußgängerampel mit rotem stehendem und grünem gehendem Symbol eingebaut (dev7).
- [x] Gemeinsame Fußgängerphase im Client vom Tester bestätigt: Fahrzeuge Rot, Fußgänger Grün, Fußgänger Rot, Fahrzeuge wieder freigegeben.
- [x] Fahrrad-, Geradeaus-, Rechts- und sämtliche vereinbarten Kombipfeilsignale sowie Warnblinklicht ergänzt (dev12).
- [x] Anforderungstaster mit Warteanzeige, Verknüpfung per Klick und gemeinsamer Fußgängerphase bei Fahrzeug-Allrot eingebaut (dev7).
- [x] Vorlauf, Fußgängergrün, Räumzeit und nächste Fahrzeuggruppe automatisch getestet; fünf Servertests bestanden.
- [x] Getrennte Fußgängerüberwege A/B nach Freigabe eingebaut (dev8).
- [x] Taster und Fußgängerampeln je Überweg per Rechtsklick A / Schleichen + Rechtsklick B zuordnen; Taster fordern nur ihren eigenen Überweg an.
- [x] Beide Anforderungen in Eingangsreihenfolge nacheinander abarbeiten; beide Fahrzeuggruppen bleiben während Fußgängerfreigaben Rot.
- [x] Getrennte Überwege, Warteschlange, gespeicherte Gruppenzuordnung und Altverbindungen automatisch geprüft; sechs Servertests bestanden.
- [x] Getrennte Tasterreaktionen und Wechsel Fahrzeugrot/Fußgängergrün im Client vom Tester bestätigt (dev8).
- [x] Kleinere Taster mit direkter Mastmontage und Wandplatzierung eingebaut (dev9).
- [x] Vier Mastseiten, Platzierung, Verbrauch, Rückgabe beider Teile und weitere A/B-Funktion automatisch geprüft; sieben Servertests bestanden.
- [x] Mastmontage und Betätigung des montierten Tasters vom Tester bestätigt (dev9).
- [ ] Passende Fahrzeugrichtungen erst in einem späteren Ausbau parallel freigeben.
- [x] Bedienmenü mit Geräteliste, Statusanzeigen, Start/Stopp und kleiner Kreuzungsübersicht eingebaut (dev10).
- [x] Zufahrten und Fahrtrichtungen für Fahrzeugampeln sowie Überwege A/B für Fußgängergeräte im Menü zuweisen und speichern; Änderungen stoppen die Steuerung.
- [x] Menüaktionen, Seitenauswahl, gespeicherte Zuordnungen und Rechte/Entfernung automatisch geprüft; acht Servertests bestanden.
- [x] Menüanzeige, Geräteauswahl und Start/Stopp im Minecraft-Client vom Tester bestätigt (dev10).
- [x] Überwegwechsel eines Tasters A/B im Menü vom Tester bestätigt: Steuerung stoppt, nach Neustart wird der zugewiesene Überweg angefordert.
- [ ] Weitere Menüfälle einschließlich Fahrzeug-Zufahrten, Abbiegezuordnung und weiterer Bildschirmgrößen im Client prüfen.
- [x] Linksabbieger-Pfeilampel und geschützte Einzelphasen je Zufahrt nach Ankündigung und Freigabe eingebaut (dev11).
- [x] Übrige Fahrzeugrichtungen und sämtliche Fußgängerüberwege während Linksphasen Rot; Übergänge, Fußgängerwarteschlange, Wiederherstellung und Rückkehr zum Grundprogramm automatisch geprüft. Neun Servertests bestanden.
- [x] Neue Linksabbiegerampel im Client getestet: schaltet, während andere Ampeln Rot bleiben (dev11).
- [x] Rechtsabbieger und Kombipfeile mit eigenen geschützten Programmen ergänzt (dev12); alle neuen Ampelmodelle laut Tester sichtbar/leuchtend.
- [ ] Weitere einstellbare Kreuzungsschaltungen ausbauen.
- [ ] Konfliktfreie Phasen, Ausfallverhalten und Verhalten beim Laden von Weltbereichen prüfen.
- [ ] Mobile Baustellenampeln ergänzen.

### Bestätigter Ausbau bis dev15

- [x] Kompaktes Menü mit kleinerer Schrift/Buttons und scrollbarer Tabelle statt Seitenwechsel (dev13).
- [x] Übersicht vom Tester für den normalen Kreuzungsbetrieb akzeptiert.
- [x] Mittelpunkt und Radiuswahl 16/24/32/48, Standard 32 (dev14).
- [x] Automatische Erkennung aller Fahrzeug-/Pfeil-/Fahrrad-/Fußgängerampeln, Taster und Warnlichter (dev15).
- [x] Automatische Überwegzuordnung, Speicherung, manuelle Korrekturen und Schutz fremder Verbindungen eingebaut; sechzehn Servertests bestanden.
- [x] Erkennung, Zuordnung und Schaltablauf im aufgebauten Einzelspielertest vom Tester bestätigt (dev15).
- [ ] Sondergeometrien, Chunkwechsel und Mehrspielerfälle weiter prüfen.

### Verbindungen lösen – vor Umsetzung angekündigt, in dev16 gebaut

- [x] Einzelne Verbindung im Steuergerät-Menü lösen.
- [x] Alle Verbindungen nach kurzer Bestätigung lösen.
- [x] Bewusst getrennte Geräte von sofortiger automatischer Wiederverbindung ausnehmen.
- [x] Gelöste Verbindungen auch für ungeladene Geräte dauerhaft entwerten; keine Chunk-Zwangsladung.
- [x] Sichere Zustände, Speicherung, erneutes bewusstes Verknüpfen, Rechte und alte Menüs prüfen.
- [x] Achtzehn automatisierte Servertests bestanden; alte Endpunktdaten nach Reset per NBT-Wiederherstellung geprüft.
- [ ] Neue Funktionen im Minecraft-Client testen und bestätigen lassen.
- [ ] Reale Chunkwechsel und weitere Mehrspielerfälle ergänzend prüfen.

## 6. Digitale Anzeigen und Parkleitsystem

- [ ] Änderbare Textanzeigen ausbauen und ihre Lesbarkeit verbessern.
- [ ] Wechselverkehrszeichen, Tempolimits und Stauwarnungen ergänzen.
- [ ] Fahrstreifensperrungen ergänzen.
- [ ] Parkplatz- und Parkhauswegweisung ergänzen.
- [ ] Frei-/Besetzt- und Restplatzanzeigen ausbauen.
- [ ] Ein- und Ausfahrtszählung ergänzen und mit Belegungswerten verbinden.
- [ ] Speicherung, Synchronisierung und Wiederherstellung nach Neustarts prüfen.

## 7. Baustellentechnik

- [ ] Baken und Absperrungen ergänzen.
- [ ] Warnleuchten ergänzen.
- [ ] Richtungspfeiltafeln ergänzen.
- [ ] Zusammenspiel mit mobilen Ampeln prüfen.

## 8. Veröffentlichung vorbereiten

- [ ] Dokumentierte Client- und Mehrspielertests durchführen.
- [ ] Vollständigkeit anhand des Entwicklungsplans und Zeichenverzeichnisses prüfen.
- [ ] Installation, Bedienung und bekannte Grenzen dokumentieren.
- [ ] Öffentliche Test-JAR als Vorabversion bereitstellen.
- [ ] Rückmeldungen auswerten und Fehler beheben.
- [ ] Erste stabile Version veröffentlichen.

## 9. Nach Fertigstellung: Rechte, Zusatzmods und Kostendeckung klären

- [ ] Nach Fertigstellung der vollständigen Mod die Aufteilung in eine kostenlose, einzeln nutzbare Basismod und separate Zusatzmods konkret festlegen.
- [ ] Dann die aktuellen Minecraft-Bedingungen erneut prüfen und eine schriftliche Anfrage an Mojang/Microsoft zur möglichen Abgabe von Zusatzmods über einen Download-Dienst gegen einen Beitrag zur reinen Kostendeckung vorbereiten.
- [ ] Antwort beziehungsweise belastbare rechtliche Klärung auswerten, bevor über kostenpflichtige Downloads oder Freischaltungen entschieden wird.
- [ ] Anschließend passende Nutzungs-, Weitergabe- und Lizenzbedingungen für eigene Bestandteile festlegen und dokumentieren.

Dieser Punkt ist für die Zeit nach Fertigstellung vorgemerkt. Ein Verkauf ist weder beschlossen noch als erlaubt bestätigt; eine Anfrage wurde noch nicht versandt.

Es gibt derzeit keinen fest zugesagten Termin für die fertige Mod.
