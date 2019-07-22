# Hinweise zum Build-Prozess, Unit-Tests, etc. (Team 2.2)

Repository für Team 2.2  - Java Projekt VANTED Erweiterung durch Layoutverfahren für große Graphen

Die Benutzerhandbücher finden sich [hier](Abgaben/Dokumente/Benutzerhanbücher) und
die Benchmark-Auswertungen [hier](Abgaben/Benchmark-Ergebnisse/).

## Hinweise zum Multilevel Framework Add-On

### Test Coverage

Das MLF Add-On enthält ein paar Klassen aus dem Stress Minimization Add-On
die Unit-Tests für diese wurden nicht kopiert (da dies weiter Abhängigkeiten 
nach sich ziehen würde). Diese Klassen liegen im Package `sm_util`, welches
folglich für die Berechnung der Test-Coverage vom MLF Add-On ignoriert werden 
muss.

Zudem wurde das Package `pse_hack` nicht getestet, da es den Code des 
„Force Directed“ Layouters aus VANTED enthält, den wir nur leicht angepasst 
haben, um die Kompatibilität mit dem MLF sicherzustellen.

Da das Package `benchmark` nicht Teil des Add-Ons ist, gibt es für dieses
ebenfalls keine Unit-Tests.

Die Coverage-Ermittlung wurde mittels des IntelliJ-IDEA Standard-Coverage-Runner
„IntelliJ IDEA, Sampling“ durchgeführt. Dieser ermittelt nur Zeilen-Coverage, und
ist schneller, darum könnte es sein, dass sich die Werte ändern wenn eine andere
Einstellung gewählt wird.

### Assertions

Um das Add-On zu benutzen oder die Performance zu testen müssen unbedingt 
Assertions deaktiviert werden (JVM Flag: `-da`). Das MLF führt teure Überprüfungen
von Invariarianten in Assertions aus. Die Assertions geben bei korrekt 
implementierten Mergern und Placern sowieso immer `true` zurück. Sie sind 
allerdings evtl. hilfreich, um neue Merger oder Placer zu entwickeln und sind
deshalb noch im Code enthalten.

Die Assertions zu deaktivieren, steigert
die Performance bei manchen Graphen um einen Faktor von 20 (!).

## Hinweise zum Stress Minimization Add-On

### Test Coverage

Einige Unit-Tests gehen davon aus, dass Assertions aktiviert sind (JVM Flag: `-ea`).
Dieses wird jedoch in den entsprechenden Tests überprüft.

Zu mindestens auf dem Rechner auf dem die Tests ausgeführt wurden gab es mit der
benutzten IDE ‚IntelliJ‘ manchmal Unit-Tests die sich aufhingen und nicht zu Ende
ausgeführt wurden. Ein stoppen und erneutes Ausführen sollte das Problem beheben.

Manche Test warten mit fest eingestellten Zeitwerten auf das Beenden einer
bestimmten Ausgabe, zum Beispiel für die GUI oder Ausgabe. Sollte ein Test
scheitern müssen diese Werte vielleicht an die ausführende Maschine angepasst
werden.

Nur Klassen in `src/main`, also diejenigen die Teil des Add-Ons sind, wurden
getestet.

Es wurden die gleiche Coverage-Methode wie für das Multilevel-Framework benutzt.
Darum fallen die gleichen Besonderheiten an.

## Bauen der JAR-Datei

Es gibt für beide Add-Ons Build-Dateien für ANT
(`vanted-addon-mlf/build-mlf-add-on.xml`, `vanted-addon-sm/build-sm-add-on.xml`).
Bevor der Build-Prozess gestartet wird, müssen folgende Voraussetzungen erfüllt sein:

* Das `vanted-lib-repository` muss sich im Wurzelverzeichnis des geklonten Repository befinden.
  (es kann von [hier](https://bitbucket.org/vanted_dev/vanted-lib-repository/src/master/) heruntergeladen werden)
* Zusätzlich muss noch VANTED kompiliert sein und unter `VANTED/target/jar/vanted-fatjar-ver2.6.5-build-[...].jar`
  gespeichert sein (VANTED kann mittels der `build.xml` im VANTED-Verzeichnis gebaut werden).
* Für einen Bau in IntelliJ haben wir diese [Anleitung](Literature/build_with_intellij.md) verfasst.)
