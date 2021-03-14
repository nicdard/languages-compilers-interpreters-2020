#include <stdlib.h>
#include <stdio.h>

#include "chunk.h"
#include "memory.h"
#include "vm.h"

/**
 * Adds a new line information to the chunk.
 * It uses run-length encoding. 
 */
static void addLine(Chunk* chunk, int line);

void initChunk(Chunk* chunk) {
    chunk->count = 0;
    chunk->capacity = 0;
    chunk->code = NULL;
    chunk->linesCapacity = 0;
    chunk->linesCount = 0;
    chunk->lines = NULL;
    initValueArray(&chunk->constants);
}

void writeChunk(Chunk* chunk, uint8_t byte, int line) {
    bool isFull = chunk->capacity < chunk->count + 1;
    if (isFull) {
        int oldCapacity = chunk->capacity;
        chunk->capacity = GROW_CAPACITY(oldCapacity);
        chunk->code = GROW_ARRAY(
            uint8_t,
            chunk->code,
            oldCapacity,
            chunk->capacity
        );
    }
    chunk->code[chunk->count] = byte;
    chunk->count++;
    addLine(chunk, line);
}

void writeConstant(Chunk* chunk, Value value, int line) {
    int index = addConstant(chunk, value);
    if (index < 256) {
        writeChunk(chunk, OP_CONSTANT, line);
        writeChunk(chunk, (uint8_t)index, line);
    } else {
        writeChunk(chunk, OP_CONSTANT_LONG, line);
        writeChunk(chunk, (uint8_t)index, line);
        writeChunk(chunk, (uint8_t)((index >> 8) & 0xff), line);
        writeChunk(chunk, (uint8_t)((index >> 16) & 0xff), line);
    }
}

int addConstant(Chunk* chunk, Value constant) {
    push(constant);
    writeValueArray(&chunk->constants, constant);
    pop();
    return chunk->constants.count - 1;
}

void freeChunk(Chunk* chunk) {
    FREE_ARRAY(uint8_t, chunk->code, chunk->capacity);
    FREE_ARRAY(LineStart, chunk->lines, chunk->linesCapacity);
    freeValueArray(&chunk->constants);
    initChunk(chunk);
}

int getLine(Chunk* chunk, int offset) {
    int start = 0;
    int end = chunk->linesCount - 1;
    LineStart* lineStart;
    while (start >= 0 && end < chunk->linesCount) {
        int mid = (end + start) / 2;
        lineStart = &chunk->lines[mid];
        if (lineStart->offset > offset) {
            end = mid - 1;
        } else if (
            mid == chunk->linesCount - 1
            || offset < chunk->lines[mid + 1].offset
        ) {
            break;
        } else {
            start = mid + 1;
        }
    }
    return lineStart->line;
}

static void addLine(Chunk* chunk, int line) {
    bool isSameAsLast = 
        chunk->linesCount > 0 
        && chunk->lines[chunk->linesCount - 1].line == line;
    if (isSameAsLast) {
        return;
    }
    bool isFull = chunk->linesCapacity < chunk->linesCount + 1;
    if (isFull) {
        int oldLinesCapacity = chunk->linesCapacity;
        chunk->linesCapacity = GROW_CAPACITY(oldLinesCapacity);
        chunk->lines = GROW_ARRAY(
            LineStart,
            chunk->lines,
            oldLinesCapacity,
            chunk->linesCapacity
        );
    }
    LineStart* lineStart = &chunk->lines[chunk->linesCount++];
    lineStart->line = line;
    lineStart->offset = chunk->count - 1;
}