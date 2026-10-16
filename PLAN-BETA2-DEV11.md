# NG-Verkehr · Beta2 dev11 · Fertigstellung der 20 Betriebsgeräte

Bauauftrag: 17. Oktober 2026, Fortsetzung der 20 Bauten aus dev10. Basis: beta2-dev10. Status: als private Testfassung gebaut und mit NG-Fahrzeuge dev11 im unten genannten Umfang geprüft.

- Tatsächliche Fahrerzuordnung der NG-Fahrzeuge verwenden; normale Minecraft-Zuordnung genügt dort nicht.
- Vorhandene Kennzeichen, Besitzer, Tankwerte in Litern und Verschmutzung lesend anbinden.
- Kostenloses Tanken und Waschen über die vorhandenen Fahrzeugoperationen anbinden, mit Berechtigungsprüfung, Mengenbeleg, dauerhafter Transaktionssperre und Wiederholschutz.
- Gegenstelle vorerst auf den konkret geprüften Fahrzeuge-Stand begrenzen; unbekannte Versionen sichtbar ablehnen.
- Echte gemeinsame Prüfung mit einer isolierten Fahrzeuge-dev10-Testfixture. Fertigen Fahrzeuge-dev11-Build verwenden, ohne Fahrzeugquellcode zu verändern.
- Fehlende Elektroantriebs- und Wirtschaftsgegenstellen bleiben offen. Keine Starterbatterie als Traktionsakku verwenden, keine Ersatzwährung erzeugen.

Diese Fortsetzung ersetzt nicht die parallele Entwicklung von NG-Fahrzeuge. Die 35 Wirtschaftsvorschläge bleiben Planung. MC02-Installation und öffentliche Downloads sind nicht Teil dieses Bauauftrags.

Aktualisierung: Fahrzeuge dev11 ist inzwischen fertig; die Anbindung wird deshalb gegen den aktuellen Stand geprüft. Buslinien aus dem Fahrzeug werden ebenfalls berücksichtigt. Ältere oder unbekannte Fahrzeuge-Versionen werden nicht als kompatibel angenommen.

## Ergebnis

170/170 Servertests bestanden, darunter 14 konkrete Integrationstests; zehn Ablaufprogramme ebenfalls bestanden. Die Fahrzeuge-Test-JAR entspricht exakt der aktuellen dev11-JAR. Zwei echte Clients mit beiden Mods haben jeweils alle 20 Verkehrsbedienungen und die gemeinsame Einstellung geprüft. Ein echter Fahrzeug-Waschvorgang und dessen Abbruch wurden bestätigt. Native Fahrzeugwerte und gespeicherte Servicebelege überstehen echten Neustart und tatsächliche Chunk-Entladung/Rückkehr; Wiederholungen erzeugen keine zweite Leistung.

Geprüft sind Fahrer-/Besitzertrennung, Kennzeichenzufahrt, kostenloses Tanken/Waschen und Buslinien-Abgleich. Es wird damit kein vollständiger Zwei-Fahrer-Test aller Fahrzeuge-Funktionen behauptet. Elektroantrieb und Euro-Anbieter bleiben offen; kostenpflichtige Services gesperrt. MC02 und öffentliche Downloads wurden nicht geändert.

Verkehrs-JAR SHA256: `99b3a8300a34050f2e147ebb71235fe83b951ea6fd13880bd55fe658a72fbf9b`.
Fahrzeuge-dev11-JAR SHA256: `ee8e463506766783cf1a331e2ec5a70601a04d45d81a1fa5bb3e2839aa28f8ee`.
