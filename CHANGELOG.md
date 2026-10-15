# Versionsübersicht

Dieses Dokument trennt gebaute Entwicklungsfassungen von einer öffentlichen Veröffentlichung. Die hier genannten JARs wurden bislang für begleitete Tests bereitgestellt; sie sind noch keine GitHub-Releases.

## 0.1.0-dev8 – 15. Oktober 2026

- Getrennte Fußgängerüberwege A/B mit eigenen Tastern und Fußgängerampeln ergänzt.
- Normaler Rechtsklick mit dem Verknüpfer verbindet A; Schleichen + Rechtsklick verbindet B.
- Taster fordern nur ihren eigenen Überweg an; Warteanzeigen sind ebenfalls getrennt.
- Anforderungen werden in Eingangsreihenfolge einzeln bedient. Beide Fahrzeuggruppen bleiben währenddessen Rot; zwischen Fußgängerfreigaben liegen vollständige Räum- und Vorlaufzeiten.
- Nach höchstens zwei unterschiedlichen Überwegen erhält der Fahrzeugverkehr wieder eine Phase; neu wartende Anforderungen bleiben gespeichert.
- Gruppen und Warteschlange werden gespeichert. Alte dev7-Verbindungen werden als A übernommen.
- Build und Ressourcenprüfung bestanden; sechs automatisierte Servertests bestanden.
- Zwei Phasentests über je 100.000 Ticks bestanden; A/B-Lasttest mit 86 vollständigen Phasen je Überweg, ohne konkurrierende Freigaben.
- **Clienttest ausstehend:** Fußgängerampeln UND Taster des zweiten Überwegs schleichend neu verbinden. Grafiken und 37 Bauteile bleiben erhalten.

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
