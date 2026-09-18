# 🚨 Metro 911 Emergency Route Planner

A **Design and Analysis of Algorithms (DAA)** mini project that simulates real-time emergency dispatching (ambulances, fire engines and police cruisers) across an interactive metropolitan street map. It uses the **Floyd–Warshall Algorithm** ($O(V^3)$) to precompute routes and **Dijkstra's Algorithm** only for benchmark comparison.

---

## 🌟 Key Features

### 1. 🗺️ Live Interactive Street Map & Dispatch Simulation
* **Map-style Java2D canvas:** A clean offline street-map baselayer with water, parks, local streets and clearly labelled emergency locations. It has no external API key, billing account or network dependency.
* **Live Emergency Vehicle Animation:** Real-time animated vehicles traveling along the shortest path with siren flashing effects (red/blue strobes), heading rotation, and speed profiles.
* **Drag-and-Drop Positioning:** Click and drag any city location across the map in real-time.
* **Dynamic Road Blockage & Obstacle Toggling:** Click on any road edge on the canvas to simulate accidents, road closures, or traffic blockades — triggering live recalculation of the all-pairs shortest paths.
* **Automated Closest Unit Dispatch:** Uses the precomputed $O(1)$ all-pairs distance lookup matrix to find and dispatch the nearest available emergency response unit to any incident location.

### 2. 📋 Route Data & Reconstruction
* **All-Pairs Shortest Distances ($D[i][j]$):** Complete distance table between every source and destination.
* **Next-Hop Matrix ($\Pi[i][j]$):** Demonstrates path reconstruction in $O(L)$ time.
* **Direct Road Network Matrix:** Underlying adjacency matrix representation.

### 3. 📊 DAA Research & Complexity Benchmarks
* **Live Performance Profiling:** Microsecond runtime benchmarking comparing Floyd–Warshall against $V \times \text{Dijkstra}$.
* **Theoretical Complexity Comparison:** Side-by-side comparison of Floyd–Warshall ($O(V^3)$), Dijkstra ($O((V+E)\log V)$), Bellman-Ford ($O(VE)$), and A* Search.
* **Graph Topology Metrics:** Real-time calculation of Vertices $|V|$, Edges $|E|$, Graph Density, and Network Diameter.

---

## 🚀 How to Run

### Option 1: Quick Batch Script (Windows)
Double-click `run.bat` or run:
```powershell
.\run.bat
```

### Option 2: Manual Terminal Commands
```powershell
javac -d out src/*.java
java -cp out Main
```

*Requirements: Java SE Development Kit (JDK 8 or higher). Zero external dependencies required.*

---

## 🎮 Interactive Controls Guide

| Action | Control |
|---|---|
| **Select Start Location** | Left-click any node on the canvas or choose from the dropdown |
| **Select Destination** | Right-click any node on the canvas or choose from the dropdown |
| **Move Location** | Click and drag any node across the map |
| **Toggle Road Blockage** | Click on any road segment / distance badge |
| **Dispatch Vehicle** | Click **🚨 Dispatch Vehicle** |
| **Nearest Service Dispatch** | Click **⚡ Closest Unit** |
| **Inspect route data** | Open the **Route Data & Matrices** tab |

---

## 📖 DAA Theoretical Concept for Presentation

> In emergency dispatch systems, incident requests arrive unpredictably at any location in the city network. Rather than executing a single-source search (like Dijkstra) on every individual emergency call, the **Floyd–Warshall algorithm** precomputes the shortest path for all $V \times V$ pairs once in $O(V^3)$ time during initialization. When an emergency call occurs, the response system can immediately retrieve the optimal route and locate the closest response vehicle in $O(1)$ time.
