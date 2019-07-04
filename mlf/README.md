# Team 2.2

Repository für Team 2.2  - Java Projekt VANTED Erweiterung durch Layoutverfahren für große Graphen

## Hinweise zum Multilevel Framework Add-On

### Test Coverage

Das MLF Add-On enthält ein paar Klassen aus dem Stress Minimization Add-On
die Unit-Tests für diese wurden nicht kopiert (da dies weiter Abhängigkeiten 
nach sich ziehen würde). Diese Klassen liegen im Package `sm_util`, welches
folglich für die Berechnung der Test-Coverage vom MLF Add-On ignoriert werden 
muss.

Zudem wurde das Package `pse_hack` nicht getestet, da es den Code des 
"Force Directed" Layouters aus VANTED enthält, den wir nur leicht angepasst 
haben, um die Kompatibilität mit dem MLF sicherzustellen.

Da das Package `benchmark` nicht Teil des Add-Ons ist, gibt es für dieses
ebenfalls keine Unit-Tests.

### Assertions

Um das Add-On zu benutzen oder die Performance zu testen müssen unbedingt 
Assertions deaktiviert werden (Flag: `-da`). Das MLF führt teure Überprüfungen
von Invariarianten in Assertions aus. Die Assertions geben bei korrekt 
implementierten Mergern und Placern sowieso immer `true` zurück. Sie sind 
allerdings evtl. hilfreich, um neue Merger oder Placer zu entwickeln und sind
deshalb noch im Code enthalten.

Die Assertions zu deaktivieren, steigert
die Performance bei manchen Graphen um einen Faktor von 20 (!).

## Hinweise zum Stress Minimization Add-On
