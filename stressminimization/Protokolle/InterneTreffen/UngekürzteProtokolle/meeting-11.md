# Protokoll des 11. internen Treffens

Datum | Uhrzeit
------|--------
18.07 | 17:00  

- Jacob hat Foliensatz zu Multilevel gemacht
- Benjamin zu statusquo und Aufgabe
- Für die Demo:
    * Multilevel: kleinere Graphen: kreis und ?
    * Stressminimization: jagmesh8 (full stress) & commanche mit 300 landmarks (abbrechen wenn layout schön genug)
    * Stressmin:
        * Landmark slider
        * auto redraw
    * Multilevel:
        * Comboboxen für die einzelnen Algorithmen
        * Scaling
        * Level ausgeben & zeigen

- Präsentation:             Σ 6min
    * Projektthema (kurz)   ≈ 2min
    * Planung               ≈ 0.5min
    * Anforderungen         ≈ 0.5min
    * Design                ≈ 1.5min
    * Architektur           ≈ 1.5min

- Projektthema
    * Vorallem um Terminologie einzuführen?
    * Ansatz des Multilevel Frameworks
    * Wie arbeiten coarsening, placement und layout algorithmen im framework insgesamt zusammen
    * Ansatz der Stress Minimisierung
    * Wie ist Stress definiert & wie wird er minimiert (iteratives lösung eines Gleichungssystems zum finden der Nullstelle des Gradienten der bounded stress function)
    * Landmark Erweiterung
    * Evtl an der Einführung orientieren, die uns Karsten im ersten Treffen gegeben hat? => entsprechendes Dokument

- Planung
    * Je eine Teilgruppe bearbeitet ein Addon (Spezialisierung der Gruppen)
    * Aufgaben Formulierung individuell in Issues & gemeinsam auf dem wöchentlichen Treffen
    * Koordination auf dem wöchentlichen Treffen
    * Phasen: Dokumente & Grundlagen für Implementierung und Tests (Milestone 1); Fertige optimierbare Implementierungen (Milestone 2); Verbesserte Implementerung (Milestone 3)

- Anforderungen
    * Implementierungen von stressmin & multilevel
    * Mehre Auswahlmöglichkeiten für Placement & Coarsening
    * Interaktive Darstellung der Algorithmen und Status ausgabe sowie stoppen und pausieren
    * Performance

- Design
    * Multilevel Architektur mit Erweiterung des Graphen Models
    * Multilevel Erweiterbarkeit mit anderen Addons => Framework!
    * Stress Optimizer bzw. Gleichungssystem Lösungsverfahren ausgelagert
    * Folie: Multilevel Framework Klassendiagram

- Architektur
    * Umsetzung der Interaktivität & Hintergrundausführung: Delegate Pattern (Layouting, Beschreibung, Stop und Pause) & Observer Pattern (Layout & Status Updates) <- Klassendiagram
    * Spezialisierte Implementerung zur Lösung des Gleichungssystems genutzt (bereitgestellt von Apache Math)
    * Design & Architektur unterschieden sich für das Multilevel Framework durch die Anforderungen von VANTED
    * Klassendiagram vom Multilevel Framework

- Spezielle Features
    * Multilevel: UI mit Parameter Anzeige der einzelnen Algorithmen

- Wir treffen uns Montags (10:00 Uhr, Fachschaft) nochmal
- Präsentation auf Deutsch
- Einen Hauptredner für Präsentation und Demo: Joshi
- Eine Demoperson die am Laptop sitzt: David
- Wer leiht den Presenter aus: Silvan
- Stressmin Themaübersicht & Design & Architektur: David & Joshi
- 1-3 Folien je Unterpunkt
- Multilevel Design und Architektur: Jacob
- Eine Folie zur Planung: Milestones: Benjamin
- Umsetzung der Interaktivität & Hintergrundausführung Skript und Folien: Benjamin
- Anforderungen: David
- Weiteren schnellen Graphen für Multilevel suchen: Silvan
