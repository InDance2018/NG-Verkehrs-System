# 0.1.0-beta2-dev1 – Wegweisung

Private Entwicklungsfassung nach der öffentlichen Beta 1. Zwei zusammengehörige Aufgaben gemeinsam umgesetzt:

1. Beschriftbare Ortseingangs-/Ortsausgangsschilder und einfache Straßenwegweiser.
2. Autobahnwegweiser mit mehreren Zielen und Fahrspuren.

## Neue Bauteile und Bedienung

Im Inventarreiter **NG · Schilder und Pfosten**:

- **Ortseingangsschild:** Ortsname und Zusatz/Kreis.
- **Ortsausgangsschild:** Ortsname und Zusatz/Kreis, mit rotem Schrägstrich.
- **Straßenwegweiser:** zwei Ziele, jeweils eigene Entfernung und Richtung.
- **Autobahnwegweiser:** drei Ziele mit Entfernung und Richtung; zusätzlich ein bis vier Fahrspuren mit einzeln wählbaren Pfeilen.

Leere Haupthand + Rechtsklick auf die Mitte des Schildes öffnet das Bearbeitungsmenü. Texte eingeben; Pfeile durch Anklicken wechseln; mit **Fertig** speichern. **Abbrechen/Escape** verwirft Änderungen. Tab wechselt zwischen den Eingabefeldern.

Bis zu 48 Zeichen pro Ziel und zwölf Zeichen je Entfernungsfeld. Umlaute werden unterstützt. Entfernungen sind frei beschriftbar, zum Beispiel „25 km“. Richtungspfeile: links, geradeaus, rechts, links schräg und rechts schräg. Die aktiven Fahrspuren werden von links nach rechts dargestellt.

Die Tafeln besitzen einen zentralen Block als Anker; ihre sichtbaren Flächen können seitlich/oben darüber hinausragen. Beim Aufbau entsprechend Platz lassen. Vorhandene Masten/Ausleger können als Befestigung verwendet werden. Die Auswahlfläche liegt am zentralen Ankerblock. Die Textdarstellung ist auf der Vorderseite; Rückseiten sind metallfarben.

Beschriftungen werden serverseitig gespeichert und an beobachtende Clients übertragen. Gleichzeitige Bearbeitungen überschreiben keine neuere Änderung: Beim Konflikt erscheint ein Hinweis; das Schild bitte erneut öffnen.

## Prüfung

- Echter Forge-Build einschließlich Reobf bestanden: Minecraft 1.20.2, Forge 48.1.0, Java 17, Gradle 8.1.1 / ForgeGradle 6.0.24.
- **Alle 29 Forge-Servertests bestanden**, einschließlich bisheriger 25 Tests und vier neuer Wegweiser-Tests.
- Neue Tests prüfen NBT-/Update-Daten, Unicode-Paketübertragung und Grenzen, veraltete Bearbeitungen, fehlende Baurechte, Entfernung, ersetzte Schilder, vier Schildtypen, Ausrichtung, Menü-Snapshots und Beute.
- Sechs bisherige Phasentests sowie ein neuer Beschriftungs-Datentest bestanden.
- **101 Blöcke plus Verknüpfer = 102 Gegenstände**, genau einmal über elf Reiter verteilt; Ressourcen-/Rezept-/Übersetzungsprüfung bestanden.
- Modelle und Texturen der vier Tafeln außerhalb des Spiels gerendert und betrachtet.
- Nach den Serverprüfungen wurden nur clientseitige Bedienungsdetails für Tab-Navigation und schmalere Fenster angepasst; abschließender Build bestanden.
- JAR-CRC, neue Klassen, Versionsmetadaten, enthaltene Lizenz und SHA256 geprüft.
- **Offen:** tatsächliche Sicht- und Bedienprüfung des neuen Menüs, der Textdarstellung und aller vier Ausrichtungen im Minecraft-Client. Kein bestandener Client-Spieltest für diese Dev behauptet.

JAR-SHA256: `5920fd463459182b7bb2e5e7a01e42ebe6d1dcb6744ce9e7c4873aa5877c831b`

## Installation und Grenzen

Alte NG-Verkehrs-JAR aus dem aktiven Testprofil entfernen, dann `ng_verkehr-0.1.0-beta2-dev1.jar` einsetzen. Für Mehrspieler diese Fassung auf Testserver und allen Clients verwenden. Nicht gleichzeitig mit Beta 1 laden. Bestehende Testwelten vorher sichern.

Die neuen Tafeln sind noch keine vollständige amtliche Autobahnwegweisung mit sämtlichen Piktogrammen, Anschlussstellen-/Autobahnnummern und Schildbauformen. Die Fahrspurpfeile sind beschriftbare Wegweisung, keine automatische Analyse der gebauten Fahrwege.

Das öffentliche Webhandbuch und die beiden PDFs dokumentieren weiterhin Beta 1 mit 98 Gegenständen. Die vier neuen Schilder sind in dieser Testanleitung beschrieben; eine öffentliche Handbuchaktualisierung folgt zur nächsten Beta.

Lizenz 1.0 liegt der JAR und dem Paket bei. Keine öffentliche Dev-JAR-Veröffentlichung und kein MC02-Upload.
