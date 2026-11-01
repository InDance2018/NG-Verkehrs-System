# Originalquellstand: 0.1.0-beta2-montage2

Stand: 01.11.2026.

## Wiedergefunden und veröffentlicht

Der ursprüngliche Projektbaum wurde wiedergefunden, nicht aus einer JAR dekompiliert. Die Versionszuordnung wurde gegen build.gradle, den ursprünglichen Paketierungsschritt und die gespeicherte Release-Prüfung abgeglichen.

[Originalquell-Snapshot herunterladen](source/NG-Verkehr-0.1.0-beta2-montage2-source-reviewed.zip)

- 3.620 unveränderte Originaldateien: 102 Haupt-Java-Dateien, 3.499 Originalressourcen, 10 Java-Testdateien, vier zusätzliche QA-Java-Dateien, drei Gradle-Konfigurationsdateien sowie zwei ausgewählte Werkzeuge.
- ZIP: 3.154.638 Bytes.
- SHA-256: `538e84b5f13b83d6b578d787cce81d795911e2b337a70b40f1c98a8b65af52ec`.
- 128 Java-/Build-/Lizenz-/Textdateien zusätzlich unter source/ direkt lesbar. Diese lesbare Auswahl allein ist kein vollständiger Buildbaum: dafür das ZIP entpacken.
- Originalressourcen einschließlich Modelle, Texturen, Sprachdateien und zwei Teststrukturen sind enthalten. Keine Benutzerwelten oder Laufzeitdaten.
- Nicht enthalten: Buildausgaben, Caches, Logs, lokale Betriebsskripte, ältere Patchgeneratoren, Gradle-Wrapper-Binärdatei und externe Entwicklungsfixture von NG-Fahrzeuge. Dies ist ein bewusst begrenzter Quell-Snapshot, kein vollständiges Abbild der Entwicklungsmaschine.

## Nachweise und Prüfgrenzen

Archivintegrität und SHA-256 nach Übertragung geprüft; Textquellen auf gängige Geheimnisformate und interne Dateipfade geprüft. Kein entsprechender Fund. Das ist keine formale Sicherheitsgarantie.

Die gespeicherte ursprüngliche Release-Prüfung ordnet Montage2 der Client-JAR mit 3.209.054 Bytes und SHA-256 `2e56fc97624f5366e7c12ccb01227e4291689345b097facd4c9f20c6a759614c` zu. Sie meldet sechs GameTests und Modell-/Auswahlflächenprüfungen. Das sind historische Nachweise, keine heute wiederholten Tests und kein Nachweis eines bitidentischen Neubuilds.

Am 01.11. wurde der lokale Aufruf von tools/test_cycle.sh versucht; er konnte wegen fehlendem javac nicht starten. Daher werden keine neu bestandenen Java-/Forge-/Clienttests behauptet. Java 17 und Gradle 8.1.1 werden für einen erneuten Build benötigt. In gradle.properties steht noch mod_version=beta2-dev12; build.gradle setzt project.version auf Montage2. Der ursprüngliche Tippfehler im rootProject.name wurde nicht stillschweigend korrigiert. Optionale vehiclesFixture-Prüfungen benötigen die passende externe Entwicklungs-JAR.

## Änderungen und Lizenz

Der Entwicklungsbericht vom 24.10. nennt hellere Ampeln, direkte Gehäusekontakte, vollständige Pfeile und korrigierte Auswahlflächen. Reale Client-/Mehrspielerabnahme für Montage2 bleibt offen. Die 16 Integrationstests und Eigentümerabnahme vom 17.10. gelten weiterhin nur für das damalige Versionspaar.

Die [bestehende Lizenz](LICENSE.md) bleibt unverändert. Öffentliche Sichtbarkeit ist keine neue Open-Source-Lizenz. Originale LICENSE/NOTICE und Fremdlizenzhinweise befinden sich ebenfalls im Quellarchiv. Keine neue JAR und kein neuer Release werden mit diesem Import veröffentlicht.
