# brackettree
Interfejs javy dla Nawiasowca - prostego formatu danych.

Implementacja wspiera wbudowany mechanizm serializacji Javy, potrafi zapisać/wczytać funkcje lambda, tablice, rekordy i całe grafy referencji. Pozwala na interpretowanie podstawowych typów danych (int, boolean ...) oraz automatyczną serializację/deserializację prostych obiektów w czytelnej dla ludzi formie (przy pomocy mechanizmów reflekcji). Udostępnia mechanizmy do przechowywania otwartego grafu referencji (przydatne, gdy serializowana jest część stanu programu). Potrafi zastąpić proste bazy danych bez potrzeby konfiguracji i projektowania tabel.

Tworzona zgodnie z filozofią T0D0 (Tests - 0, Documentation - 0).


### Notacja BracketTree
```node1[ node2 ]``` &#x21e6; 'node2' jest potomkiem 'node1'. 'node1' jest potomkiem korzenia<br><br><br>
```node3[ node4 [] node5 ]``` &#x21e6; 'node4' i 'node5' są potomkami 'node3'. 'node3' jest potomkiem korzenia. Symbol '[]' jest niezbędny do oddzielenia 'node4' od 'node5'.<br><br>
```
nodeA[ 
  nodeB[ nodeC ]
  nodeD[]
]
nodeE[ nodeF ]
```
&#x21e7; 'nodeC' jest potomkiem 'nodeB'. 'nodeB' i 'nodeD' są potomkami 'nodeA'. Symbol '[]' po ostatnim potomku jest opcjonalny. 'nodeF' jest potomkiem 'nodeE'. 'nodeA' i 'nodeE' są potomkami korzenia.<br><br><br>
```| nodeG | [ nodeH ]``` &#x21e6; 'nodeH' jest potomkiem ' nodeG '. Jeśli wymiar tekstowy nie jest aktywny, początkowe i końcowe białe znaki są usuwane. Wymiar tekstowy jest otwierany przez znak '|'.<br><br><br>
```|nodeJ | [ ~~| node|~ |~~ ]``` &#x21e6; 'nodeJ ' jest potomkiem ' node|~ '. Napis występujący bezpośrednio przed znakiem '|' jest hasłem portalu. Wymiar tekstowy jest zamykany przez hasło portalu poprzedzone znakiem '|'. Dla ' node|~ ' hasłem portalu jest '\~~', dla 'nodeJ ' hasło portalu jest puste.<br><br><br>
