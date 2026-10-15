# Versionsübersicht

Dieses Dokument trennt gebaute Entwicklungsfassungen von einer öffentlichen Veröffentlichung. Die hier genannten JARs wurden bislang für begleitete Tests bereitgestellt; sie sind noch keine GitHub-Releases.

## 0.1.0-dev19 – Baustellenausstattung

**Nach Ankündigung im Projektgespräch und Freigabe als Testfassung umgesetzt. dev18- und dev19-Spieltests bleiben offen; dev17 ist weiterhin der zuletzt vom Tester bestätigte Stand.**

- **97 Bauteile insgesamt**, davon 23 neue: zwei Leitbaken, Absperrschranke, Bauzaunsegment, Standplatte, Baustellenleuchte, Richtungspfeiltafel, mobile Ampel, fünf Schildentwürfe und zehn gelbe Markierungen.
- Rot-weiße Baken und Schranke mit Standfüßen; ausrichtbare Gittersegmente für den Bauzaun. Zwei Segmente übereinander ergeben einen hohen Zaun.
- **Baustellenleuchte** ohne Steuergerät: Rechtsklick wechselt Blinken → Aus → Dauerlicht → Blinken.
- **Richtungspfeiltafel** mit blinkenden gelben LED-Pfeilen und Standfuß: Rechtsklick wechselt Links → Rechts → Aus → Links. Links/rechts von der Vorderseite aus gesehen.
- **Mobile Baustellenampel** mit zwei echten Blockpositionen, Standfuß und Ampelkopf. Verknüpfen am Kopf oder Fuß; genau ein Eintrag im Steuergerät. Automatische Erkennung, Menüzuordnung, Nachtbetrieb, Trennen/Neuerkennen und bestehendes Baustellenwechselprogramm nutzbar. Ein Abbauen entfernt beide Teile und liefert eine vollständige Ampel.
- Eigene Schildentwürfe für Bauarbeiten, Engstelle, Links-/Rechtsvorbeifahrt und Ende der Beschränkungen; geformte Konturen und transparente Inventargrafiken. Amtliche Detailprüfung bleibt offen.
- Gelbe Doppellinie, Kurzstriche, fünf Pfeilvarianten, Sperrfläche und zwei Leitlinien. Bisherige gelbe Linien bleiben vorhanden; Markierungen sind volle Fahrbahnblöcke.
- **Build/Reobf, Ressourcenprüfung für 97 Bauteile und alle 25 Forge-Servertests bestanden.** Neu geprüft: reale Blinkwechsel, Dauerlicht/Aus, Rechtsklickmodi und Baurechte, Modus-/Richtungsdaten, Kopf-Verknüpfung, automatische mobile Erkennung, Wechselverkehr, Trennen und Einzelbeute beim Abbauen. Bisherige Phasentests bleiben unverändert.

**Kleiner Test ohne Kreuzung:** zwei mobile Ampeln gegenüber an einer Engstelle aufstellen, verbinden und im Steuergerät „Baustellenwechsel“ wählen. Nord/Ost = A, Süd/West = B; gegenüberliegende Enden müssen verschiedene Gruppen haben. Fahrzeuggrün zum Test auf 5 Sekunden stellen, prüfen und starten. Baken, Schranke, Zaun, Schilder und Leuchten ergänzen. Leuchte/Pfeiltafel direkt am Bauteil bedienen; sie benötigen keine Verknüpfung und erscheinen nicht in der Geräteliste.

**Offene Clienttests erhalten:** dev18-Straßenbauteile/Ausleger, Programme/Zeiten, Parallelfreigaben, Nachtbetrieb und Speicherplätze sind noch nicht vom Tester bestätigt. Hinzu kommen sämtliche dev19-Ansichten/Ausrichtungen, mobile Ampel samt Kopf-/Fußbedienung und Abbauen, Leuchten-/Pfeiltafelmodi sowie Welt-Neuladen. Automatische Servertests sind keine Bestätigung dieses grafischen Spieltests. Keine Fahrzeug-KI oder automatische Analyse tatsächlich gebauter Fahrwege.

Weiterhin Entwicklungsdokumentation auf GitHub; keine öffentliche Test-JAR und kein öffentlicher Quellcode-Upload.

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

## 0.1.0-dev17 – 15. Oktober 2026

