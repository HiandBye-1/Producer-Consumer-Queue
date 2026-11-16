# Project 2 — Producer-Consumer Queue

## Overview
A Java bounded producer-consumer queue using condition variables (CV) and semaphores (SEM). It also demonstrates a lost-update race condition and its mutex-based repair.

## Environment
- Language: Java
- JDK: OpenJDK 25.0.1
- OS: Windows 11

## Build
From the project root:

```powershell
javac -d out src/*.java checker/*.java
```

## Running

**CV mode:**
```powershell
java -cp out src.Main public cv
```

**SEM mode:**
```powershell
java -cp out src.Main public sem
```

Replace `public` with another configuration name, without `.txt`.

The program prints the event trace and summary, then saves the CSV trace in `traces/`.

## Run the Checker

```powershell
powershell -ExecutionPolicy Bypass -File .\Run-Checker.ps1 config/public.txt traces/public-cv.csv
```

Change the paths to match the run being checked.

## Project Structure
- `src/` — queue, producer, consumer, race-condition, and main classes.
- `checker/` — independent trace checker.
- `config/` — workload configurations.
- `traces/` — CSV traces, summaries, and checker results.
- `evidence/` — screenshots and supporting evidence.

## Testing
Both CV and SEM modes passed the required workload tests, including stress tests. Corrupted traces were also checked to verify that invalid events are rejected.

## Limitations
This is a command-line demonstration, not a production queue service. Results depend on thread scheduling and system performance.