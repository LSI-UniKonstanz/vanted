# Projektthema

## Stress Minimization

	* Berechnet ein möglichst gutes layout für einen Graphen
	* Layout ist gut wenn geringer Stress
	* Stress berechnet sich aus der Abweichung des Abstandes zweier Knoten im layout zur Distanz der Knoten im Graph
	* Wir Minimieren den Stress durch ein iteratives Verfahren 
	* In jedem Schritt bestimmen wir durch Lösen eines Gleichungssystems ein verbessertes Layout. 
	* Weil dieser Vorgang (full stress) verlangt alle Distanzen im Graph zu bestimmen benutzen wir ein Verfahren bei dem nur wenige Knoten betrachtet werden um für Graphen mit mehr als ungefähr 3000 Knoten ein layout zu berechnen (landmark).
	* Hierfür werden bestimmte Knoten ausgewählt und nur für diese Knoten mit dem full stress Verfahren ein layout berechnet.
	* Alle anderen Knoten werden auf mit den Distanzen gewichteten Mittelpunkten zwischen den Landmarks plaziert. 

## Multilevel Framework

# Planung


	
# Anforderungen

	* Grundlegende Anforderung: Implementierung von Stress Minimization und dem Multilevel Framework
	* Anforderung der Interaktivität: Algorithmen können pausiert & gestopt werden und geben interaktiv statusinformationen aus
    * Für das Multilevel Framework die Anforderung mehre Auswahlmöglichkeiten für Placement & Coarsening Algorithmen zu haben
    * Bei beiden Algorithmen sollte die asymphtotische Laufzeitschranke in der Implementierung erreicht werden und auch ansonsten sollten die Implementierungen möglichst schnell sein

# Design

# Architektur

# Demo