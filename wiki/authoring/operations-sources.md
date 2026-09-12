# Źródła stron operacji Ashgrid

Zakres: Ashgrid 1.3.0, kod w tym repozytorium. Publiczny tekst jest po angielsku, zgodnie z WIKI_DESIGN_TEMPLATE. Identyfikatory API pozostają bez zmian. Ten rejestr jest materiałem autora i nie wymaga publikacji przez GitHub Pages.

## Podstawa redakcyjna

- `../Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE.md` względem katalogu Ashgrid: język, jednostki, zakres gwarancji i pochodzenie faktów.
- `../Minecraft Plugins/DOCUMENTATION_DESIGN_TEMPLATE/WIKI_DESIGN_TEMPLATE.md` względem katalogu Ashgrid: wspólny system wizualny, język angielski i format artykułów.
- `pom.xml`: wersja 1.3.0 i Java 21.

## Twierdzenia i źródła

Ścieżki poniżej liczone od katalogu repozytorium Ashgrid.

| Strona | Potwierdzony zakres | Źródło |
| --- | --- | --- |
| `flood-fill` | N6, BFS i kolejność +X/-X/+Y/-Y/+Z/-Z; granice deklarowanej objętości; seed poza zakresem; stabilność źródła; edycja bieżącej komórki | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/floodfill/FloodFill.java`; `src/main/java/nsk/nu/ashgrid/implementation/voxel/ops/floodfill/FloodFillQueue.java` |
| `flood-fill` | Praca odrzuconych kandydatów; zachowanie widoków clamped; kolejność zachowana między krokami | `src/test/java/nsk/nu/ashgrid/integration/voxel/OperationContractTest.java`; `src/test/java/nsk/nu/ashgrid/integration/voxel/SteppedOperationsTest.java` |
| `components` | Czyszczenie wyjścia, etykiety od 1 w kolejności X/Y/Z, wspólny rozmiar i brak aliasów, częściowe wyniki; jednostki 2V + F; rozmiar kolejki | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/components/ConnectedComponents.java`; `src/main/java/nsk/nu/ashgrid/implementation/voxel/ops/components/ConnectedComponentsBFS.java`; `src/test/java/nsk/nu/ashgrid/integration/voxel/SteppedOperationsTest.java` |
| `components`, `morphology-distance` | Różnica N6/N18/N26 i wymaganie niezmieniania współdzielonych tablic | `src/main/java/nsk/nu/ashgrid/api/voxel/neighborhood/Neighborhood3D.java` |
| `components` | Widok bitowy nie zachowuje różnych etykiet dodatnich | `src/main/java/nsk/nu/ashgrid/api/raster/view/BitGrid3iView.java` |
| `morphology-distance` | dylatacja, erozja, binarne wyjście, src.inside, kontrola wymiarów i aliasów, brak bufora roboczego wielkości objętości | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/morphology/Morphology.java`; `src/main/java/nsk/nu/ashgrid/implementation/voxel/ops/morphology/MorphologyBasic.java` |
| `morphology-distance` | open/close, n-krotne operacje; fg tylko dla źródła; n=0 kopiuje surowe wartości; końcowy wynik w dst | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/morphology/MorphologyOps.java`; `src/test/java/nsk/nu/ashgrid/integration/voxel/OperationContractTest.java` |
| `morphology-distance` | Surowe koszty 3/4/5; odległość do foreground; infinity dla pustej maski; trzy przebiegi; dokładna długość wyjścia; float precision | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/distance/DistanceTransform.java`; `src/main/java/nsk/nu/ashgrid/implementation/voxel/ops/distance/Chamfer345Distance.java` |
| `morphology-distance` | Odniesienie wyniku chamfer do Dijkstry dla małych masek, orientacje, wiele źródeł i brak źródeł | `src/test/java/nsk/nu/ashgrid/integration/voxel/ChamferDistanceTest.java` |
| `stepped-tasks` | Statusy, liczniki, budżety, cancel/close, brak rollbacku, terminalność, reentry i wyjątki | `src/main/java/nsk/nu/ashgrid/api/voxel/ops/VoxelTask.java`; `src/test/java/nsk/nu/ashgrid/integration/voxel/SteppedOperationsTest.java` |
| `stepped-tasks` | Jednostki pracy, buforowanie, stabilność między krokami, wyłączność workspace i wyjść | Konkretne implementacje wymienione powyżej oraz `SteppedOperationsTest.java` |

## Słownik

- **Foreground**: komórka zaakceptowana przez predicate lub maskę danej operacji; nie oznacza automatycznie przeszkody ani powietrza.
- **Volume**: iloczyn width × height × depth, liczony w komórkach, a nie liczba zapisanych wpisów w sparse grid.
- **Work unit**: jednostka konkretnego algorytmu. Nie oznacza milisekundy, ticka ani jednostki alokacji.
- **Candidate** (flood fill): komórka pobrana z kolejki i sprawdzana przez predicate; może nie zostać odwiedzona przez callback.
- **Component**: maksymalny połączony zbiór foreground zgodnie z wybraną siatką sąsiedztwa.
- **Chamfer distance**: minimalny koszt ścieżki kroków 3/4/5 do foreground, bez normalizacji.
- **Workspace**: obiekt przechowujący roboczą kolejkę i opcjonalnie visited bits, używany przez jedną aktywną operację.

## Ograniczenia wniosków

- Kontrola tożsamości obiektów nie wykrywa całego współdzielenia pamięci przez widoki. Publiczny tekst rozróżnia odrzucane bezpośrednie aliasy i odpowiedzialność użytkownika za odrębne backing storage.
- Zachowanie sąsiedztwa erozji wynika z `src.inside`; nie przypisano wszystkim widokom polityki zwykłego ArrayGrid3i.
- Złożoność i jednostki pracy dotyczą dołączonych implementacji, stabilnych danych i przebiegu bez wyjątku. Czas callbacków i koszty back-endu grid nie są stałą gwarancją czasową.
- Nie dodano integracji Bukkit/Paper, schedulera, uprawnień, komend ani automatycznego odczytu świata Minecraft.
- Odczytano testy jako źródła kontraktów. Sam ten rejestr nie potwierdza wykonania testów; wynik bieżącej kompilacji i uruchomienia przykładów zapisuje główny proces walidacji WIKI.

## Przykłady do weryfikacji

`content/operations.js` zawiera pięć pełnych klas Java z `main`: `FloodFillExample`, `ComponentsExample`, `MorphologyExample`, `ChamferDistanceExample`, `SteppedTasksExample`. Każdej towarzyszy dokładny blok Expected output. Wszystkie dane wejściowe przykładów powstają w pamięci; brak zależności od serwera Minecraft.
