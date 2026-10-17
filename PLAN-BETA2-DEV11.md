# NG-Verkehr · Beta2 dev11 · Fortsetzung der 20 Betriebsgeräte

Bauauftrag vom 17. Oktober 2026. Basis: beta2-dev10. Status: gebaut und mit NG-Fahrzeuge 0.1.0-dev11 im beschriebenen Umfang geprüft.

Die 20 Geräte werden durch die konkrete Fahrzeuganbindung ergänzt: tatsächlicher Fahrer und Besitzer, vorhandene Kennzeichen, Tankinhalt und Kapazität, Verschmutzung sowie Buslinien. Kostenloses Tanken und Waschen verwenden die vorhandenen Fahrzeugoperationen mit Berechtigungsprüfung, dauerhaftem Vorgangsbeleg und Wiederholschutz.

Kennzeichenfreigaben sind an Fahrzeug-UUID und Besitzer-UUID gebunden. Ein anderes Fahrzeug mit kopiertem Kennzeichen erhält keine Zufahrt. Bei der Registrierung muss das tatsächliche Fahrzeug am Leser stehen; alte ungebundene Listen aus dev10 müssen neu registriert werden.

## Prüfung

171/171 Servertests bestanden, darin 15 gezielte Integrationstests; außerdem zehn Ablaufprogramme. Die Fahrzeuge-Test-JAR entspricht exakt der aktuellen dev11-JAR. Zwei echte Clients mit beiden Mods haben jeweils alle 20 Verkehrsbedienungen und die gemeinsame Einstellung geprüft. Ein echter Fahrzeug-Waschvorgang und dessen Abbruch wurden bestätigt. Native Fahrzeugwerte und gespeicherte Servicebelege überstehen echten Neustart und tatsächliche Chunk-Entladung/Rückkehr; wiederholte Vorgänge führen keine zweite Leistung aus.

Die ausgelieferte Fassung enthält dieselben funktionalen Klassen und Ressourcen wie die Client-Testfassung; Testklassen und Fahrzeugfixture sind ausgeschlossen. Geprüfter Umfang: Fahrer-/Besitzertrennung, gebundene Kennzeichenzufahrt, kostenloses Tanken/Waschen und Buslinien-Abgleich. Dies ist kein vollständiger Zwei-Fahrer-Test aller Fahrzeugfunktionen.

Nur Fahrzeuge dev11 ist für diese konkrete Anbindung freigegeben. Elektroantrieb und Wirtschafts-Anbieter bleiben offen; eine Starterbatterie wird nicht als Traktionsakku behandelt. Kostenpflichtige Services bleiben gesperrt. Die 35 Wirtschaftsvorschläge bleiben Planung. MC02 und öffentliche Downloads wurden nicht geändert.

Verkehrs-JAR SHA256: `f2bcacfc1619f08ca91251813d1f194fb2c844f8cfec5f48b1fa70c7325475a6`.
Fahrzeuge-dev11-JAR SHA256: `ee8e463506766783cf1a331e2ec5a70601a04d45d81a1fa5bb3e2839aa28f8ee`.
