# Protokoll-Betreuer Treffen am 27. Juni 2019

## Anwesend

Betreuer: Karsten Klein, Michael Aichem(+45 min)
Gruppe 2.2: Jannik, Tobias, Rene, Theodor, Gordian, Jonas

## Inhalte

- Frage: "Sind Workarounds fuer alle Layoutalgorithmen nötig?"
    Barriere fuer kommende Algorithmen wie Stressmin oder ForceDireceted sollte vermieden werden.
    Kriterien für eine Whitelist sollten in der Documentation fest geschrieben werden, sodass weitere Algorithmen zu dieser hinzufügbar sind.
    Zusätzliche Idee: SolarPlacer koennte RandomPlacer aufrufen falls ohne SolarMerger genutzt wird.
- Tests in MLF für die entliehene Klassen aus SM: 
    Dokumentieren der Entscheidung im SDS und der abschliesender Handreichung ist ausreichend.
    Diese Klassen müssen aus der Coverage des MLF ausgenommen werden und auch dies in Handreichung vermerkt werden.
- Frage: Oracle unterstuetzt Java 9 nicht mehr. Wie ist mit dem Port zu verfahren?
    Auf zu schieben bis der Rest sauber fertig ist. Wenn dann Port für abgeschlossenes Projekt.
- Vergleich SM gegen OGDF-Stressmin:
    Der Faktor 2-3 ist in Ordnung bei kleineren(1-4K) Graphen.
    Man sollte überprüfen wodurch die Laufzeit bei groeseren Graphen anwächst.
- Benchmarking SM:
    Stress-Werte bei StressMin ausgeben lassen und vergleichen.
    ForceDirected ist vom SM in änlicher Qualitaet zu schlagen.
- Benchmarking MLF:
    2-3 Metriken im Vergleich zu OGDF und/oder ForceDirected sind zu untersuchen.
- Zu Benchmarking allgemein:
    Harte Metriken zu finden ist nicht immer möglich und sollte nicht erzwungen werden.
    Weitere Tests bestenfalls auf PCs ohne Speicherbottlenecks.
    Linecrossings in OGDF und VANTED sind zu vergleichen.
    Feste Zeiträhmen mit  möglichst vielen Layouts verschiedenen Graphen füllen.(Also was in Fester zeit möglich ist und nicht wieviel Zeit ein durchlauf eines Layouts braucht)
    Testen darf über geschriebenes Testset oder manuell erfolgen.
    Vorteile zu bisher verfügbaren Layouter aufzeigen. Und diese auch als Vergleiche nutzen.
- Zu Graphen: Neben den im Git bereit gestellten Pages stellt Yifan Hu eine Vielzahl an Graphen zur verfuegung.
    (http://yifanhu.net/gallery.html)
- Frage: 3elt wirft einen Formatfehler, gibt es eine andere Form?
    Entfernen der ersten 2 Zeilen gibt korekten Graph.
- Zu Pivot MDS:
    Begruendung warum Bibliotheken verwendet wurden oder warum nicht muss in Abschlussdokument auftauchen.
- Zu MLF SolarPlacer: ZeroEnergyLength Berechnung raus zu finden, sodass Aufwand in Implmentierung
    und Rechnung geschätzt werden kann.
    Da nur der Placer diese nutzt, schätzt die Gruppe den Einfluss auf die Gesamtlaufzeit relativ gering sein.