- Nach Nutzerfreigabe Bedienungsproblem des dev16-Tests behoben: Geräteliste aktualisiert sich ohne Schließen/erneutes Öffnen.
- Einzeltrennung entfernt Zeilen direkt; Suchen/erneute Erkennung ergänzt sie, vollständiges Lösen leert die Liste.
- Externe Verknüpfungsänderungen im offenen Menü übernehmen.
- Auswahl bleibt am selben Gerät. Entfernte Auswahl wird aufgehoben, nachrückende Geräte werden nicht versehentlich bearbeitet.
- Zeilenklicks an zuletzt angezeigte Listenfassung gebunden; veraltete Auswahl abweisen.
- Neue Geräteliste, Status und Auswahl gemeinsam veröffentlichen; volle signierte Koordinaten erhalten.
- Build/Reobf, Ressourcen-/JAR-Prüfung und zwanzig Servertests bestanden. Bestehende Tests bleiben erfolgreich.
- **Clienttest bestätigt:** Trennen, erneutes Erkennen und automatische Listenaktualisierung funktionieren laut Tester ohne erneutes Öffnen. dev17 als begleiteter Einzelspieler-Teststand abgeschlossen; Chunkwechsel, Sonderfälle und weitere Mehrspielerprüfungen bleiben offen. Weiterhin keine öffentliche GitHub-JAR.

## 0.1.0-dev16 – 15. Oktober 2026

- Vor Umsetzung auf GitHub angekündigt, anschließend freigegeben und gebaut.
- Einzelne Verbindung lösen; alle Verbindungen nach fünf Sekunden gültiger Bestätigung zurücksetzen. Abbruch und Schutz gegen unbestätigte/abgelaufene oder wiederholte Bestätigung.
- Getrennte Fahrzeug-/Pfeil-/Fahrrad-/Fußgängersignale Rot, Taster und Warnlicht aus.
- Trennsperren speichern und normale automatische Wiederverbindung verhindern. Vollreset schaltet Programm und Automatik aus; Mitte/Radius bleiben erhalten.
- Manuelles Neuverknüpfen oder bewusstes „Erneut erkennen“ gibt Sperren frei; neue Zuordnungen bleiben zunächst gestoppt.
- Gespeicherte Verbindungsfassung entwertet alte, später geladene Endpunkte ohne Zwangsladung.
- Build, Ressourcen-/JAR-Prüfung und achtzehn Servertests bestanden; Altverbindungen per NBT-Wiederherstellung simuliert.
- **Trenn-/Neuerkennungsbedienung anschließend im dev17-Spieltest bestätigt; reale Chunkwechsel bleiben offen.** Keine öffentliche GitHub-JAR.

## 0.1.0-dev15 – 15. Oktober 2026

- Automatik auf alle vorhandenen Ampeltypen und Taster erweitert, einschließlich Fußgängerampeln und Warnblinklichtern.
- Taster folgen nahen Fußgängerampeln, unabhängig von ihrer Mastseite. Ohne nahe Lampe bzw. bei gleichen Abständen entscheidet die Ausrichtungsachse.
- Beliebiger Ampel-/Tastertyp kann das erste verknüpfte Gerät sein.
- Automatische Zuordnungsänderungen stoppen; manuelle Korrekturen bleiben erhalten.
- Sechzehn Servertests bestanden, einschließlich vollständigem Aufbau mit allen Signaltypen und beiden Fußgängerüberwegen.
- **Clienttest bestätigt:** Erkennung, Zuordnung und Schaltablauf laufen laut Tester im neu aufgebauten Einzelspielertest. Sondergeometrien, Chunkwechsel und Mehrspielerfälle bleiben offen.

## 0.1.0-dev14 – 15. Oktober 2026

- Mittelpunkt mit Verknüpfer/Schleichen/Bodenklick festlegen; Radius 16/24/32/48, Standard 32.
- Automatische Fahrzeug-/Pfeil-/Fahrraderkennung nach Position/Ausrichtung in geladenen Chunks; keine Zwangsladung.
- Fremde Verbindungen und manuelle Korrekturen geschützt; Überschneidungen bleiben offen.
- Vierzehn Servertests bestanden. Geräteabdeckung anschließend in dev15 vervollständigt.

## 0.1.0-dev13 – 15. Oktober 2026

- Kompaktes Menü mit 75-Prozent-Schrift, schmalen Zeilen und kleineren Schaltflächen.
- Gerätetabelle mit festen Statusspalten, Scrollen statt Seitenwechsel, direkte Zufahrts-/Überwegtasten.
- Zwölf Servertests bestanden, darunter Auswahl aller 256 Geräteplätze über echte Paketserialisierung.
- **Tester akzeptiert die Übersicht für den normalen Betrieb.**

## 0.1.0-dev12 – 15. Oktober 2026

