# Sequence Alignment Algorithms  

This repository contains Java implementations of two fundamental **sequence alignment** algorithms used in **bioinformatics**:  

- **Needleman-Wunsch Algorithm** – Global alignment to compare entire sequences  
- **Smith-Waterman Algorithm** – Local alignment to find similar regions within sequences  

## 🔬 Bioinformatics Context  
Sequence alignment is a key technique in bioinformatics, used to compare DNA, RNA, and protein sequences. These comparisons help identify functional, structural, or evolutionary relationships between biological sequences.
The sequences such as **DNA, RNA, and proteins** are represented as strings composed of characters from specific alphabets:  

- **Nucleotide sequences** (DNA or RNA) use the alphabet {A, G, C, T/U}.  
- **Protein sequences** use the alphabet of **amino acids**.  

A crucial step in **sequence analysis** is determining similarity between biological sequences, which is achieved through **sequence alignment**. The algorithms implemented in this repository provide solutions for **global** (Needleman-Wunsch) and **local** (Smith-Waterman) alignment of genomic sequences.  

## ⚙️ Implementation Details  

The goal of a sequence alignment algorithm is to find the best way to align two strings, accounting for **matches, mismatches, and gaps**. The implementation is based on **dynamic programming**, following these steps:  

### 1️⃣ Dynamic Programming Matrix Initialization  

A matrix of size **(sequence1 length × sequence2 length)** is initialized, assigning similarity scores for every substring comparison. The matrix is filled using the **recurrence relation**:  

$$ F(i,j)=\max\begin{cases}F(i-1,j-1) + s(i,j)\\
F(i-1,j) - \delta \\
F(i,j-1) - \delta  \end{cases} $$

where:  
- $s(i,j)$ is the substitution score, retrieved using the **BLOSUM matrix** (which assigns similarity scores based on observed amino acid substitutions in homologous proteins).  
- $\delta$ is the **gap penalty**. In the implementation:  
  - `delta` represents the **gap open penalty**.  
  - `gamma` represents the **gap extension penalty** (since extending an existing gap has a lower penalty than opening a new one).  

### 2️⃣ Backtracking for Optimal Alignment  

To reconstruct the **best alignment**, backtracking is performed:  
- Each cell in the matrix stores the **score value** along with information on **which of the three possible neighboring cells** led to the current value.  
- If a gap extension score was considered in **Step 1**, the backtracking process **must follow the gap extension path**.  
- To optimize storage, backtracking information is encoded in a **single byte** (`000urabc` format):  
  - `u` (Under) and `r` (Right) bits indicate whether the traceback path comes from below or the right.  
  - `abc` bits encode which of the **three neighboring cells** contributed to the maximum score.  

This structure ensures efficient traceback and allows for accurate sequence alignment reconstruction.

## 📂 Repository Structure  

- `/GlobalAlign/` – Contains the implementation of the **Needleman-Wunsch** algorithm for **protein sequences**  
- `/LocalAlign/` – Contains the implementation of the **Smith-Waterman** algorithm for **protein sequences** 
- `/Nucleotides/` – Contains the implementation for **nucleotide sequences**
Each folder contains the corresponding well-commented Java implementations

📩 Feel free to explore, contribute, or reach out for any questions! 🚀
