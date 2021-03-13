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
    // To optimise we use three dedicated instructions for those constants.
    OP_NIL,
    OP_TRUE,
    OP_FALSE,
    // Pops two element from the stack and pushes back the the constant true if they are equal.
    OP_EQUAL,
    // Pops two element from the stack and pushes back the the constant true if the first is greater than the second.
    OP_GREATER,
    // Pops two element from the stack and pushes back the the constant true if the first is smaller than the second.
    OP_LESS,
    // Retrieves a constant indexed with 3-byte long address, using little-endian encoding.
    OP_CONSTANT_LONG,
    // Unary negation arithmetic operator.
    OP_NEGATE,
    // Pops two element from the stack and pushes back the sum of them.
    OP_ADD,
    // Pops two element from the stack and pushes back the difference of them.
    OP_SUBTRACT,
    // Pops two element from the stack and pushes back the product of them.
    OP_MULTIPLY,
    // Pops two element from the stack and pushes back the quotient of them.
    OP_DIVIDE,
    // Logical not, pops one element from the stack and pushes back its logical negation.
    OP_NOT,
    // Defines a global variable. Pops two elements from the stack, 
    // the first is the initialiser expression, the second is the variable name.
    OP_DEFINE_GLOBAL,
    // Access a global variable by its name. The name is popped from the stack.
    OP_GET_GLOBAL,
    // Sets the value of an already defined variable.
    OP_SET_GLOBAL,
    OP_GET_LOCAL,
    OP_SET_LOCAL,
    // Hoists a stack value to the heap to let it be accessible by closures that 
    // may need it. Pops the first operand from the stack.
    OP_CLOSE_UPVALUE,
    OP_GET_UPVALUE,
    OP_SET_UPVALUE,
    // Pops the first element from the stack.
    OP_POP,
    // Pops and print the first element of the stack.
    OP_PRINT,
    // Always jump backward using the next two bytes as the offset of the jump.
    OP_LOOP,
    // Pops as many values from the stack as the next bytecode value indicates.
    // Those values are the arguments of the function call.
    OP_CALL,
    // The next byte code represents the number n of upvalues for the closure.
    // 2n bytes follow, each consecutive pair represents the locality and the index
    // of an upvalue.
    // It consumes also an operand from the stack, which is the function object for which
    // a closure is build.  
    OP_CLOSURE,
    // Always jump ahead using the next two bytes as the offset of the jump.
    OP_JUMP,
    // Pops the first element from the stack and uses it as the condition.
    // It assumes the next two bytes to be the offset of the conditional jump.
    OP_JUMP_IF_FALSE,
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