- Rechts-/Geradeaus-Pfeilampeln, Geradeaus/Links, Geradeaus/Rechts, Links/Rechts und alle drei Richtungen ergänzt.
- Fahrradampel und gelbes Ein-Linsen-Warnblinklicht ergänzt.
- Geschützte Einzelphasen für Rechtsabbieger und Kombipfeile; Fahrrad-/Geradeausgruppe im Grundprogramm.
- 46 Bauteile plus Verknüpfer, elf Servertests und 200.000-Tick-Prüfung aller 24 geschützten Routen bestanden.
- **Tester bestätigt: Alle neuen Ampeln leuchten.**

## 0.1.0-dev11 – 15. Oktober 2026

- Vor Umsetzung auf GitHub angekündigt und zur Umsetzung freigegeben.
- Linksabbieger-Pfeilampel mit roten, gelben und grünen Pfeilsymbolen sowie vier Signalzuständen ergänzt.
- Zufahrt Nord/Ost/Süd/West im vorhandenen Steuergerät-Menü zuordnen.
- Jeweils nur eine Linksabbieger-Zufahrt freigegeben; andere Fahrzeuge und beide Fußgängerüberwege Rot.
- Linksphasen: 2 Sekunden Vorlauf, 1 Sekunde Rot-Gelb, 10 Sekunden Grün, 3 Sekunden Gelb, 2 Sekunden Räumzeit.
- Nach Nord/Süd-Geradeaus folgen Nord-/Süd-Linksabbieger einzeln; nach Ost/West-Geradeaus folgen Ost/West einzeln. Bereits vor Einschubbeginn wartende Fußgängeranforderungen werden vorher bedient.
- Nächste Fahrzeuggruppe setzt anschließend fort. Ohne Linksabbieger bleibt das bisherige Programm unverändert.
- Linksphasen und Warteschlange werden gespeichert; geladene aktive Phasen beginnen mit erneutem Rot-Vorlauf. Stoppen/fehlende Steuerung erzwingen Rot.
- Vorhandene Kreisampeln mit Linkszuordnung verwenden ebenfalls geschützte Linksphasen. Der Pfeilblock behält seine Linksrolle; Rechtsabbieger bleiben gesperrt.
- 38 Bauteile plus Verknüpfer. Build und Ressourcenprüfung bestanden.
- Neun automatisierte Servertests sowie drei Phasentests über je 100.000 Ticks bestanden.
- **Clienttest anschließend bestätigt:** Linksampel schaltet, alle anderen bleiben Rot.

## 0.1.0-dev10 – 15. Oktober 2026

- Steuergerät-Menü mit Geräteliste, Statusanzeigen, fünf Geräten pro Seite und kleiner Kreuzungsübersicht ergänzt.
- Leere Hand + normaler Rechtsklick öffnet das Menü. Start/Stopp im Menü oder direkt mit Schleichen + Rechtsklick.
- Zufahrt Nord/Ost/Süd/West und Fahrtrichtung Geradeaus/Links/Rechts für Fahrzeugampeln speichern.
- Geradeaus bleibt im bisherigen Grundprogramm Nord/Süd = A, Ost/West = B. Vorbereitete Abbieger bleiben Rot, bis eigene geschützte Programme und Pfeilampeln folgen.
- Fußgängerampeln und Taster können ihren Überweg A/B im Menü wechseln.
- Zuordnungsänderungen stoppen die Steuerung; anschließend gezielt neu starten.
- Automatische Erfassung bestehender geladener Verbindungen und Speicherung der Geräteliste.
- Menüaktionen prüfen Rechte, Entfernung, Steuergerät und tatsächliche Geräteverbindung. Neu verknüpfte Geräte nach erneutem Öffnen.
- Build und Ressourcenprüfung bestanden. Acht automatisierte Servertests bestanden.
- **Clienttest vom Tester bestätigt:** Menüanzeige, Geräteauswahl und Start/Stopp funktionieren. Ein Taster lässt sich im Menü von Überweg A auf B ändern; die Steuerung stoppt dabei, und nach Neustart fordert der Taster den zugewiesenen Überweg an.
- Weitere Clienttests der Fahrzeug-Zufahrten, vorbereiteten Abbiegezuordnungen und anderer Bildschirmgrößen bleiben offen.

## 0.1.0-dev9 – 15. Oktober 2026

