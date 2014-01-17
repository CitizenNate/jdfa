jdfa
====

A java based graphical editor for Finite State Machines. 

controls:
arrow keys - move selected object
1 - cycle focus through states
2 - cycle focus through out transitions of currently selected state
q - new state
w - new transition
a - set label on current selection
s - bend transition left
d - bend transition right
z - reduce ring count on state
x - increase ring count on state
c - swap label from one side of transition to other

Saving produces the save file, as well as a txt file that tests the DFA, and a tex file for the tikzpicture.