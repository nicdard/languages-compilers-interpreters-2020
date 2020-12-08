#include <stdio.h>

#include "common.h"
#include "vm.h"
#include "debug.h"

/**
 * Runs the current chunk of code.
 */
static InterpretResult run();
/**
 * Empties the VM's stack.
 */
static void resetStack();

/**
 * The global instance of the VM. 
 * It would be better not to use a global variable,
 * but here is done for the sake of simplicity. 
 */ 
VM vm;

void initVM() {
    resetStack();
}

void freeVM() {
    
}

InterpretResult interpret(Chunk* chunk) {
    vm.chunk = chunk;
    vm.ip = vm.chunk->code;
    return run();
}

void push(Value value) {
    *vm.stackTop = value;
    vm.stackTop++;
}

Value pop() {
    vm.stackTop--;
    return *vm.stackTop;
}

static InterpretResult run() {
#define READ_BYTE() (*vm.ip++)
#define READ_CONSTANT() (vm.chunk->constants.values[READ_BYTE()])
#define READ_LONG_CONSTANT(index) (vm.chunk->constants.values[index])
#define BINARY_OP(op) \
    do { \
        double b = pop(); \
        double a = pop(); \
        push(a op b); \
    } while (false)

    while (true) {
#ifdef DEBUG_TRACE_EXECUTION
        printf("        ");
        for (Value* slot = vm.stack; slot < vm.stackTop; ++slot) {
            printf("[");
            printValue(*slot);
            printf("]");
        }
        printf("\n");
        disassembleInstruction(vm.chunk, (int)(vm.ip - vm.chunk->code));
#endif
        uint8_t instruction;
        switch (instruction = READ_BYTE()) {
            case OP_CONSTANT: {
                Value constant = READ_CONSTANT();
                push(constant);
                break;
            }
            case OP_CONSTANT_LONG: {
                uint32_t constantIndex = READ_BYTE();
                constantIndex |= (READ_BYTE() << 8);
                constantIndex |= (READ_BYTE() << 16);
                Value constant = READ_LONG_CONSTANT(constantIndex);
                push(constant);
                break;
            }
            case OP_NEGATE: push(-pop()); break;
            case OP_ADD: BINARY_OP(+); break;
            case OP_SUBTRACT: BINARY_OP(-); break;
            case OP_MULTIPLY: BINARY_OP(*); break;
            case OP_DIVIDE: BINARY_OP(/); break;
            case OP_RETURN: {
                printValue(pop());
                printf("\n");
                return INTERPRET_OK;
            }
        } 
    }

#undef BINARY_OP
#undef READ_LONG_CONSTANT
#undef READ_CONSTANT
#undef READ_BYTE
}

static void resetStack() {
    vm.stackTop = vm.stack;
}