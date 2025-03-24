# Sequence Alignment Algorithms

This repository contains Java implementations of two fundamental sequence alignment algorithms widely used in bioinformatics:&#8203;:contentReference[oaicite:2]{index=2}

- **Needleman-Wunsch Algorithm**: :contentReference[oaicite:3]{index=3}&#8203;:contentReference[oaicite:4]{index=4}
- **Smith-Waterman Algorithm**: :contentReference[oaicite:5]{index=5}&#8203;:contentReference[oaicite:6]{index=6}

## Overview

:contentReference[oaicite:7]{index=7} :contentReference[oaicite:8]{index=8} :contentReference[oaicite:9]{index=9}&#8203;:contentReference[oaicite:10]{index=10}

## Implementation Details

1. **Scoring System**:
   - **Substitution Matrix**: :contentReference[oaicite:11]{index=11}&#8203;:contentReference[oaicite:12]{index=12}
   - **Gap Penalties**: :contentReference[oaicite:13]{index=13}&#8203;:contentReference[oaicite:14]{index=14}
     - *Gap Opening Penalty*: Applied when a gap is introduced.
     - *Gap Extension Penalty*: Applied when an existing gap is extended.

2. **Dynamic Programming Matrix**:
   - :contentReference[oaicite:15]{index=15}&#8203;:contentReference[oaicite:16]{index=16}
   - :contentReference[oaicite:17]{index=17}&#8203;:contentReference[oaicite:18]{index=18}
   - :contentReference[oaicite:19]{index=19}&#8203;:contentReference[oaicite:20]{index=20}

     \[ F(i,j) = \max \begin{cases} F(i-1,j-1) + s(i,j) \\ F(i-1,j) - \delta \\ F(i,j-1) - \delta \end{cases} \]

     Where \( s(i,j) \) is the substitution score from the BLOSUM matrix, and \( \delta \) is the gap penalty.

3. **Backtracking**:
   - :contentReference[oaicite:21]{index=21}&#8203;:contentReference[oaicite:22]{index=22}
   - :contentReference[oaicite:23]{index=23}&#8203;:contentReference[oaicite:24]{index=24}

## Repository Structure

- **GlobalAlign.java**: :contentReference[oaicite:25]{index=25}&#8203;:contentReference[oaicite:26]{index=26}
- **LocalAlign.java**: :contentReference[oaicite:27]{index=27}&#8203;:contentReference[oaicite:28]{index=28}
- **Nucleotides.java**: :contentReference[oaicite:29]{index=29}&#8203;:contentReference[oaicite:30]{index=30}

## How to Run

1. :contentReference[oaicite:31]{index=31}&#8203;:contentReference[oaicite:32]{index=32}

   ```bash
   git clone https://github.com/fralar-code/Sequence-Alignment-Algorithms.git
