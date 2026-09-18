# Metro 911 Emergency Route Planner — Seminar Guide

## Project in one sentence

Metro 911 is a Java desktop simulation that selects and animates the shortest safe road route for an emergency vehicle, even when roads are closed.

## What problem does it solve?

During an emergency, an ambulance, fire engine or police unit must reach an incident quickly. A driver should not manually compare every possible road. The system models the city as a weighted graph, calculates the best route, shows it on the map and can select the nearest suitable emergency base.

## Why was this project built?

It turns an important DAA topic into a realistic application. Instead of presenting shortest-path formulas alone, it demonstrates how graph algorithms support time-critical public-safety decisions. It also makes the effect of a road closure visible: block a road and the system recalculates a safe alternative route.

## Who uses it?

The intended user is a simulated 911 dispatcher. In a real product, this role would be a call-taker or emergency command-center operator. It is an academic simulation, not a production emergency service.

## Where is it useful?

It applies to a connected urban road network with known locations: hospitals, fire stations, police stations, transit hubs, incident sites and districts. The data in this project is a fictional metropolitan city, so it can be demonstrated safely without depending on a live city map.

## When does it calculate routes?

Routes are precomputed when the application starts and recomputed whenever the dispatcher closes or reopens a road. This makes ordinary dispatch selection immediate after the network is ready.

## How does it work?

1. The city is represented as a weighted, undirected graph.
2. Each place is a vertex; each road is an edge; its length in kilometres is its weight.
3. Floyd–Warshall computes shortest distances for every pair of vertices.
4. A next-hop matrix records how to reconstruct each shortest route.
5. The dispatcher chooses a start, destination and vehicle, or uses Closest Base.
6. The selected path is drawn on the map and the vehicle is animated along it.
7. When a road is blocked, its weight becomes infinity, the matrices are recalculated and the displayed route changes.

## Why Floyd–Warshall?

Floyd–Warshall is appropriate here because calls can occur at any location and the dispatcher may need routes between many pairs of locations. It performs a one-time all-pairs precomputation in `O(V³)` time and stores `O(V²)` data. After that, the distance for a requested pair is available in `O(1)` time; the actual route is reconstructed by following next hops in `O(L)`, where `L` is the number of locations on the route.

For this project there are 18 vertices, so the algorithm performs at most `18³ = 5,832` comparison stages—small enough for an instant desktop demonstration.

## Why is Dijkstra included?

Dijkstra is not used to dispatch the vehicle. It is used in the analytics tab as a comparison. Dijkstra is excellent for one source at a time, while Floyd–Warshall is better for this project’s precomputed all-pairs dispatch model. The benchmark demonstrates the difference between a greedy single-source algorithm and dynamic programming.

## Why was the DP step visualizer removed?

It was a teaching-only animation of every matrix update. It added many controls but did not help a dispatcher choose or travel a route. The project now keeps the meaningful DAA material—the route matrices, formula and benchmark—while making the live emergency map the main presentation.

## What does each source file do?

| File | Responsibility |
| --- | --- |
| `Main.java` | Starts the Swing application. |
| `RoutePlannerUI.java` | Builds the window, tabs, dispatch controls, route summary and event handling. |
| `CityGraph.java` | Defines the 18 locations, road weights, road closures and graph helper methods. |
| `FloydWarshall.java` | Computes the all-pairs distance matrix and next-hop matrix. |
| `Dijkstra.java` | Runs a single-source shortest-path calculation for benchmark comparison. |
| `EmergencyVehicle.java` | Stores vehicle type, route state, speed, location and siren animation state. |
| `GraphVisualizerPanel.java` | Draws the interactive map, roads, markers, selected route and moving vehicle. |
| `AnalyticsPanel.java` | Displays topology metrics, timing and algorithm comparison. |
| `UITheme.java` | Provides common colors, typography and button styling. |
| `CaptureScreenshots.java` | Exports screenshots of the three presentation tabs to `screenshots/`. |

## What does each screen do?

| Screen | Purpose |
| --- | --- |
| **Map & 911 Dispatch** | Main demo screen. Select start and destination, choose a vehicle, dispatch it, find the closest base and simulate road closures. |
| **Route Data & Matrices** | Shows shortest distance, next-hop and direct-road matrices. Use it as evidence that the routing result is computed, not guessed. |
| **DAA Research & Benchmarks** | Explains complexity, measures the algorithms and shows graph metrics such as locations, active roads, density and diameter. |

## Suggested 45-second demonstration

“This system models a city as a weighted graph. Locations are vertices and roads are weighted edges. At startup, Floyd–Warshall computes every shortest path, so a dispatcher can obtain a route or nearest unit immediately. I select an incident and press Closest Base; the application selects the suitable emergency base and animates the route. If I close a road, the graph changes, the matrices are recomputed and the system chooses a new safe route. Dijkstra is included only to compare the all-pairs dynamic-programming approach with a single-source greedy approach.”

## Likely viva questions

**Is this real 911 software?** No. It is an academic simulation with fictional locations and road lengths.

**Can Floyd–Warshall handle road closure?** Yes. The closed edge is removed from the active adjacency matrix and Floyd–Warshall is run again.

**Does it handle negative cycles?** Real road distances are non-negative, so negative cycles cannot occur in this model. The route reconstruction also contains a safety guard.

**What are the limitations?** Locations and distances are manually defined; traffic, GPS, one-way roads and live incidents are simulated rather than connected to public data.

**What is the future scope?** Import real road data, use live traffic, use GPS positions, support multiple simultaneous vehicles, prioritise calls and use a production-grade routing service.
