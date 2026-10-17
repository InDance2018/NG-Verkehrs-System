# 0.1.0-beta2-dev2 – Sechs Aufgaben gemeinsam

**Testnachtrag vom 17.10.2026:** Der Projektinhaber bestätigt alle bisherigen DEV-Stände auf MC02 sowie den bestandenen Drei-Spieler-/Chunk-/Neustarttest. [Aktueller Testbericht](PRAXISTEST-2026-10-17.md). Ältere Testangaben im folgenden Dokument beschreiben den damaligen Prüfstand.


Private Entwicklungsfassung nach beta2-dev1. Alle sechs angekündigten Bereiche sind in dieser Testfassung enthalten. **Client-Sicht-/Bedienprüfung und Tests mit konkreten fremden Fahrzeugmods stehen noch aus.**

## 1. Schilderbrücken und Befestigungen

Neue Schilderbrückenstütze, Querträger und Schilderaufhängung. Stützen übereinander setzen und Querträger als modulare Brücke aneinanderbauen; mit Aufhängungen die Tafeln ergänzen. Alle Teile besitzen ausrichtbare Modelle und passende Auswahl-/Kollisionsformen.

## 2. Weitere Wegweisung

Beschriftbare Ausfahrtsschilder, Entfernungstafeln und Umleitungswegweiser. Leere Haupthand + Rechtsklick auf die zentrale Schildposition öffnet den bekannten Editor. Ausfahrt und Umleitung haben zwei Zielzeilen; die Entfernungstafel drei. Ziele, Entfernungen und Pfeile bleiben frei einstellbar und gespeichert. Noch keine vollständige amtliche Abdeckung aller Bauformen und Piktogramme.

## 3. Digitale Verkehrsanzeigen

Neue Wechselverkehrszeichen und Fahrstreifenanzeigen. Verfügbare gemeinsame Betriebsarten: Aus, Tempolimit, Stauwarnung, Fahrstreifen gesperrt (rotes Kreuz) und Fahrstreifen frei (grüner Pfeil). Tempolimits 20–130 km/h in Zehnerschritten. Die Anzeigen sind veränderbare Beschilderung; sie steuern keine Fahrzeug-KI und erzwingen keine Fahrgeschwindigkeit.

## 4. Gemeinsame Anzeigensteuerung

Neuer **Verkehrs- und Parkleitrechner** und eigener **Anzeigen-Verknüpfer**:

1. Rechner aufstellen und mit leerer Hand rechtsklicken.
2. Betriebsart/Tempolimit sowie bei Bedarf Parkplatzkapazität und Belegung einstellen; mit Fertig speichern.
3. Mit dem Anzeigen-Verknüpfer zuerst den Rechner anklicken, anschließend die gewünschten Anzeigen und Zähler.
4. Bis zu 256 Geräte innerhalb von 256 Blöcken in derselben Dimension verknüpfen.
5. Schleichen + Rechtsklick mit dem Anzeigen-Verknüpfer auf ein Endgerät löst dessen Verbindung.

Die Anzeigen übernehmen die gemeinsamen Werte etwa alle zehn Serverticks. Für unterschiedliche Anzeigengruppen separate Rechner verwenden. Gerätetexte und Parkplatznamen bleiben je Anzeige individuell.

Verbindungen und Daten werden gespeichert. Entfernte oder ersetzte Rechner werden nicht automatisch als alter Rechner übernommen: digitale Anzeigen gehen aus, Parkleitanzeigen zeigen OFFLINE. Es werden keine zusätzlichen Chunks geladen. Bei ungeladenem Rechner werden keine Durchfahrten gezählt.

Die bestehende Ampelsteuerung und der bisherige Ampel-Verknüpfer bleiben eigenständige Systeme.

## 5. Parkleitsystem

Neue **Parkplatzwegweiser** und **Parkleitanzeige** mit eigenem Namen/Ziel, freien Plätzen, Gesamtkapazität und Frei-/Besetztstatus. Parkplatzwegweiser besitzen zusätzlich einen einstellbaren Richtungspfeil.

Kapazität und Belegung werden am zugehörigen Rechner eingestellt, nicht an der Anzeige. Kapazität: 1–100.000; Belegung bleibt zwischen null und Kapazität. Manuelle Korrektur ist über das Rechner-Menü möglich. Die ältere Parkplatzanzeige aus Beta 1 ist ein anderes Bauteil; für dieses System die neuen Geräte verwenden.

## 6. Automatische Ein-/Ausfahrtszählung

Einfahrtszähler und Ausfahrtszähler mit demselben Parkplatzrechner verknüpfen. In oder unter der Fahrbahn platzieren; die Fahrzeuge müssen höchstens drei Blöcke über dem Sensor fahren. Die Fahrtrichtung muss zur Ausrichtung des Sensors passen: gezählt wird die Überfahrt von der Vorderseite zur Rückseite, entsprechend dem vorgesehenen Plattenpfeil. Die tatsächliche Pfeildarstellung bitte im Clienttest gegenprüfen.

