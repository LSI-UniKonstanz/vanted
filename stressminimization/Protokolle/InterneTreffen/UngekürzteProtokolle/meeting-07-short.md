# Protokoll des 6. internen Treffens

Datum | Uhrzeit
------|--------
20.06 | 14:00   

- StressMinimization
  * globalen Optmierungs verfahren
  * benchmarks wegen blöden fehler stimmen nicht mit der tatsächlichen ausführungszeit in der UI überein
  * Performance wegen hintergrundausführungsoverhead und selections gesunken.
  * Optimierungsmöglichkeiten:
      + Landmark MDS: $O(k^2 \cdot n)$
      + Layouting auf Kreise beschränken Jede Iteration theoretisch linear (mal faktor). Iterations Zahl aber prinzipiell nicht beschränkt... Trotzdem bessere Möglichkeit.
  * Parameter haben primitive UI, die sollte verbessert werden nach Multilevel Vorbild

- MultilevelFramework
  * Bei Forcedirected sieht man gut die visuelle Verbesserung durch das MultilevelFramework
  * Solarmerger: Problem mit einzelnen Knoten, die nicht gemerget werden
      + führt zu "Fäden"
      + vielleicht nicht alle nodes besucht
  * Solarplacement:
       + funktioniert nur mit solarmerger => zu zero placer werden lassen
       + noch nicht abschließend getested
  * Auseinandersetzung mit Force Directed und Hintergrundausführung
      + Force Directed hat einfach auf allen Ebenen gestartet
      + Aktuelle Lösung mit bussy waiting
  * Ähnliche Hintergrundausführungsprobleme mit Stressmin und Multilevel als Multilevel Algorithmus => Mögliche Lösung: Nicht Paralellisierte Algorithmen "unsichtbar" getrennt in VANTED registrieren oder Algorithmen ändern
  * Hintergrundausführung: Fortschritt log wird von anderen Algorithmen überschrieben => Fortschritt ins Parameterfeld?

- VisualTesting packet: Graphen aus benchmark in VANTED generieren
