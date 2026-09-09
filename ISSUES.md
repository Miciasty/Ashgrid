# Ashgrid — ISSUES

## Cel pliku

Ten plik powstał 2026-09-09 po przeglądzie wspólnych zasad Blackframe i przyjęciu [blackframe.md, rewizja 2.0](../blackframe.md). Służy do zaplanowania korekt tej biblioteki oraz przekazywania pracy między kolejnymi, niezależnymi sesjami. Nie trzeba znać historii rozmowy: poniżej są powód zadania, miejsca w kodzie, kryteria odbioru i powiązania z innymi projektami.

To lista prac i miejsce zapisu dowodów, a nie dokumentacja gotowych funkcji ani informacja, że błędy już naprawiono. Nie wszystkie pozycje są błędami wykonania: część wymaga doprecyzowania umowy z użytkownikiem lub sprawdzenia istniejących zabezpieczeń. Przegląd obejmował README, POM, workflow i wybrane źródła/testy; nie jest pełnym audytem całego kodu. Podczas przygotowania pliku nie zmieniano implementacji i nie uruchamiano testów bibliotek.

## Punkt odniesienia

- Rola projektu: Komórki, chunki, przechodzenie promienia przez siatkę oraz operacje na zbiorach wokseli.
- Wersja zadeklarowana w lokalnym POM: **1.2.0**. To nie jest potwierdzenie publikacji.
- Stan źródeł podczas przygotowania: **04404cf** na gałęzi docs/blackframe-contract-v2-20260909; commit zapisuje stan sprzed zmian dokumentacji.
- Zależności: Ashcore 1.0.1. Po zmianach normalizacji promieni, tolerancji lub SPI potrzebna jest weryfikacja integracji.
- Dokument nadrzędny: rewizja **2.0 z 2026-09-09**. Numery sekcji w zadaniach odnoszą się do tej rewizji.

## Jak rozpocząć nową sesję

1. Przeczytaj lokalne AGENTS.md/instrukcje użytkownika, [kontrakt Blackframe](../blackframe.md) i cały ten plik. Jeśli kontraktu brakuje w osobnym klonie, uzyskaj właściwą rewizję przed rozstrzyganiem wspólnych zasad.
2. Sprawdź aktualny Git i różnice względem powyższego punktu odniesienia. W tej pracy obowiązywała instrukcja użytkownika: przed zmianami utworzyć nową gałąź i zacommitować obecną wersję. Zachowaj cudze zmiany; nie resetuj repozytorium. Dla katalogu bez Git nie wymyślaj istniejącego commita.
3. Zacznij od wskazanego P1, odtwórz obserwację i sprawdź istniejące testy. Ustal kontrakt przed korektą zachowania. Wpis INSPEKCJA nie zastępuje reprodukcji.
4. Naprawiaj zadania w granicach tego projektu. Zmianę wspólnego kontraktu prowadź u właściciela niższej warstwy, a potrzebną pracę w innym repozytorium zapisz pod jego ID. Rutynowa poprawka nie wymaga edycji blackframe.md.
5. Po zmianach uruchom odpowiednie testy i końcowe clean verify. Aktualizuj statusy i dziennik poniżej: co zmieniono, rzeczywisty wynik kontroli, decyzje zgodności, pozostałe zależności i następny krok. Nie publikuj artefaktów tylko po to, aby sprawdzić kod.

Zalecana kolejność ustaleń wspólnych: Ashcore → Ashgrid → Ashspace, następnie Ashtrace i Ashnav zgodnie z ich zależnościami. Ashnav nie musi czekać na Ashtrace; niezależne zadania lokalne można podejmować wcześniej. Ashtemplate można poprawiać osobno. Ashmesh nie ma obecnie lokalnego katalogu, więc ten backlog nie zleca jego implementacji.

Maven używa zależności rozstrzygniętych z POM i repozytoriów artefaktów. Zmiana pliku w sąsiednim checkout nie podmienia ich automatycznie. Przy integracji zapisz konkretne wersje, commity i wynik rozstrzygnięcia zależności. Dla próbnego builda dolnej warstwy użyj odróżnialnej wersji roboczej lub izolowanego repozytorium testowego; nie nadpisuj istniejącego wydania inną zawartością.

## Oznaczenia

