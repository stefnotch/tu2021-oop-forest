# tu2021-oop-forest
The task was to simulate some trees and a few bug populations. Each bug population had to be its own thread. They could eat the trees (shared datastructure) they were sitting on and each other (stopping threads). They could also reproduce (creating threads) or painfully starve to death.

This solution creates an entire "waiting graph", just like how one would imagine a very simplistic database to work. Pretty fun idea ;)

The intended solution was to enforce a total ordering of locks, so that in the worst case, when everyone locks their previous neighbor, the bottom right one can still do its job.

Also, Java is a terrible programming language. Not just because multithreading is so unsafe in it.