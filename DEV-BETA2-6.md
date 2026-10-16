# 0.1.0-beta2-dev6 – Parksystem-Paket mit elf Arbeitspunkten

Private Testfassung vom 16. Oktober 2026 für Minecraft Java **1.20.2 / Forge 48.1.0 / Java 17**. **281 Blöcke plus zwei Verknüpfer = 283 Gegenstände**, elf Inventarreiter. Schilderausbau bleibt bei **181 festen Schildvarianten** pausiert.

## Status aller elf freigegebenen Punkte

| Nr. | Aufgabe | Ergebnis |
|---|---|---|
| 1 | Konkrete Fahrzeugmod prüfen | Filter und Diagnosebefehl vorhanden; echter Fremdmod-Kompatibilitätstest offen, da noch keine konkrete Fahrzeugmod festgelegt/nachgewiesen ist. |
| 2 | Rechner-Menü und Verbindungsstatus live | Umgesetzt; mit zwei verbundenen Minecraft-Clients geprüft. Ungespeicherte Eingaben bleiben erhalten. |
| 3 | Automatische Anzeigeumschaltung | Umgesetzt und servertestgeprüft: frei/gesperrt nach Parkplatzbelegung. |
| 4 | Darstellung und Bedienung im Client | Zwei echte Minecraft-Clients gestartet; Rendering, Widgetgrenzen, Live-Menü und Höhenbedienung automatisiert geprüft. Testbilder erzeugt; abschließende visuelle Sichtung dieser Bilder bleibt offen. |
| 5 | Echter Serverneustart | Bestanden: gespeicherte Zähler, Filter, Verbindungen und Höhenwerte nach vollständigem Prozessneustart wiederhergestellt. |
| 6 | Tatsächliche Chunk-Entladung | Bestanden: entfernte Zone wirklich entladen, Offline-Status weitergegeben und nach Nachladen korrekt wiederhergestellt. |
| 7 | Mehrspielerbetrieb | Im isolierten Test mit zwei gleichzeitig verbundenen Minecraft-Clients bestanden: Live-Werte, geschützte veraltete Klicks und Speichern mit anschließendem Update des zweiten Clients. |
| 8 | Einzelne Stellplätze überwachen | Umgesetzt und servertestgeprüft; eigener Stellplatzsensor und wählbarer Zählmodus. |
| 9 | Automatische Einfahrtsschranke | Umgesetzt und servertestgeprüft; Belegung, Fahrzeugfilter, Höhe, Kollision und Hindernisschutz. |
| 10 | Einstellbare Höhenanzeige | Umgesetzt; lokale Werte 1,5–6,5 m. Änderung über Client/Server und Übernahme beim zweiten Client geprüft. |
| 11 | Wegweisung zu freien Parkzonen | Umgesetzt und servertestgeprüft; feste Zielzone oder automatische Auswahl einer freien Zone. |

Damit sind neun Punkte im beschriebenen Umfang umgesetzt/geprüft; Punkt 1 und die abschließende visuelle Prüfung von Punkt 4 bleiben teilweise offen. Automatisierte Tests gelten nicht als allgemeine Freigabe aller Fahrzeugmods oder sämtlicher Spielaufbauten.

## Bedienung

### Stellplätze

Neues Bauteil **Stellplatzsensor** unter **NG · Anzeigen**. Den flachen Sensor unter den vorgesehenen Mittelpunkt des geparkten Fahrzeugs setzen und mit dem Anzeigen-Verknüpfer an den Zonenrechner anschließen.

Im Rechner mit leerer Hand rechtsklicken → **Parkoptionen** → **Zählung: einzelne Stellplätze** → Fertig. Kapazität entspricht der Zahl zugeordneter Sensoren; belegte Sensoren bestimmen die Belegung. Ein- und Ausfahrtszähler buchen in diesem Modus nicht zusätzlich. Für die bisherige Zufahrtszählung **Zählung: Ein-/Ausfahrten** wählen.

Der Sensor ordnet die Bodenposition der Fahrzeug-Wurzelentität genau einem Bodenblock zu. Grün = frei, Rot = belegt. Er übernimmt den Fahrzeugfilter seines Zonenrechners. Kein Sensor bzw. ein nicht erreichbarer zugeordneter Sensor macht die Zone im Stellplatzmodus offline; es werden keine fehlenden Chunks nachgeladen.

### Schranke und Höhenanzeige

Neue **Automatische Parkschranke** mit dem Rechner der eigenen Zone verbinden. Sie öffnet für erkannte Fahrzeuge bei freien Plätzen und passender Höhe. Bei voller oder nicht erreichbarer Zone öffnet sie nicht regulär. Ein Hindernis im Schließbereich hält eine offene Schranke offen; kurze Nachlaufzeit verhindert Flackern. Die geschlossene Schranke hat eine echte Kollision. Ein Element belegt einen Block; für breitere Einfahrten mehrere Elemente passend aufstellen und demselben Rechner zuordnen.

