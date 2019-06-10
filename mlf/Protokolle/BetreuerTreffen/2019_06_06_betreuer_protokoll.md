# Protokoll-Betreuer Treffen am 06. Juni 2019

## Anwesend: 
Betreuer: Karsten Klein, Michael Aichem, Wilhelm Kerle
Gruppe 2.2: Jannik, Tobias, Rene, Theodor, Gordian, Jonas

## Inhalte:
- ausgegebener Graph "3LT" ist nicht bearbeitbar
- reminder: CircleLayouter setzt auf Siepinski die Knoten auf NAN.

### Vorstellung SM: 
- Layout von Sierpinski6 ausgehed von Random Layout Siepinski.
- Siepinski8(9000) braucht mit halber Stunde zu lange.
- Focus bei gegebener Qualitaet auf Laufzeit. (Qualitaet geht besser ist aber fuer Prototyp in Ordnung) Vll auch per Parameter sinnvoll ein zu stellen.
- Karsten meinte erkennen zu koennen dass kein Unfung passiert. Anfangslayout sollte Verbesserung bringen(gegebene Ungeeimtheiten duerften damit verschwinden).
### Vorstellung MLF: 
- Aufruf des Multilevelframeworks auf Sierpinski6 mit Circle Layouter. Durschauen aller Level
- Aufruf des Multilevelframeworks auf Sierpinski6 mit einer Form von SM die keine random placement macht. Durschauen aller Level.
- OGDF baut keine Level mit unter 25 MergedNodes. Wir sollten ueber sinnvolle Levelgroesen nachdenken.
- Knotengroese als Parameter sollte auch abhaengig definiert waere.

### Allgemeines
- Benchmarks: Aufbau eines Benchmark-paketes was immer wieder nach jeder Aenderung angewendet werde kann. 
- Gerne auch Vergleiche mit OGDF. Entweder Abgabe und dann rechnen auf Vergleichsmachine oder innerhalb der Gruppe.

### Einzelgespräche
In folgender Reihenfolge:
- Theodor, René, Jannik
- Gordian, Tobias, Jonas 