
 A Web Opinion Visualiser.
 
 Artificial Intelligence Project.

 I was required to develop a multithreaded AI search application that can generate a word
 cloud from the top 20 - 32 words associated with an internet search term.
 
 Project Overview.
 
 As an index page loaded a user given an opportunity to select a number of words to display for 
 a visual summary of the most prominent words used on a web page.
 If a user leaves an input box blank ,default string would display "Empty search term".
 
 For this project i used Best First Search algorithm.Best First Search refers to a family of algorithms that use a heuristic evaluation function to expand the most promising node from a selection of candidate nodes in a
 search tree or semantic network.
 The entire node queue is sorted after the child nodes of the current node have been added.
 A priority queue is normally used in best first approaches to performing the sorting of nodes as they are discovered.
 PriorityQueue is being used for storing nodes sorted by Comparator based on heuristic score and ConcurrentSkipListSet is used to track visited nodes.
 
 Heuristic score was achieved by giving title most priority as 70 weight,heading 1 was given 20 and for any text in the body 10 weight that was done via JSoup API traversing trough the Document Nodes from the Net.
 After the Heuristic scored node is passed to Fuzzy Logic algorithm based fuzzy set and rules i have written the scored nodes are filtered for highest score and put into Map to determine top 20 - 32 words associated with an internet search term.
 
 The Project is multithreaded by using Future and ExecutorService.Future <V> is an interface that represents the result of an asynchronous computation. Once the computation is finished, we can obtain the result of it by using the get() method.This is a blocking operation and waits until the outcome (V) is available.
 
 
 
 
 
 
 