Schranke mit leerer Hand öffnen → **Parkoptionen** → maximale Höhe einstellen. Klick erhöht um 0,1 m; nach 6,5 m beginnt die Auswahl bei 1,5 m. Berücksichtigt werden physische Entity-Höhe und mitfahrende Passagiere. Fremdmods mit abweichenden Physikobjekten oder unpassenden Bounding-Boxen müssen konkret geprüft werden.

Die neue **Einstellbare Höhenanzeige** zeigt einen lokal einstellbaren Wert und benötigt für die reine Anzeige keinen Rechner. Den gleichen Grenzwert separat an der Schranke einstellen. Die Tafel erzeugt keine unsichtbare Höhenbarriere.

### Automatische Verkehrsanzeigen

Am Zonenrechner unter **Parkoptionen** die automatische Umschaltung einschalten. Verbundene Wechselverkehrszeichen zeigen bei freien Plätzen das eingestellte Tempolimit, bei voller Zone die Sperrung. Fahrstreifenanzeigen wechseln zwischen grünem Pfeil und rotem Kreuz. Bei fehlender Verbindung erscheinen keine irreführenden Freigaben.

### Zonenwegweisung

Parkplatzwegweiser mit einem **Parkzonen-Sammelrechner** verbinden. Unter **Parkoptionen** zwischen Gesamtsumme, automatisch freier Zone und einer festen Zone wechseln. Die automatische Auswahl nimmt die erste freie Zone in Verknüpfungsreihenfolge; sind alle Zonen voll, erscheint eine Besetztanzeige. Fehlen Zonen der Gesamtsicht, bleibt die Wegweisung konservativ offline.

Name und Restplätze folgen der ausgewählten Zone. Pfeile richten sich nach der Lage ihres Rechners zum Schild, einschließlich eines Rückwärtspfeils für Ziele hinter dem Schild. Dies ist keine Berechnung tatsächlich gebauter Straßenrouten. Bei Gesamtsumme bleibt der eigene Pfeil einstellbar.

### Fahrzeugdiagnose

Operatorbefehl: `/ngverkehr vehiclecheck X Y Z`. Einen Parkrechner oder ein verbundenes Gerät angeben. Der Befehl zeigt bis zu 16 nahe Entitäten mit echter Registry-ID, Filterergebnis, Wurzelstatus und Entity-Höhe sowie nicht registrierte Einträge der Fahrzeugliste. Damit lässt sich eine künftig gewählte Fahrzeugmod gezielt prüfen; der Befehl ist kein vorweggenommener Kompatibilitätsnachweis.

## Prüfung

- **63/63 Forge-Servertests bestanden**, einschließlich elf neuer Ausbauprüfungen.
- Alle acht vorhandenen deterministischen Testprogramme bestanden.
- Build/Reobf erfolgreich; **1.519 JSON-Dateien**, Modelle, Texturverweise, Rezepte, Beute, Übersetzungen und vollständige Inventarzuordnung geprüft.
- Isolierter dedizierter Forge-Server tatsächlich beendet und mit derselben gespeicherten Welt neu gestartet; gespeicherte Werte und Verbindungen korrekt.
- Entfernte Zone tatsächlich entladen; Sammler/Tafel anschließend offline; nach erneutem Laden korrekte Zähler und Verbindung.
- Zwei unabhängige Minecraft-Clients gleichzeitig am isolierten Server verbunden. Live-Belegung, Erhalt nicht gespeicherter Namenseingaben, Ablehnung veralteter Entfernungsanfragen, Höhenänderung über echtes Netzwerk und Übernahme im zweiten Client bestanden.
- Client-Menü bei 1280 × 800 und GUI-Skalierung 2 ohne außerhalb des Fensters liegende Widgets geprüft; Welt und Menüs gerendert, Testbilder erzeugt. Abschließende visuelle Auswertung dieser Bilder noch offen. Die Testumgebung hatte keine Audioausgabe; Audiofunktionen waren nicht Gegenstand dieser Prüfung.

Die echte Chunk-Prüfung führte zu einer Korrektur: Fernzugriffe verwenden jetzt ausschließlich bereits geladene Chunks und erneuern keine Ladetickets durch normale Rechnerabfragen.

## Installation

Alte NG-Verkehrs-JAR aus dem privaten Testprofil entfernen, neue JAR auf Clients und Testserver verwenden und vorher die Testwelt sichern. Alle Beteiligten benötigen dieselbe dev6; das Netzwerkformat wurde erweitert.

Öffentliche Downloadfassung bleibt Beta 1. Keine Installation auf MC02 durch diesen Auftrag. Private Dev-JAR und Quellcode werden nicht im öffentlichen Repository veröffentlicht.
