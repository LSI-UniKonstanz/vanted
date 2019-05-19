# Protokoll-Internes Treffen am 16. Mai 2019

## Anwesend: 
Gordian Schoenherr, Jannik Loescher, Tobias Franz, Rene Groß, Theodor Gutschlag,
Karsten Klein, Michael Aichem, Benjamin Moeck

## Inhalte:
- Recap der letzten 2 Wochen: Einlesen in mögliche Implementierungen und nachvollziehen von Vanted um SDD erstellen zu können
- Rückmeldung zum bisherigen SDD:
    -Design Goals: "asymptotically" durch praktische Test im direkten Zeitverhaeltnis ersetzen. Schwächere Performance sollte gegebenfalls durch erklärbar sein. Vorschlag: Formulierung mit "unreasonably".
    - Die angegebenen Class- und Component-Diagrams sind nicht ausreichend. Nach unserem Wissensstand sollte es möglich sein, die Funktion einzelner Komponenten/Klassen zu beschreiben.
    - Als mögliche Input-formate sind GraphML,GML sinnvoller und auch einfacher um zu setzten als das angegebene PNML.
    - Gewichtete und gerichtete Graphen sollen unter Missachtung von Gewichten und Gerichten berechnet werden. Eine Rückmeldung an den User bei Start des Layouters ist möglich jedoch nicht unbedingt nötig(in Documentation festhalten genügt).
    - Layouts unzusammenhängender Graphen müssen in ihren Zusammenhangskomponenten berechnet und nebeneinander angezeigt werden.
    - Bis Dienstag erhalten wir von den Betreuern informationen wie Kantengewichte aus zu lesen sind.
- Änderungen im SDD nach der Abgabe sind in dem gegebenen Rahmen möglich wenn diese Änderungen im Anhang des SDD vermerkt werden.

- Betreffend der Auswahl von Mergern für das MLF:
 Die Auswahl und Implementierung von Mergern sollte nicht projektfüllend sein. Als nicht triviale Merger sind sowohl "Solar merger" als auch "independent set merger" akzeptiert.

- Durchgehen  des Papers GKN04  zu SM:
Der Ausgangsgraph für die schrittweise Optimierung in SM sollte aus performance gründen nicht zuällig generiert werden. Die Verwendung von zusätzlichen Java-Bibliotheken für Matrizenrechnung erscheint den Betreuern nicht sinnvoll.

-Arbeiten als Team:
    - Man braucht klare Strukturen und Deadlines. 
    - Teammitglieder müssen Verantwortung für ihre Aufgaben übernehem.
    - Sollte es zu Verspätungen kommen, halten diese den Fortschritt des ganzen Projektes auf. 
    Daraus resultierenden Diskussionen sollten keinesfalls auf persönlicher Ebene geführt werde, sind jedoch berechtigt und nötig.
    - Wenn ein Teammitglied einer Aufgabe nicht nach kommen kann oder Hilfe benötigt um diese zu Bewältigen ist dies so schnell wie möglich mit zu teilen. Verantwortung für eine Aufgabe übernommen zu haben heißt nicht, dass jede Zeile des Codes von dieser Person Stammen muss.
