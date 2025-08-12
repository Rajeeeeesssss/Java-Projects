Here’s the adapted version:

---

## Getting Started

Welcome to the **Real-Time Currency Converter** Java project!
This is a Java Swing-based GUI application that converts currencies using live exchange rates from the [ExchangeRate API](https://www.exchangerate-api.com/).

Follow this guide to set up and run the project in Visual Studio Code or any Java IDE.

---

## Folder Structure

The workspace contains:

* `src`: the folder with your Java source files (e.g., `App.java`)
* `lib`: the folder containing external dependencies (`json-20210307.jar`)
* `bin`: the default output folder for compiled `.class` files

> You can customize the folder structure by editing `.vscode/settings.json`.

---

## Dependency Management

This project uses the **org.json** library for JSON parsing.
You can manage dependencies in VS Code via the `JAVA PROJECTS` view, or manually place the JAR file in the `lib` folder.

**Download `org.json` jar:**
[https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar](https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar)

---

## How to Run

1. **Download and place** `json-20210307.jar` in the `lib` folder.
2. **Replace** the API key in `App.java` with your own key from [ExchangeRate API](https://www.exchangerate-api.com/).
3. Compile:

   ```bash
   javac -cp ".;lib/json-20210307.jar" src/App.java -d bin
   ```
4. Run:

   ```bash
   java -cp ".;bin;lib/json-20210307.jar" App
   ```
