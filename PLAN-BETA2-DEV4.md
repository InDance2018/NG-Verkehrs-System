# 0.1.0-beta2-dev4 – Fahrzeugerkennung und getrennte Parkzonen

Vom Nutzer freigegebener nächster Ausbau nach beta2-dev3. Der Schilderausbau pausiert.

## Vorgesehener Umfang

1. Fahrzeugerkennung der Parkplatzsensoren erweitern: vorhandene Fahrzeugmod feststellen, vorhandene Standarderkennung und Entity-Type-Tags erhalten; Erkennung gezielt konfigurierbar machen und ungeprüfte Fremdmod-Kompatibilität ausdrücklich kennzeichnen.
2. Getrennte Parkzonen: jeder Parkplatzrechner verwaltet weiter eine eigene Belegung; zusätzliche Verknüpfung mehrerer Zonen mit einer Gesamtanzeige. Einzelzonen bleiben unabhängig, Gesamtkapazität und freie Plätze werden zusammengezählt. Gespeicherte Zuordnungen, Offline-Verhalten und Bearbeitungsrechte prüfen.

Vorhandene dev2/dev3-Spieltests stehen noch aus. Neue Logik erhält automatisierte Servertests; reale Fahrzeugmod-/Mehrspielerprüfung bleibt von den verfügbaren Fahrzeugen abhängig.

Private Dev-Fassung. Die öffentliche Downloadfassung bleibt Beta 1. Kein Upload auf MC02.
