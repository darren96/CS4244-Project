#!/usr/bin/python

import os, sys, subprocess

# list of strings:
clauses=[]

# k=tuple: ("high-level" variable name, number of bit (0..4))
# v=variable number in CNF
vars={}
vars_last=1

def read_lines_from_file (fname):
    f=open(fname)
    new_ar=[item.rstrip() for item in f.readlines()]
    f.close()
    return new_ar

def write_CNF(fname, clauses, VARS_TOTAL):
    f=open(fname, "w")
    f.write ("p cnf "+str(VARS_TOTAL)+" "+str(len(clauses))+"\n")
    [f.write(c+" 0\n") for c in clauses]
    f.close()

def mathematica_to_CNF(s, d):
    for k in d.keys():
        s=s.replace(k, d[k])
    s=s.replace("!", "-").replace("||", " ").replace("(", "").replace(")", "")
    s=s.split ("&&")
    return s

def add_popcnt1(v1, v2, v3, v4, v5):
    global clauses
    s="(!a||!b)&&" \
      "(!a||!c)&&" \
      "(!a||!d)&&" \
      "(!a||!e)&&" \
      "(!b||!c)&&" \
      "(!b||!d)&&" \
      "(!b||!e)&&" \
      "(!c||!d)&&" \
      "(!c||!e)&&" \
      "(!d||!e)&&" \
      "(a||b||c||d||e)"

    clauses=clauses+mathematica_to_CNF(s, {"a":v1, "b":v2, "c":v3, "d":v4, "e":v5})

def add_right_or_left(n1, n2):
    global clauses
    s="(!a1||!b1)&&(!a1||!c1)&&(!a1||!d1)&&(!a1||!e1)&&(a1||b1||c1||d1||e1)&&(!a2||b1)&&" \
      "(!a2||!b2)&&(!a2||!c2)&&(!a2||!d2)&&(!a2||!e2)&&(a2||b2||c1||c2||d1||e1)&&(a2||b2||c2||d1||d2)&&" \
       "(a2||b2||c2||d2||e2)&&(!b1||!b2)&&(!b1||!c1)&&(!b1||!d1)&&(!b1||!e1)&&(b1||b2||c1||d1||e1)&&" \
       "(!b2||!c2)&&(!b2||!d1)&&(!b2||!d2)&&(!b2||!e1)&&(!b2||!e2)&&(!c1||!c2)&&(!c1||!d1)&&(!c1||!e1)&&" \
       "(!c2||!d2)&&(!c2||!e1)&&(!c2||!e2)&&(!d1||!d2)&&(!d1||!e1)&&(!d2||!e2)"
    
    clauses=clauses+mathematica_to_CNF(s, {
	"a1": vars[(n1,0)], "b1": vars[(n1,1)], "c1": vars[(n1,2)], "d1": vars[(n1,3)], "e1": vars[(n1,4)],
	"a2": vars[(n2,0)], "b2": vars[(n2,1)], "c2": vars[(n2,2)], "d2": vars[(n2,3)], "e2": vars[(n2,4)]})

def add_right(n1, n2):
    global clauses
    s="(!a1||!b1)&&(!a1||!c1)&&(!a1||!d1)&&(a1||b1||c1||d1)&&!a2&&(!b1||!b2)&&(!b1||!c1)&&(!b1||!d1)&&" \
      "(b1||b2||c1||d1)&&(!b2||!c1)&&(!b2||!c2)&&(!b2||!d1)&&(!b2||!d2)&&(!b2||!e2)&&(b2||c1||c2||d1)&&" \
      "(b2||c2||d1||d2)&&(b2||c2||d2||e2)&&(!c1||!c2)&&(!c1||!d1)&&(!c2||!d1)&&(!c2||!d2)&&(!c2||!e2)&&" \
      "(!d1||!d2)&&(!d2||!e2)&&!e1"

    clauses=clauses+mathematica_to_CNF(s, {
	"a1": vars[(n1,0)], "b1": vars[(n1,1)], "c1": vars[(n1,2)], "d1": vars[(n1,3)], "e1": vars[(n1,4)],
	"a2": vars[(n2,0)], "b2": vars[(n2,1)], "c2": vars[(n2,2)], "d2": vars[(n2,3)], "e2": vars[(n2,4)]})

# n=1..5
def add_eq_var_n(name, n):
    global clauses
    global vars
    for i in range(5):
        if i==n-1:
            clauses.append(vars[(name,i)]) # always True
        else:
            clauses.append("-"+vars[(name,i)]) # always False

def alloc_distinct_variables(names):
    global vars
    global vars_last
    for name in names:
        for i in range(5):
            vars[(name,i)]=str(vars_last)
            vars_last=vars_last+1

        add_popcnt1(vars[(name,0)], vars[(name,1)], vars[(name,2)], vars[(name,3)], vars[(name,4)])

    # make them distinct:
    for i in range(5):
        clauses.append(vars[(names[0],i)] + " " + vars[(names[1],i)] + " " + vars[(names[2],i)] + " " + vars[(names[3],i)] + " " + vars[(names[4],i)])

def add_eq_clauses(var1, var2):
    global clauses
    clauses.append(var1 + " -" + var2)
    clauses.append("-"+var1 + " " + var2)

def add_eq (n1, n2):
    for i in range(5):
        add_eq_clauses(vars[(n1,i)], vars[(n2, i)])

def print_vars(clause):
    for c in clause:
        if c.startswith("-")==False and c!="0":
            # https://stackoverflow.com/questions/8023306/get-key-by-value-in-dictionary
            t=vars.keys()[vars.values().index(c)]
            print t[0], t[1]+1

alloc_distinct_variables(["Red", "Green", "White", "Yellow", "Blue"])
alloc_distinct_variables(["Brit", "Swede", "Dane", "Norwegian", "German"])
alloc_distinct_variables(["Tea", "Coffee", "Milk", "Beer", "Water"])
alloc_distinct_variables(["PallMall", "Dunhill", "Blends", "Bluemasters", "Prince"])
alloc_distinct_variables(["Dogs", "Birds", "Cats", "Horse", "Fish"])

# 1. The Brit lives in the red house
add_eq("Brit","Red")

# 2. The Swede keeps dogs as pets
add_eq("Swede","Dogs")

# 3. Dane drinks tea
add_eq("Dane","Tea")

# 4. Green house is on the left of the white house
add_right("Green", "White")

# 5. Green house's owner drinks coffee
add_eq("Green","Coffee")

# 6. Person who smokes Pall Mall rears birds
add_eq("PallMall","Birds")

# 7. Owner of yellow house smokes Dunhill
add_eq("Yellow","Dunhill")

# 8. Man living in the center house drinks milk
add_eq_var_n("Milk", 3) # i.e., 3rd house

# 9. Norwegian lives in the first house
add_eq_var_n("Norwegian", 1)

# 10. Man who smokes Blends lives next to the one who keeps cats
add_right_or_left("Blends","Cats") # left or right

# 11. Man who keeps horse lives next to the man who smokes Dunhill
add_right_or_left("Horse","Dunhill") # left or right

# 12. Owner who smokes Bluemasters drinks beer
add_eq("Bluemasters","Beer")

# 13. German smokes Prince
add_eq("German","Prince")

# 14. Norwegian lives next to the blue house
add_right_or_left("Norwegian", "Blue") # left or right

# 15. Man who smokes Blends has a neighbor who drinks water
add_right_or_left("Blends", "Water") # left or right
# Tried add_right("Blends", "Water") but don't think that is right, since the neighbor can be on left or right

write_CNF("einstein.cnf", clauses, vars_last-1)