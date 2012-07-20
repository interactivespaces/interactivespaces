.. _expression-language-chapter-label:

The Expression Language
***********************

The Interactive Spaces Expression Language is used in a variety
of places, from the Activity configurations to the Master API.
It is pretty easy to use and is based on the Apache OGNL expression
language.

Accessing Arrays and Maps
---------------

You can access an array with *[]*. For example, suppose there is an array called
*foo*. *foo[0]* will get the first element in the array.

The same syntax is used for maps. For example, suppose there is a map called
*metadata*. *metadata['author']* will get the map value with key *author*.
If there is no value associated with the given key, the expression will
be equal to *null*.

Conditionals
------------

*e1 ? e2 : e3* is the conditional operator. *e1* is first evaluated. If its value is *true*,
the value of the expression will be *e2*. Otherwise, it will be *e3*.

You can check the equality of two expressions using the *==* or *eq* operators.

