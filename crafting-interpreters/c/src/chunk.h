#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

/**
 * Operation codes: one-byte, defines the type of operation to be performed.
 */
typedef enum {
    // Retrieves a constant indexed with 1-byte long address.
    OP_CONSTANT,
    // Retrieves a constant indexed with 3-byte long address, using little-endian encoding.
    OP_CONSTANT_LONG,
    OP_RETURN
} OpCode;

typedef struct {
    // The byte offset of the first instruction in the line.
    int offset;
    // The new line
    int line;
} LineStart;

/**
 * Stores a series of bytecode instructions, using a dynamic array, which provides:
 * - cache-friendly, dense storage;
 * - costant-time indexed element lookup;
 * - costant-time appending to the end of the array. (Amortized analysis)
 */
typedef struct {
    // The number of slots used.
    int count;
    // The total number of slots.
    int capacity;
    // The operations codes and constant index.
    uint8_t* code;
    // The constant pool.
    ValueArray constants;
    // The lines array capacity.
    int linesCapacity;
    // The lines array current occupied space.
    int linesCount;
    // The line information of each operation code.
    LineStart* lines;
} Chunk;

/**
 * Initialises an empty Chunk.
 */
void initChunk(Chunk* chunk);
/**
 * Appends a new operation at the end of the Chunk.
 * The line parameter is used to track the line 
 * in the source code which produced the opcode.
 */
void writeChunk(Chunk* chunk, uint8_t byte, int line);
/**
 * Appends a new constant value to the chunk.
 * It uses little-endian encoding to handle values longer than one byte.
 */
void writeConstant(Chunk* chunk, Value value, int line);
/**
 * Destroies the chunk.
 */
void freeChunk(Chunk* chunk);
/**
 * Adds a constant to the constant pool of chunk.
 */
int addConstant(Chunk* chunk, Value constant);
/**
 * Given the index of an instruction, determines the line 
 * where the instruction occours in the source code.
 * It assumes that line numbers for the instructions always monotonically increase. 
 */
int getLine(Chunk* chunk, int offset);

#endif