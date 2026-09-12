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
| `stepped-tasks`, diagram `work-budget` | `GridOps.beginFill`: jedna zapisana komórka na jednostkę; kolejność X/Y/Z; terminalność i pozostawienie częściowych zapisów | `src/main/java/nsk/nu/ashgrid/api/raster/ops/GridOps.java`; `src/main/java/nsk/nu/ashgrid/api/voxel/ops/VoxelTask.java` |

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

## Wizualizacje operacji — 2026-09-12

Implementacja: `assets/diagrams-operations.js`. Pięć interaktywnych figur to małe modele JavaScript objaśniające powyższy kod Java, nie powiązanie strony z biblioteką ani pomiar jej wydajności. Nie uruchamiają świata Minecraft, schedulera ani kodu użytkownika. Kontrolki są natywnymi, podpisanymi przyciskami i polami wyboru, bez automatycznej animacji. Wartości, etykiety oraz opisy stanu uzupełniają kolor.

| Figura | Zakres i jawne ograniczenie | Domyślny wynik oraz scenariusz kontroli |
| --- | --- | --- |
| `flood-fill` | Dokładna siatka 5 × 2 × 1 z przykładu; N6, kolejka kandydatów, zbiór pomyślnych odwiedzin i ostatni przetworzony kandydat są rozdzielone. Z nie ma sąsiadów wewnątrz tej objętości. Ściana pozostaje oznaczona `#` także jako `Q#` i `C#`. | Start: seed (0,0,0), work 0, visited 0, queued 1. Po sześciu kliknięciach: COMPLETED, work 6, visited 4, queued 0. Seed po prawej daje również 4 odwiedziny / 6 kandydatów. Seed w ścianie: 0 odwiedzin / 1 kandydat. |
| `components` | Dokładne trzy komórki z przykładu; oba widoczne przekroje Z należą do jednej siatki 3 × 3 × 2. Etykiety są obliczane skanem X/Y/Z i BFS dla wybranego pełnego sąsiedztwa 3D. | N6: 3 komponenty, etykiety 1/2/3. N18: 2, etykiety 1/1/2. N26: 1, etykiety 1/1/1. |
| `morphology` | Model liczy wszystkie komórki bounded grid 9 × 9 × 7 i pokazuje wybrany przekrój XY. To nie jest erozja płaskiej maski o depth=1. Źródło: blok x/y 2…6, z 1…5, tunel x=y=4 oraz komórka (0,0,3). Wszystkie bufory są oddzielne; poza zakresem jest tło. | Dilate, N6, Z=3: foreground 25 → 48 w przekroju, 121 → 278 w objętości. Dwanaście kombinacji czterech operacji i trzech sąsiedztw porównano komórka po komórce we wszystkich siedmiu przekrojach z implementacją Java. |
| `distance-map` | 7 × 7 × 1, raw chamfer; odległość końcowa wyliczana bezpośrednio jako minimum `4*min(dx,dy)+3*abs(dx-dy)` dla foreground. Bez animacji dwóch przebiegów implementacji. Koszt narożnika 3D = 5 opisano oddzielnie, bo taki krok nie istnieje przy depth=1. Maskę można zmieniać presetem albo współrzędnymi i przyciskiem. | Center (3,3,0): 0; sąsiad przez ścianę: 3; diagonalny przez krawędź: 4; (0,0,0): 12. Pusta maska: wszystkie komórki Infinity. Preset dwóch źródeł (1,2,0) i (5,4,0): wszystkie 49 odległości zgodne z Java. |
| `work-budget` | Model `GridOps.beginFill` zapisuje 7 do 5 × 3 × 1 komórek. Nie utożsamia jednostki z czasem i nie symuluje wyjątków/FAILED. Start nowej ilustracji zeruje jej siatkę; samo cancel niczego nie cofa. | Budget 3, RUNNING, work 0. Jeden krok: 3 zapisane, returned 3. Cancel: CANCELLED i te same 3 zapisy; kolejny step zwraca 0. Budget 0 niczego nie zmienia. Pięć kroków z budżetem 3: COMPLETED i 15 zapisów. |

Walidacja modeli wykonana 2026-09-12: tymczasowy harness Node uruchomił fabryki, zdarzenia kontrolek i cleanup listenerów; sprawdził kolejkę, etykiety, preset pustej maski, edycję foreground, zerowy budżet, anulowanie i terminalne wywołanie step. Osobny program Java uruchomiony przez JDK 25 z lokalnymi klasami Ashgrid i Ashcore 1.2.0 potwierdził dokładną zgodność 6804 wartości morfologii (12 × 567) i 49 odległości modelu dwóch źródeł. Pliki harnessu są tymczasowe, nie są treścią strony. Kontrola wyglądu i pełny build WIKI należą do walidacji integracyjnej prowadzonej przez proces główny.
