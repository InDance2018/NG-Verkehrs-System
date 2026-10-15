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
- [ ] Fahrrad- und Richtungssignale ergänzen.
- [x] Anforderungstaster mit Warteanzeige, Verknüpfung per Klick und gemeinsamer Fußgängerphase bei Fahrzeug-Allrot eingebaut (dev7).
- [x] Vorlauf, Fußgängergrün, Räumzeit und nächste Fahrzeuggruppe automatisch getestet; fünf Servertests bestanden.
- [ ] Getrennte Fußgängerüberwege A/B umsetzen – wartet auf Freigabe.
- [ ] Taster und Fußgängerampeln je Überweg zuordnen; jeder Taster fordert nur seinen Überweg an.
- [ ] Gleichzeitige Anforderungen nacheinander abarbeiten; während jeder Fußgängerfreigabe zunächst beide Fahrzeuggruppen Rot halten.
- [ ] Getrennte Überwege automatisch und im Client prüfen.
- [ ] Passende Fahrzeugrichtungen erst in einem späteren Ausbau parallel freigeben.
- [ ] Einstellbare Kreuzungsschaltungen ausbauen.
- [ ] Konfliktfreie Phasen, Ausfallverhalten und Verhalten beim Laden von Weltbereichen prüfen.
- [ ] Mobile Baustellenampeln ergänzen.

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

Es gibt derzeit keinen fest zugesagten Termin für die fertige Mod.
