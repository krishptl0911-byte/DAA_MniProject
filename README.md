# 🚨 911 Emergency Vehicle Route Planner & DAA Algorithm Visualizer

A comprehensive **Design and Analysis of Algorithms (DAA)** mini project that simulates real-time emergency vehicle dispatching (Ambulances, Fire Engines, Police Cruisers) across an urban city network using the **Floyd–Warshall Algorithm** ($O(V^3)$ Dynamic Programming) alongside **Dijkstra's Algorithm** for comparative benchmarking.

---

## 🌟 Key Features

### 1. 🗺️ Live 2D Interactive City Graph & Dispatch Simulation
* **Interactive Java2D Canvas:** Anti-aliased graphical city map featuring custom visual badges and glowing halos for city facilities (Hospitals 🏥, Fire Stations 🚒, Police Stations 🚓, Airports ✈️, Transit Stations 🚉, Incident Sites ⚠️).
* **Live Emergency Vehicle Animation:** Real-time animated vehicles traveling along the shortest path with siren flashing effects (red/blue strobes), heading rotation, and speed profiles.
* **Drag-and-Drop Positioning:** Click and drag any city location across the map in real-time.
* **Dynamic Road Blockage & Obstacle Toggling:** Click on any road edge on the canvas to simulate accidents, road closures, or traffic blockades — triggering live recalculation of the all-pairs shortest paths.
* **Automated Closest Unit Dispatch:** Uses the precomputed $O(1)$ all-pairs distance lookup matrix to find and dispatch the nearest available emergency response unit to any incident location.

### 2. ⚡ Floyd–Warshall Step-by-Step Dynamic Programming Visualizer
* **Interactive Step Player:** Step Forward, Step Backward, Play, Pause, and Jump controls.
* **Live Recurrence Formula Breakdown:** Displays the DP transition formula:
  $$D^{(k)}[i][j] = \min\left(D^{(k-1)}[i][j],\, D^{(k-1)}[i][k] + D^{(k-1)}[k][j]\right)$$
* **Color-Coded Matrix Highlighting:**
  - 🟨 **Amber**: Currently evaluated cell $(i, j)$
  - 🟩 **Emerald**: Cell updated with a shorter path via intermediate pivot $k$
  - 🟦 **Blue & Cyan**: Intermediate path components $D[i][k]$ and $D[k][j]$
  - 🟪 **Indigo**: Active pivot row and column $k$

### 3. 📋 Matrix Inspector & Route Reconstruction
* **All-Pairs Shortest Distances ($D[i][j]$):** Complete distance table between every source and destination.
* **Next-Hop Matrix ($\Pi[i][j]$):** Demonstrates path reconstruction in $O(L)$ time.
* **Direct Road Network Matrix:** Underlying adjacency matrix representation.

### 4. 📊 DAA Research & Complexity Benchmarks
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
| **Inspect DP Steps** | Navigate to the **⚡ Floyd–Warshall DP Step Visualizer** tab and press **Play** |

---

## 📖 DAA Theoretical Concept for Presentation

> In emergency dispatch systems, incident requests arrive unpredictably at any location in the city network. Rather than executing a single-source search (like Dijkstra) on every individual emergency call, the **Floyd–Warshall algorithm** precomputes the shortest path for all $V \times V$ pairs once in $O(V^3)$ time during initialization. When an emergency call occurs, the response system can immediately retrieve the optimal route and locate the closest response vehicle in $O(1)$ time, guaranteeing minimum response delay.
