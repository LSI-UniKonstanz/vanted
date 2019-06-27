# Protokoll des 8. internen Treffens

Datum | Uhrzeit
------|--------
27.06 | 17:00  

- StressMinimization
    * Landmark MDS implementiert
    * Layout qualität hat abgenommen
    * Auführungszeit wesentlich besser
    * keine Speicherprobleme mehr
    * layout lässt sich durch gute Gewichtungsfunktion für das barycenter Placement ausgleichen
    * UI mit slidern ist Vorhanden und funktioniert
    * Selection und Komponenten dominieren die Laufzeit

- MultilevelFramework
    * Solar Merger & Solar Placement sind implementiert
    * Test definiert und Performence gebenchmarkt
    * Force Directed: layout wird besser, Laufzeit aber nicht
    * Stress Minimization: layout wird nicht besser, Laufzeit aber schon

- Weiteres Vorgehen
    * Joshi schreibt getting started für Stressmin
    * Für Multilevel wird ebenfalls eine getting started guide geschrieben
    * Java Doc generieren und schauen: was fehlt an öffentlicher Dokumentation hier?
    * David wird den Selections Prozess, sowie den Komponenten Bestimmungsprozess optimieren
    * Silvan wird Force Directed aufpäppeln
    * Jakob wird implementieren, dass der Solar Placement auf Zero Placement defaultet
    * Jakob wird implementieren, dass Attribute für Zusammenfassung, Kinderknoten, etc. gespeichert werden
    * Die Addons müssen als JAR oder sonstiges Endformat verpackt werden.