- **P1** — poprawność, publiczne gwarancje lub wymagana weryfikacja; rozstrzygnąć przed deklaracją zgodności z rewizją 2.0 i następnym wydaniem objętego zakresu.
- **P2** — porządkowanie lub pogłębiona kontrola po pilnych korektach; nie pomijać bez zapisanej decyzji.
- **INSPEKCJA** — potwierdzony zapis lub mechanizm w źródle; podany skutek może wymagać jeszcze testu wykonania.
- **AUDYT** — zakres do sprawdzenia, bez twierdzenia, że wszystkie wymienione miejsca są błędne.
- **DECYZJA** — trzeba wybrać i udokumentować wspierany kontrakt lub migrację.
- Statusy: **OTWARTE**, **W TOKU**, **ZABLOKOWANE** (z konkretną zależnością), **GOTOWE** (z dowodem spełnienia kryteriów), **NIE DOTYCZY** (z uzasadnieniem). Zachowuj identyfikatory po zamknięciu.

## Kolejka

| ID | Priorytet | Typ | Zadanie |
| --- | --- | --- | --- |
| [GRID-001](#grid-001) | P1 | INSPEKCJA | Skorygować parametry przejścia DDA dla ujemnego kierunku |
| [GRID-002](#grid-002) | P1 | AUDYT | Uzgodnić wizyty na granicach oraz jednostki mapowania |
| [GRID-003](#grid-003) | P1 | INSPEKCJA | Zweryfikować maskę i znaczenie odległości Chamfer345 |
| [GRID-004](#grid-004) | P1 | AUDYT | Sprawdzić zakres indeksów, rozmiary buforów i mutację danych |
| [GRID-005](#grid-005) | P1 | AUDYT | Zweryfikować kontrakty i pakowanie providerów SPI |
| [GRID-006](#grid-006) | P1 | DECYZJA | Utrzymać zgodność SquareXZChunkScheme i helperów |
| [GRID-007](#grid-007) | P1 | INSPEKCJA | Dostosować CI, pakowanie i dowody wydania |

<a id="grid-001"></a>

## GRID-001 — Skorygować parametry przejścia DDA dla ujemnego kierunku

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.2, 4.2, 4.3, 4.5

**Gdzie:** [DDA3DTraverser.java](src/main/java/nsk/nu/ashgrid/implementation/voxel/traversal/DDA3DTraverser.java), [VoxelTraverser.java](src/main/java/nsk/nu/ashgrid/api/voxel/traversal/VoxelTraverser.java), [TraversalAndQueryIntegrationTest.java](src/test/java/nsk/nu/ashgrid/integration/voxel/TraversalAndQueryIntegrationTest.java).

**Stan podczas przeglądu:** DDA oblicza nx = granica - origin.x, a tMaxX = nx / abs(direction.x). Przy origin=(0.2,0.2,0.2), direction=(-1,0,0), tMax=1 obliczenie daje pierwsze tExit=-0.2, mimo że przejście do x=0 jest w odległości +0.2. To konkretny kontrprzykład z inspekcji, jeszcze nie uruchomiony jako test.

**Znaczenie:** Promień skierowany w lewo może zgłaszać odwrócone przedziały albo komórki z nieprawidłową odległością. Korzystają z tego raycast i Ashtrace.

**Praca do wykonania:** Najpierw odtwórz pierwszy callback. Popraw wyznaczenie dodatniego czasu do granicy dla obu znaków kierunku na każdej osi. Następnie sprawdź zapytania korzystające z DDA bez maskowania błędu przez tolerancję.

**Warunki zamknięcia:**

- [x] Przy powyższych danych dodatnie odcinki wizyty to komórka (0,0,0) z [0,0.2) i (-1,0,0) z [0.2,1), z uzasadnioną tolerancją porównań liczbowych.
- [x] Testy obejmują sześć kierunków osiowych, kierunki mieszane, start ujemny i natychmiastowe przerwanie callbacku.
- [x] Każdy przedział spełnia uzgodnione zasady i rosnący parametr; Raycast/LineOfSight oraz [TRACE-002](../Ashtrace/ISSUES.md#trace-002) sprawdzono z poprawioną zależnością.

**Powiązania:** [CORE-001](../Ashcore/ISSUES.md#core-001) określa poprawny Ray. Powiadom Ashtrace w [TRACE-002](../Ashtrace/ISSUES.md#trace-002) o poprawce i wersji Ashgrid.

**Wynik korekty 2026-09-09:** Odtworzono błąd testem i debuggerem: pierwszy callback [0,-0.2). Skorygowano znak czasu do granicy na każdej osi i odrzucono niepoprawne promienie/NaN; indeksy nie zawijają się. CorrectionRegressionTest i TraversalBoundaryTest obejmują sześć kierunków, mieszane znaki, granice, przerwanie, bardzo małe składowe oraz Raycast/LOS. Fixture src/it/ConsumerContracts.java potwierdza TRACE-002 z poprawionym JAR i Ashcore 1.1.0-SNAPSHOT. Wersje, SHA i wynik: docs/RELEASE.md.

<a id="grid-002"></a>

## GRID-002 — Uzgodnić wizyty na granicach oraz jednostki mapowania

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 3.2, 3.3, 4.1, 4.3

**Gdzie:** [VoxelTraverser.java](src/main/java/nsk/nu/ashgrid/api/voxel/traversal/VoxelTraverser.java), [DDA3DTraverser.java](src/main/java/nsk/nu/ashgrid/implementation/voxel/traversal/DDA3DTraverser.java), [VoxelSpace.java](src/main/java/nsk/nu/ashgrid/api/voxel/space/VoxelSpace.java), [ChunkScheme.java](src/main/java/nsk/nu/ashgrid/api/grid/indexing/ChunkScheme.java), [SquareXZChunkScheme.java](src/main/java/nsk/nu/ashgrid/implementation/grid/indexing/SquareXZChunkScheme.java).

**Stan podczas przeglądu:** API traversal opisuje przedziały półotwarte i +INF. DDA przy remisie wybiera jedną oś (X, potem Y, potem Z), co wymaga decyzji o wizytach zerowej długości. VoxelSpace ma skalę/początek i konwersję floor; Ashspace ma własny mapper. Część dokumentacji nazywa współrzędne world, mimo pracy w siatce jednostkowej.

**Znaczenie:** Punkt na styku klocków może być liczony przez sąsiednie biblioteki inaczej. Dotknięcie narożnika nie musi oznaczać przejścia przez wnętrze każdej sąsiedniej komórki.

**Praca do wykonania:** Spisz reguły startu, końca, tMax=0, dodatniej nieskończoności, NaN i remisów osi. Porównaj ordinary traversal z supercover. Ustal relację jednostek ChunkScheme/VoxelSpace/GridSpaceMapper3 i zachowaj zgodność istniejącego API.

**Warunki zamknięcia:**

- [x] Testy obejmują twarz, krawędź, narożnik, kierunek ujemny z granicy i dokładny koniec odcinka.
- [x] Udokumentowano dopuszczalność zerowych przedziałów, porządek remisów i warunki zakończenia nieograniczonego traversal.
- [x] Przy uzgodnionej skali i początku mapowanie ujemnych współrzędnych oraz granic zgadza się z testami [SPACE-001](../Ashspace/ISSUES.md#space-001); różnic nie maskuje uniwersalny epsilon.

**Powiązania:** Współdzielone ustalenia z [SPACE-001](../Ashspace/ISSUES.md#space-001) i [TRACE-002](../Ashtrace/ISSUES.md#trace-002); nie wprowadzaj zależności produkcyjnej Ashgrid od Ashspace.

**Wynik korekty 2026-09-09:** Zachowano floor(origin), remisy X→Y→Z i zerowe przedziały, z jawnym opisem końca [0,tMax), zera, +INF, NaN i wyczerpania int. Clipped traversal ma półotwarte granice równoległe. Supercover opisano i naprawiono osobno; nie narzucono mu listy DDA. Testy SPACE-001 potwierdzają zgodność przy skalach 0.5/1/2, przesuniętym początku i granicach. Dzielenie w VoxelSpace oraz mnożenie przez odwrotność w Ashspace mogą zaokrąglać inaczej dla innych skal; to jawne ograniczenie i dalsza praca SPACE-001, bez epsilona zmieniającego przynależność.

<a id="grid-003"></a>

## GRID-003 — Zweryfikować maskę i znaczenie odległości Chamfer345

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 3.2, 4.2, 4.4, 4.5

**Gdzie:** [DistanceTransform.java](src/main/java/nsk/nu/ashgrid/api/voxel/ops/distance/DistanceTransform.java), [Chamfer345Distance.java](src/main/java/nsk/nu/ashgrid/implementation/voxel/ops/distance/Chamfer345Distance.java), [LineAndDistanceIntegrationTest.java](src/test/java/nsk/nu/ashgrid/integration/voxel/LineAndDistanceIntegrationTest.java).

**Stan podczas przeglądu:** Interfejs opisuje wyłącznie zamianę maski na odległości. Implementacja zwraca wagi 3/4/5 bez dzielenia przez 3 i używa 1e9f jako INF. Maski przejść zawierają przekątne o zgodnych znakach osi, ale brak przekątnych o znakach mieszanych. Sprawdzenie symetrii wyniku jest konieczne; nie wykonano go w tej sesji.

**Znaczenie:** Odległość jednego kroku osiowego wynosi tu 3, więc wynik nie jest od razu liczbą bloków. Niepełna maska może też różnie traktować lustrzane układy przeszkód.

**Praca do wykonania:** Na siatce 2x2x1 porównaj odległość (0,0,0)→(1,1,0) z (0,1,0)→(1,0,0), ustawiając pierwszy punkt jako jedyne foreground. Dla pełnej metryki 3-4-5 obie przekątne powinny kosztować 4; obecne maski sugerują 4 i 6. Odtwórz wynik, ustal zamierzony model i popraw maski, jeżeli potwierdzi się rozbieżność.

**Warunki zamknięcia:**

- [x] Testy obejmują wszystkie orientacje przekątnych, odbicia/permutacje osi i porównanie z prostym wzorcem kosztów na małych siatkach.
- [x] Opis wyjaśnia foreground=0, jednostkę wagi, przybliżenie względem odległości geometrycznej i przypadek braku foreground.
- [x] Sprawdzono walidację wymiarów, produktu rozmiarów i bufora wyjściowego.

**Powiązania:** Nie przedstawiaj wyniku jako gotowego testu przechodniości postaci w Ashnav; znaczenie odległości musi być jawne.

**Wynik korekty 2026-09-09:** Reprodukcja dała 4 i 6 dla lustrzanych przekątnych. Pełna maska 13+13 sąsiadów przechodzi porównanie z Dijkstrą dla wszystkich źródeł w trzech niekubicznych siatkach i masek wieloźródłowych. Wagi 3/4/5 zachowano; brak foreground daje +INF zamiast nieudokumentowanego 1e9f. Udokumentowano jednostki, przybliżenie geometryczne i precyzję float. Wymiary i długość bufora są sprawdzane przed odczytem maski.

<a id="grid-004"></a>

## GRID-004 — Sprawdzić zakres indeksów, rozmiary buforów i mutację danych

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.2, 4.3, 4.4, 4.5

**Gdzie:** [ArrayGrid3i.java](src/main/java/nsk/nu/ashgrid/implementation/raster/arrays/ArrayGrid3i.java), [BitGrid3.java](src/main/java/nsk/nu/ashgrid/implementation/raster/bitset/BitGrid3.java), [SubGrid3i.java](src/main/java/nsk/nu/ashgrid/api/raster/view/SubGrid3i.java), [Neighborhood3D.java](src/main/java/nsk/nu/ashgrid/api/voxel/neighborhood/Neighborhood3D.java), [ChunkScheme.java](src/main/java/nsk/nu/ashgrid/api/grid/indexing/ChunkScheme.java).

**Stan podczas przeglądu:** README słusznie mówi, że siatki nie są thread-safe. Potrzebny jest celowany audyt iloczynów wymiarów, granic int, tablic sąsiedztwa i widoków, a nie ogólne stwierdzenie, że każdy backend jest błędny.

**Znaczenie:** Zbyt duży rozmiar może przepełnić licznik przed alokacją. Widok może nadal czytać zmieniającą się oryginalną siatkę, co wpływa na powtarzalność wyszukiwania.

**Praca do wykonania:** Sprawdź wymiary przed mnożeniem/alokacją, flattening, zakresy półotwarte i granice chunków. Zdefiniuj własność tablic sąsiedztwa oraz widoków i warunek stabilnych danych podczas operacji. Nie przeprowadzaj testów wymagających faktycznej wielogigabajtowej alokacji.

**Warunki zamknięcia:**

- [x] Małe testy błędnych i skrajnych wymiarów potwierdzają przewidywalne odrzucenie bez dużych alokacji.
- [x] Widoki, sąsiedztwa i iteratory mają opis własności danych i zachowania po mutacji.
- [x] README rozróżnia koszt pełnej objętości, odwiedzonych komórek i danych rzadkich, uwzględniając pamięć.

**Powiązania:** [NAV-002](../Ashnav/ISSUES.md#nav-002) i [NAV-004](../Ashnav/ISSUES.md#nav-004) opierają się na tych kontraktach; nie dodawaj synchronizacji bez wymagania obsługi współbieżności.

**Wynik korekty 2026-09-09:** Dodano wspólne sprawdzanie dodatniej objętości int przed alokacją/flatteningiem w dense/bit/chunk, flood fill, components, morphology i distance. Bounds/neighbors nie zawijają int, subgrid/slice sprawdzają położenie, inclusive region kończy się przy MAX_VALUE, puste ułamkowe AABB nie odwiedza komórek. Własność widoków i tablic, ograniczenia mutacji oraz koszty pamięci opisano w API/README. GridLimitsTest/OperationContractTest używają małych siatek i niealokujących atrap. Poprawiono też flood fill na clamped view i klasyfikację masek pośrednich morfologii.

<a id="grid-005"></a>

## GRID-005 — Zweryfikować kontrakty i pakowanie providerów SPI

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** AUDYT  
**Kontrakt:** sekcje 4.1, 4.2, 4.5, 6

**Gdzie:** [ServiceDiscoveryTest.java](src/test/java/nsk/nu/ashgrid/spi/ServiceDiscoveryTest.java), [RegionIterators.java](src/main/java/nsk/nu/ashgrid/api/voxel/region/RegionIterators.java), [DDA3DTraverser.java](src/main/java/nsk/nu/ashgrid/implementation/voxel/traversal/DDA3DTraverser.java), [README.md](README.md).

**Stan podczas przeglądu:** Quick start pobiera traverser przez ServiceRegistry.require("dda"). W projekcie są testy SPI, więc należy ocenić ich zakres i zasoby w rzeczywistym JAR, zamiast zakładać brak testów.

**Znaczenie:** Kod może działać z klas IDE, a po spakowaniu nie odnajdywać providera. Zmiana identyfikatora to zmiana zachowania dla klientów.

**Praca do wykonania:** Sprawdź pliki META-INF/services, tożsamość providerów, wykrywanie duplikatów i zgodność każdej implementacji z interfejsem. Nie narzucaj identycznego zestawu odwiedzin zwykłej rasteryzacji i supercover, jeśli kontrakty są różne.

**Warunki zamknięcia:**

- [x] Quick start działa ze spakowanym JAR i identyfikatorami opisanymi w README.
- [x] Testy obejmują rejestrację wszystkich reklamowanych providerów i te wspólne właściwości, które gwarantują ich interfejsy.
- [x] Kontrakt kolejności pochodzi z [CORE-003](../Ashcore/ISSUES.md#core-003) lub jest jawnie ograniczony do identyfikowanego algorytmu.

**Powiązania:** [CORE-003](../Ashcore/ISSUES.md#core-003); konsumenci SPI obejmują Ashtrace.

**Wynik korekty 2026-09-09:** PackagedArtifactIT sprawdza siedem zasobów/identyfikatorów, pochodzenie klas z głównego JAR, brakujący i zdublowany ID oraz kompilację/wykonanie README bez klas projektu na classpath. Zachowano nieokreśloną kolejność ServiceRegistry i jawny wybór ID zgodnie z CORE-003; providerzy mają własne reguły kolejności. Audyt wykazał niepełny supercover (10 zamiast 22 kontaktów narożnikowych); poprawiona implementacja przechodzi niezależny test 343 odcinków w obu kierunkach. RegionIterators/AABBVoxelIterator nie reklamują rejestracji SPI.

<a id="grid-006"></a>

## GRID-006 — Utrzymać zgodność SquareXZChunkScheme i helperów

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** DECYZJA  
**Kontrakt:** sekcje 3.2, 5, 5.1, 8

**Gdzie:** [SquareXZChunkScheme.java](src/main/java/nsk/nu/ashgrid/implementation/grid/indexing/SquareXZChunkScheme.java), [VoxelSpace.java](src/main/java/nsk/nu/ashgrid/api/voxel/space/VoxelSpace.java), [README.md](README.md).

**Stan podczas przeglądu:** SquareXZChunkScheme znajduje się w implementation, ale oficjalne przykłady Ashspace i Ashnav go importują. VoxelSpace pełni istniejącą rolę mapowania skali i początku.

**Znaczenie:** Proste przeniesienie klasy lub usunięcie helpera ze względu na nowy podział odpowiedzialności może zepsuć istniejące programy.

**Praca do wykonania:** Jawnie określ wspierane API i zaplanuj ewentualną fasadę/deprecation z zachowaniem migracji. Wyjaśnij granicę jednostkowej siatki i frame-aware mapowania w Ashspace.

**Warunki zamknięcia:**

- [x] Oficjalne przykłady klientów nadal kompilują się albo istnieje świadoma, wersjonowana migracja.
- [x] README zawiera obietnicę stabilności używanych publicznie implementacji i konkretne ograniczenia.
- [x] Zmiany mapowania i walidacji oceniono pod względem zachowania, nie tylko sygnatur.

**Powiązania:** [SPACE-001](../Ashspace/ISSUES.md#space-001), [SPACE-005](../Ashspace/ISSUES.md#space-005) i [NAV-007](../Ashnav/ISSUES.md#nav-007); uzgodnienia API prowadzi Ashgrid.

**Wynik korekty 2026-09-09:** SquareXZChunkScheme pozostaje wspieranym publicznym API w dotychczasowym pakiecie, podobnie VoxelSpace. Zapisano zgodność źródłową/binarną, zmiany zachowania i ograniczenia jednostek. javap porównał 70 klas: zero usuniętych sygnatur, dwie dodane metody GridMath. Przykłady Ashspace/Ashtrace kompilują się i działają; snippet Ashnav po dodaniu wyłącznie class/main również. Rozwojowa wersja 1.3.0-SNAPSHOT nie nadpisuje 1.2.0.

<a id="grid-007"></a>

## GRID-007 — Dostosować CI, pakowanie i dowody wydania

**Status:** GOTOWE

**Priorytet:** P1  
**Dowód:** INSPEKCJA  
**Kontrakt:** sekcje 2, 4.5, 6

**Gdzie:** [pom.xml](pom.xml), [.github/workflows/maven.yml](.github/workflows/maven.yml), [.github/workflows/publish.yml](.github/workflows/publish.yml), [README.md](README.md).

**Stan podczas przeglądu:** CI uruchamia mvn -B package, a kontrakt wymaga clean verify. POM ustawia source/target 21 bez jawnego przypięcia maven-compiler-plugin; Javadoc ma doclint=none i failOnError=false. Profil central istnieje, lecz pokazany workflow deploy nie aktywuje go i publikuje do GitHub Packages. Początkowa gałąź: master; CI filtruje master. Wybór JAR wyklucza sources/javadoc, ale nadal bierze pierwszy pasujący plik.

**Znaczenie:** Zielony wynik obecnego CI nie jest dowodem wykonania całej bramki jakości ani obecności artefaktu w Maven Central. Brak automatyzacji Central nie dowodzi braku publikacji ręcznej.

**Praca do wykonania:** Ustaw rzeczywistą bramkę clean verify, dobierz przypięty compiler plugin i release 21, sprawdź generowanie dokumentacji oraz jednoznaczną identyfikację artefaktów. Potwierdź utrzymywane gałęzie, docelowe wersje zależności i sposób publikacji do każdej używanej destynacji. JUnit pozostaw w test scope; nie usuwaj go w imię niezależności produkcyjnej.

**Warunki zamknięcia:**

- [x] Zapisano wynik mvn -B clean verify z wymaganymi testami oraz wersje JDK/Maven; CI obejmuje faktycznie utrzymywane gałęzie i PR-y.
- [x] Główny JAR, sources, Javadoc i wymagane zasoby są sprawdzone. Błędny Javadoc nie jest po cichu uznawany za poprawny; nie trzeba przy tym mechanicznie włączać każdej reguły stylistycznej doclint.
- [x] Wskazano używane cele publikacji, tag/wersję i dowody dostępności albo jawnie pozostawiono publikację jako niezweryfikowaną. Sam deploy nie służy jako test poprawek.
- [x] Sprawdzono efektywne zależności i ich scope; test integracyjny korzysta z zamierzonej wersji dolnej warstwy, a nie przypadkowej starej kopii z lokalnego Maven.

**Powiązania:** Wspólny wzorzec: [TEMPLATE-001](../Ashtemplate/ISSUES.md#template-001) i [TEMPLATE-002](../Ashtemplate/ISSUES.md#template-002). Tę korektę można wykonać niezależnie od napraw algorytmów. Istniejącego numeru wydania nie nadpisuj innym artefaktem.

**Wynik korekty 2026-09-09:** Końcowe mvn -B clean verify oraz dependency:tree: 80 testów jednostkowych + 4 testy artefaktów PASS, JDK 25.0.2, Maven 3.9.16, compiler 3.13.0 release 21. Javadoc all,-missing/failOnError=true PASS. IntelliJ build PASS. CI obejmuje wszystkie branche i PR-y, Temurin 21/25, dokładne nazwy main/sources/Javadoc. Domyślny Ashcore 1.0.1 oraz osobny wariant 1.1.0-SNAPSHOT zostały zidentyfikowane i przetestowane; JUnit pozostaje test-only. Publikacja do Packages/Release/Central i zdalne CI są jawnie niezweryfikowane; nie uruchamiano deploy. Szczegóły w docs/RELEASE.md.

## Stan przekazania i dziennik sesji

**Historyczny stan przed korektą 2026-09-09:** wszystkie zadania pozostawały OTWARTE. Utworzono dokumentację; nie wprowadzono korekt kodu, nie wykonano buildów bibliotek ani publikacji. Nie uznawaj samego dodania ISSUES.md za realizację żadnego zadania.

**Aktualny stan:** GRID-001–GRID-007 GOTOWE w opisanym zakresie. Gałąź fix/ashgrid-issues-20260909, snapshot wejściowy d58bfba, wersja 1.3.0-SNAPSHOT. Lokalnie 80 + 4 testy Ashgrid oraz 103 testy konsumentów PASS; testy Ashgrid przechodzą z Ashcore 1.0.1 i 1.1.0-SNAPSHOT. Dowody i migracja: [docs/RELEASE.md](docs/RELEASE.md). To nie jest deklaracja pełnego audytu każdego publicznego API ani publikacji.

**Następny krok:** przed wydaniem uruchomić zdalne CI Java 21/25, wybrać niewydany numer i docelową opublikowaną wersję Ashcore oraz potwierdzić destynacje. Kontynuować zakresy SPACE-001/SPACE-005, TRACE-002 i NAV-007 w ich repozytoriach; nie mylić lokalnych prób integracyjnych z aktualizacją ich POM.

Po kolejnej sesji dopisz wiersz i uzupełnij statusy odpowiednich zadań. Zapisz także nieudane próby i ograniczenia środowiska; nie opisuj kontroli niewykonanej jako zaliczonej.

| Data / commit | ID i decyzja | Zmiana | Polecenie / test i rzeczywisty wynik | Pozostałe zależności / następny krok |
| --- | --- | --- | --- | --- |
| 2026-09-09 / punkt odniesienia powyżej | Wszystkie: OTWARTE | Utworzenie planu korekt | Inspekcja statyczna; testów bibliotek nie uruchomiono | Rozpocząć od wskazanego P1 |

| 2026-09-09 / commit dodający ten wpis; snapshot d58bfba | GRID-001–GRID-007: GOTOWE | Korekty algorytmów, zakresów, danych, kontraktów API/SPI, Javadoc, CI; wersja 1.3.0-SNAPSHOT | Dwie pierwsze regresje FAIL przed poprawką; supercover 10/22 FAIL. Końcowe clean verify 80 + 4 PASS, JDK 25.0.2/Maven 3.9.16/release 21. Ashspace 31, Ashtrace 43, Ashnav 29 PASS; przykłady i fixture SPACE-001/TRACE-002 PASS; javap bez usuniętych sygnatur | Publikacja i zdalne CI niewykonane; szersze kwestie mappera pozostają w SPACE-001. Dowody/SHA/polecenia: docs/RELEASE.md |
