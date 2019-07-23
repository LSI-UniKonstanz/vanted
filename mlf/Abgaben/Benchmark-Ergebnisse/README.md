# Benchmark-Ergebnisse

Die Zeiten sind jeweils in ms gemessen. Die ODS-Datei enthält die CSV-Dateien
als Tabellen zusammen mit ein paar darauf basierenden Diagrammen.
Die ODGF-Vergleiche wurden mit vergleichbaren Einstellungen durchgeführt.

Bei den Benchmarks mit dem SM-Add-On wurden für diese die Animationen
und vergleichbare Einstellungen ausgeschaltet.

# Qualitätsmetriken
Die Dateien `*qualitymetrics*` enthalten die beiden Qualitätsmetriken
`Kantenkreuzungen` und `Stress`. Die Stressermittlung ging von den
Standardeinstellungen des SM-Add-Ons aus, also sind diese Werte für OGDF
aufgrund anderer Ziellängen für Kanten schlecht vergleichbar.

# Graphen
In den „Graphen“-Verzeichnissen befinden sich die gelayouteten Ergebnisse der
Benchmarks. Für die Qualitätsmetriken wurden die Graphen in den Archiven
`MultilevelFramework-Add-On/Graphen/benchmarks_mlf_new.tar.xz`
und `StressMinimization-Add-On/Graphen/benchmarks_sm_brief.tar.xz` verwendet.
Die anderen Archive enthalten ältere, nicht ausgewertete Benchmarks.

# Hardware-Spezifikationen des benuzten Testrechners
```
system         81BG (LENOVO_MT_81BG_BU_idea_FM_ideapad 320-15IKB)
memory         128KiB BIOS
processor      Intel(R) Core(TM) i5-8250U CPU @ 1.60GHz
memory         256KiB L1 cache
memory         1MiB L2 cache
memory         12GiB System Memory
memory         4GiB SODIMM DDR4 Synchronous Unbuffered (Unregistered) 2133 MHz (0,5 ns)
memory         8GiB SODIMM DDR4 Synchronous Unbuffered (Unregistered) 2133 MHz (0,5 ns)
```
