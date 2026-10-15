# NG-Verkehrs-System – 0.1.0-beta1

Erste Beta auf Grundlage des bestätigten Entwicklungsstands **0.1.0-dev20**. dev20 ist die letzte Dev-Version.

**Minecraft Java 1.20.2 · Forge 48.1.0 · Java 17**

97 Blöcke und ein Verknüpfer, verteilt auf elf Inventarreiter: Straßenbau, Markierungen, Verkehrszeichen, Ampeln, Baustellenausstattung und Anzeigen.

## Installation

Die Datei `ng_verkehr-0.1.0-beta1.jar` in den Ordner `mods` der passenden Forge-Installation legen. Auf Client und einem gegebenenfalls verwendeten Testserver dieselbe Version verwenden. Alte NG-Verkehrs-JARs vorher aus dem aktiven Mods-Ordner entfernen.

## Handbuch und Downloads

- [Onlinehandbuch Deutsch / English](https://nevergate.de/ng-verkehr/)
- [PDF-Handbuch Deutsch](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-DE.pdf)
- [PDF manual English](https://nevergate.de/ng-verkehr/downloads/NG-Verkehrs-System-Handbuch-EN.pdf)
- [Lizenz im Wiki](https://github.com/InDance2018/NG-Verkehrs-System/wiki/Lizenz)
- [Lizenzdatei](https://github.com/InDance2018/NG-Verkehrs-System/blob/main/LICENSE.md)
- [NeverGate Discord](https://discord.gg/STQDCpnHRD)

## Änderungen gegenüber dev20 und Teststand

Lizenz 1.0 und Fremdlizenzhinweise beigelegt, NeverGate-Angaben und Handbuchverweise ergänzt sowie das Fußgängertaster-Inventarsymbol angehoben und zentriert.

Alle 35 Java-Klassen sind bytegenau identisch mit dev20. Ressourcen-, Metadaten- und ZIP-Prüfungen bestanden; alle 98 Gegenstände sind genau einem der elf Reiter zugeordnet. Die 25 dokumentierten Forge-Servertests und die Einzelspielerbestätigung stammen aus dev20. Für diese Ressourcenfassung wurde kein neuer Forge-Testlauf durchgeführt.

### Bestätigte Spieltests am 15. Oktober 2026

Vom Nutzer auf Grundlage eigener Tests bestätigt:

- Modangaben im Spiel geprüft und in Ordnung.
- Einzelspieler: Anlagen aufgebaut und verknüpft; nach Verlassen und erneutem Betreten der Testwelt vollständig vorhanden und funktionsfähig.
- Minecraft-1.20.2-Server mit zwei Spielern: Beide können unabhängig Ampeln bauen und verlinken; alle getesteten Ampeln laufen.
- Nach Verlassen und erneutem Beitreten bleiben Aufbauten und Verbindungen erhalten und laufen weiter.
- Zwei vollständige Server-Neustarts ohne Verlust der getesteten Anlagen oder Funktionen.
- Entfernungstest: 1.500 Blöcke wegteleportiert, zwei Minuten gewartet und zurückgekehrt; alles vorhanden und funktionsfähig. Eine tatsächliche Chunk-Entladung wurde dabei nicht durch Serverprotokollierung gemessen.

Diese Bestätigungen beziehen sich auf die getesteten Aufbauten und sind keine pauschale Zusage für jede Serverkonfiguration. Die Beta ist kein abgeschlossener Vollausbau. Vor Verwendung bestehende Welten sichern.

---

# NG-Verkehrs-System – Nutzungsbedingungen

**Lizenzfassung:** 1.0
**Stand:** 15. Oktober 2026
**Herausgeber:** Ingo Vetter unter dem Projektnamen NeverGate
**Website und Kontakt:** https://nevergate.de/ – Kontaktdaten im Impressum

© 2026 Ingo Vetter / NeverGate. Alle Rechte an den eigenen, geschützten Projektbestandteilen vorbehalten, soweit nachfolgend keine Erlaubnis erteilt wird.

### 1. Geltungsbereich

Diese Bedingungen gelten für die ausdrücklich mit dieser Lizenz bereitgestellten Beta-Versionen von NG-Verkehrs-System sowie die zugehörigen eigenen Modelle, Texturen und Dokumentationen.

NeverGate erteilt ein einfaches, nicht ausschließliches Nutzungsrecht im nachfolgend beschriebenen Umfang. Eine Übertragung des Urheberrechts erfolgt nicht.

### 2. Erlaubte Nutzung

Die Mod darf kostenlos heruntergeladen, installiert und im Einzelspieler sowie auf kostenlos zugänglichen privaten und öffentlichen Minecraft-Servern verwendet werden.

Notwendige Sicherungskopien und Anpassungen der vorgesehenen Konfigurationsdateien sind erlaubt.

Screenshots, Videos, Streams und Vorstellungen der Mod sind erlaubt, auch auf werbefinanzierten Kanälen. Die Darstellung darf keine offizielle Partnerschaft mit NeverGate vortäuschen.

### 3. Modpacks und Weitergabe

Die unveränderte Original-JAR darf in kostenlos angebotene Modpacks aufgenommen und zusammen mit diesen weitergegeben werden, sofern:

- NeverGate als Herausgeber genannt wird,
- ein Link zu https://nevergate.de/ng-verkehr/ enthalten ist,
- diese Lizenz und vorhandene Fremdlizenzhinweise beiliegen,
- keine Gebühren für die Mod oder ihren Download verlangt werden.

Eigenständige Downloadspiegel außerhalb solcher Modpacks benötigen die vorherige schriftliche Zustimmung von NeverGate. Das Verlinken auf die offiziellen Downloads ist erlaubt.

### 4. Änderungen und Übernahme von Bestandteilen

Die Veröffentlichung veränderter Modfassungen und die Übernahme von Code, Modellen, Texturen oder Dokumentation in andere Projekte benötigen die vorherige schriftliche Zustimmung des jeweiligen Rechteinhabers.

Urheber-, Herkunfts- und Lizenzhinweise dürfen nicht entfernt oder irreführend verändert werden. Die Mod darf nicht als eigene Entwicklung ausgegeben werden.

Gesetzlich zwingend erlaubte Handlungen bleiben unberührt.

### 5. Kommerzielle Nutzung

Der Verkauf der Mod, kostenpflichtige Downloads, die Weitergabe gegen Entgelt und kostenpflichtige Freischaltungen ihrer Funktionen werden durch diese Lizenz nicht gestattet.

Für den Einsatz auf kommerziell betriebenen Servern ist eine gesonderte schriftliche Erlaubnis erforderlich. Die in Abschnitt 2 erlaubten Videos und Streams bleiben hiervon ausgenommen.

Eine Erlaubnis von NeverGate ersetzt keine gegebenenfalls erforderlichen Erlaubnisse anderer Rechteinhaber.

### 6. Minecraft, Forge und Fremdbestandteile

NG-Verkehrs-System ist ein unabhängiges Projekt und kein offizielles Produkt von Mojang, Microsoft oder dem Forge-Projekt.

Rechte an Minecraft, Forge, verwendeten Bibliotheken, Schriftarten und sonstigen Fremdbestandteilen verbleiben bei den jeweiligen Rechteinhabern. Für diese Bestandteile gelten deren eigene Lizenzbedingungen.

Diese Lizenz beansprucht keine ausschließlichen Rechte an amtlichen Verkehrszeichen als solchen.

### 7. Beta-Status und Haftung

Beta-Versionen können Fehler enthalten. Die Bezeichnung „Beta“ enthält keine Zusicherung bestimmter Funktionen, vollständiger Kompatibilität oder dauerhafter Unterstützung.

Vor der Verwendung in bestehenden Welten wird eine Sicherung empfohlen. Ein Anspruch auf zukünftige Updates besteht ohne gesonderte Vereinbarung nicht.

Für Mängel und Schäden gelten die gesetzlichen Bestimmungen. Diese Bedingungen enthalten keinen pauschalen Ausschluss gesetzlicher Haftung.

### 8. Handbuch und PDFs

Das Handbuch und die PDF-Ausgaben dürfen für die eigene Nutzung kostenlos gelesen, gespeichert und ausgedruckt werden.

Links auf die offiziellen Ausgaben dürfen geteilt werden. Eine anderweitige Veröffentlichung vollständiger oder bearbeiteter Ausgaben benötigt die vorherige schriftliche Zustimmung, soweit sie nicht gesetzlich erlaubt ist.

### 9. Lizenzfassung und Sondererlaubnisse

Maßgeblich ist die der jeweiligen Modfassung beigefügte Lizenz. Spätere Änderungen gelten nicht automatisch rückwirkend für bereits unter dieser Fassung erteilte Rechte.

Weitergehende Erlaubnisse können schriftlich über die auf https://nevergate.de/ angegebenen Kontaktmöglichkeiten vereinbart werden.

