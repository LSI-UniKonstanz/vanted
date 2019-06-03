# Protokoll des 3. Betreuenden Treffens

Datum | Uhrzeit | Anwesend
------|---------|---------
03.06 | 11:45   | Gruppe 2.1, Karsten Klein

 - Beginn: 17:05
 - Heute Zwischenpräsentation
 - Beim Multilevel gibt es bis jetzt eine funktionierende Implementierung, aber die Laufzeit nimmt nicht signifikant ab
 - Karsten: Es sollte auch wesentlich schneller sein
 - Kanten bei Coarsening Algorithmen: Im Moment werden alle Kanten gemergt
 - Karsten: Das ist sinnvoll, man kann über ein Kantengewicht nachdenken (verschieden Strategien). Anfangen mit erster Option.
 - Stress-Minimization ist im Moment zu langsam.
 - Erstmal Breitensuche implementieren
 - Layouts nach einzelnen Schritten ausgeben
 - Speicherprobleme?
 - Selection auch berücksichigen, ja: Standard vorgehen
 - Kantengewichte ignorieren
 - Performance Probleme: Auch sonst emails schreiben
 - Benchmarks: Können wir auch mit `currentTimeMillis` machen (Kein großer Aufwand). Benchmarking sollte vergleichbar sein.
 - Background Execution: auch hier gibt es schon eine Implementierung.
 - Karsten: Es wäre gut, wenn wir auch dokumentieren, was uns in der Dokumentation von VANTED fehlt. Wenn wir etwas in VANTED nicht verstehen / nicht rauskriegen, dann gibt es auch einen Hiwi, den/die wir fragen können.
