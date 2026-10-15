# NG-Verkehrs-System

## Private Testfassung: 0.1.0-beta2-dev2 – sechs Aufgaben gemeinsam

**Umgesetzt, zum Spieltest bereit:** Schilderbrücken/Befestigungen; Ausfahrts-, Entfernungs- und Umleitungsschilder; digitale Verkehrsanzeigen; gemeinsame Anzeigensteuerung; Parkleitsystem; automatische Ein-/Ausfahrtszählung. **114 Blöcke plus zwei Verknüpfer = 116 Gegenstände**, elf Inventarreiter. Reobf-Build erfolgreich, **37/37 Forge-Servertests bestanden**. Client-Sicht-/Bedienprüfung und reale Mehrspielertests der neuen Systeme stehen noch aus. Fahrzeugerkennung zunächst Boote/Loren, besetzte MISC-Fahrzeugentitäten und erweiterbarer Entity-Type-Tag; konkrete Fahrzeugmods sind noch nicht geprüft. [Umfang und Testanleitung](DEV-BETA2-2.md) · [ursprüngliche Ankündigung](PLAN-BETA2-DEV2.md).

**Vorherige private Testfassung: 0.1.0-beta2-dev1.** Gemeinsam umgesetzt: beschriftbare Orts-/Straßenwegweiser und Autobahntafeln mit drei Zielen und bis zu vier Fahrspuren. 101 Blöcke plus Verknüpfer, elf Reiter, 29 Forge-Servertests bestanden. Client-Sicht-/Bedienprüfung offen. [Umfang und Testanleitung](DEV-BETA2-1.md). Die öffentliche Downloadfassung bleibt Beta 1.

