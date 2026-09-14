# Emergency Vehicle Route Planner

A Java mini project that uses the **Floyd–Warshall algorithm** to find the shortest route between all pairs of city locations. It is designed as an emergency-response example: an ambulance, fire engine, or police vehicle can immediately obtain the best route to a destination.

## Why Floyd–Warshall?

An emergency can happen at any city location. Floyd–Warshall calculates shortest paths for **every source-destination pair** in one run. Once the calculation is complete, the system can respond instantly to a route request.

* **Input:** A weighted city road network.
* **Output:** The shortest route and total distance between selected locations.
* **Time complexity:** `O(V³)`
* **Space complexity:** `O(V²)`

## Features

* Select a start location and destination.
* View the reconstructed shortest route and total distance.
* Inspect the direct-road matrix and all-pairs shortest-distance matrix.
* Demonstrates route reconstruction using a `next` matrix.
* Runs using only the Java standard library (Java Swing).

## Run the project

Open a terminal in this folder and run:

```powershell
javac -d out src/*.java
java -cp out Main
```

Or, on Windows, simply double-click `run.bat`.

## Demo example

Choose **Central Hospital** as the starting location and **Accident Zone** as the destination. The application displays the shortest route and its distance. Open the **Direct Road Network** tab first to show that not all places have a direct road, then open **All-Pairs Shortest Distances** to show how Floyd–Warshall has calculated alternatives.

## Explanation for presentation

> This application models city locations as vertices and roads as weighted edges. The Floyd–Warshall algorithm repeatedly checks whether an intermediate location produces a shorter route between two locations. It stores both the shortest distance and the next location in the route. This is useful for emergency services because shortest routes for any pair of locations are already available when an emergency request arrives.

## Suggested GitHub upload

```powershell
git init
git add .
git commit -m "Add Floyd-Warshall emergency route planner"
```
