# Protokoll des 5. internen Treffens

Datum | Uhrzeit
------|--------
06.06 | 17:00   

- Stressminimization:
  * TODO: Algorithmus von Nutzer parametrisieren lassen
    + Epsilon
    + Alpha
    + Abbruch Kriterium
  * Kann in zwei Wochen fertig sein.
- Background Execution:
  * Läuft (Fast abgeschlossen)
  * Schon was gemacht für UI.
- Multilevel:
  * Thomas ist ausgestiegen
    + Joshi schreibt Mail an Betreuende
  * Milestone 2 fast erreicht, benchmarks fehlen
  * Benchmarks können von Stressminimization übernommen werden.

## Milestone 2

> First non-comparative implementations of multilevel framework and stress minimization layout algorithm are functional, benchmarks defined

 * Bis jetzt Benchmarks:
   - Fünf graphen (Stern, Rad, Pfad, Vollständig, zwei zufällige Netzwerke mit Ähnlichkeiten zu in der Natur vorkommen den Graphen)
 * Skalierungstests sind sinnvoll.
 * Visuelle Benchmarks? -> verschieben auf später
 * Memory Usage: Abgeschätzt durch Anzeige in VANTED

 * Stressminimization: Stress Majorization versagt bei sehr großen Graphen (n > 10.000). Vielleicht LandmarkMDS implementieren?
   - Ähnlich zu Multilevel
   - Benchmarks an Karsten schicken und dann nach Rücksprache neu evaluieren.
