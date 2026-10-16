# 0.1.0-beta2-dev3 – 163 zusätzliche Verkehrszeichen

Private Entwicklungsfassung auf Basis von beta2-dev2. Die Schilder erhalten auf Nutzerwunsch Vorrang vor den zwölf weiteren Ausbauvorschlägen. Der Umfang wurde vor der Umsetzung in [PLAN-BETA2-DEV3.md](PLAN-BETA2-DEV3.md) angekündigt.

## Umfang

**163 neue feste Schildvarianten; insgesamt 181 feste Schildvarianten.** Die Mod enthält jetzt **277 Blöcke plus zwei Verknüpfer = 279 Gegenstände** in den bisherigen elf Inventarreitern. Die sieben beschriftbaren Wegweisertypen aus dev1/dev2 und alle bisherigen Systeme sind weiter enthalten.

- **65 Gefahrzeichen:** Steigungen/Gefälle von 4 bis 25 Prozent in beiden Richtungen (44 Varianten), Kreuzung/Einmündung, Links-/Rechts- und Doppelkurven, unebene Fahrbahn, Rutschgefahr, Seitenwind, einseitige Engstellen, Stau, Gegenverkehr, Lichtzeichenanlage, Fußgänger, Kinder, Radverkehr und Bahnübergang.
- **48 Verbotszeichen:** fehlende feste Tempolimits 5/10/20/40/60/70/90/110/130 km/h, Ende-Schilder für alle 14 Tempolimits, Fahrzeug-/Fußgängerverbote, Wendeverbot, Überholverbote mit Aufhebung sowie Haltverbote mit Anfangs-/End-/Mittevarianten für beide Straßenseiten.
- **42 Schilder im Gebotsreiter:** Mindestgeschwindigkeiten und deren Ende von 10 bis 130 km/h in Zehnerschritten, Fahrtrichtungsgebote, Kreisverkehr, Rad-/Gehwege und Einbahnstraßen.
- **8 Richtzeichen:** Ende der Vorfahrtstraße, Vorrang vor Gegenverkehr, Fußgängerüberweg beidseitig, Autobahn/Kraftfahrstraße jeweils mit Ende.

Eigene gezeichnete Piktogramme, Kreis-/Dreieck-/Rechteck-/Rautenformen mit Metallrückseite und schmaler Kante. Bedruckte Inventarsymbole, deutsche und englische Namen, eindeutige Registrierungen, Steinmetzrezepte, Beute und Spitzhacken-Zuordnung sind enthalten.

## Verwendung

Minecraft Java **1.20.2**, Forge **48.1.0**, Java **17**. Die JAR aus `mods/` auf Client und Testserver einsetzen; die vorherige NG-Verkehrs-JAR aus dem aktiven Testprofil entfernen. Vorhandene Testwelten sichern.

Die neuen Schilder stehen in **NG · Gefahrzeichen**, **NG · Gebotszeichen**, **NG · Verbotszeichen** und **NG · Richtzeichen**. Im Steinmetz wird aus einer **unbeschrifteten Schildtafel** jeweils ein Schild gefertigt. Beim Setzen richtet sich die bedruckte Seite zum Spieler aus. Die festen Schilder erhalten keine Texteingabe; frei beschriftbare Wegweiser bleiben eigene Bauteile. Die Zeichen sind Beschilderung und erzwingen keine Fahrzeug-KI-Regeln.

## Prüfstand

- Forge-Kompilierung und reobf-JAR erfolgreich; keine bloß umbenannte Vorgänger-JAR.
- **38 von 38 erforderlichen Forge-Servertests bestanden.** Die bisherigen 37 Tests bleiben erfolgreich. Der neue Test prüft alle 163 zusätzlichen Registrierungen/Items in vier horizontalen Ausrichtungen einschließlich Drehung, Auswahlform und Blockzustands-NBT-Rundlauf.
- Ressourcenprüfung bestanden: alle 277 Blöcke/279 Items genau einmal in elf Reitern; Modelle/Texturen, DE-/EN-Namen, Rezepte, Beute und Spitzhacken-Tag.
- Generator erneut ausgeführt: identisches Ergebnis, keine doppelten Inventareinträge.
- Zeichennummern gegen die Komplettübersicht 04/2025 abgeglichen; numerische Untervarianten von 275/279 folgen deren allgemeiner Zahlenregel.
- Alle 163 Texturen in einer Gesamtübersicht visuell geprüft; Details zu Aufhebungsstreifen, Schildrändern, Kraftfahrstraße und Haltverbotspfeilen separat nachgebessert/geprüft.
- Finale JAR: CRC/Version und SHA-256 geprüft; sämtliche nicht vom Build expandierten Quelldateien unter Ressourcen bytegenau mit der JAR verglichen.

**Offen:** Sichtprüfung der Modelle im Minecraft-Client, reale Bedien-/Inventar-/Rezeptprüfung, Welt-Neuladen und Mehrspielerprüfung dieser Fassung. Der NBT-Test ersetzt keinen tatsächlichen Server-Neustart. Die Grafiken sind eigene stilisierte Modgrafiken, keine geprüften normgerechten Fertigungsvorlagen.

## Kurzer Spieltest

1. Je ein rundes, dreieckiges, rechteckiges und rautenförmiges Schild aus den vier Reitern setzen.
2. Alle vier Blickrichtungen sowie Vorderseite, Metallrückseite und Kanten ansehen.
3. Zahlenlesbarkeit bei Tempolimits/Steigungen und Pfeile der Haltverbotsvarianten prüfen.
4. Aus einer unbeschrifteten Schildtafel im Steinmetz ein neues Zeichen fertigen; setzen und abbauen.
5. Welt verlassen und wieder betreten; Position und Ausrichtung kontrollieren.
6. Bestehende Ampel-, Wegweiser- und Parkleitanlagen kurz mitprüfen.

## Katalog und weitere Arbeit

Referenz: [Komplettübersicht VzKat 04/2025](https://www.vzkat.de/2017/Anhang/Anhang.htm), lokal nachvollziehbar unter `docs/reference/vzkat-2025-overview.json`. **1.152 eindeutige Nummern** nach Zusammenführen mehrfach genannter Nummern; einschließlich Obernummern, Varianten, Markierungen und Verkehrseinrichtungen. Das ist keine Zahl notwendiger Einzelblöcke. Deshalb wird aus 181 festen Schildvarianten keine pauschale Restzahl abgeleitet. Die vollständige Zuordnung aller vorhandenen Systeme und der weitere Ausbau fehlen noch. Insbesondere Zusatzzeichen, viele Fahrzeug-/Gewichts-/Maßverbote, weitere Piktogramme und spezielle Wegweisungs-/Verkehrslenkungstafeln sind noch nicht vollständig.

Die öffentliche Downloadfassung bleibt Beta 1. Webhandbuch und beide öffentlichen PDFs dokumentieren weiterhin Beta 1. Private Dev-Fassung, kein MC02-Upload und kein öffentlicher Dev-JAR-/Quellcode-Upload.

## Prüfsumme

`ng_verkehr-0.1.0-beta2-dev3.jar` – SHA-256:

`d4ababa8d2a5e60e8977a528298b1a4b2cacf6cd377f8bd22d5ff28d9b37da9e`