- Anforderungstaster von 6 × 10 × 4 auf 5 × 6 × 2,25 Modellpixel verkleinert.
- Direkte Montage am NG-Schildermast: mit dem Taster-Blockitem auf die gewünschte Mastseite rechtsklicken.
- Mast und Taster bilden eine gemeinsame Blockposition; das Gehäuse sitzt direkt am Mast. Die Ampel darüber bleibt erhalten.
- Wand- und Bodenplatzierung bleiben möglich.
- Beim normalen Abbauen einer Mastmontage werden ein Taster und ein Mast zurückgegeben; Wand-/Bodentaster erzeugen keine zusätzlichen Masten.
- Bisherige Verknüpfung und A/B-Bedienung bleiben erhalten. Für Mastmontage alte freistehende Taster abbauen, am Mast neu platzieren und neu verknüpfen.
- Build und Ressourcenprüfung bestanden; sieben automatisierte Servertests bestanden.
- **Clienttest vom Tester bestätigt:** Taster sitzt am Mast und lässt sich betätigen. Weitere Ansichten bleiben offen.

## 0.1.0-dev8 – 15. Oktober 2026

- Getrennte Fußgängerüberwege A/B mit eigenen Tastern und Fußgängerampeln ergänzt.
- Normaler Rechtsklick mit dem Verknüpfer verbindet A; Schleichen + Rechtsklick verbindet B.
- Taster fordern nur ihren eigenen Überweg an; Warteanzeigen sind ebenfalls getrennt.
- Anforderungen werden in Eingangsreihenfolge einzeln bedient. Beide Fahrzeuggruppen bleiben währenddessen Rot; zwischen Fußgängerfreigaben liegen vollständige Räum- und Vorlaufzeiten.
- Nach höchstens zwei unterschiedlichen Überwegen erhält der Fahrzeugverkehr wieder eine Phase; neu wartende Anforderungen bleiben gespeichert.
- Gruppen und Warteschlange werden gespeichert. Alte dev7-Verbindungen werden als A übernommen.
- Build und Ressourcenprüfung bestanden; sechs automatisierte Servertests bestanden.
- Zwei Phasentests über je 100.000 Ticks bestanden; A/B-Lasttest mit 86 vollständigen Phasen je Überweg, ohne konkurrierende Freigaben.
- **Clienttest vom Tester bestätigt:** Taster reagieren getrennt nach Seite; Fahrzeugampeln wechseln Rot und Fußgänger erhalten Grün. Die automatische Prüfung der vollständigen Warteschlange bleibt zusätzlich dokumentiert.
- Fußgängerampeln UND Taster des zweiten Überwegs werden schleichend als B verbunden. 37 Bauteile bleiben erhalten.

## 0.1.0-dev7 – 15. Oktober 2026

- Fußgängerampel mit rotem stehendem und grünem gehendem Symbol, schwarzem Gehäuse und Sonnenblenden ergänzt.
- Gelben Anforderungstaster mit Warteanzeige ergänzt.
- Beide neuen Geräte lassen sich mit dem vorhandenen Ampel-Verknüpfer am Steuergerät anmelden; Rechtsklick auf den Taster fordert Grün an.
- Gemeinsame Fußgängerphase: reguläres Fahrzeug-Gelb beenden, mindestens 2 Sekunden Allrot, 6 Sekunden Fußgängergrün, 4 Sekunden Räumzeit, anschließend nächste Fahrzeuggruppe.
- Wiederholtes Drücken verlängert eine laufende Fußgängerphase nicht.
- Speicherung von Anforderung/Phase sowie Rot-Vorlauf beim Laden einer aktiven Phase ergänzt.
- 37 Bauteile plus Verknüpfer; Build und Ressourcenprüfung bestanden.
- Fünf automatisierte Servertests bestanden. Phasentest über 100.000 Ticks mit 108 vollständigen Fußgängerphasen ohne konkurrierende Freigaben bestanden.
- **Clienttest vom Tester bestätigt:** Fahrzeuge wechseln auf Rot, Fußgänger erhalten Grün; anschließend Fußgänger Rot und Fahrzeugverkehr wieder freigegeben.
- Zunächst gemeinsame Fußgängerphase bei vollständig gestoppten Fahrzeuggruppen; getrennte Querungen sind noch nicht enthalten.
- Getrennte Überwege A/B wurden anschließend nach Freigabe in dev8 umgesetzt.

## 0.1.0-dev6 – 15. Oktober 2026

