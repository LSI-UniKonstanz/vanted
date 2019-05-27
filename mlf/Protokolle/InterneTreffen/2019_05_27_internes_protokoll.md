# Protokoll - Internes Treffen am 27. Mai 2019

## Anwesend:
 Jannik, Tobias, Gordian, Theo, René, Jonas

## Inhalte:
- Tobias VANTED Build-Prozess fixen
- Entscheidung: @author-Attribute zum JavaDoc hinzufügen
- Besprechung, wie mit der Selection umgegangen werden soll:
  - MLF: Eigene von Implementierung von einer Methode, 
   die die Zusammenhangskomponenten aus der Selection "baut" 
   (als weitere "Graph"-Objekte)
- "Last Week in SM": NodeValueMatrix implementieren, 
 ShortestDistances implementieren
- "Last Week in MLF": MultilevelGraph mit einem Graph pro Ebene
- HashMap<Node, ...> kann verwandt werden, da VANTED es auch überall verwendet
 (Wir waren uns zunächst nicht sicher, da die Nodes nicht "hashCode()" 
 nicht überschreiben)
- MLF: Weiteres Treffen am Donnerstag um 14:00, um den trivialen Merger und 
 Placer zu implementieren
