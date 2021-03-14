#ifndef vm_clox_h
#define vm_clox_h

#include "chunk.h"
#include "object.h"
#include "table.h"
#include "value.h"

/**
 * The maximum number of ongoing function calls.
 */
#define FRAMES_MAX 64
/**
 * The maximum size of the VM's stack.
 */
#define STACK_MAX (FRAMES_MAX * 1024)

/**
 * A function invocation.
 */
typedef struct {
    // The invoked closure.
    ObjClosure* closure;
    // Store the address of this function.
    // When the control flow is returned to this function
    // we restart from the instruction pointed by ip.
    uint8_t* ip;
    // Pointer to the first slot inside the VM's value stack
    // that this function invocation can use.
    Value* slots;
} CallFrame;

/**
 * The VM executes a chunk of code.
 */
typedef struct {
    // The frame call stack. Statically allocated for sped.
    CallFrame frames[FRAMES_MAX];
    int frameCount;
    // The VM's value (operand) stack.
    Value stack[STACK_MAX];
    // Reference to the head of the stack, which is the next free slot on top of the stack itself.
    Value* stackTop;
    // Stores all global variables.
    Table globals;
    // Stores all interned strings.
    Table strings;
    Obj* objects;
    ObjUpvalue* openUpvalues;
    int grayCount;
    int grayCapacity;
    Obj** grayStack;
    // Running total of the number of bytes of managed memory the VM has allocated.
    size_t bytesAllocated;
    // The threshold over which the next GC is triggered.
    size_t nextGC;
} VM;

/**
 * The interpreter will use this information to set the right process' exit code.
 */
typedef enum {
    INTERPRET_OK,
    INTERPRET_COMPILE_ERROR,
    INTERPRET_RUNTIME_ERROR
} InterpretResult;

extern VM vm;
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