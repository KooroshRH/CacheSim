# CacheSim
A simulator for modelize cache memory of CPU.

## Getting Started
This project is for Computer Architecture course in AmirKabir University of Technology.

For input of this simulator we must provide these informations:
- Total cache size
- Block size
- Unified vs. split I- and D-caches (Von Neumann. vs. Harvard)
- Associativity
- Write back vs. write through
- Write allocate vs. write no-allocate

And also expecting these informations at the end:
- Number of instruction references
- Number of data references
- Number of instruction misses
- Number of data misses
- Number of words fetched from memory
- Number of words copied back to memory

### Input Format
> **Important**
This simulator reads instructions from file

Here is the format of main cache attributes:
- "block size" - "unified or separated" - "associativity" - "write policy" - "write_miss policy"
  - Explanation for each of the entries:
  - "block size": Cache’s block size in bytes which should be a power of 2. (e.g. 8, 64, 1024...)
  - "unified or separated": An indicator of cache’s architecture. “0” Denotes Von Neumann and “1” denotes Harvard.
  - "associativity": An integer indicating how associative the cache is. Remember that the cache’s size (explained shortly) should be divisible by its associativity.
  - "write policy": A string that should be either “wb” (write-back) or “wt” (write-through).
  - "write_miss policy": A string that should be either “wa” (write-allocate) or “nw” (no-write allocate)
- Then, if <unified or separated> is 0, your program should read a line as follows:
  - "unified size"
  - Which is the cache’s size in bytes while being a power of 2.
- Otherwise, if <unified or separated> is 1, your program should read the following line:
  - "instruction cache size" - "data cache size"
  - Both of which are the respective caches’ sizes in bytes while being powers of 2.
  
Then in every line of input we face first **Type of instructions** and **address of data or instruction in hex**

For types we have this rules:
- **0 Data load reference**
- **1 Data store reference**
- **2 Instruction load reference**

The number following the reference type is the byte address of the memory reference itself. This number is in hexadecimal format, and specifies a 32-bit byte address in the range 0-0xffffffff, inclusive.

> **Tip** we use LRU replace policy for this cache simulator

### Example
#### Example for input
```
16 - 1 - 1 - wb - wa
128 - 128
0 00000 data read miss (compulsory)
2 10000 instruction miss (compulsory, replaces 00000 if assoc=1 & unified)
2 20000 instruction miss (compulsory, replaces 00000 if assoc=2 & unified)
2 30000 instruction miss (compulsory, replaces 00000 if assoc=2 & unified)
2 40000 instruction miss (compulsory, replaces 00000 if assoc=4 & unified)
0 00000 data read miss (miss if assoc=1 & unified)
2 10001 instruction miss (miss if assoc=1 & unified)
```
#### Example for output
```
***CACHE SETTINGS***
Split I- D-cache
I-cache size: 128
D-cache size: 128
Associativity: 1
Block size: 16
Write policy: WRITE BACK
Allocation policy: WRITE ALLOCATE

***CACHE STATISTICS***
INSTRUCTIONS
accesses: 5
misses: 5
miss rate: 1.0000 (hit rate 0.0000)
replace: 4
DATA
accesses: 2
misses: 1
miss rate: 0.5000 (hit rate 0.5000)
replace: 0
TRAFFIC (in words)
demand fetch: 24
copies back: 0
```
**Hope you enjoy**
