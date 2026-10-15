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
- [x] Erste Leitplanken-Endstücke und Übergänge ergänzen (dev18).
- [ ] Weitere Bauformen ergänzen und neue Teile im Client prüfen.
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
- [x] Passende Fahrzeugrichtungen in dev18 mit konservativer Konfliktprüfung parallel freigeben; Clienttest offen.
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
- [x] Trenn- und Neuerkennungsfunktionen im bestätigten dev17-Spieltest erprobt.
- [ ] Reale Chunkwechsel und weitere Mehrspielerfälle ergänzend prüfen.

### Live-Geräteliste – dev17

- [x] Nach Nutzerfreigabe Trennen, Suchen und Erneut erkennen ohne manuelle Menü-Neuöffnung anzeigen.
- [x] Liste bei vollständigem Lösen leeren und bei Neuerkennung im selben Menü wieder füllen.
- [x] Auswahl anhand der Geräteposition erhalten; beim Entfernen der Auswahl kein anderes Gerät automatisch wählen.
- [x] Veraltete Klicks und Teilübertragungen gegen versehentliche Aktionen absichern.
- [x] Zwanzig Servertests einschließlich Live-Listenabgleich, 256 verbundenen Geräten und voller Koordinatenübertragung bestanden.
- [x] Automatische Listenaktualisierung im grafischen Minecraft-Client vom Tester bestätigt (dev17).

**dev17 ist als begleiteter Einzelspieler-Teststand bestätigt. Der vollständige Mod-Ausbau bleibt offen.**

## 0.1.0-dev18 – Straßenbau und Kreuzungsprogramme

**Nach Ankündigung und Freigabe als gemeinsame Testfassung umgesetzt. Clienttest noch offen.** dev17 bleibt der vom Tester bestätigte bisherige Stand.

- **74 Bauteile insgesamt:** 14 neue Markierungen und 14 Straßen-/Befestigungsteile. Darunter Leitlinien, Sperrflächen, Bus-/Radmarkierung, Wartelinie, zusätzliche Pfeile, Parkbucht, Verkehrsinseln, Bordsteinabsenkung, Leitplankenende/-übergang, verstärkter Mast und mehrteiliger Ampelausleger.
- Eigene kompakte **Programmansicht** im Steuergerät; Geräteliste bleibt live und scrollbar.
- Fahrzeuggrün 5–120 s, Abbiegergrün 5–60 s, Fußgängergrün 6–60 s; geschützte Gelb-/Rot-Gelb-/Räumzeiten.
- **Kreuzung**, **Fußgängerüberweg** und **Baustellenwechselverkehr**, dazu drei speicherbare Einstellungsplätze. Ändern/Laden stoppt; anschließend bewusst starten.
- Zuschaltbare parallele geschützte Fahrzeugrouten mit konservativer Konfliktmatrix. Fußgänger parallel nur zur passenden expliziten Geradeaus-/Fahrradgruppe; gewöhnliche Kreisampeln oder ungeladene Endpunkte erzwingen getrennte Freigaben. Die Prüfung verwendet Zufahrt/Fahrtrichtung und Überweggruppe, keine Analyse real gebauter Fahrwege. Aufbau und manuelle Zuordnungen müssen dazu passen.
- Nachtblinken manuell oder automatisch bei Weltzeit 13000–23000; Fußgänger Rot, Anforderungen aus. Rückkehr über mindestens 2 s Allrot und 1 s Rot-Gelb.
- Aktuelle/nächste Phase mit Restzeit, dauerhafte Einstellungen und Speicherplätze.
- Automatische Erkennung vorhandener Signale auch am hohen Ausleger; Tastermontage zusätzlich am verstärkten Mast. Verbindungen, Trennsperren und Live-Geräteliste bleiben erhalten.
- **Build/Reobf, Ressourcenprüfung (74 Blöcke), sechs deterministische Phasentests und 23 Forge-Servertests bestanden.** Dazu gespeicherte Programme, Nachtübergänge, reale Leitplankenanschlüsse, Auslegererkennung und Rückfall bei unklarer Kreisampel geprüft.

### Kurzer Testaufbau

Steuergerät öffnen → oben **Programm**. Für einen schnellen Test Fahrzeuggrün auf 5 s stellen, anschließend Starten. Änderungen stoppen die Steuerung. S1–S3 speichern/laden eigene Einstellungen.

Auslegeransatz oben auf verstärkte Masten setzen, gerade Segmente anschließen, Abschluss mit Aufhängung am Ende und Ampel direkt darunter. Ampel zu ankommenden Fahrzeugen ausrichten. Verknüpfer/Mitte/Radius funktionieren wie bisher; nach automatischer Zuordnung prüfen und starten.

Im normalen Kreuzungsprogramm gilt N/S = A, O/W = B. Im Baustellenprogramm gilt **N/O = A, S/W = B**, damit gegenüberliegende Enden wechseln. Für den reinen Fußgängerüberweg Fahrzeugampeln beider Straßenrichtungen als A verbinden; Fahrzeuggruppe B bleibt Rot. Keine automatische Erkennung einer real gebauten Engstelle.

**Gemeinsamer dev18-Spieltest steht aus:** neue Modelle und Ausrichtungen, Programme, Zeiten, Parallelfreigaben, Nachtbetrieb und Speicherplätze im Minecraft-Client prüfen. Dies ist keine stabile Gesamtveröffentlichung. Weiterhin keine öffentliche GitHub-Test-JAR und kein öffentlicher Quellcode-Upload.

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
