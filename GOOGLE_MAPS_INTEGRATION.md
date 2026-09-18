# Google Maps integration requirements

The current Java Swing map is intentionally offline and fully functional. A genuine interactive Google map cannot be enabled without a key belonging to the project owner.

## What is required

1. Create a Google Cloud project.
2. Enable the **Maps JavaScript API** and attach billing (or obtain a Maps Demo Key for a prototype).
3. Create an API key and restrict it to the application’s allowed host/origin.
4. Add a WebView runtime to this Swing application (OpenJFX `javafx-web`) or convert the map screen to a web front end.
5. Keep the key out of source control; load it from an environment variable or a local ignored configuration file.

## Important design note

Google Maps supplies the visual basemap and map interactions. The seminar’s Floyd–Warshall algorithm should remain the routing engine for the fictional 18-node graph; otherwise the project becomes a wrapper around Google routing and no longer demonstrates the DAA implementation.

## Why it is not activated in this repository

No Google Cloud API key, enabled billing project or JavaFX WebView dependency was provided. Those are account-specific credentials and cannot be created safely by this project. Google’s Maps JavaScript API requires an API key and billing for normal use; its demo key is only for prototyping.

After a key and preferred deployment choice are supplied, the next implementation step is to replace `GraphVisualizerPanel` with an embedded WebView map while passing the same vertices, road closures and highlighted Floyd–Warshall route to JavaScript overlays.
