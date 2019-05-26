# API Documentation Guideline
  * Keine @author tags notwendig
  * Zu Public Interfaces (Coarsening, Placement Algorithms): Jede Methode dokumentieren, ausführliche Dokumentation der Interface, Code Beispiel (klein)
  * Internal Interfaces: Short, to the point documentation, class + methods
  * Private helpers / Util classes: Variable names and code should be self documenting, so java-doc may be short, only document important private methods
  * Bei Klassen, die sehr änhlich zu Beispielklassen sind (AddonAdapter) auf Beispiel Addon verweisen, sehr knapp dokumentieren
  * Algorithmus Implementierungen: Als Comment Grundideen aus den Papern restaten, verweißen
  * GUI Klassen für VANTED: Im Code dokumentieren wofür was gemacht wird, wo was im Endeffekt angezeigt wird, Implementierungen im VANTED schwer einsichtig
  * Unit Tests: In Javadoc schreiben, warum dieser Test gemacht wird, welche Fehler / Edge cases gesucht werden / Aufgrund welches Bugs wir den Test eigefügt haben
