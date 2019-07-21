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

    * Haben uns aufgeteilt in Teilgruppen: jede Teilgruppe bearbeitet ein Addon (bzw am Anfang hat sich einer mit der Hintergrundausführung beschäftigt)
    * damit wurde Spezialisierung der Gruppen auf ein Thema erreicht
    * die Aufgaben Formulierung wurden individuell in Issues und gemeinsam bei den wöchentlichen Teffen formuliert
    * Koordination wie weit jeder ist und was noch zu machen ist auch bei wöchentlichen Treffen
    * wir hatten einzelnen Phasen festgelegt die zu erreichen waren (und wurden): 
        * Bis zum ersten Milestone sollte das Software Requirement Document und Software Design Document fertig gestellt, die Klassenstruktur implementiert und Test-Cases formuiert sein (Milestone 1) 
        * Dann sollten fertige optimierbare Implementierungen von Multilevel und Stressmin und Benchmarks definiert sein (Milestone 2) 
        * zuletzt sollte eine verbesserte Implementerung fertig gestellt und die Software getestet werden (Milestone 3)	

​	
# Anforderungen

	* Grundlegende Anforderung: Implementierung von Stress Minimization und dem Multilevel Framework
	* Anforderung der Interaktivität: Algorithmen können pausiert & gestopt werden und geben interaktiv statusinformationen aus
	* Für das Multilevel Framework die Anforderung mehre Auswahlmöglichkeiten für Placement & Coarsening Algorithmen zu haben
	* Bei beiden Algorithmen sollte die asymphtotische Laufzeitschranke in der Implementierung erreicht werden und auch ansonsten sollten die Implementierungen möglichst schnell sein

# Design

    * Auslagerung des Stress Optimizers (Gleichungssystemlösers)

# Architektur

    * zur Hintergrundausführung und der Interaktivität haben wir zwei Pattern verwendet:
    * Zum einen das Delegate-Pattern, welches "Verhalten" eines Objektes deligiert
    	- In dem Klassendiagram deligiert BackgroundExecutionAlgorithm andere Objekte
    	- delgiert an GraphHelper, das updaten des Graphen, mit den neuen Koordinaten auf der Benutzeroberfläche, oder das öffnen von Fenstern mit den Multilevel Graph Levels.
    	- desweitern das updaten der Beschreibung an die MainFrame Klasse
    	- sowie das starten, stoppen und pausieren, des Algorithmus (Multilevel oder Stressmin), wenn der Start-, Stop- oder Pausebutton gedrückt wurde
    * Zum anderen das Observer-Pattern, um andere Klassen zu informieren, wenn eine Klasse Aktualisierungen besitzt:
    	- in diesem Fall wird die BackgroundExecutionAlgorithm Klasse informiert, wenn es neue Aktualisierungen in der Algorithmus Klasse gibt 
    	- Diese Aktualisierungen sind hier zum Beispiel der Status oder ein neues Layout bzw. Levels des Multilevel Graphen
    * Verwendung des Apache Math Frameworks im Stress Optimierungsverfahren: u.A. Nutzung eines spezialisieren Gleichungslösungsverfahrens

# Demo
