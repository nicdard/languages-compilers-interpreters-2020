#ifndef vm_clox_h
#define vm_clox_h

#include "chunk.h"
#include "value.h"

/**
 * The maximum size of the VM's stack.
 */
#define STACK_MAX 1024

/**
 * The VM executes a chunk of code.
 */
typedef struct {
    // The chunk of bytecode currently being executed.
    Chunk* chunk;
    // The location of the instruction about to be executed.
    uint8_t* ip;
    // The VM's stack.
    Value stack[STACK_MAX];
    // Reference to the head of the stack, which is the next free slot on top of the stack itself.
    Value* stackTop;
} VM;

/**
 * The interpreter will use this information to set the right process' exit code.
 */
typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

/**
 * Creates the VM.
 */
void initVM();
/**
 * Teardown the VM.
 */
void freeVM();
/**
 * Interprets a chunk of bytecode.
 */
InterpretResult interpret(const char* source);
/**
 * Adds value on top of the stack.
 */
void push(Value value);
/**
 * Gets and removes the stack's head.
 */
Value pop();


#endif