Erkennungsbreite: drei Blöcke quer zur Fahrtrichtung. Ein Fahrzeug muss vor und nach der Mittellinie erfasst werden; bloßes Hineinstellen/Spawnen zählt nicht. Gegenverkehr zählt nicht. Doppelte gleiche Buchungen desselben Fahrzeugs werden am Rechner fünf Sekunden lang unterdrückt. Rückwärtsbewegungen und sehr schnelle Sprünge über den gesamten Erfassungsbereich sind kein vollständiges Verkehrsmodell.

**Erster Erkennungsumfang:**
- Boote und Loren.
- Von Spielern besetzte Fahrzeugentitäten der technischen Kategorie MISC.
- Zusätzlich über den Entity-Type-Tag `ng_verkehr:parking_vehicles` freigegebene Fahrzeugtypen.

Fußgänger werden nicht gezählt. Normale Reittiere zählen ohne ausdrückliche Tag-Freigabe nicht. Für unbesetzte Fahrzeuge aus Fremdmods kann der Tag per Datenpaket erweitert werden. Es gibt noch keinen Kompatibilitätsnachweis für eine konkrete Fahrzeugmod. Ein- und Ausfahrten müssen entsprechend getrennt eingerichtet werden; der Zähler erkennt keine Stellplatzbelegung ohne Durchfahrt.

## Inventar und Installation

Schilderbrücken und neue Wegweiser: **NG · Schilder und Pfosten**. Rechner, elektronische Anzeigen, Parkplatzgeräte, Sensoren und Anzeigen-Verknüpfer: **NG · Anzeigen**.

**114 Blöcke plus zwei Verknüpfer = 116 Gegenstände** in elf Inventarreitern. Gegenüber dev1: 13 neue Blöcke und ein neuer Verknüpfer.

Minecraft Java 1.20.2 / Forge 48.1.0 / Java 17. Alte NG-Verkehrs-JAR aus dem aktiven Testprofil entfernen und `ng_verkehr-0.1.0-beta2-dev2.jar` einsetzen. Auf Testserver und Clients dieselbe Fassung verwenden. Testwelten vorher sichern.

## Prüfstand

- Echter Forge-Build einschließlich Reobf erfolgreich.
- **37 von 37 Forge-Servertests bestanden**: bisherige 29 Tests plus acht neue Systemtests.
- Geprüft: gemeinsame Anzeigenwerte, Offline-Rückfall bei Rechnerersatz, Begrenzung/Doppelzählung, gespeicherte Zählerstände und Verknüpfungen, echte Bootsüberfahrt mit Vorwärts-/Rückwärtsprüfung, Fußgängerausschluss, manuelle Korrektur, Bearbeitungsrechte, veraltete Menüs, Manipulationsschutz zentraler Werte, Paketübertragung, Brückengeometrie und zusätzliche Wegweiser.
- Sechs bisherige Phasentests und zwei Daten-/Grenztests bestanden.
- Ressourcen, Modelle, Texturen, Rezepte, Beute, DE-/EN-Bezeichnungen und vollständige Inventarzuordnung geprüft.
- Modelle außerhalb des Spiels gerendert und betrachtet. Danach nur clientseitige Schriftgröße, P-Kennzeichnung und eindeutige Bezeichnung der Parkleitanzeige angepasst; abschließender Build erfolgreich.
- **Offen:** Sicht-/Bedienprüfung aller neuen Bauteile und Menüs im Minecraft-Client; konkrete Fahrzeugmod-Integration; reale Mehrspielertests der neuen Systeme. Speicherprüfungen dieses Ausbaus erfolgen über NBT-Rundlauf und Welt-/Blockdaten im automatisierten Serverlauf.

## Empfohlener kurzer Spieltest

1. Schilderbrücke sowie Ausfahrt, Entfernungstafel und Umleitung aufbauen und beschriften.
2. Zwei elektronische Anzeigen mit einem Rechner verbinden und alle fünf Betriebsarten einschließlich Tempolimit durchschalten.
3. Parkplatzwegweiser und Parkleitanzeige verbinden; Kapazität/Belegung am Rechner ändern und Frei/Besetzt prüfen.
4. Ein-/Ausfahrtszähler verbinden und mit Boot/Lore in beiden Richtungen testen; Fußgänger dürfen nicht buchen.
5. Welt verlassen und wieder betreten; gespeicherte Texte, Verbindungen, Modi und Belegung prüfen.
6. Ein Endgerät trennen bzw. den Rechner entfernen: keine grüne Freigabe und keine scheinbar aktuellen Parkplatzwerte ohne Verbindung.

Die öffentliche Version bleibt Beta 1. Das öffentliche Webhandbuch und die beiden PDFs dokumentieren weiterhin deren Umfang. Diese Dev erhält ihre eigene Testanleitung und wird nicht auf MC02 geladen.

## Prüfsumme der Test-JAR

SHA-256 (`ng_verkehr-0.1.0-beta2-dev2.jar`):

`959ea2f142b877e30b4f39c463440e55e30c70078bcaf1830d64bb372f881b0c`