- Ampel-Verknüpfer mit gespeichertem Steuergerät und automatischer Blockpositionserkennung ergänzt.
- Rechtsklick verbindet Gruppe A; Schleichen + Rechtsklick verbindet Gruppe B.
- Leere Hand + Rechtsklick am Steuergerät startet oder stoppt die Steuerung.
- Meldungen bei fehlender Auswahl, falschem Ziel, falscher Dimension, zu großer Entfernung oder fehlendem Steuergerät.
- Vier automatisierte Servertests bestanden.
- **Clienttest vom Tester bestätigt:** deutsche Farbfolge, Verknüpfen, Start/Stopp und wechselnde Gruppen A/B mit zwei Ampeln an einem Steuergerät.
- Fußgängerampel und Anforderungstaster folgen als nächster Entwicklungsschritt.

## 0.1.0-dev5 – 15. Oktober 2026

- Alle 14 vorhandenen Schildtafeln mit passenden runden, dreieckigen, achteckigen, rautenförmigen oder rechteckigen Konturen versehen.
- Graue quadratische Außenflächen entfernt; Inventargrafiken besitzen transparente Außenbereiche.
- Weltmodelle entlang der sichtbaren Kontur mit Metallrückseite und schmalen Seitenkanten modelliert.
- Build und Ressourcenprüfung bestanden; Modellkonturen gegen die deckenden Texturpixel aller 14 Tafeln geprüft.
- **Sichtprüfung bestätigt:** Die gezeigten Schildformen werden im Einzelspieler ohne graue Außenflächen dargestellt; vom Tester ausdrücklich bestätigt.
- **Weiter offen:** sämtliche Schildvarianten, Rückseiten und Ausrichtungen im Spiel prüfen. Auswahl- und Kollisionsumrisse sind weiterhin vereinfacht.

## 0.1.0-dev4 – 15. Oktober 2026

- Leitplanke mit abgestuftem Wellenprofil, verzinkter Oberfläche und Stahlprofil-Pfosten überarbeitet.
- Fahrzeugampel mit dunklem Gehäuse, runden Signalflächen, dreidimensionalen Sonnenblenden und rückseitigen Halterungen überarbeitet.
- Inventarmodell der Ampel auf die neue 3D-Bauform umgestellt.
- Auswahlumriss der Fahrzeugampel an das größere Modell angepasst.
- Build und Ressourcen-/Modellprüfungen bestanden.
- **Sichtprüfung bestätigt:** Neue Leitplanken- und Ampelform in einer Einzelspieleransicht sichtbar dargestellt. Dies bestätigt noch keine vollständige Funktionsprüfung aller Ampelphasen.

## 0.1.0-dev3 – 15. Oktober 2026

- Leitplanken-Inventarmodell um einen Querholm ergänzt.
- Fahrzeugampel im Inventar von vorne dargestellt.
- Eigene Inventargrafiken für digitale Textanzeige und Parkplatzanzeige ergänzt.
- Platzierte Geräte verwenden weiterhin ihre tatsächlichen Anzeigewerte.
- Build und Ressourcenprüfung bestanden.
- Nachfolgende Einzelspielerbilder zeigten Ampel, beide Anzeigen und verbundene Leitplanken; eine vollständige Funktionsprüfung ist daraus nicht ableitbar.

## 0.1.0-dev2 – 15. Oktober 2026

- 14 Schildtafeln im Inventar, in der Schnellleiste und als gehaltene Items mit ihrer Vorderseite dargestellt.
- Platzierte Schilder behalten eine bedruckte Vorderseite und eine Metallrückseite.
- Build und Ressourcenprüfung bestanden.
- Nachfolgende Einzelspielerbilder bestätigten erkennbare Schilder im Inventar und in der Welt.

## 0.1.0-dev1 – 15. Oktober 2026

- Grundsystem mit 35 Bauteilen erstellt.
- Erste Straßenbeläge, Fahrbahnmarkierungen, Gehwegplatten und Bordsteinplatte ergänzt.
- Erste Schildtafeln, Schildermast und verbindbare Leitplanke ergänzt.
- Fahrzeugampel, Steuergerät sowie digitale Text- und Parkplatzanzeige ergänzt.
- Drei automatisierte Servertests bestanden: sicheres Rot bei Steuerungsausfall, Speicherung/Begrenzung von Parkplatzwerten und Speicherung/Begrenzung von Anzeigetexten.
- Erste Einzelspielertests zeigten noch unzureichende Inventaransichten mehrerer Bauteile.

## Weiter offen

Vollständige deutsche Zeichenabdeckung, maßgebliche Teile des Straßen- und Autobahnausbaus, weitere Signalarten, erweiterte Kreuzungssteuerung, automatische Parkplatzzählung, Baustellentechnik und umfassende Spieltests. Details stehen im [Entwicklungsplan](ROADMAP.md).
