# NG-Verkehr · Planung Beta 2 dev9

Bauauftrag vom 16. Oktober 2026, auf Basis dev8. Status: als private dev9-Testfassung umgesetzt. 123/123 Servertests bestanden; zwei echte Clients haben jeweils alle 15 Bedienansichten geprüft. Echter Neustart und tatsächliche Chunk-Entladung/Rückkehr sind bestätigt. Die endgültigen Funktionsklassen entsprechen der Client-Testfassung.

1. Parkscheinautomat: Parkdauer wählen, Zahlung anfordern und gültigen Parkschein ausgeben.
2. Einfahrtsticket-Automat: personengebundenes Ticket mit Einfahrtszeit und Parkzone.
3. Parkhaus-Kassenautomat: Ticket prüfen und zeitabhängige Parkgebühr abrechnen.
4. Ausfahrtsticket-Leser: bezahltes Ticket oder gültige Dauerberechtigung prüfen und Schranke freigeben.
5. Tarifverwaltung: Stundentarif, Freiminuten und Tageshöchstpreis am Verkehrsrechner.
6. Dauerparker-Ausweis: wiederverwendbare, zonengebundene Zufahrtsberechtigung.
7. Reservierbarer Stellplatzbügel: Öffnung nur mit passender Berechtigung, mit Hindernisschutz.
8. Parkhaus-Etagenanzeige: freie Plätze je Etage beziehungsweise zugeordneter Parkzone.
9. Höhenkontrolle mit Vorwarnung und Ausweichhinweis.
10. Fußgänger-Bedarfsampel: Taster, Warteanzeige und sichere Freigabephase.
11. Akustischer Ampelgeber: Orientierungs- und Freigabeton.
12. Fahrradampel mit Anforderungssensor und eigener sicherer Freigabephase.
13. Dynamische Geschwindigkeitsanlage: Tempolimit abhängig von Stau, Baustellen- oder Tunnelsperrung.
14. Zuflussregelung für Auffahrten: dosierte Freigabe per eigener Ampel.
15. Zentrale Störungsanzeige: Verbindungs- und Sensorfehler am Verkehrsrechner.

## Wirtschaft und Zahlungen

Die Währung wird ausschließlich im eigenständigen **NG-Wirtschaftssystem** entwickelt. NG-Verkehr erzeugt keine Münzen, Scheine oder eigenen Kontostände. Diese Version stellt eine dokumentierte serverseitige Schnittstelle für Zahlungen bereit. Ohne angebundenen Zahlungsanbieter werden kostenpflichtige Vorgänge abgewiesen; ein ausstehender Zahlungsvorgang erzeugt kein bezahltes Ticket. Kostenlose Tarife bleiben nutzbar. Eine echte Integration kann erst mit einer verfügbaren, passenden Wirtschaftssystem-Implementierung bestätigt werden.

## Lieferung und Prüfung

Gebaut: private dev9-JAR für Minecraft 1.20.2 / Forge 48.1.0 / Java 17, deutsche Anleitung, gezielte Funktions- und Regressionstests. Persistenz, Rechte, Ticketvalidierung, Hindernisschutz und sichere Ampelphasen werden geprüft. Die öffentliche Downloadversion wird durch diesen Bauauftrag nicht automatisch geändert.


## Grenzen und Installation

Die Zahlungsanbieter-Schnittstelle ist implementiert. Eine echte Belastung über NG-Wirtschaftssystem bleibt bis zu dessen passender Anbindung offen; positive Beträge werden ohne Anbieter abgewiesen. Die Erkennung einer konkreten externen Fahrzeugmod ist nicht pauschal bestätigt. MC02 wurde durch diesen dev9-Bauauftrag nicht geändert. Private JAR-Prüfsumme (SHA-256): `28b31e41ffbf7053fafb86d129d151a66323e89382170e573a9f2495287b41a5`.
