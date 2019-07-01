# Protokoll des 6. internen Treffens

Datum | Uhrzeit
------|--------
20.06 | 14:00   

- StressMinimization
  * globalen Optmierungs verfahren
  * benchmarks wegen blöden fehler stimmen nicht mit der tatsächlichen ausführungszeit in der UI überein
  * Performance wegen hintergrundausführungsoverhead und selections gesunken.
  * Landmark MDS
      + Laufzeit $k^2 * n$
  * Möglichkeit:
      + Kreise um Knoten ziehen und nur die Nodes in dem Kreis layouten
      + Alternative zu Landmark MDS
      + Laufzeit: Linear
      + Gute Möglichkeit.
      + Das alles einmal pro Iteration
      + und mehrfach iterieren
  * Parameter haben primitive UI
  * Parameter UI: Siehe Issues und Joshi spricht sich mit Silvan ab, wie er das bei Multilevel gemacht hat.

- MultilevelFramework
  * Bei Forcedirected sieht man gut die Verbesserung durch das MultilevelFramework
  * es gibt einen scaling parameter, der den graph nach placement skaliert, unter Annahme: halb so viele Knoten in jedem Level
  * Solarmerger: Problem mit einzelnen Knoten, die nicht gemerget werden
      + führt zu "Fäden"
      + vielleicht nicht alle nodes besucht
  * Solarplacement:
       + funktioniert nur mit solarmerger => zu zero placer werden lassen
       + noch nicht abschließend getested
  * Auseinandersetzung mit Force Directed und Hintergrundausführung
      + Force Directed hat einfach auf allen Ebenen gestartet
      + Aktuelle Lösung mit bussy waiting
  * Verwendung mit Stressmin:
      + nur background execution wird registriert
  * Problem Multilevel auf Multilevel:
      + anderere execute methode
  * Nicht Hintergrundalgorithmen separat registrieren?
  * Multilevel mit Stressmin: Fehler graph may not be null
      + irgendwo wird einmal zu viel check ausgeführt
      + vielleicht Problem mit den aktiven Graph und reattachen in BackgrundExecutionAlgorithm
  * Multilevel wird nach erster Ausführung mehrfach gestartet
      + vielleicht nur Problem mit logging und nicht ausführung
  * Hintergrundausführung: fortschritt log wird überschrieben von anderen Algorithmen => ins Parameterfeld

- VisualTesting packet: Graphen aus benchmark in VANTED generieren
