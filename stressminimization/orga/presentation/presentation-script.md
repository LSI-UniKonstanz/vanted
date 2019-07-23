# Projektthema

## Stress Minimization

	* Berechnet ein möglichst gutes layout für einen Graphen
	* Layout ist gut wenn geringer Stress
	* Stress berechnet sich aus der Abweichung des Abstandes zweier Knoten im layout zur Distanz der Knoten im Graph
	* Wir Minimieren den Stress durch ein iteratives Verfahren 
	* In jedem Schritt bestimmen wir durch Lösen eines Gleichungssystems ein verbessertes Layout. 
	* Landmarks bei Großen Graphen

## Multilevel Framework

	* Im Zentrum steht das Schrittweise zusammenfassen des ursprungsgraphen zu leichter layoutbaren graphen
	* Multilevel besteht aud drei Phasen: coarsening,layouting und placement
	* COARSENING: Zusammenfügen der Knoten zu kleinerem Graph (Wiederholen bis kleiner nicht möglich)
	* LAYOUTING: Ausrichten des entstandenen Graphen
	* PLACEMENT: Knoten werden nahe ihres Repräsentanten schrittweise wieder eingefügt bis Ursprungsgraph erreicht


# Planung
	* Bis zum ersten Milestone sollte das Software Requirement Document und Software Design Document fertig gestellt, die Klassenstruktur implementiert und Test-Cases formuiert sein (Milestone 1) 
	* Dann sollten fertige optimierbare Implementierungen von Multilevel und Stressmin und Benchmarks definiert sein (Milestone 2) 
	* zuletzt sollte eine verbesserte Implementerung fertig gestellt und die Software getestet werden (Milestone 3)	
	* Aufteilung in Gruppen die je nach Aufgabenlage neu festgelegt wurden
	* Aufgabenverteilung Über Issues im  Git

​	
# Anforderungen

	* Grundlegende Anforderung: Implementierung von Stress Minimization und dem Multilevel Framework
	* Anforderung der Interaktivität: Algorithmen können pausiert & gestopt werden und geben interaktiv statusinformationen aus
	* Für das Multilevel Framework die Anforderung mehre Auswahlmöglichkeiten für Placement & Coarsening Algorithmen zu haben
	* Bei beiden Algorithmen sollte die asymphtotische Laufzeitschranke in der Implementierung erreicht werden und auch ansonsten sollten die Implementierungen möglichst schnell sein

# Architektur

	* Stressmin Optimizer für Stress Minimization
	* Multilevel Coarsening und Placement algorithmen
	* Beide nutzen mehrere Funktionen von Vanted selbst

# Design Background

    * zur Hintergrundausführung und der Interaktivität haben wir zwei Pattern verwendet:
    * Zum einen das Delegate-Pattern
    	- In dem Klassendiagram deligiert BackgroundExecutionAlgorithm andere Objekte
    	- Starten, Stoppen und Pausieren, des Algorithmus (Multilevel oder Stressmin), auf Knopfdruck
    * Zum anderen das Observer-Pattern
	- um andere Klassen zu informieren, wenn eine Klasse Aktualisierungen besitzt

# Design Stressmin 
	* Landmarks für graphen mit >3000 Knoten
		- Platzierung der anderen Knoten um die Landmarks
	* IndexNodeSet
		- Bidirektionale Zuweisung von Indizes und Knoten
		- Konstante Zeit (Anders als Hashmap)
		- Ermöglicht z.B. Mengenschnitt in Konstanter Zeit
	* Apache Math wird für Matritzenrehnungen in jedem Iterationsschritt genutzt

# Design Multilevel

	* Multilevel-Graph-Datenstruktur: 
		- speichert alle durch coarsening erzeugten graphen als in VANTED nutzbare Graphen
		- Jeder Knoten hat Attribute die Informationen zu Eltern/Kindern enthalten

	* Abstrakte Klassen zum erstellen von Placement und Coarsening-algorithmen:
		- erweiterbarkeit ist zentral
		- mithilfe dieser Komponente lassen sich coarsening und placement algorithmen erstellen die vom Framework genutzt werden können
		- Die klassen enthalten jeweils Helferfunktionen die sicheren Zugriff auf die Multileveldatenstruktur erlauben.

	* Multilevel Layout algorithmus
		- Steuert den Ablauf des multilevel verfahrens 
		- Erkennt welche layoutverfahren zulässig sind über eine Whitelist
		- erlaubt das Hinzufügen von weiteren layoutverfahren durch hinzufügen zur Whitelist
  

# Demo

    * Stress Min
        - Helikopter (300 landmarks)
    * Multilevel
        - barabasi albert 1 (Solar, StressMin)
    * Beide
        - Konstanz graph