**Beta 1 veröffentlicht · 15. Oktober 2026:** [0.1.0-beta1 – JAR und Downloadpaket](https://github.com/InDance2018/NG-Verkehrs-System/releases/tag/v0.1.0-beta1). Grundlage ist dev20 als letzte Dev-Version. [Lizenz](LICENSE.md) · [Lizenz im Wiki](https://github.com/InDance2018/NG-Verkehrs-System/wiki/Lizenz) · [Downloadbeitrag mit Handbuch/PDF-Links](BETA1-RELEASE.md).

**Beta-Spieltests vom Nutzer bestätigt:** Einzelspieler mit erneutem Weltbeitritt, zwei Spieler mit unabhängig aufgebauten und verknüpften Ampelanlagen, Verlassen/Neubeitritt, zwei Server-Neustarts sowie Entfernungstest mit 1.500 Blöcken Abstand und zwei Minuten Wartezeit. Alles vorhanden und funktionsfähig. Dies bestätigt die getesteten Aufbauten; tatsächliche Chunk-Entladung wurde nicht protokolliert. Weitere Sonderfälle bleiben Teil der Beta-Erprobung.


**Handbuch:** [Deutsch / English – Katalog und Anleitungen](https://nevergate.de/ng-verkehr/) · [PDF DE](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-DE.pdf) · [PDF EN](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-EN.pdf)

**Spielteststand bestätigt · 15. Oktober 2026:** Der Tester bestätigt alle bisherigen Entwicklungsstände bis einschließlich **0.1.0-dev20**. Damit sind die zuvor offenen dev18-/dev19-/dev20-Spieltests im begleiteten Einzelspieler-Teststand als bestätigt erfasst. Diese Rückmeldung ist die Grundlage des Teststatus; eine stabile Gesamtveröffentlichung ist damit nicht erklärt.
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

Stand: **15. Oktober 2026 · letzte Entwicklungsfassung 0.1.0-dev20; Beta 0.1.0-beta1 veröffentlicht**

- Grundsystem mit **97 registrierten Bauteilen**.
- Erste Straßenblöcke mit weißen und gelben Linien, Richtungspfeilen und Fußgängerüberweg.
- Gehwegplatten, Bordsteinplatte, Schildermast und verbindbare Leitplanke.
- 14 Schildtafeln einschließlich einer unbeschrifteten Tafel; darunter erste Vorfahrt-, Park- und Geschwindigkeitszeichen.
- Fahrzeugampel mit vier Regelzuständen und dunklem Blinkzustand sowie ein Ampelsteuergerät.
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

**Bedienmenü dev10 – Clienttest vom Tester bestätigt:** Leere Hand + Rechtsklick am Steuergerät öffnet ein Bedienmenü mit Geräteliste, Status, Start/Stopp, Zufahrten Nord/Ost/Süd/West, Fahrtrichtung und Fußgängerüberweg A/B. Schleichen + Rechtsklick bleibt direkter Start/Stopp-Zugriff. Zuordnungen speichern sofort und stoppen die Steuerung; anschließend bewusst neu starten. Bis dev16 erschienen neue Geräte erst nach erneutem Öffnen; dev17 aktualisiert die Liste im offenen Menü.

Geradeaus nutzt das bisherige Grundprogramm Nord/Süd = A und Ost/West = B. In dev10 waren Abbiegezuordnungen zunächst vorbereitet. Seit dev11 erhalten Linksabbieger eigene geschützte Phasen; seit dev12 gilt dies auch für Rechtsabbieger und Kombipfeile. Acht automatisierte Servertests sind bestanden. Der Tester bestätigt im Minecraft-Client: Menüanzeige, Geräteauswahl, Start/Stopp sowie Wechsel eines Tasters von Überweg A auf B. Die Änderung stoppt die Steuerung; nach dem Neustart fordert der Taster den zugewiesenen Überweg an. Die vorbereiteten Fahrzeug-Zufahrten und Abbiegezuordnungen sind damit noch nicht vollständig im Client geprüft.

**Linksabbieger dev11 – Clienttest bestätigt:** Der Tester bestätigt, dass die Pfeilampel schaltet und alle anderen Ampeln dabei Rot bleiben.

**Weitere Ampeltypen dev12:** Rechts-, Geradeaus- und Kombipfeile (Geradeaus/Links, Geradeaus/Rechts, Links/Rechts, alle drei Richtungen), Fahrradampel und gelbes Warnblinklicht. Fahrzeug-/Fahrrad-Geradeaus laufen mit A/B; Abbieger und Kombipfeile erhalten eigene geschützte Phasen. Je Sonderphase 2 Sekunden Rot-Vorlauf, 1 Sekunde Rot-Gelb, 10 Sekunden Grün, 3 Sekunden Gelb, 2 Sekunden Räumzeit. Alle übrigen regelnden Signale Rot. Warnlicht blinkt unabhängig davon. Elf Servertests und ein Test aller 24 geschützten Zufahrt-/Routenkombinationen über 200.000 Ticks bestanden. Der Tester bestätigt die leuchtende Darstellung der neuen Modelle.

**Kompaktes Menü dev13 – vom Tester für den normalen Betrieb akzeptiert:** Schrift auf 75 Prozent verkleinert, schmale Gerätezeilen, kleine Schaltflächen, feste Statusspalte und scrollbare Tabelle statt fünf Geräten pro Seite. Direkte N/O/S/W- und Überweg-A/B-Tasten. Zwölf Servertests einschließlich Auswahl aller 256 Geräteplätze über echte Paketserialisierung bestanden.

**Automatische Erkennung dev14/dev15 – im Einzelspieleraufbau bestätigt:** Steuergerät auswählen, erstes Gerät einmal verbinden und mit dem Verknüpfer per Schleichen/Rechtsklick einen Bodenblock als Kreuzungsmitte festlegen. Radius direkt zwischen 16, 24, 32 und 48 Blöcken wählen; Standard 32. Die Automatik prüft etwa alle zwei Sekunden geladene Weltbereiche, ohne zusätzliche Chunks zu laden. Fahrzeug-/Pfeil-/Fahrradampeln werden nach Position und Ausrichtung zugeordnet. Gegenüberliegende Zufahrten gehören zur selben Fahrzeuggruppe; Querverkehr zur anderen.

Seit dev15 werden auch Fußgängerampeln, Taster und Warnblinklichter automatisch verbunden. Fußgängergruppen folgen der N/S- bzw. O/W-Linsenachse. Taster übernehmen die Gruppe der nahen Fußgängerampel desselben Steuergeräts; ohne nahe Lampe bzw. bei Gleichstand entscheidet die eigene Ausrichtungsachse. Manuelle Korrekturen und fremde Verbindungen bleiben erhalten. Neue/geänderte Zuordnungen stoppen auf Rot; nach Prüfung bewusst starten. Bei überlappenden Automatikbereichen bleiben neue uneindeutige Funde offen.

**Sechzehn automatisierte Servertests bestanden.** Der Tester meldet nach dem Aufbau mit neuem Steuergerät: Erkennung, Zuordnung und Schaltablauf laufen im getesteten Einzelspieleraufbau. Das bestätigt diesen Aufbau, keine pauschale Freigabe sämtlicher Sondergeometrien, Chunkwechsel oder Mehrspielerfälle.

### Neu in dev17: Geräteliste aktualisiert sich im geöffneten Menü

Nach dem Test der Trennfunktionen wurde die bisher nötige manuelle Menü-Neuöffnung als Bedienungsproblem gemeldet und der Umbau freigegeben. Getrennte Geräte verschwinden jetzt direkt; Suchen und Erneut erkennen fügen neue Geräte in dieselbe offene Liste ein. Alle lösen leert sie, erneute Erkennung füllt sie wieder. Auch Änderungen durch Verknüpfer, andere Spieler oder den Erkennungsticker werden übernommen.

Die Auswahl bleibt am gleichen Gerät, wenn Zeilen davor verschwinden. Wird das ausgewählte Gerät getrennt, wird die Auswahl aufgehoben; die nächste Ampel übernimmt keine versehentliche Einstellung. Veraltete Zeilenklicks werden abgewiesen. Neue Liste, Positionen, Status und Auswahl werden gemeinsam sichtbar. Kein Schließen/erneutes Öffnen erforderlich.

**Zwanzig Servertests bestanden**, einschließlich Live-Leeren/Wiederbefüllen, Auswahlstabilität, veralteten Klicks, tatsächlich 256 verbundenen Geräten und vollständiger Koordinatenübertragung. **Clienttest dev17 bestätigt:** Der Tester bestätigt Trennen, erneutes Erkennen und die automatische Listenaktualisierung ohne Schließen/erneutes Öffnen im getesteten Einzelspieleraufbau. Damit ist dev17 für diesen begleiteten Teststand abgeschlossen. Reale Chunkwechsel, Sondergeometrien und weitere Mehrspielerfälle bleiben offen.

### Neu in dev16: Verbindungen lösen – nach Ankündigung umgesetzt

- **Verbindung lösen:** ausgewähltes eigenes Gerät trennen, übrige Verbindungen erhalten. Steuerung stoppt.
- **Alle lösen:** fünf Sekunden gültige Bestätigung mit Abbrechen; anschließend alle Verbindungen/Anforderungen gelöst, Programm und Automatik aus.
- Getrennte regelnde Signale Rot, Taster und Warnblinklicht aus. Mitte und Radius bleiben erhalten.
- Bewusst getrennte Positionen bleiben auch nach dem Laden für normale automatische Suche gesperrt. Erneutes manuelles Verknüpfen gibt das einzelne Gerät frei.
- **Erneut erkennen:** Trennsperren bewusst freigeben und vorhandenen Bereich automatisch neu erfassen; anschließend prüfen und starten.
- Eine gespeicherte Verbindungsfassung entwertet beim vollständigen Lösen auch alte Endpunkte, die erst später geladen werden. Keine Chunk-Zwangsladung.

**Achtzehn Servertests bestanden.** Neue Tests umfassen alle Gerätetypen, gespeicherte Trennsperren, Bestätigung/Abbruch/Zeitablauf, alte wiederhergestellte Endpunktdaten, fremde Verbindungen und bewusstes Wiederverbinden. Das Wiederladen alter Endpunktdaten wurde per NBT simuliert; reale Chunkwechsel und weitere Mehrspielerfälle bleiben offen. Die Bedienung der Trenn- und Neuerkennungsfunktionen wurde anschließend im dev17-Spieltest bestätigt.

Der Ausbau wurde vor der Umsetzung hier angekündigt und anschließend innerhalb der erteilten Freigabe gebaut. Es gibt weiterhin keine öffentliche GitHub-Test-JAR.

Weitere Ansichten, Neustart-/Mehrspielertests und der vollständige Ausbau bleiben offen.

## Beta-1-Vorbereitung – Webhandbuch und NeverGate-Angaben

- **[Webhandbuch und Katalog](https://nevergate.de/ng-verkehr/)** veröffentlicht und auf der NeverGate-Startseite verlinkt. Deutsch/Englisch umschaltbar, elf Kategorien und Suche.
- Alle **98 Gegenstände** mit Bildern aus den tatsächlichen Modmodellen/-texturen, Nutzungshinweisen und Rezepten aus den Moddateien. Rezeptdialoge und passende Anleitungslinks.
- **14 zweisprachige Anleitungen**, unter anderem zu Installation, Aufbau, Verknüpfung/Automatik, Fußgängern, Programmen/Zeiten, Parallelbetrieb, Nachtbetrieb, Baustellen, Leuchten, Anzeigen und Fehlersuche.
- Vollständige PDFs mit je **35 Seiten**: [Deutsch](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-DE.pdf) · [English](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-EN.pdf). Alle Seiten gerendert und visuell kontrolliert, Inhaltsübersicht und Schriftlizenzhinweis enthalten.
- Autor und Rechtehinweis im Modmenü auf **NeverGate** präzisiert; Website-, Discord- und Handbuchverweise ergänzt. Bestehendes All Rights Reserved bleibt erhalten; keine neue offene/Verkaufslizenz und keine Änderung an Fremdrechten.
- Vormerkung gemeinsam umgesetzt: Fußgänger-Anforderungstaster im Inventarfeld angehoben und zentriert. Weltmontage und Schaltlogik unverändert.

**Technischer Umfang:** reine Ressourcenfassung auf Basis der bereits reobfuskierten dev20-JAR. Ausschließlich Modmetadaten, Manifest und Taster-Itemmodell geändert; sämtliche Java-Klassen bytegenau identisch. ZIP-/Ressourcen-/Metadatenprüfung bestanden. Die 25 Forge-Servertestergebnisse stammen aus dev20; kein neuer Gradle-Build oder Forge-Testlauf für die Beta-Ressourcenfassung behauptet.

**Teststand:** Nutzer bestätigt weiterhin alle Spielstände bis dev20. Neues Taster-Inventarsymbol und neue Metadaten noch im Client ansehen. Web-Sprachwechsel, Suche, Filter und Dialoge per DOM geprüft; eine echte Browser-Sichtprüfung des Layouts war hier nicht verfügbar. Zusätzliche reale Server-Neustart-, Chunk- und Mehrspielerprüfungen für das Basispaket bleiben ein separater offener Arbeitspunkt. Keine Änderung an MC02.

Öffentlich bereitgestellt sind Handbuch, Bilder und PDF-Ausgaben. Weiterhin kein öffentlicher GitHub-JAR- oder Quellcode-Upload.

## 0.1.0-dev20 – Getrennte Inventarreiter

Auf Nutzerwunsch den Sammelreiter durch **elf klar benannte NG-Reiter** ersetzt:

- Straßenbau
- Markierungen
- Schilder und Pfosten
- Gefahrzeichen
- Gebotszeichen
- Verbotszeichen
- Richtzeichen
- Zusatzzeichen
- Ampeln und Zubehör
- Baustelle
- Anzeigen

**Alle 98 Gegenstände (97 Blöcke plus Verknüpfer) sind genau einmal zugeordnet.** Schilderpfosten, Schildhalter und leere Tafeln stehen bei Schilder und Pfosten. Ampelmasten, Ausleger, Steuergerät, Verknüpfer und Taster stehen bei Ampeln und Zubehör. Mobile Baustellenampel, Warntechnik und gelbe Markierungen stehen bei Baustelle. Schildtafeln sind nach Zeichenart sortiert; Halt/Vorfahrt gewähren sind für diese praktische Inventaraufteilung bei Gebotszeichen eingeordnet.

Eigene passende Symbole, deutsche Reiternamen und feste Reihenfolge. Minecraft kann zusätzliche Reiterseiten anzeigen; die normalen Seitenpfeile bleiben nutzbar. Block-/Itemkennungen und bestehende Weltbauteile bleiben erhalten.

Build/Reobf, Ressourcenprüfung und **alle 25 vorhandenen Forge-Servertests bestanden**. Die Zuordnungsprüfung umfasst Vollständigkeit, Dopplungen, leere Reiter, Symbole und Übersetzungen. **Clienttest bis einschließlich dev20 vom Tester bestätigt; dies umfasst auch die zuvor offenen dev18-/dev19-Spieltests und die Inventarreiter.** Weiterhin keine öffentliche GitHub-JAR oder Quellcodeveröffentlichung.

## 0.1.0-dev19 – Baustellenausstattung

**Nach Ankündigung im Projektgespräch und Freigabe als Testfassung umgesetzt. dev18 und dev19 wurden zusammen mit allen bisherigen Entwicklungsständen bis dev20 vom Tester bestätigt.**

- **97 Bauteile insgesamt**, davon 23 neue: zwei Leitbaken, Absperrschranke, Bauzaunsegment, Standplatte, Baustellenleuchte, Richtungspfeiltafel, mobile Ampel, fünf Schildentwürfe und zehn gelbe Markierungen.
- Rot-weiße Baken und Schranke mit Standfüßen; ausrichtbare Gittersegmente für den Bauzaun. Zwei Segmente übereinander ergeben einen hohen Zaun.
- **Baustellenleuchte** ohne Steuergerät: Rechtsklick wechselt Blinken → Aus → Dauerlicht → Blinken.
- **Richtungspfeiltafel** mit blinkenden gelben LED-Pfeilen und Standfuß: Rechtsklick wechselt Links → Rechts → Aus → Links. Links/rechts von der Vorderseite aus gesehen.
- **Mobile Baustellenampel** mit zwei echten Blockpositionen, Standfuß und Ampelkopf. Verknüpfen am Kopf oder Fuß; genau ein Eintrag im Steuergerät. Automatische Erkennung, Menüzuordnung, Nachtbetrieb, Trennen/Neuerkennen und bestehendes Baustellenwechselprogramm nutzbar. Ein Abbauen entfernt beide Teile und liefert eine vollständige Ampel.
- Eigene Schildentwürfe für Bauarbeiten, Engstelle, Links-/Rechtsvorbeifahrt und Ende der Beschränkungen; geformte Konturen und transparente Inventargrafiken. Amtliche Detailprüfung bleibt offen.
- Gelbe Doppellinie, Kurzstriche, fünf Pfeilvarianten, Sperrfläche und zwei Leitlinien. Bisherige gelbe Linien bleiben vorhanden; Markierungen sind volle Fahrbahnblöcke.
- **Build/Reobf, Ressourcenprüfung für 97 Bauteile und alle 25 Forge-Servertests bestanden.** Neu geprüft: reale Blinkwechsel, Dauerlicht/Aus, Rechtsklickmodi und Baurechte, Modus-/Richtungsdaten, Kopf-Verknüpfung, automatische mobile Erkennung, Wechselverkehr, Trennen und Einzelbeute beim Abbauen. Bisherige Phasentests bleiben unverändert.

**Kleiner Test ohne Kreuzung:** zwei mobile Ampeln gegenüber an einer Engstelle aufstellen, verbinden und im Steuergerät „Baustellenwechsel“ wählen. Nord/Ost = A, Süd/West = B; gegenüberliegende Enden müssen verschiedene Gruppen haben. Fahrzeuggrün zum Test auf 5 Sekunden stellen, prüfen und starten. Baken, Schranke, Zaun, Schilder und Leuchten ergänzen. Leuchte/Pfeiltafel direkt am Bauteil bedienen; sie benötigen keine Verknüpfung und erscheinen nicht in der Geräteliste.

**Clienttest nachträglich bestätigt:** Der Tester meldet sämtliche bisherigen Entwicklungsstände bis einschließlich dev20 als bestätigt. Die vorherige offene Testliste für dev18/dev19 ist damit im begleiteten Spieltest abgeschlossen. Keine Fahrzeug-KI oder automatische Analyse tatsächlich gebauter Fahrwege.

Weiterhin Entwicklungsdokumentation auf GitHub; keine öffentliche Test-JAR und kein öffentlicher Quellcode-Upload.

## 0.1.0-dev18 – Straßenbau und Kreuzungsprogramme

**Nach Ankündigung und Freigabe umgesetzt. Clienttest anschließend zusammen mit allen bisherigen Entwicklungsständen bis dev20 vom Tester bestätigt.**

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

**Clienttest nachträglich bestätigt:** Die Rückmeldung des Testers umfasst alle bisherigen Entwicklungsstände bis einschließlich dev20. Die zuvor offene dev18-Testliste ist damit im begleiteten Einzelspieler-Teststand bestätigt. Weiterhin keine öffentliche GitHub-Test-JAR und kein öffentlicher Quellcode-Upload.

## Technik und Testversionen

| Voraussetzung | Aktueller Entwicklungsstand |
|---|---|
| Spiel | Minecraft Java 1.20.2 |
| Modloader | Forge 48.1.0 als bisherige Testbasis |
| Java | 17 |
| Mod-ID | `ng_verkehr` |

Einzelspieler- und Zwei-Spieler-Servertests der Beta 1 sind vom Nutzer bestätigt. Testfassungen können sich noch ändern. Vor dem Austausch einer Test-JAR Minecraft schließen und die vorherige Fassung entfernen, damit nicht zwei Versionen gleichzeitig geladen werden.

Dieses Repository enthält zunächst die **Projektankündigung und Entwicklungsdokumentation**. Die öffentliche Beta-JAR und das Downloadpaket stehen unter [Releases](https://github.com/InDance2018/NG-Verkehrs-System/releases/tag/v0.1.0-beta1) bereit. Der Mod-Quellcode wird hier weiterhin nicht veröffentlicht.

## Entwicklung verfolgen und Fehler melden

- [Entwicklungsplan](ROADMAP.md)
- [Versionsübersicht](CHANGELOG.md)
- [Fehler und Vorschläge](https://github.com/InDance2018/NG-Verkehrs-System/issues)

Bei Fehlern bitte die Mod-Version, Minecraft-/Forge-Version, eine kurze Beschreibung und die Schritte zum Nachstellen angeben. Ein Screenshot oder der relevante Log-Ausschnitt hilft bei der Prüfung.

Ein Veröffentlichungstermin für die fertige Mod steht noch nicht fest. Neue Entwicklungsstände werden hier dokumentiert.

---
**Ein Projekt von NeverGate · GitHub: [InDance2018](https://github.com/InDance2018)**
