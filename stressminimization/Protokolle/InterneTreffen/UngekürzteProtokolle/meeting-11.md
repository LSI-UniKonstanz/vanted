# Protokoll des 11. internen Treffens

Datum | Uhrzeit
------|--------
18.07 | 17:00  

- Email von Karsten (=> Neu planen?)

- Präsentation:             Σ 6min **(oder 7-8min? )**
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
    * Je eine Gruppe bearbeitet ein Addon
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
    * Multilevel Erweiterbarkeit mit anderen Addons
    * Stress Optimizer bzw. Gleichungssystem Lösungsverfahren ausgelagert

- Architektur
    * Umsetzung der Interaktivität & Hintergrundausführung: Delegate Pattern (Layouting, Beschreibung, Stop und Pause) & Observer Pattern (Layout & Status Updates)
    * Spezialisierte Implementerung zur Lösung des Gleichungssystems genutzt (bereitgestellt von Apache Math)
    * Multilevel...
