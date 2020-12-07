#ifndef clox_debug_h
#define clox_debug_h

#include "chunk.h"

/**
 * Disassemble an entire chunk.
 */
void disassembleChunk(Chunk* chunk, const char* name);
/**
 * Disassemble one instruction from chunk at the given offset;
 * Return the offset of the next instruction. 
 * This is because instructions can have different sizes.
 */
int disassembleInstruction(Chunk* chunk, int offset);

#endif