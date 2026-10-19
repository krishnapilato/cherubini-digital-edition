# Cherubini Archive

![Java SE 27](https://img.shields.io/badge/Java%20SE-27-ED8B00?style=flat&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-Desktop-1565C0?style=flat)
![SQLite](https://img.shields.io/badge/SQLite-Database-003B57?style=flat&logo=sqlite&logoColor=white)
![React 19.3](https://img.shields.io/badge/React-19.3-20232A?style=flat&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite-Bundler-646CFF?style=flat&logo=vite&logoColor=white)

> A modern, accessible digital edition of the 19th-century Milanese-Italian dictionary.

## About the Project
The **Cherubini Archive** is a software revitalization project aimed at preserving and exploring the rich linguistic heritage of nineteenth-century Milan. Originally conceptualized to recover technical terms, construction materials, and historical trades hidden within the dictionary's definitions[cite: 2], this project modernizes the tool for contemporary linguistic research.

The interface is designed with a minimal, clean editorial aesthetic. Inspired by warm paper themes and utilizing subtle blue copy-editor accents, the web explorer emphasizes rigorous readability, smooth typographic transitions, and structured display plates. Every architectural choice is made to ensure the application is 100% responsive, accessible, and capable of achieving maximum Lighthouse scores without relying on prefabricated templates.

### Original Authors
* **Francesco Cherubini**: Author of the historical *Vocabolario Milanese-Italiano*.
* **Stefano Pilato**: Creator of the original *Voci tecniche* MS Access 2000 software edition from which this database was extracted[cite: 1, 2].

## Repository Structure

- **`/desktop`**  
  The standalone dictionary client built with **Java 27** and **JavaFX**. It utilizes a local **SQLite** database to provide lightning-fast offline querying, preserving the original application's Boolean (AND) search capabilities and incremental letter-by-letter filtering[cite: 1, 2].

- **`/web`**  
  A browser-based, static JSON search interface powered by **React 19.3** and **Vite**. It features an elegant layout with fuzzy search capabilities to gracefully handle archaic dialect spelling variations. It incorporates footnote-based technical details to unpack historical abbreviations smoothly, keeping the reading experience uncluttered.