layout py licensing information please do not distribute or publish solutions to this project you are free to use and extend these projects for educational purposes the pacman ai projects were developed at uc berkeley primarily by john denero denero cs berkeley edu and dan klein klein cs berkeley edu for more info see http inst eecs berkeley edu cs188 sp09 pacman html from util import manhattandistance from game import grid import os import random visibility_matrix_cache class layout a layout manages the static information about the game board def __init__ self layouttext self width len layouttext 0 self height len layouttext self walls grid self width self height false self food grid self width self height false self capsules self agentpositions self numghosts 0 self processlayouttext layouttext self layouttext layouttext self initializevisibilitymatrix def getnumghosts self return self numghosts def initializevisibilitymatrix self global visibility_matrix_cache if reduce str __add__ self layouttext not in visibility_matrix_cache from game import directions vecs 0.5 0 0.5 0 0 0.5 0 0.5 dirs directions north directions south directions west directions east vis grid self width self height directions north set directions south set directions east set directions west set directions stop set for x in range self width for y in range self height if self walls x y false for vec direction in zip vecs dirs dx dy vec nextx nexty x dx y dy while nextx nexty int nextx int nexty or not self walls int nextx int nexty vis x y direction add nextx nexty nextx nexty x dx y dy self visibility vis visibility_matrix_cache reduce str __add__ self layouttext vis else self visibility visibility_matrix_cache reduce str __add__ self layouttext def iswall self pos x col pos return self walls x col def getrandomlegalposition self x random choice range self width y random choice range self height while self iswall x y x random choice range self width y random choice range self height return x y def getrandomcorner self poses 1 1 1 self height 2 self width 2 1 self width 2 self height 2 return random choice poses def getfurthestcorner self pacpos poses 1 1 1 self height 2 self width 2 1 self width 2 self height 2 dist pos max manhattandistance p pacpos p for p in poses return pos def isvisiblefrom self ghostpos pacpos pacdirection row col int x for x in pacpos return ghostpos in self visibility row col pacdirection def __str__ self return n join self layouttext def deepcopy self return layout self layouttext def processlayouttext self layouttext coordinates are flipped from the input format to the x y convention here the shape of the maze each character represents a different type of object wall food o capsule g ghost p pacman other characters are ignored maxy self height 1 for y in range self height for x in range self width layoutchar layouttext maxy y x self processlayoutchar x y layoutchar self agentpositions sort self agentpositions i 0 pos for i pos in self agentpositions def processlayoutchar self x y layoutchar if layoutchar self walls x y true elif layoutchar self food x y true elif layoutchar o self capsules append x y elif layoutchar p self agentpositions append 0 x y elif layoutchar in g self agentpositions append 1 x y self numghosts 1 elif layoutchar in 1 2 3 4 self agentpositions append int layoutchar x y self numghosts 1 def getlayout name back 1 set the layout directory and name to check for different enviroment setups layoutdir layoutname name strip if name endswith lay else name strip lay if os path isdir layouts layoutdir layouts elif os path isdir src layoutdir src if os path isdir layoutdir+ layouts layoutdir layouts layout trytoload layoutdir layoutname if layout none layout trytoload layoutname try again in super directory if layout none and back 0 curdir os path abspath os chdir layout getlayout name back 1 os chdir curdir return layout def trytoload fullname if not os path exists fullname return none f open fullname try return layout line strip for line in f finally f close
